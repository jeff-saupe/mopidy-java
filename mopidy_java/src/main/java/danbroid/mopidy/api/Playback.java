package danbroid.mopidy.api;

import danbroid.mopidy.ResponseHandler;
import danbroid.mopidy.model.TlTrack;
import danbroid.mopidy.model.Track;

/**
 * Created by dan on 13/12/17.
 */
public class Playback extends Api {

	protected Playback(Api parent) {
		super(parent, "playback.");
	}

	public void getCurrentTlTrack(ResponseHandler<TlTrack> handler) {
		call(new Call(methodPrefix + "get_current_tl_track", TlTrack.class).setHandler(handler));
	}

	/**
	 * @param handler receives the current tlid or null
	 */
	public void getCurrentTLID(ResponseHandler<Integer> handler) {
		call(new Call(methodPrefix + "get_current_tlid", Integer.class).setHandler(handler));
	}


	/**
	 * @param handler receives the current Track or null
	 */
	public void getCurrentTrack(ResponseHandler<Track> handler) {
		call(new Call(methodPrefix + "get_current_track", Track.class).setHandler(handler));
	}

	public void getCurrentState(){

	}
}
