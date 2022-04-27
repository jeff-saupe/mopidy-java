package saupe.mopidy;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URI;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import saupe.mopidy.api.Action;
import saupe.mopidy.api.Dispatch;
import saupe.mopidy.events.EventListener;
import saupe.mopidy.misc.JsonKeywords;
import saupe.mopidy.model.PlaybackState;
import lombok.extern.slf4j.Slf4j;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import saupe.mopidy.api.Call;
import saupe.mopidy.api.Call.CallState;

@Slf4j
public class MopidyClient extends WebSocketClient {
    private final AtomicInteger ID_GENERATOR = new AtomicInteger(0);
    private final List<Call<?>> calls = new ArrayList<>();
    private final List<EventListener> eventListeners = new ArrayList<>();

    private Thread callThread;

    private final int ERROR_TRANSPORT = -1;
    private Action closeHandler;

    public MopidyClient(String host, int port) {
        super(URI.create("ws://" + host + ":" + port + "/mopidy/ws"));
    }

    public MopidyClient(String url) {
        super(URI.create(url));
    }

    public MopidyClient(URI uri) {
        super(uri);
    }

    /**
     * Dispatches call to the web socket.
     *
     * @param call Call to be dispatched
     */
    public synchronized <T> Dispatch<T> dispatch(Call<T> call) {
        int id = ID_GENERATOR.incrementAndGet();
        call.setId(id);
        calls.add(id, call);
        return call.getDispatch();
    }

    public void send() {
        CallQueue queue = new CallQueue();

        Iterator<Call<?>> iterator = calls.iterator();
        while(iterator.hasNext()) {
            Call<?> call = iterator.next();
            queue.enqueue(call);
            iterator.remove();
        }

        new Thread(queue).start();
    }

    class CallQueue implements Runnable {
        private final Queue<Call<?>> calls = new LinkedList<>();

        public synchronized void enqueue(Call<?> call) {
            if (call != null) {
                calls.add(call);
            }
        }

        public synchronized void dequeue() {
            calls.poll();
        }

        @Override
        public void run() {
            while (calls.size() > 0) {
                Call<?> call = calls.peek();
                if (call.getState() == CallState.NOT_CALLED) {
                    call.setState(CallState.ONGOING);
                    call.setTimestamp(System.currentTimeMillis());

                    String request = call.toString();
                    log.info("call(): request:{}", request);
                    send(request);
                } else if (call.getState() == CallState.DONE) {
                    dequeue();
                }
            }
        }
    }

    private Optional<Call<?>> findById(int id) {
        return calls.stream().filter(c -> c.getId() == id).findFirst();
    }

    @Override
    public void onOpen(ServerHandshake serverHandshake) {
        log.info("onOpen()");
    }

    public void onClose(Action action) {
        this.closeHandler = action;
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        log.info("onClose() Connection closed by " + (remote ? "remote peer" : "us") + " Code: " + code + " Reason: "
                + reason);
        if (closeHandler != null) closeHandler.execute();
    }

    @Override
    public void onError(Exception e) {
        log.error(e.getMessage(), e);

        String message = e.getCause() != null ? e.getCause().getLocalizedMessage() : e.getLocalizedMessage();

        calls.forEach(call -> call.onError(ERROR_TRANSPORT, message, null));
    }

    @Override
    public void onMessage(String text) {
        processMessage(text);
    }

