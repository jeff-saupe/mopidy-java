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
		String METHOD = "method";
		String JSONRPC = "jsonrpc";
		String PARAMS = "params";
		String ID = "id";
		String RESULT = "result";
		String ERROR = "error";
		String CODE = "code";
		String MESSAGE = "message";
		String DATA = "data";
		String URIS = "uris";
	}

	interface State {
		String PAUSED = "paused";
		String PLAYING = "playing";
		String STOPPED = "stopped";
	}

	interface Method {
		String GET_VERSION = "core.get_version";
		String BROWSE = "core.library.browse";
		String GET_IMAGES = "core.library.get_images";
		String LOOKUP = "core.library.lookup";
		String GET_URI_SCHEMES = "core.get_uri_schemes";
	}

}
