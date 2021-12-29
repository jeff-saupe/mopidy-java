package saupe.mopidy.api;

import com.google.gson.JsonElement;

import saupe.mopidy.model.PlaybackState;
import saupe.mopidy.model.TlTrack;
import saupe.mopidy.model.Track;
import lombok.extern.slf4j.Slf4j;

/*
 * See: https://github.com/mopidy/mopidy/blob/develop/mopidy/core/playback.py
 */

@Slf4j
public class Playback extends Api {
    protected Playback(Api parent) {
        super(parent, "playback.");
    }

    /**
     * Get the currently playing or selected track.
     *
     * @return {@link TlTrack} or Null
     */
    public Call<TlTrack> getCurrentTlTrack() {
        return createCall("get_current_tl_track", TlTrack.class);
    }

    /**
     * Get the currently playing or selected track.
     *
     * @return {@link Track} or Null
     */
    public Call<Track> getCurrentTrack() {
        return createCall("get_current_track", Track.class);
    }

    /**
     * Get the currently playing or selected TlId.
     *
     * @return Integer or Null
     */
    public Call<Integer> getCurrentTlId() {
        return createCall("get_current_tlid", Integer.class);
    }

    /**
     * Get the current stream title.
     *
     * @return String or Null
     */
    public Call<String> getStreamTitle() {
        return createCall("get_stream_title", String.class);
    }

    /**
     * Get the playback state.
     *
     * @return {@link PlaybackState}
     */
    public Call<PlaybackState> getState() {
        return new Call<PlaybackState>(methodPrefix + "get_state", client) {
            @Override
            protected PlaybackState parseResult(JsonElement response) {
                return PlaybackState.valueOf(response.getAsString().toUpperCase());
            }
        };
    }

    /**
     * Set the playback state.
     * <p>
     * Possible states and transitions:
     * <ul>
     *      <li>STOPPED -> PLAYING : {@link #play(Integer)}</li>
     *      <li>STOPPED -> PAUSED : {@link #pause()}</li>
     *      <li>PLAYING -> STOPPED : {@link #stop()}</li>
     *      <li>PLAYING -> PAUSED : {@link #pause()}</li>
     *      <li>PLAYING -> PLAYING : {@link #play(Integer)}</li>
     *      <li>PAUSED  -> PLAYING : {@link #resume()}</li>
     *      <li>PAUSED  -> STOPPED : {@link #stop()}</li>
     * </ul>
     *
     * @param state PlaybackState, must be: PLAYING, PAUSED or STOPPED.
     * @return Void
     */
    public Call<Void> setState(PlaybackState state) {
        return createCall("set_state", Void.class)
                .addParam("new_state", state.toString());
    }

    /**
     * Get time position in milliseconds.
     *
     * @return Long
     */
    public Call<Long> getTimePosition() {
        return createCall("get_time_position", Long.class);
    }

    /**
     * Change to the next track.
     * <p>
     * The current playback state will be kept.
     * If it was playing, playing it will continue.
     * If it was paused, it will still be paused, etc.
     *
     * @return Void
     */
    public Call<Void> next() {
        return createCall("next", Void.class);
    }

    /**
     * Pause playback.
     *
     * @return Void
     */
    public Call<Void> pause() {
        return createCall("pause", Void.class);
    }

    /**
     * Play the currently active track.
     * Note that the track must already be in the tracklist.
     *
     * @return Void
     */
    public Call<Void> play() {
        return createCall("play", Void.class);
    }

    /**
     * Play the given track.
     * <p>
     * Note that the track must already be in the tracklist.
     *
     * @param tlId ID of the track
     * @return Void
     */
    public Call<Void> play(Integer tlId) {
        return createCall("play", Void.class)
                .addParam("tlid", tlId);
    }

    /**
     * Change to the previous track.
     * <p>
     * The current playback state will be kept.
     * If it was playing, playing will continue.
     * If it was paused, it will still be paused, etc.
     *
     * @return Void
     */
    public Call<Void> previous() {
        return createCall("previous", Void.class);
    }

    /**
     * If paused, resume playing the current track.
     *
     * @return void
     */
    public Call<Void> resume() {
        return createCall("resume", Void.class);
    }

    /**
     * Seeks to time position given in milliseconds.
     *
     * @param timePosition Time position in milliseconds
     * @return True if successful, else False
     */
    public Call<Boolean> seek(long timePosition) {
        return createCall("seek", Boolean.class)
                .addParam("time_position", timePosition);
    }

    /**
     * Stop playing.
     *
     * @return Void
     */
    public Call<Void> stop() {
        return createCall("stop", Void.class);
    }
}