package saupe.mopidy.api;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import saupe.mopidy.model.TlTrack;
import saupe.mopidy.model.Track;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;

/*
 * See: https://github.com/mopidy/mopidy/blob/develop/mopidy/core/tracklist.py
 * TODO: filter
 */

@Slf4j
public class Tracklist extends Api {
    public Tracklist(Api parent) {
        super(parent, "tracklist.");
    }

    /**
     * Get tracklist.
     *
     * @return Array of {@link TlTrack}
     */
    public Call<TlTrack[]> getTlTracks() {
        return createCall("get_tl_tracks", TlTrack[].class);
    }

    /**
     * Get tracklist.
     *
     * @return Array of {@link Track}
     */
    public Call<Track[]> getTracks() {
        return createCall("get_tracks", Track[].class);
    }

    /**
     * Get length of the tracklist.
     *
     * @return Integer
     */
    public Call<Integer> getLength() {
        return createCall("get_length", Integer.class);
    }

    /**
     * Get the tracklist version.
     * <p>
     * It is increased every time the tracklist is changed and is not reset before Mopidy is restarted.
     *
     * @return Integer
     */
    public Call<Integer> getVersion() {
        return createCall("get_version", Integer.class);
    }

    /**
     * Get consume mode.
     * <p>
     * True: Tracks are removed from the tracklist when they have been played.
     * False: Tracks are not removed from the tracklist.
     *
     * @return Boolean
     */
    public Call<Boolean> getConsume() {
        return createCall("get_consume", Boolean.class);
    }

    /**
     * Set consume mode.
     * <p>
     * True: Tracks are removed from the tracklist when they have been played.
     * False: Tracks are not removed from the tracklist.
     *
     * @param consume Boolean
     * @return Void
     */
    public Call<Void> setConsume(boolean consume) {
        return createCall("set_consume", Void.class)
                .addParam("value", consume);
    }

    /**
     * Get random mode.
     * <p>
     * True: Tracks are selected at random from the tracklist.
     * False: Tracks are played in the order of the tracklist.
     *
     * @return Boolean
     */
    public Call<Boolean> getRandom() {
        return createCall("get_random", Boolean.class);
    }

    /**
     * Set random mode.
     * <p>
     * True: Tracks are selected at random from the tracklist.
     * False: Tracks are played in the order of the tracklist.
     *
     * @param random Boolean
     * @return Void
     */
    public Call<Void> setRandom(boolean random) {
        return createCall("set_consume", Void.class)
                .addParam("value", random);

    }

    /**
     * Get repeat mode.
     * <p>
     * True: The tracklist is played repeatedly.
     * False: The tracklist is played once.
     *
     * @return Boolean
     */
    public Call<Boolean> getRepeat() {
        return createCall("get_repeat", Boolean.class);
    }

    /**
     * Set repeat mode.
     * <p>
     * To repeat a single track, set both {@link #setRepeat(boolean)} and {@link #setSingle(boolean)}.
     * <p>
     * True: The tracklist is played repeatedly.
     * False: The tracklist is played once.
     *
     * @param repeat Boolean
     * @return Void
     */
    public Call<Void> setRepeat(boolean repeat) {
        return createCall("set_repeat", Void.class)
                .addParam("value", repeat);

    }

    /**
     * Get single mode.
     * <p>
     * True: Playback is stopped after current song, unless in ``repeat`` mode.
     * False: Playback continues after current song.
     *
     * @return Boolean
     */
    public Call<Boolean> getSingle() {
        return createCall("get_single", Boolean.class);
    }

    /**
     * Set single mode.
     * <p>
     * True: Playback is stopped after current song, unless in ``repeat`` mode.
     * False: Playback continues after current song.
     *
     * @param single Boolean
     * @return Void
     */
    public Call<Void> setSingle(boolean single) {
        return createCall("set_single", Void.class)
                .addParam("value", single);
    }

    /**
     * The position of the current track in the tracklist.
     *
     * @return Integer or Null
     */
    public Call<Integer> index() {
        return createCall("index", Integer.class);
    }

    /**
     * The position of the given track in the tracklist.
     *
     * @param tlTrack The track to find the index of
     * @return Integer or Null
     */
    public Call<Integer> index(TlTrack tlTrack) {
        return createCall("index", Integer.class)
                .addParam("tl_track", getGson().toJsonTree(tlTrack));
    }

    /**
     * The position of the given track in the tracklist.
     *
     * @param tlId TlId of the track to find the index of
     * @return Integer or Null
     */
    public Call<Integer> index(Integer tlId) {
        return createCall("index", Integer.class)
                .addParam("tlid", tlId);
    }

