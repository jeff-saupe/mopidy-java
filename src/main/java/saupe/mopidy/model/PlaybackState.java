package saupe.mopidy.model;

public enum PlaybackState {
    PLAYING, PAUSED, STOPPED;

    @Override
    public String toString() {
        return name().toLowerCase();
    }

    public static PlaybackState fromString(String name) {
        return PlaybackState.valueOf(name.toUpperCase());
    }
}