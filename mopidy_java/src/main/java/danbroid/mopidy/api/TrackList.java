package danbroid.mopidy.api;

import com.google.gson.JsonElement;

import danbroid.mopidy.ResponseHandler;
import danbroid.mopidy.model.TlTrack;
import danbroid.mopidy.model.Track;

/**
 * Created by dan on 13/12/17.
 */
public class TrackList extends Api {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TrackList.class);

	public TrackList(Api parent) {
		super(parent, "tracklist.");
	}

	//"""Get tracklist as list of :class:`mopidy.models.TlTrack`."""
	public void getTlTrackList(ResponseHandler<TlTrack[]> handler) {
		call(new Call<TlTrack[]>(methodPrefix + "get_tl_tracks")
				.setResultType(TlTrack[].class).setHandler(handler));
	}

	//"""Get tracklist as list of :class:`mopidy.models.Track`."""
	public void getTracks(ResponseHandler<Track[]> handler) {
		call(new Call<Track[]>(methodPrefix + "get_tracks")
				.setResultType(Track[].class).setHandler(handler));
	}

	//Get The length of the tracklist
	public void getLength(ResponseHandler<Integer> handler) {
		call(new Call<Integer>(methodPrefix + "get_length")
				.setResultType(Integer.class).setHandler(handler));
	}

	/*
	  Get the tracklist version.
	  Integer which is increased every time the tracklist is changed.
	  Is not reset before Mopidy is restarted.
	 */
	public void getVersion(ResponseHandler<Integer> handler) {
		call(new Call<Integer>(methodPrefix + "get_version")
				.setResultType(Integer.class).setHandler(handler));
	}

	/*
	Get consume mode.
        :class:`True`
            Tracks are removed from the tracklist when they have been played.
        :class:`False`
            Tracks are not removed from the tracklist.
	 */
	public void getConsume(ResponseHandler<Boolean> handler) {
		call(new Call<Boolean>(methodPrefix + "get_consume")
				.setResultType(Boolean.class).setHandler(handler));
	}

	/*
	Set consume mode.

        :class:`True`
            Tracks are removed from the tracklist when they have been played.
        :class:`False`
            Tracks are not removed from the tracklist.
	 */
	public void setConsume(boolean consume, ResponseHandler<Void> handler) {
		call(new Call(methodPrefix + "set_consume")
				.addParam("value", consume)
				.setHandler(handler));

	}

	/*
	Get random mode.
        :class:`True`
            Tracks are selected at random from the tracklist.
        :class:`False`
            Tracks are played in the order of the tracklist.
	 */
	public void getRandom(ResponseHandler<Boolean> handler) {
		call(new Call<Boolean>(methodPrefix + "get_random")
				.setResultType(Boolean.class).setHandler(handler));
	}

	/*
	Set random mode.

        :class:`True`
            Tracks are selected at random from the tracklist.
        :class:`False`
            Tracks are played in the order of the tracklist.
	 */
	public void setRandom(boolean random, ResponseHandler<Void> handler) {
		call(new Call(methodPrefix + "set_random")
				.addParam("value", random)
				.setHandler(handler));

	}

	/*
	Get repeat mode.

        :class:`True`
            The tracklist is played repeatedly.
        :class:`False`
            The tracklist is played once.
	 */
	public void getRepeat(ResponseHandler<Boolean> handler) {
		call(new Call<Boolean>(methodPrefix + "get_repeat").setResultType(Boolean.class).setHandler(handler));
	}

	/*
	Set repeat mode.

        To repeat a single track, set both ``repeat`` and ``single``.

        :class:`True`
            The tracklist is played repeatedly.
        :class:`False`
            The tracklist is played once.
	 */
	public void setRepeat(boolean repeat, ResponseHandler<Void> handler) {
		call(new Call(methodPrefix + "set_repeat")
				.addParam("value", repeat)
				.setHandler(handler));

	}

	/*
	 Get single mode.

        :class:`True`
            Playback is stopped after current song, unless in ``repeat`` mode.
        :class:`False`
            Playback continues after current song.
	 */
	public void getSingle(ResponseHandler<Boolean> handler) {
		call(new Call<Boolean>(methodPrefix + "get_single")
				.setResultType(Boolean.class).setHandler(handler));
	}

	/*
	Set single mode.

        :class:`True`
            Playback is stopped after current song, unless in ``repeat`` mode.
        :class:`False`
            Playback continues after current song.
	 */
	public void setSingle(boolean single, ResponseHandler<Void> handler) {
		call(new Call(methodPrefix + "set_single")
				.addParam("value", single)
				.setHandler(handler));
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

	public void add(String uri, JsonElement uris, ResponseHandler<TlTrack[]> handler) {
		call(new Call<TlTrack[]>(methodPrefix + "add").setResultType(TlTrack[].class)
				.addParam("uri", uri)
				.addParam("uris", uris)
				.addParam("tracks", (JsonElement) null)
				.addParam("at_position", (JsonElement) null)
				.setHandler(handler));
	}

	public void clear(ResponseHandler<Void> handler) {
		call(new Call<Void>(methodPrefix + "clear").setHandler(handler));
	}
}

