package danbroid.mopidy.interfaces;

/**
 * Created by dan on 13/12/17.
 */
public enum PlaybackState {
	PAUSED, PLAYING, STOPPED;

	@Override
	public String toString() {
		return name().toLowerCase();
	}

	public static PlaybackState fromString(String name) {
		return PlaybackState.valueOf(name.toUpperCase());
	}

}
