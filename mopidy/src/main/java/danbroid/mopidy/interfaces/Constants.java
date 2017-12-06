package danbroid.mopidy.interfaces;

/**
 * Created by dan on 6/12/17.
 */
public interface Constants {

	interface Key {
		String TITLE = "title";
		String TIME_POSITION = "time_position";
		String TL_TRACK = "tl_track";
		String EVENT = "event";
		String MUTE = "mute";
		String VOLUME = "volume";
		String OLD_STATE = "old_state";
		String NEW_STATE = "new_state";
		String PLAYLIST = "playlist";
		String URI = "uri";
	}

	interface State {
		String PAUSED = "paused";
		String PLAYING = "playing";
		String STOPPED = "stopped";
	}
}