    /**
     * The TlId of the track that will be played after the current track.
     * <p>
     * Not necessarily the same TlId as returned by {@link #getNextTlId()}
     *
     * @return Integer or Null
     */
    public Call<Integer> getEotTlId() {
        return createCall("get_eot_tlid", Integer.class);
    }

    /**
     * The TlId of the track that will be played if calling {@see danbroid.mopidy.api.Playback#next}
     * <p>
     * For normal playback, this is the next track in the tracklist.
     * If repeat is enabled, the next track can loop around the tracklist.
     * When random is enabled this should be a random track, all tracks should be played once before the tracklist repeats.
     *
     * @return Integer or Null
     */
    public Call<Integer> getNextTlId() {
        return createCall("get_next_tlid", Integer.class);
    }

    /**
     * Returns the TlId of the track that will be played if calling {@see danbroid.mopidy.api.Playback#previous}
     * <p>
     * For normal playback, this is the previous track in the tracklist.
     * <p>
     * If random and/or consume is enabled, it should return the current track instead.
     *
     * @return Integer or Null
     */
    public Call<Integer> getPreviousTlId() {
        return createCall("get_previous_tlid", Integer.class);
    }

    /**
     * Add tracks to the tracklist.
     * <p>
     * If {@code at_position} is given, the tracks are inserted at the given position in the tracklist.
     * If {@code at_position} is not given, the tracks are appended to the end of the tracklist.
     *
     * @param atPosition Position in tracklist to add tracks
     * @param uris       List of URIs for tracks to add
     * @return Array of {@link TlTrack}
     */
    public Call<TlTrack[]> add(@Nullable Integer atPosition, String[] uris) {
        Call<TlTrack[]> call = createCall("add", TlTrack[].class);
        call.addParam("uris", getGson().toJsonTree(uris));
        if (atPosition != null)
            call.addParam("at_position", atPosition);
        return call;
    }

    public Call<TlTrack[]> add(int atPosition, String uri) {
        return add(atPosition, new String[]{uri});
    }

    public Call<TlTrack[]> add(String uri) {
        return add(-1, uri);
    }

    public Call<TlTrack[]> add(String[] uris) {
        return add(-1, uris);
    }

    /**
     * Clear the tracklist.
     * <p>
     * Triggers the {@code tracklist_changed} event.
     *
     * @return Void
     */
    public Call<Void> clear() {
        return createCall("clear", Void.class);
    }

    /**
     * Move the tracks in the slice [start:end] to {@code to_position}.
     * <p>
     * Triggers the {@code tracklist_changed} event.
     *
     * @param start      Position of first track to move
     * @param end        Position after last track to move
     * @param toPosition New position for the tracks
     * @return Void
     */
    public Call<Void> move(int start, int end, int toPosition) {
        return createCall("move", Void.class)
                .addParam("start", start)
                .addParam("end", end)
                .addParam("to_position", toPosition);
    }

    /**
     * Remove the matching tracks from the tracklist.
     *
     * @param tlIds Array of track IDs
     * @return Array of {@link TlTrack}
     */
    public Call<TlTrack[]> remove(int[] tlIds) {
        JsonArray array = new JsonArray(tlIds.length);
        for (int tlId : tlIds) array.add(tlId);

        JsonObject criteria = new JsonObject();
        criteria.add("tlid", array);

        return createCall("remove", TlTrack[].class)
                .addParam("criteria", criteria);
    }

    /**
     * Shuffles the entire tracklist.
     * <p>
     * Triggers the {@code tracklist_changed} event.
     *
     * @return Void
     */
    public Call<Void> shuffle() {
        return createCall("shuffle", Void.class);
    }

    /**
     * Shuffles the slice [start:end], only if {@code start} and {@code code} are given.
     * <p>
     * Triggers the {@code tracklist_changed} event.
     *
     * @param start Position of first track to shuffle
     * @param end   Position after last track to shuffle
     * @return Void
     */
    public Call<Void> shuffle(int start, int end) {
        return createCall("shuffle", Void.class)
                .addParam("start", start)
                .addParam("end", end);
    }

    /**
     * Returns a slice of the tracklist, limited by the given start and end positions.
     *
     * @param start Position of first track to include in slice
     * @param end   Position after last track to include in slice
     * @return TlTrack
     */
    public Call<TlTrack> slice(int start, int end) {
        return createCall("slice", TlTrack.class);
    }
}