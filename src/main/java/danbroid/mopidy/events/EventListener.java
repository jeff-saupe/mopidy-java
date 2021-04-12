package danbroid.mopidy.events;

import com.google.gson.JsonObject;
import danbroid.mopidy.model.PlaybackState;

/*
See: https://github.com/mopidy/mopidy/blob/develop/mopidy/core/listener.py
 */

public interface EventListener {

    /**
     * Called on all events.
     */
    void onEvent();

    /**
     * Called whenever track playback is paused.
     *
     * @param tl_track      The track that was playing when playback paused
     * @param time_position The time position in milliseconds
     */
    void onTrackPlaybackPaused(JsonObject tl_track, long time_position);

    /**
     * Called whenever track playback is resumed.
     *
     * @param tl_track      The track that was playing when playback resumed
     * @param time_position The time position in milliseconds
     */
    void onTrackPlaybackResumed(JsonObject tl_track, long time_position);

    /**
     * Called whenever a new track starts playing.
     *
     * @param tl_track The track that just started playing
     */
    void onTrackPlaybackStarted(JsonObject tl_track);

    /**
     * Called whenever playback of a track ends.
     *
     * @param tl_track      The track that was played before playback stopped
     * @param time_position The time position in milliseconds
     */
    void onTrackPlaybackEnded(JsonObject tl_track, long time_position);

    /**
     * Called whenever playback state is changed.
     *
     * @param oldState The state before the change
     * @param newState The state after the change
     */
    void onPlaybackStateChanged(PlaybackState oldState, PlaybackState newState);

    /**
     * Called whenever the tracklist is changed.
     */
    void onTracklistChanged();

    /**
     * Called when playlists are loaded or refreshed.
     */
    void onPlaylistsLoaded();

    /**
     * Called whenever a playlist is changed.
     *
     * @param playlist The changed playlist
     */
    void onPlaylistChanged(JsonObject playlist);

    /**
     * Called whenever a playlist is deleted.
     *
     * @param uri The URI of the deleted playlist
     */
    void onPlaylistDeleted(String uri);

    /**
     * Called whenever an option is changed.
     */
    void onOptionsChanged();

    /**
     * Called whenever the volume is changed.
     *
     * @param volume The new volume in the range [0..100]
     */
    void onVolumeChanged(int volume);

    /**
     * Called whenever the mute state is changed.
     *
     * @param mute The new mute state
     */
    void onMuteChanged(boolean mute);

    /**
     * Called whenever the time position changes by an unexpected amount, e.g. at seek to a new time position.
     *
     * @param time_position The position that was seeked to in milliseconds
     */
    void onSeeked(long time_position);

    /**
     * Called whenever the currently playing stream title changes.
     *
     * @param title The new stream title
     */
    void onStreamTitleChanged(String title);

}