package saupe.mopidy.model;

import lombok.Getter;
import lombok.Setter;

/*
 * See: https://github.com/mopidy/mopidy/blob/develop/mopidy/models/__init__.py#L137-L175
 */

@Getter
@Setter
public class Album extends Base {
    // Album URI
    private String uri;
    // Album name
    private String name;
    // Album artists
    private Artist[] artists;
    // Number of tracks in album or Null if unknown
    private Integer num_tracks;
    // Number of discs in album or Null if unknown
    private Integer num_discs;
    // Album release date (YYYY or YYYY-MM-DD)
    private String date;
    // MusicBrainz ID
    private String musicbrainz_id;
}