    protected void processMessage(String text) {
        log.trace("processMessage(): {}", text);

        try {
            // Check if message contains a JSON object
            JsonElement element = JsonParser.parseString(text);
            if (element.isJsonObject()) {
                JsonObject object = element.getAsJsonObject();

                // Event
                if (object.has(JsonKeywords.EVENT)) {
                    processMessageEvent(object.get(JsonKeywords.EVENT).getAsString(), object);
                    return;
                }

                // JSON RPC
                if (object.has(JsonKeywords.JSONRPC)) {
                    processMessageResponse(object.get(JsonKeywords.ID).getAsInt(), object.get(JsonKeywords.RESULT));
                    return;
                }

                // Error
                if (object.has(JsonKeywords.ERROR)) {
                    log.error("Got error: {}", text);

                    object = object.getAsJsonObject(JsonKeywords.ERROR);
                    int id = object.get(JsonKeywords.ID).getAsInt();
                    String message = object.get(JsonKeywords.MESSAGE).getAsString();
                    int code = 0;
                    if (object.has(JsonKeywords.CODE)) code = object.get(JsonKeywords.CODE).getAsInt();

                    processMessageError(id, message, code, object.get(JsonKeywords.DATA));
                    return;
                }

                log.error("Unhandled data: {}", text);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    protected void processMessageError(int id, String message, int code, JsonElement data) {
        log.trace("processMessageError(): id:{} message: {}", id, message);

        Optional<Call<?>> call = findById(id);
        if (call.isPresent()) {
            call.get().onError(code, message, data);
        } else {
            log.error("No call found for request: " + id);
        }
    }

    protected void processMessageResponse(int id, JsonElement result) {
        log.trace("processMessageResponse(): id:{} result: {}", id, result);

        Optional<Call<?>> call = findById(id);
        if (call.isPresent()) {
            call.get().onResult(result);
        } else {
            log.error("No call found for request: " + id);
        }
    }

    /**
     * Parse and dispatch the event to the registered EventListeners.
     *
     * @param event The name of the event
     * @param data  The event data
     * @return True if the event was processed, otherwise False
     */
    protected boolean processMessageEvent(String event, JsonObject data) {
        switch (event) {
            case "on_event":
                eventListeners.forEach(EventListener::onEvent);
                break;

            case "track_playback_paused":
                eventListeners.forEach(e -> e.onTrackPlaybackPaused(
                        data.get(JsonKeywords.TL_TRACK).getAsJsonObject(),
                        data.get(JsonKeywords.TIME_POSITION).getAsLong()));
                break;

            case "track_playback_resumed":
                eventListeners.forEach(e -> e.onTrackPlaybackResumed(
                        data.get(JsonKeywords.TL_TRACK).getAsJsonObject(),
                        data.get(JsonKeywords.TIME_POSITION).getAsLong()));
                break;

            case "track_playback_started":
                eventListeners.forEach(e -> e.onTrackPlaybackStarted(data.get(JsonKeywords.TL_TRACK).getAsJsonObject()));
                break;

            case "track_playback_ended":
                eventListeners.forEach(e -> e.onTrackPlaybackEnded(
                        data.get(JsonKeywords.TL_TRACK).getAsJsonObject(),
                        data.get(JsonKeywords.TIME_POSITION).getAsLong()));
                break;

            case "playback_state_changed":
                eventListeners.forEach(e -> e.onPlaybackStateChanged(
                        PlaybackState.fromString(data.get(JsonKeywords.OLD_STATE).getAsString()),
                        PlaybackState.fromString(data.get(JsonKeywords.NEW_STATE).getAsString())));
                break;

            case "tracklist_changed":
                eventListeners.forEach(EventListener::onTracklistChanged);
                break;

            case "playlists_loaded":
                eventListeners.forEach(EventListener::onPlaylistsLoaded);
                break;

            case "playlist_changed":
                eventListeners.forEach(e -> e.onPlaylistChanged(data.getAsJsonObject(JsonKeywords.PLAYLIST)));
                break;

            case "playlist_deleted":
                eventListeners.forEach(e -> e.onPlaylistDeleted(data.get(JsonKeywords.URI).getAsString()));
                break;

            case "options_changed":
                eventListeners.forEach(e -> e.onOptionsChanged());
                break;

            case "volume_changed":
                eventListeners.forEach(e -> e.onVolumeChanged(data.get(JsonKeywords.VOLUME).getAsInt()));
                break;

            case "mute_changed":
                eventListeners.forEach(e -> e.onMuteChanged(data.get(JsonKeywords.MUTE).getAsBoolean()));
                break;

            case "seeked":
                eventListeners.forEach(e -> e.onSeeked(data.get(JsonKeywords.TIME_POSITION).getAsLong()));
                break;

            case "stream_title_changed":
                eventListeners.forEach(e -> e.onStreamTitleChanged(data.get(JsonKeywords.TITLE).getAsString()));
                break;

            default:
                log.error("Unknown event: " + data);
                return false;
        }
        return true;
    }

    /**
     * Register one or more event listeners.
     *
     * @param eventListeners Listener(s) to be registered
     */
    public void addEventListener(EventListener... eventListeners) {
        this.eventListeners.addAll(Arrays.asList(eventListeners));
    }

    /**
     * Amount of calls currently in queue.
     *
     * @return Integer
     */
    public int getCallQueueSize() {
        return calls.size();
    }

}