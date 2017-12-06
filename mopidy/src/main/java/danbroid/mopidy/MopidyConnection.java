package danbroid.mopidy;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import danbroid.mopidy.interfaces.Constants;
import danbroid.mopidy.interfaces.EventListener;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

/**
 * Wraps the websocket communication with the Mopidy server
 */

public final class MopidyConnection {

	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MopidyConnection.class);
	private final String url;

	private WebSocket socket;
	private JsonParser parser = new JsonParser();
	private EventListener eventListener = new EventListenerImpl();

	public MopidyConnection(String url) {
		this.url = url;
	}

	public MopidyConnection(String host, int port) {
		this("ws://" + host + ":" + port + "/mopidy/ws");
	}

	public void setEventListener(EventListener eventListener) {
		this.eventListener = eventListener;
	}

	public void start() {
		OkHttpClient client = new OkHttpClient();
		log.trace("start(): connecting to: {}", url);
		Request request = new Request.Builder().url(url).build();

		this.socket = client.newWebSocket(request, new WebSocketListener() {
			@Override
			public void onMessage(WebSocket webSocket, String text) {
				MopidyConnection.this.onMessage(webSocket, text);
			}
		});

	}

	public void stop() {
		log.debug("stop(): {}", url);
		if (socket != null) {
			socket.close(1000, "Finished");
			socket = null;
		}
	}

	public void onMessage(WebSocket webSocket, String text) {
		try {
			JsonElement e = parser.parse(text);
			if (e.isJsonObject()) {
				JsonObject o = e.getAsJsonObject();
				if (o.has(Constants.Key.EVENT)) {
					sendEvent(o.get(Constants.Key.EVENT).getAsString(), o);
				} else {
					log.error("unhandled data: {}", text);
				}
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	public void sendEvent(String event, JsonObject o) {
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
				eventListener.onTrackPlaybackEnded(o.get(Constants.Key.TL_TRACK).getAsJsonObject(), o.get(Constants.Key.TIME_POSITION).getAsLong());
				break;
			case "playback_state_changed":
				eventListener.onPlaybackStateChanged(o.get(Constants.Key.OLD_STATE).getAsString(), o.get(Constants.Key.NEW_STATE).getAsString());
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
				log.error("unexpected event: " + o);
				break;
		}
	}

}
