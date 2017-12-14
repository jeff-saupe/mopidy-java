package danbroid.mopidy;

import com.google.gson.JsonObject;

import danbroid.mopidy.interfaces.EventListener;
import danbroid.mopidy.interfaces.PlaybackState;

/**
 * see: core/listener.py
 */

public class EventListenerImpl implements EventListener {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(EventListenerImpl.class);

	public void onOptionsChanged() {
		log.trace("onOptionsChanged()");
	}

	public void onVolumeChanged(int volume) {
		log.trace("onVolumeChanged(): {}", volume);
	}

	public void onMuteChanged(boolean mute) {
		log.trace("onMuteChanged(): {}", mute);
	}

	public void onSeeked(long time_position) {
		log.trace("onStreamSeeked(): {}", time_position);
	}

	public void onStreamTitleChanged(String title) {
		log.trace("onStreamTitleChanged(): {}", title);
	}

	public void onTrackPlaybackPaused(JsonObject tl_track, long time_position) {
		log.trace("onTrackPlaybackPaused(): position: {} track:{}", time_position, tl_track);
	}

	public void onTrackPlaybackResumed(JsonObject tl_track, long time_position) {
		log.trace("onTrackPlaybackResumed(): position: {} track:{}", time_position, tl_track);
	}

	public void onTrackPlaybackStarted(JsonObject tl_track) {
		log.trace("onTrackPlaybackStarted(): track:{}", tl_track);
	}

	public void onTrackPlaybackEnded(JsonObject tl_track, long time_position) {
		log.trace("onTrackPlaybackEnded(): position: {} track:{}", time_position, tl_track);
	}

	@Override
	public void onPlaybackStateChanged(PlaybackState oldState, PlaybackState newState) {
		log.trace("onPlaybackStateChanged(): {} -> {}", oldState, newState);
	}

	public void onTracklistChanged() {
		log.trace("onTracklistChanged() ");
	}

	// Called when playlists are loaded or refreshed.
	public void onPlaylistsLoaded() {
		log.trace("onPlaylistsLoaded() ");
	}


	//    Called whenever a playlist is changed.
	public void onPlaylistChanged(JsonObject playlist) {
		log.trace("onPlaylistChanged() :{}", playlist);
	}

	//Called whenever a playlist is deleted.
	public void onPlaylistDeleted(String uri) {
		log.trace("onPLaylistDeleted(): {}", uri);
	}

}
