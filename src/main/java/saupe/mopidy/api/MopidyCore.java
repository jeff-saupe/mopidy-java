package saupe.mopidy.api;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/*
 * See: https://github.com/mopidy/mopidy/blob/develop/mopidy/core/actor.py
 */

@Slf4j
@Getter
public class MopidyCore extends Api {
    private final Library   library     = new Library(this);
    private final History   history     = new History(this);
    private final Mixer     mixer       = new Mixer(this);
    private final Playback  playback    = new Playback(this);
    private final Playlists playlists   = new Playlists(this);
    private final Tracklist tracklist   = new Tracklist(this);

    public MopidyCore() {
        super("core.");
    }

    /**
     * Get list of URI schemes that can be handled.
     *
     * @return Array of String
     */
    public Call<String[]> getUriSchemes() {
        return createCall("get_uri_schemes", String[].class);
    }

    /**
     * Get version of the Mopidy core API.
     *
     * @return String
     */
    public Call<String> getVersion() {
        return createCall("get_version", String.class);
    }
}