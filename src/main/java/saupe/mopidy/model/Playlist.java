package saupe.mopidy.model;

import lombok.Getter;
import lombok.Setter;

/*
 * See: https://github.com/mopidy/mopidy/blob/develop/mopidy/models/__init__.py#L301-L335
 */

@Getter
@Setter
public class Playlist extends Base {
    // Playlist URI
    private String uri;
    // Playlist name
    private String name;
    // Playlist's tracks
    private Track[] tracks;
    // Last playlist's modification time in milliseconds since Unix epoch or Null if unknown
    private long last_modified;
}