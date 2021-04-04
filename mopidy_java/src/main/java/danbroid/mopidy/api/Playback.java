package danbroid.mopidy.api;

import com.google.gson.JsonElement;

import danbroid.mopidy.ResponseHandler;
import danbroid.mopidy.interfaces.CallContext;
import danbroid.mopidy.interfaces.PlaybackState;
import danbroid.mopidy.model.TlTrack;
import danbroid.mopidy.model.Track;
import danbroid.mopidy.transport.WebSocketTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by dan on 13/12/17.
 * Finished implementation from playback.py
 */
public class Playback extends Api {
	private static final Logger log = LoggerFactory.getLogger(Playback.class);

	protected Playback(Api parent) {
		super(parent, "playback.");
	}

	public Call<TlTrack> getCurrentTlTrack() {
		return createCall("get_current_tl_track", TlTrack.class);
	}

	/**
	 * receives the current tlid or null
	 */
	public Call<Integer> getCurrentTLID() {
		return createCall("get_current_tlid", Integer.class);
	}


	/**
	 * receives the current Track or null
	 */
	public Call<Track> getCurrentTrack() {
		return createCall("get_current_track", Track.class);
	}

	public Call<PlaybackState> getState(ResponseHandler<PlaybackState> handler) {
		return new Call<PlaybackState>(methodPrefix + "get_state", getConnection()) {
			@Override
			protected PlaybackState parseResult(CallContext callContext, JsonElement response) {
				return PlaybackState.valueOf(response.getAsString().toUpperCase());
			}
		};
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
	public Call<Void> setState(PlaybackState state) {
		return createCall("set_state", Void.class)
				.addParam("new_state", state.toString());

	}

	public Call<String> getStreamTitle() {
		return createCall("get_stream_title", String.class);
	}

	public Call<Long> getTimePosition() {
		return createCall("get_time_position", Long.class);
	}

	/*
	Change to the next track.

The current playback state will be kept. If it was playing, playing
will continue. If it was paused, it will still be paused, etc.
	 */
	public Call<Void> next() {
		return createCall("next", Void.class);
	}

	/**
	 * Pause current playback
	 */
	public Call<Void> pause() {
		return createCall("pause", Void.class);
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
	public Call<Void> play(Integer tlid, TlTrack tlTrack) {
		JsonElement jsonTrack = null;
		if (tlTrack != null)
			jsonTrack = getGson().toJsonTree(tlTrack);

		return createCall("play", Void.class)
				.addParam("tlid", tlid)
				.addParam("tl_track", jsonTrack);
	}


	/*
	Change to the previous track.

        The current playback state will be kept. If it was playing, playing
        will continue. If it was paused, it will still be paused, etc.

	 */
	public Call<Void> previous() {
		return createCall("previous", Void.class);
	}


	//If paused, resume playing the current track
	public Call<Void> resume() {
		return createCall("resume", Void.class);
	}

	/*
			 Seeks to time position given in milliseconds.

					:param time_position: time position in milliseconds
					:type time_position: int
					:rtype: :class:`True` if successful, else :class:`False`
	 */
	public Call<Boolean> seek(long time_position) {
		return createCall("seek", Boolean.class)
				.addParam("time_position", time_position);
	}


	//stop playing
	public Call<Void> stop() {
		return createCall("stop", Void.class);
	}

}
