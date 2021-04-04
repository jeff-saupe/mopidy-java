package danbroid.mopidy.interfaces;

import com.google.gson.JsonObject;

/**
 * Created by dan on 6/12/17.
 */
public interface EventListener {

	void onOptionsChanged();

	void onVolumeChanged(int volume);

	void onMuteChanged(boolean mute);

	void onSeeked(long time_position);

	void onStreamTitleChanged(String title);

	void onTrackPlaybackPaused(JsonObject tl_track, long time_position);

	void onTrackPlaybackResumed(JsonObject tl_track, long time_position);

	void onTrackPlaybackStarted(JsonObject tl_track);

	void onTrackPlaybackEnded(JsonObject tl_track, long time_position);

	void onPlaybackStateChanged(PlaybackState oldState, PlaybackState newState);

	void onTracklistChanged();

	// Called when playlists are loaded or refreshed.
	void onPlaylistsLoaded();

	//    Called whenever a playlist is changed.
	void onPlaylistChanged(JsonObject playlist);

	//Called whenever a playlist is deleted.
	void onPlaylistDeleted(String uri);

}