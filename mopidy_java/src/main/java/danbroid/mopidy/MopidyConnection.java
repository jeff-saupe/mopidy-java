package danbroid.mopidy;


import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import danbroid.mopidy.api.Call;
import danbroid.mopidy.api.Core;
import danbroid.mopidy.interfaces.CallContext;
import danbroid.mopidy.interfaces.Constants;
import danbroid.mopidy.interfaces.EventListener;
import danbroid.mopidy.interfaces.PlaybackState;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

/**
 * Wraps the websocket communication with the Mopidy server
 */

public class MopidyConnection extends Core implements CallContext {

	private static final org.slf4j.Logger
			log = org.slf4j.LoggerFactory.getLogger(MopidyConnection.class);
	public static final int ERROR_TIMEOUT = -20;

	private String url;

	private WebSocket socket;
	private JsonParser parser = new JsonParser();
	private EventListener eventListener = new EventListenerImpl();
	private String version;

	public static final long DEFAULT_CALL_TIMEOUT = 3000;
	private long timeout = DEFAULT_CALL_TIMEOUT;


	private AtomicInteger requestID = new AtomicInteger(0);
	private HashMap<Integer, Call> calls = new HashMap<>();

	public void setEventListener(EventListener eventListener) {
		this.eventListener = eventListener;
	}


	public void setUrl(String url) {
		log.trace("setUrl(): {}", url);
		this.url = url;
	}

	public String getUrl() {
		return url;
	}

	public void start(String url) {
		log.info("start(): {}", url);
		setUrl(url);
		start();
	}

	public void start(String host, int port) {
		start("ws://" + host + ":" + port + "/mopidy/ws");
	}


	public void start() {
		log.info("start() url: {}", url);
		if (url == null) return;

		stop();

		OkHttpClient client = new OkHttpClient.Builder()
				.readTimeout(5, TimeUnit.SECONDS).connectTimeout(5, TimeUnit.SECONDS).build();
		Request request = new Request.Builder().url(url).build();


		this.socket = client.newWebSocket(request, new WebSocketListener() {
			@Override
			public void onMessage(WebSocket webSocket, String text) {
				MopidyConnection.this.onMessage(text);
			}
		});


		version = null;

		getVersion(new ResponseHandler<String>() {
			@Override
			public void onResponse(CallContext context, String result) {
				MopidyConnection.this.version = result;
				log.trace("version: {}", version);
				onConnect();
			}
		});
	}

	protected void onConnect() {
		log.info("onConnect()");
	}

	/**
	 * Dispatches call to the web socket
	 * This trivially calls sendCall so override this method
	 * to deal with threading
	 */
	@Override
	public void call(Call call) {
		sendCall(call);
	}

	protected void sendCall(Call call) {
		if (socket == null) start();
		if (socket == null) return;

		int id = requestID.incrementAndGet();
		call.setID(id);
		call.setTimestamp(System.currentTimeMillis());
		calls.put(id, call);

		String request = call.toString();
		log.trace("call(): request<{}>", request);
		socket.send(request);
	}

	/**
	 * Call timeout errors on all calls that havent been processed after CALL_TIMEOUT
	 */
	protected void expireCalls() {
		if (!calls.isEmpty()) {
			log.trace("expireCalls(): " + getCallQueueSize());
			for (Call call : calls.values().toArray(new Call[]{})) {
				if (System.currentTimeMillis() - call.getTimestamp() > timeout) {
					onError(call.getID(), "Call timeout", ERROR_TIMEOUT, call.getRequest());
				}
			}
		}
	}


	/**
	 * Shutsdown the socket.
	 */
	public void stop() {
		if (socket != null) {
			log.debug("stop(): {}", url);
			socket.close(1000, "Finished");
			socket = null;
			calls.clear();
		}
	}

	/**
	 * A message has been received.
	 * This trivially calls processMessage so override this method
	 * to deal with threading
	 *
	 * @param text The JSON message
	 */
	public void onMessage(String text) {
		processMessage(text);
	}

