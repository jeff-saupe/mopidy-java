package danbroid.mopidy.app.activities;

import com.google.gson.JsonObject;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.UiThread;

import java.util.HashSet;

import danbroid.mopidy.app.MopidyConnection;
import danbroid.mopidy.interfaces.EventListener;
import danbroid.mopidy.interfaces.PlaybackState;

/**
 * Created by dan on 14/12/17.
 */
@EBean(scope = EBean.Scope.Singleton)
public class Playback implements EventListener {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Playback.class);

	@Bean
	MopidyConnection conn;

	@AfterInject
	void init() {
		conn.setEventListener(this);
	}

	private HashSet<EventListener> listeners = new HashSet<>();

	public synchronized void addListener(EventListener listener) {
		if (!listeners.contains(listener)) listeners.add(listener);
	}

	public synchronized void removeListener(EventListener listener) {
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
		log.trace("onVolumeChanged(): {}", volume);
		for (EventListener listener : listeners) {
			listener.onVolumeChanged(volume);
		}
	}

	@Override
	public void onMuteChanged(boolean mute) {
		log.trace("onMuteChanged(): {}", mute);
		for (EventListener listener : listeners) {
			listener.onMuteChanged(mute);
		}
	}

	@Override
	public void onSeeked(long time_position) {
		log.trace("onStreamSeeked(): {}", time_position);
		for (EventListener listener : listeners) {
			listener.onSeeked(time_position);
		}
	}

	@Override
	public void onStreamTitleChanged(String title) {
		log.trace("onStreamTitleChanged(): {}", title);
		for (EventListener listener : listeners) {
			listener.onStreamTitleChanged(title);
		}
	}


	@Override
	public void onTrackPlaybackPaused(JsonObject tl_track, long time_position) {
		log.trace("onTrackPlaybackPaused(): position: {} track:{}", time_position, tl_track);
		for (EventListener listener : listeners) {
			listener.onTrackPlaybackPaused(tl_track, time_position);
		}
	}


	@Override
	public void onTrackPlaybackResumed(JsonObject tl_track, long time_position) {
		log.trace("onTrackPlaybackResumed(): position: {} track:{}", time_position, tl_track);
		for (EventListener listener : listeners) {
			listener.onTrackPlaybackResumed(tl_track, time_position);
		}
	}


	@Override
	public void onTrackPlaybackStarted(JsonObject tl_track) {
		log.trace("onTrackPlaybackStarted(): track:{}", tl_track);
		for (EventListener listener : listeners) {
			listener.onTrackPlaybackStarted(tl_track);
		}
	}

	@UiThread
	@Override
	public void onTrackPlaybackEnded(JsonObject tl_track, long time_position) {
		log.trace("onTrackPlaybackEnded(): position: {} track:{}", time_position, tl_track);
		for (EventListener listener : listeners) {
			listener.onTrackPlaybackEnded(tl_track, time_position);
		}
	}

	@UiThread
	@Override

	public void onTracklistChanged() {
		log.trace("onTracklistChanged() ");
		for (EventListener listener : listeners) {
			listener.onTracklistChanged();
		}
	}

	@UiThread
	@Override
	// Called when playlists are loaded or refreshed.
	public void onPlaylistsLoaded() {
		log.trace("onPlaylistsLoaded() ");
		for (EventListener listener : listeners) {
			listener.onPlaylistsLoaded();
		}
	}

	@UiThread
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

}
