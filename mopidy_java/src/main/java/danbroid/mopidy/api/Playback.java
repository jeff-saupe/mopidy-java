package danbroid.mopidy.api;

import com.google.gson.JsonElement;

import danbroid.mopidy.ResponseHandler;
import danbroid.mopidy.interfaces.CallContext;
import danbroid.mopidy.interfaces.PlaybackState;
import danbroid.mopidy.model.TlTrack;
import danbroid.mopidy.model.Track;

/**
 * Created by dan on 13/12/17.
 * Finished implementation from playback.py
 */
public class Playback extends Api {

	protected Playback(Api parent) {
		super(parent, "playback.");
	}

	public void getCurrentTlTrack(ResponseHandler<TlTrack> handler) {
		call(new Call<TlTrack>(methodPrefix + "get_current_tl_track")
				.setResultType(TlTrack.class).setHandler(handler));
	}

	/**
	 * @param handler receives the current tlid or null
	 */
	public void getCurrentTLID(ResponseHandler<Integer> handler) {
		call(new Call<Integer>(methodPrefix + "get_current_tlid")
				.setResultType(Integer.class).setHandler(handler));
	}


	/**
	 * @param handler receives the current Track or null
	 */
	public void getCurrentTrack(ResponseHandler<Track> handler) {
		call(new Call<Track>(methodPrefix + "get_current_track")
				.setResultType(Track.class).setHandler(handler));
	}

	public void getState(ResponseHandler<PlaybackState> handler) {
		call(new Call<PlaybackState>(methodPrefix + "get_state") {
			@Override
			protected PlaybackState parseResult(CallContext callContext, JsonElement response) {
				return PlaybackState.valueOf(response.getAsString().toUpperCase());
			}
		}.setHandler(handler)
				.setResultType(PlaybackState.class));

	}

	/*
	Set the playback state.

Must be :attr:`PLAYING`, :attr:`PAUSED`, or :attr:`STOPPED`.

Possible states and transitions:

	.. digraph:: state_transitions

				"STOPPED" -> "PLAYING" [ label="play" ]
	"STOPPED" -> "PAUSED" [ label="pause" ]
	"PLAYING" -> "STOPPED" [ label="stop" ]
	"PLAYING" -> "PAUSED" [ label="pause" ]
	"PLAYING" -> "PLAYING" [ label="play" ]
	"PAUSED" -> "PLAYING" [ label="resume" ]
	"PAUSED" -> "STOPPED" [ label="stop" ]
*/
	public void setState(PlaybackState state, ResponseHandler<Void> handler) {
		call(new Call<Void>(methodPrefix + "set_state")
				.addParam("new_state", state.toString()).setHandler(handler));

	}

	public void getStreamTitle(ResponseHandler<String> handler) {
		call(new Call<String>(methodPrefix + "get_stream_title").setHandler(handler).setResultType(String.class));
	}

	public void getTimePosition(ResponseHandler<Long> handler) {
		call(new Call<Long>(methodPrefix + "get_time_position").setHandler(handler).setResultType(Long.class));
	}

	/*
	Change to the next track.

The current playback state will be kept. If it was playing, playing
will continue. If it was paused, it will still be paused, etc.
	 */
	public void next(ResponseHandler<Void> handler) {
		call(new Call(methodPrefix + "next").setHandler(handler));
	}

	/**
	 * Pause current playback
	 *
	 * @param handler
	 */
	public void pause(ResponseHandler<Void> handler) {
		call(new Call(methodPrefix + "pause").setHandler(handler));
	}

	/**
	 * Pause current playback
	 **/
	public void pause() {
		pause(null);
	}


	/**
	 * Play the given track, or if the given tl_track and tlid is
	 * :class:`None`, play the currently active track.
	 * <p>
	 * Note that the track **must** already be in the tracklist.
	 * <p>
	 * :param tl_track: track to play
	 * :type tl_track: :class:`mopidy.models.TlTrack` or :class:`None`
	 * :param tlid: TLID of the track to play
	 * :type tlid: :class:`int` or :class:`None`
	 */
	public void play(Integer tlid, TlTrack tlTrack, ResponseHandler<Void> handler) {
		JsonElement jsonTrack = null;
		if (tlTrack != null)
			jsonTrack = getGson().toJsonTree(tlTrack);

		call(new Call<Void>(methodPrefix + "play")
				.addParam("tlid", tlid)
				.addParam("tl_track", jsonTrack)
				.setHandler(handler));
	}

	public void play() {
		play(null, null, null);
	}

	/*
	Change to the previous track.

        The current playback state will be kept. If it was playing, playing
        will continue. If it was paused, it will still be paused, etc.

	 */
	public void previous(ResponseHandler<Void> handler) {
		call(new Call(methodPrefix + "previous").setHandler(handler));
	}


	//If paused, resume playing the current track
	public void resume(ResponseHandler<Void> handler) {
		call(new Call(methodPrefix + "resume").setHandler(handler));
	}

	/*
			 Seeks to time position given in milliseconds.

					:param time_position: time position in milliseconds
					:type time_position: int
					:rtype: :class:`True` if successful, else :class:`False`
	 */
	public void seek(long time_position, ResponseHandler<Boolean> handler) {
		call(new Call<Boolean>(methodPrefix + "seek")
				.addParam("time_position", time_position)
				.setHandler(handler));
	}


	//stop playing
	public void stop(ResponseHandler<Void> handler) {
		call(new Call(methodPrefix + "stop").setHandler(handler));
	}

}
