package danbroid.mopidy.api;

import danbroid.mopidy.ResponseHandler;
import danbroid.mopidy.model.TlTrack;

/**
 * Created by dan on 13/12/17.
 */
public class TrackList extends Api {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TrackList.class);

	public TrackList(Api parent) {
		super(parent, "tracklist.");
	}

	//"""Get tracklist as list of :class:`mopidy.models.TlTrack`."""
	public Call<TlTrack[]> getTlTrackList() {
		return createCall("get_tl_tracks", TlTrack[].class);
	}

	//"""Get tracklist as list of :class:`mopidy.models.Track`."""
	public Call<TlTrack[]> getTracks() {
		return createCall("get_tracks", TlTrack[].class);
	}

	//Get The length of the tracklist
	public Call<Integer> getLength(ResponseHandler<Integer> handler) {
		return createCall("get_length", Integer.class);
	}

	/*
	  Get the tracklist version.
	  Integer which is increased every time the tracklist is changed.
	  Is not reset before Mopidy is restarted.
	 */
	public Call<Integer> getVersion(ResponseHandler<Integer> handler) {
		return createCall("get_version", Integer.class);
	}

	/*
	Get consume mode.
        :class:`True`
            Tracks are removed from the tracklist when they have been played.
        :class:`False`
            Tracks are not removed from the tracklist.
	 */
	public Call<Boolean> getConsume() {
		return createCall("get_consume", Boolean.class);
	}

	/*
	Set consume mode.

        :class:`True`
            Tracks are removed from the tracklist when they have been played.
        :class:`False`
            Tracks are not removed from the tracklist.
	 */
	public Call<Void> setConsume(boolean consume) {
		return createCall("set_consume", Void.class)
				.addParam("value", consume);
	}

	/*
	Get random mode.
        :class:`True`
            Tracks are selected at random from the tracklist.
        :class:`False`
            Tracks are played in the order of the tracklist.
	 */
	public Call<Boolean> getRandom() {
		return createCall("get_random", Boolean.class);
	}

	/*
	Set random mode.

        :class:`True`
            Tracks are selected at random from the tracklist.
        :class:`False`
            Tracks are played in the order of the tracklist.
	 */
	public Call<Void> setRandom(boolean random) {
		return createCall("set_consume", Void.class)
				.addParam("value", random);

	}

	/*
	Get repeat mode.

        :class:`True`
            The tracklist is played repeatedly.
        :class:`False`
            The tracklist is played once.
	 */
	public Call<Boolean> getRepeat() {
		return createCall("get_repeat", Boolean.class);
	}

	/*
	Set repeat mode.

        To repeat a single track, set both ``repeat`` and ``single``.

        :class:`True`
            The tracklist is played repeatedly.
        :class:`False`
            The tracklist is played once.
	 */
	public Call<Void> setRepeat(boolean repeat) {
		return createCall("set_repeat", Void.class)
				.addParam("value", repeat);

	}

	/*
	 Get single mode.

        :class:`True`
            Playback is stopped after current song, unless in ``repeat`` mode.
        :class:`False`
            Playback continues after current song.
	 */
	public Call<Boolean> getSingle() {
		return createCall("get_single", Boolean.class);
	}

	/*
	Set single mode.

        :class:`True`
            Playback is stopped after current song, unless in ``repeat`` mode.
        :class:`False`
            Playback continues after current song.
	 */
	public Call<Void> setSingle(boolean single) {
		return createCall("set_single", Void.class)
				.addParam("value", single);

	}


	/*
	Add tracks to the tracklist.

        If ``uri`` is given instead of ``tracks``, the URI is looked up in the
        library and the resulting tracks are added to the tracklist.

        If ``uris`` is given instead of ``uri`` or ``tracks``, the URIs are
        looked up in the library and the resulting tracks are added to the
        tracklist.

        If ``at_position`` is given, the tracks are inserted at the given
        position in the tracklist. If ``at_position`` is not given, the tracks
        are appended to the end of the tracklist.

        Triggers the :meth:`mopidy.core.CoreListener.tracklist_changed` event.

        :param tracks: tracks to add
        :type tracks: list of :class:`mopidy.models.Track` or :class:`None`
        :param at_position: position in tracklist to add tracks
        :type at_position: int or :class:`None`
        :param uri: URI for tracks to add
        :type uri: string or :class:`None`
        :param uris: list of URIs for tracks to add
        :type uris: list of string or :class:`None`
        :rtype: list of :class:`mopidy.models.TlTrack`
	 */

	public Call<TlTrack[]> add(String[] uris, int position) {
		Call<TlTrack[]> call = createCall("add", TlTrack[].class)
				.addParam("uris", getGson().toJsonTree(uris));
		if (position > -1)
			call.addParam("at_position", position);
		return call;
	}

	public Call<TlTrack[]> add(String uri, int position) {
		return add(new String[]{uri}, position);
	}

	public Call<TlTrack[]> add(String uri) {
		return add(uri, -1);
	}

	public Call<TlTrack[]> add(String uris[]) {
		return add(uris, -1);
	}


	public Call<Void> clear() {
		return createCall("clear", Void.class);
	}
}

