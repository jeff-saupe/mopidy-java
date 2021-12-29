package saupe.mopidy.model;

import lombok.Getter;
import lombok.Setter;

/*
 * See: https://github.com/mopidy/mopidy/blob/develop/mopidy/models/__init__.py#L338-L361
 */

@Getter
@Setter
public class SearchResult extends Base {
    // Search result URI
    private String uri;
    // Matching tracks
    private Track[] tracks;
    // Matching artists
    private Artist[] artists;
    // Matching albums
    private Album[] albums;
}