package danbroid.mopidy;


import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

import danbroid.mopidy.api.Call;
import danbroid.mopidy.api.Core;
import danbroid.mopidy.interfaces.CallContext;
import danbroid.mopidy.interfaces.Constants;
import danbroid.mopidy.interfaces.EventListener;
import danbroid.mopidy.interfaces.PlaybackState;
import danbroid.mopidy.transport.Transport;
import danbroid.mopidy.transport.WebSocketTransport;

/**
 * Wraps the websocket communication with the Mopidy server
 */

public class MopidyConnection extends Core implements CallContext, Transport.Callback {

	private static final org.slf4j.Logger
			log = org.slf4j.LoggerFactory.getLogger(MopidyConnection.class);
	public static final int ERROR_TIMEOUT = -20;

	private String url;


	private JsonParser parser = new JsonParser();
	private EventListener eventListener = new EventListenerImpl();
	private String version;

	public static final long DEFAULT_CALL_TIMEOUT = 3000;
	private long timeout = DEFAULT_CALL_TIMEOUT;


	private AtomicInteger requestID = new AtomicInteger(0);
	private HashMap<Integer, Call> calls = new HashMap<>();
	private WebSocketTransport transport;

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


		transport = new WebSocketTransport(this);
		transport.connect(url);

		version = null;

		getVersion().call(new ResponseHandler<String>() {
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

	@Override
	protected MopidyConnection getConnection() {
		return this;
	}

	protected void sendCall(Call call) {
		if (url == null) return;
		if (transport == null) start();


		int id = requestID.incrementAndGet();
		call.setID(id);
		call.setTimestamp(System.currentTimeMillis());
		calls.put(id, call);

		String request = call.toString();
		log.trace("call(): request<{}>", request);
		transport.send(request);
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
		if (transport != null) {
			log.debug("stop(): {}", url);
			transport.close();
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
	@Override
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


	public boolean isStarted() {
		return transport != null;
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
