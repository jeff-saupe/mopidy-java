package saupe.mopidy.model;

import lombok.Getter;
import lombok.Setter;

/*
 * A tracklist track.
 * Wraps a regular track and it's tracklist ID. It allows the same track to appear multiple times in the tracklist.
 *
 * See: https://github.com/mopidy/mopidy/blob/develop/mopidy/models/__init__.py#L262-L298
 */

@Getter
@Setter
public class TlTrack extends Base {
    // Tracklist ID
    private int tlid;
    // Track
    private Track track;

    @Override
    public String toString() {
        return String.format("TlTrack[%s, %s]", tlid, track);
    }
}