	protected void processMessage(String text) {
		log.trace("processMessage(): {}", text);
		try {
			JsonElement e = parser.parse(text);
			if (e.isJsonObject()) {
				JsonObject o = e.getAsJsonObject();

				if (o.has(Constants.Key.ERROR)) {
					o = o.getAsJsonObject(Constants.Key.ERROR);
					int id = o.get(Constants.Key.ID).getAsInt();
					String message = o.get(Constants.Key.MESSAGE).getAsString();
					int code = o.get(Constants.Key.CODE).getAsInt();
					JsonElement data = o.get(Constants.Key.DATA);
					onError(id, message, code, data);
					return;
				}

				if (o.has(Constants.Key.EVENT)) {
					processEvent(o.get(Constants.Key.EVENT).getAsString(), o);
					return;
				}

				if (o.has(Constants.Key.JSONRPC)) {
					processResponse(o.get(Constants.Key.ID).getAsInt(), o.get(Constants.Key.RESULT));
					return;
				}
				log.error("unhandled data: {}", text);

			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	protected void onError(int id, String message, int code, JsonElement data) {

		Call call = popCall(id);

		if (call == null) {
			log.error("no call found for request: " + id);
			return;
		}

		call.onError(code, message, data);
	}

	protected void processResponse(int id, JsonElement result) {
		log.trace("processResponse(): id:{} result: {}", id, result);
		Call call = popCall(id);

		if (call == null) {
			log.error("no call found for request: " + id);
			return;
		}

		call.processResult(this, result);
	}

	protected Call popCall(int id) {
		Call call = calls.remove(id);
		return call;
	}

	/**
	 * Parse and dispatch event to the {@link #eventListener}
	 *
	 * @param event The name of the event
	 * @param o     The event data
	 * @return true if the event was processed else false
	 */
	protected boolean processEvent(String event, JsonObject o) {
		switch (event) {
			case "track_playback_paused":
				eventListener.onTrackPlaybackPaused(
						o.get(Constants.Key.TL_TRACK).getAsJsonObject(),
						o.get(Constants.Key.TIME_POSITION).getAsLong());
				break;
			case "track_playback_resumed":
				eventListener.onTrackPlaybackResumed(
						o.get(Constants.Key.TL_TRACK).getAsJsonObject(),
						o.get(Constants.Key.TIME_POSITION).getAsLong());
				break;
			case "track_playback_started":
				eventListener.onTrackPlaybackStarted(o.get(Constants.Key.TL_TRACK).getAsJsonObject());
				break;
			case "track_playback_ended":
				eventListener.onTrackPlaybackEnded(
						o.get(Constants.Key.TL_TRACK).getAsJsonObject(),
						o.get(Constants.Key.TIME_POSITION).getAsLong());
				break;
			case "playback_state_changed":
				eventListener.onPlaybackStateChanged(
						PlaybackState.fromString(o.get(Constants.Key.OLD_STATE).getAsString()),
						PlaybackState.fromString(o.get(Constants.Key.NEW_STATE).getAsString()));
				break;
			case "tracklist_changed":
				eventListener.onTracklistChanged();
				break;
			case "playlists_loaded":
				eventListener.onPlaylistsLoaded();
				break;
			case "playlist_changed":
				eventListener.onPlaylistChanged(o.getAsJsonObject(Constants.Key.PLAYLIST));
				break;
			case "playlist_deleted":
				eventListener.onPlaylistDeleted(o.get(Constants.Key.URI).getAsString());
				break;
			case "stream_title_changed":
				eventListener.onStreamTitleChanged(o.get(Constants.Key.TITLE).getAsString());
				break;
			case "seeked":
				eventListener.onSeeked(o.get(Constants.Key.TIME_POSITION).getAsLong());
				break;
			case "mute_changed":
				eventListener.onMuteChanged(o.get(Constants.Key.MUTE).getAsBoolean());
				break;
			case "volume_changed":
				eventListener.onVolumeChanged(o.get(Constants.Key.VOLUME).getAsInt());
				break;
			case "options_changed":
				eventListener.onOptionsChanged();
				break;
			default:
				log.error("unknown event: " + o);
				return false;
		}
		return true;
	}

	/**
	 * Returns the size in bytes of all messages enqueued to be transmitted to the server. This
	 * doesn't include framing overhead. It also doesn't include any bytes buffered by the operating
	 * system or network intermediaries. This method returns 0 if no messages are waiting
	 * in the queue. If may return a nonzero value after the web socket has been canceled; this
	 * indicates that enqueued messages were not transmitted.
	 */
	public long getQueueSize() {
		return socket == null ? 0 : socket.queueSize();
	}


	public boolean isStarted() {
		return socket != null;
	}

	public long getTimeout() {
		return timeout;
	}

	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}

	public int getCallQueueSize() {
		return calls.size();
	}
}
