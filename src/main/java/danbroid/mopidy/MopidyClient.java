package danbroid.mopidy;


import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import danbroid.mopidy.api.*;
import danbroid.mopidy.events.*;
import danbroid.mopidy.misc.JSONKeywords;
import danbroid.mopidy.model.PlaybackState;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.enums.ReadyState;
import org.java_websocket.handshake.ServerHandshake;

/**
 * Wraps the websocket communication with the Mopidy server
 */

@Slf4j
public class MopidyClient extends WebSocketClient {
    @Getter
    private final Core core = new Core(this);

    // Calls
    private final AtomicInteger requestId = new AtomicInteger(0);
    private final HashMap<Integer, Call<?>> calls = new HashMap<>();

    private final int ERROR_TRANSPORT = -1;
    private boolean closeConnection = false;
    private List<EventListener> eventListeners = new ArrayList<>();

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
    public synchronized void call(Call<?> call) {
        int id = requestId.incrementAndGet();
        call.setID(id);
        call.setTimestamp(System.currentTimeMillis());
        calls.put(id, call);

        // Make sure the connection is already open.
        if (getReadyState() == ReadyState.OPEN) {
            sendCalls();
        }
    }

    private void sendCalls() {
        for (Call<?> call : calls.values()) {
            String request = call.toString();
            log.info("call(): request:{}", request);
            send(request);
        }
    }

    protected Call<?> popCall(int id) {
        return calls.remove(id);
    }

    @Override
    public void onOpen(ServerHandshake serverHandshake) {
        log.info("onOpen()");

        // Send calls now that have been added before the connection was open.
        sendCalls();

        // Close connection now if it was requested during connection opening.
        if (closeConnection) close();
    }

    @Override
    public void close() {
        if (getReadyState() == ReadyState.NOT_YET_CONNECTED) {
            // Connection will be closed after it was opened.
            closeConnection = true;
        } else {
            super.close();
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        log.info("onClose() Connection closed by " + (remote ? "remote peer" : "us") + " Code: " + code + " Reason: "
                + reason);
    }

    @Override
    public void onError(Exception e) {
        log.error(e.getMessage(), e);

        String message = e.getCause() != null ? e.getCause().getLocalizedMessage() : e.getLocalizedMessage();

        calls.values().forEach(call -> call.onError(ERROR_TRANSPORT, message, null));
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
                if (object.has(JSONKeywords.EVENT)) {
                    processMessageEvent(object.get(JSONKeywords.EVENT).getAsString(), object);
                    return;
                }

                // JSON RPC
                if (object.has(JSONKeywords.JSONRPC)) {
                    processMessageResponse(object.get(JSONKeywords.ID).getAsInt(), object.get(JSONKeywords.RESULT));
                    return;
                }

                // Error
                if (object.has(JSONKeywords.ERROR)) {
                    log.error("Got error: {}", text);

                    object = object.getAsJsonObject(JSONKeywords.ERROR);
                    int id = object.get(JSONKeywords.ID).getAsInt();
                    String message = object.get(JSONKeywords.MESSAGE).getAsString();
                    int code = 0;
                    if (object.has(JSONKeywords.CODE)) code = object.get(JSONKeywords.CODE).getAsInt();

                    processMessageError(id, message, code, object.get(JSONKeywords.DATA));
                    return;
                }

                log.error("Unhandled data: {}", text);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    protected void processMessageError(int id, String message, int code, JsonElement data) {
        Call<?> call = popCall(id);
        if (call != null) {
            call.onError(code, message, data);
        } else {
            log.error("No call found for request: " + id);
        }
    }

    protected void processMessageResponse(int id, JsonElement result) {
        log.trace("processResponse(): id:{} result: {}", id, result);

        Call<?> call = popCall(id);
        if (call != null) {
            call.processResult(result);
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
                        data.get(JSONKeywords.TL_TRACK).getAsJsonObject(),
                        data.get(JSONKeywords.TIME_POSITION).getAsLong()));
                break;

            case "track_playback_resumed":
                eventListeners.forEach(e -> e.onTrackPlaybackResumed(
                        data.get(JSONKeywords.TL_TRACK).getAsJsonObject(),
                        data.get(JSONKeywords.TIME_POSITION).getAsLong()));
                break;

            case "track_playback_started":
                eventListeners.forEach(e -> e.onTrackPlaybackStarted(data.get(JSONKeywords.TL_TRACK).getAsJsonObject()));
                break;

            case "track_playback_ended":
                eventListeners.forEach(e -> e.onTrackPlaybackEnded(
                        data.get(JSONKeywords.TL_TRACK).getAsJsonObject(),
                        data.get(JSONKeywords.TIME_POSITION).getAsLong()));
                break;

            case "playback_state_changed":
                eventListeners.forEach(e -> e.onPlaybackStateChanged(
                        PlaybackState.fromString(data.get(JSONKeywords.OLD_STATE).getAsString()),
                        PlaybackState.fromString(data.get(JSONKeywords.NEW_STATE).getAsString())));
                break;

            case "tracklist_changed":
                eventListeners.forEach(EventListener::onTracklistChanged);
                break;

            case "playlists_loaded":
                eventListeners.forEach(EventListener::onPlaylistsLoaded);
                break;

            case "playlist_changed":
                eventListeners.forEach(e -> e.onPlaylistChanged(data.getAsJsonObject(JSONKeywords.PLAYLIST)));
                break;

            case "playlist_deleted":
                eventListeners.forEach(e -> e.onPlaylistDeleted(data.get(JSONKeywords.URI).getAsString()));
                break;

            case "options_changed":
                eventListeners.forEach(e -> e.onOptionsChanged());
                break;

            case "volume_changed":
                eventListeners.forEach(e -> e.onVolumeChanged(data.get(JSONKeywords.VOLUME).getAsInt()));
                break;

            case "mute_changed":
                eventListeners.forEach(e -> e.onMuteChanged(data.get(JSONKeywords.MUTE).getAsBoolean()));
                break;

            case "seeked":
                eventListeners.forEach(e -> e.onSeeked(data.get(JSONKeywords.TIME_POSITION).getAsLong()));
                break;

            case "stream_title_changed":
                eventListeners.forEach(e -> e.onStreamTitleChanged(data.get(JSONKeywords.TITLE).getAsString()));
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
    public void registerListener(EventListener... eventListeners) {
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