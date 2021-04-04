package danbroid.mopidy;


import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

import danbroid.mopidy.api.Call;
import danbroid.mopidy.api.Core;
import danbroid.mopidy.interfaces.CallContext;
import danbroid.mopidy.interfaces.JSONConstants;
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
	public static final int ERROR_TIMEOUT = -2;
	public static final int ERROR_TRANSPORT = -1;

	private String url;


	private JsonParser parser = new JsonParser();
	private EventListener eventListener = new EventListenerImpl();

	public static final long DEFAULT_CALL_TIMEOUT = 3000;
	private long timeout = DEFAULT_CALL_TIMEOUT;


	private AtomicInteger requestID = new AtomicInteger(0);
	private HashMap<Integer, Call> calls = new HashMap<>();
	private Transport transport;

	public void setEventListener(EventListener eventListener) {
		this.eventListener = eventListener;
	}


	public void setURL(String url) {
		log.trace("setURL(): {}", url);
		this.url = url;
	}

	public String getURL() {
		return url;
	}

	public Call<String> start(String url) {
		log.info("start(): {}", url);
		setURL(url);
		return start();
	}

	public Call<String> start(String host, int port) {
		setURL(host, port);
		return start();
	}


	public Call<String> start() {
		log.info("start() url: {}", url);
		if (url == null) return null;

		stop();

		transport = createTransport();
		transport.connect(url);

		return getVersion();
	}

	protected Transport createTransport() {
		return new WebSocketTransport(this);
	}

	public Call<String> getVersion() {
		return createCall("get_version", String.class);
	}

	@Override
	public void onConnected() {
		log.info("onConnected()");
	}

	@Override
	public void onDisconnected() {
		log.warn("onDisconnected()");
	}

	/**
	 * Dispatches call to the web socket
	 */
	@Override
	public synchronized void call(Call call) {
		if (url == null) return;
		if (transport == null) start(null);


		int id = requestID.incrementAndGet();
		call.setID(id);
		call.setTimestamp(System.currentTimeMillis());
		calls.put(id, call);

		String request = call.toString();
		log.trace("call(): request:{}", request);
		transport.send(request);
	}


	/**
	 * Shutdown the socket and clear call queue
	 */
	public synchronized void stop() {

		if (transport != null) {
			log.debug("stop(): {}", url);
			transport.close();
			transport = null;
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

	@Override
	public synchronized void onError(Throwable t) {
		log.error(t.getMessage(), t);
		String message = t.getLocalizedMessage();
		if (t.getCause() != null) message = t.getCause().getLocalizedMessage();
		for (Call call : calls.values()) {
			call.onError(ERROR_TRANSPORT, message, null);
		}
	}

	protected void processMessage(String text) {
		//log.trace("processMessage(): {}", text);
		try {
			JsonElement e = parser.parse(text);
			if (e.isJsonObject()) {
				JsonObject o = e.getAsJsonObject();

				if (o.has(JSONConstants.ERROR)) {
					log.error("got error: {}",text);
					o = o.getAsJsonObject(JSONConstants.ERROR);
					int id = o.get(JSONConstants.ID).getAsInt();
					String message = o.get(JSONConstants.MESSAGE).getAsString();
					int code = 0;
					if (o.has(JSONConstants.CODE)) code = o.get(JSONConstants.CODE).getAsInt();

					JsonElement data = o.get(JSONConstants.DATA);
					processError(id, message, code, data);
					return;
				}

				if (o.has(JSONConstants.EVENT)) {
					processEvent(o.get(JSONConstants.EVENT).getAsString(), o);
					return;
				}

				if (o.has(JSONConstants.JSONRPC)) {
					processResponse(o.get(JSONConstants.ID).getAsInt(), o.get(JSONConstants.RESULT));
					return;
				}
				log.error("unhandled data: {}", text);

			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	protected void processError(int id, String message, int code, JsonElement data) {

		Call call = popCall(id);

		if (call == null) {
			log.error("no call found for request: " + id);
			return;
		}

		call.onError(code, message, data);
	}

	protected void processResponse(int id, JsonElement result) {
		//log.trace("processResponse(): id:{} result: {}", id, result);
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
						o.get(JSONConstants.TL_TRACK).getAsJsonObject(),
						o.get(JSONConstants.TIME_POSITION).getAsLong());
				break;

			case "track_playback_resumed":
				eventListener.onTrackPlaybackResumed(
						o.get(JSONConstants.TL_TRACK).getAsJsonObject(),
						o.get(JSONConstants.TIME_POSITION).getAsLong());
				break;

			case "track_playback_started":
				eventListener.onTrackPlaybackStarted(o.get(JSONConstants.TL_TRACK).getAsJsonObject());
				break;

			case "track_playback_ended":
				eventListener.onTrackPlaybackEnded(
						o.get(JSONConstants.TL_TRACK).getAsJsonObject(),
						o.get(JSONConstants.TIME_POSITION).getAsLong());
				break;

			case "playback_state_changed":
				eventListener.onPlaybackStateChanged(
						PlaybackState.fromString(o.get(JSONConstants.OLD_STATE).getAsString()),
						PlaybackState.fromString(o.get(JSONConstants.NEW_STATE).getAsString()));
				break;

			case "tracklist_changed":
				eventListener.onTracklistChanged();
				break;

			case "playlists_loaded":
				eventListener.onPlaylistsLoaded();
				break;

			case "playlist_changed":
				eventListener.onPlaylistChanged(o.getAsJsonObject(JSONConstants.PLAYLIST));
				break;

			case "playlist_deleted":
				eventListener.onPlaylistDeleted(o.get(JSONConstants.URI).getAsString());
				break;

			case "stream_title_changed":
				eventListener.onStreamTitleChanged(o.get(JSONConstants.TITLE).getAsString());
				break;

			case "seeked":
				eventListener.onSeeked(o.get(JSONConstants.TIME_POSITION).getAsLong());
				break;

			case "mute_changed":
				eventListener.onMuteChanged(o.get(JSONConstants.MUTE).getAsBoolean());
				break;

			case "volume_changed":
				eventListener.onVolumeChanged(o.get(JSONConstants.VOLUME).getAsInt());
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

	@Override
	public MopidyConnection getConnection() {
		return this;
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

	public void setURL(String host, int port) {
		setURL("ws://" + host + ":" + port + "/mopidy/ws");
	}

	public Transport getTransport() {
		return transport;
	}
}
