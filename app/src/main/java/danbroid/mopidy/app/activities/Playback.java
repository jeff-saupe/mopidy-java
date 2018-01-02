package danbroid.mopidy.app.activities;

import com.google.gson.JsonObject;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.SupposeUiThread;

import java.util.HashSet;
import java.util.Set;

import danbroid.mopidy.AndroidMopidyConnection;
import danbroid.mopidy.interfaces.CallContext;
import danbroid.mopidy.interfaces.EventListener;
import danbroid.mopidy.interfaces.PlaybackState;
import danbroid.mopidy.model.TlTrack;
import danbroid.mopidy.util.UIResponseHandler;

/**
 * Created by dan on 14/12/17.
 */
@Deprecated
@EBean(scope = EBean.Scope.Singleton)
public class Playback implements EventListener {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Playback.class);

	@Bean
	AndroidMopidyConnection conn;
	private boolean mute;
	private int volume;
	private long time_position;
	private String streamTitle;
	private TlTrack tl_track;


	@AfterInject
	void init() {
		conn.setEventListener(this);

		conn.getMixer().getMute().call(new UIResponseHandler<Boolean>() {
			@Override
			public void onUIResponse(CallContext context, Boolean result) {
				onMuteChanged(result);
			}
		});

		conn.getMixer().getVolume().call(new UIResponseHandler<Integer>() {
			@Override
			public void onUIResponse(CallContext context, Integer result) {
				onVolumeChanged(result);
			}
		});

		conn.getPlayback().getTimePosition().call(new UIResponseHandler<Long>() {
			@Override
			public void onUIResponse(CallContext context, Long result) {
				onSeeked(result);
			}
		});

		conn.getPlayback().getCurrentTlTrack().call(new UIResponseHandler<TlTrack>() {
			@Override
			public void onUIResponse(CallContext context, TlTrack result) {
				tl_track = result;
			}
		});


	}

	private Set<EventListener> listeners = new HashSet<>();

	public TlTrack getTlTrack() {
		return tl_track;
	}

	public long getTimePosition() {
		return time_position;
	}


	public boolean getMute() {
		return mute;
	}

	public int getVolume() {
		return volume;
	}

	public String getStreamTitle() {
		return streamTitle;
	}


	@SupposeUiThread
	public void addListener(EventListener listener) {
		if (!listeners.contains(listener)) {
			listeners.add(listener);
			initListener(listener);
		}
	}

	private void initListener(EventListener listener) {
		listener.onPlaybackStateChanged(null, state);
	}

	@SupposeUiThread
	public void removeListener(EventListener listener) {
		if (listeners.contains(listener))
			listeners.remove(listener);
	}

	private PlaybackState state;


	@Override
	public void onPlaybackStateChanged(PlaybackState oldState, PlaybackState newState) {
		this.state = newState;
		for (EventListener listener : listeners) {
			listener.onPlaybackStateChanged(oldState, newState);
		}
	}

	public PlaybackState getState() {
		return state;
	}


	@Override
	public void onOptionsChanged() {
		log.trace("onOptionsChanged()");
		for (EventListener listener : listeners) {
			listener.onOptionsChanged();
		}
	}


	@Override
	public void onVolumeChanged(int volume) {
		this.volume = volume;
		log.trace("onVolumeChanged(): {}", volume);
		for (EventListener listener : listeners) {
			listener.onVolumeChanged(volume);
		}
	}

	@Override
	public void onMuteChanged(boolean mute) {
		this.mute = mute;
		log.trace("onMuteChanged(): {}", mute);
		for (EventListener listener : listeners) {
			listener.onMuteChanged(mute);
		}
	}

	@Override
	public void onSeeked(long time_position) {
		this.time_position = time_position;
		log.trace("onStreamSeeked(): {}", time_position);
		for (EventListener listener : listeners) {
			listener.onSeeked(time_position);
		}
	}

	@Override
	public void onStreamTitleChanged(String title) {
		this.streamTitle = title;
		log.trace("onStreamTitleChanged(): {}", title);
		for (EventListener listener : listeners) {
			listener.onStreamTitleChanged(title);
		}
	}


	@Override
	public void onTrackPlaybackPaused(JsonObject tl_track, long time_position) {
		this.time_position = time_position;
		log.trace("onTrackPlaybackPaused(): position: {} track:{}", time_position, tl_track);
		for (EventListener listener : listeners) {
			listener.onTrackPlaybackPaused(tl_track, time_position);
		}
	}


	@Override
	public void onTrackPlaybackResumed(JsonObject tl_track, long time_position) {
		this.time_position = time_position;

		log.trace("onTrackPlaybackResumed(): position: {} track:{}", time_position, tl_track);
		for (EventListener listener : listeners) {
			listener.onTrackPlaybackResumed(tl_track, time_position);
		}
	}


	@Override
	public void onTrackPlaybackStarted(JsonObject tl_track) {
		log.trace("onTrackPlaybackStarted(): track:{}", tl_track);
		this.tl_track = conn.getGson().fromJson(tl_track, TlTrack.class);
		for (EventListener listener : listeners) {
			listener.onTrackPlaybackStarted(tl_track);
		}
	}


	@Override
	public void onTrackPlaybackEnded(JsonObject tl_track, long time_position) {
		log.trace("onTrackPlaybackEnded(): position: {} track:{}", time_position, tl_track);
		for (EventListener listener : listeners) {
			listener.onTrackPlaybackEnded(tl_track, time_position);
		}
	}


	@Override
	public void onTracklistChanged() {
		log.trace("onTracklistChanged() ");
		for (EventListener listener : listeners) {
			listener.onTracklistChanged();
		}
	}


	@Override
	// Called when playlists are loaded or refreshed.
	public void onPlaylistsLoaded() {
		log.trace("onPlaylistsLoaded() ");
		for (EventListener listener : listeners) {
			listener.onPlaylistsLoaded();
		}
	}


	@Override
	//    Called whenever a playlist is changed.
	public void onPlaylistChanged(JsonObject playlist) {
		log.trace("onPlaylistChanged() :{}", playlist);
		for (EventListener listener : listeners) {
			listener.onPlaylistChanged(playlist);
		}
	}


	@Override
	//Called whenever a playlist is deleted.
	public void onPlaylistDeleted(String uri) {
		log.trace("onPLaylistDeleted(): {}", uri);
		for (EventListener listener : listeners) {
			listener.onPlaylistDeleted(uri);
		}
	}

	public AndroidMopidyConnection getConnection() {
		return conn;
	}

	public void togglePlay() {
		if (isPaused()) {
			conn.getPlayback().play(null, null).call();
		} else {
			conn.getPlayback().pause().call();
		}
	}

	public boolean isPaused() {
		return PlaybackState.PAUSED.equals(state);
	}
}
