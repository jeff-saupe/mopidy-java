package danbroid.mopidy;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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
import danbroid.mopidy.model.Album;
import danbroid.mopidy.model.Artist;
import danbroid.mopidy.model.Base;
import danbroid.mopidy.model.Image;
import danbroid.mopidy.model.Ref;
import danbroid.mopidy.model.Track;
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

	private String url;

	private WebSocket socket;
	private JsonParser parser = new JsonParser();
	private EventListener eventListener = new EventListenerImpl();
	private String version;

	private AtomicInteger requestID = new AtomicInteger(0);
	private HashMap<Integer, Call> calls = new HashMap<>();
	private Gson gson;

	public void setEventListener(EventListener eventListener) {
		this.eventListener = eventListener;
	}

	public MopidyConnection(String host, int port) {
		this("ws://" + host + ":" + port + "/mopidy/ws");
	}

	public MopidyConnection(String url) {
		super(null);
		this.url = url;
		this.gson = getGsonBuilder().create();
	}


	public Gson getGson() {
		return gson;
	}

	public GsonBuilder getGsonBuilder() {
		RuntimeTypeAdapterFactory<Base> runtimeTypeAdapterFactory = RuntimeTypeAdapterFactory
				.of(Base.class, "__model__");

		for (Class<Base> clz : new Class[]{Album.class, Artist.class, Image.class, Ref.class, Track.class}) {
			runtimeTypeAdapterFactory.registerSubtype(clz, clz.getSimpleName());
		}
			/*	.registerSubtype(Image.class, "Image")
				.registerSubtype(Ref.class, "Ref");
*/
		return new GsonBuilder()
				.registerTypeAdapterFactory(runtimeTypeAdapterFactory);
	}

	public void start() {
		OkHttpClient client = new OkHttpClient();
		log.trace("start(): connecting to: {}", url);
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
			}
		});

	}


	/**
	 * Dispatches call to the web socket
	 */
	@Override
	public final void call(Call call) {
		prepareCall(call);
		String request = call.toString();
		log.trace("call(): request<{}>", request);
		socket.send(request);
	}

	protected void prepareCall(Call call) {
		int id = requestID.incrementAndGet();
		call.getRequest().addProperty(Constants.Key.ID, id);
		calls.put(id, call);
	}


	/**
	 * Shutsdown the socket.
	 */
	public void stop() {
		log.debug("stop(): {}", url);
		if (socket != null) {
			socket.close(1000, "Finished");
			socket = null;
			calls.clear();
		}
	}

	/**
	 * A message has been received
	 *
	 * @param text The JSON message
	 */
	public void onMessage(String text) {
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
		return calls.remove(id);
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
						o.get(Constants.Key.OLD_STATE).getAsString(),
						o.get(Constants.Key.NEW_STATE).getAsString());
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


}
