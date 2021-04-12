package danbroid.mopidy.model;

import lombok.Getter;
import lombok.Setter;

/*
 * See: https://github.com/mopidy/mopidy/blob/develop/mopidy/models/__init__.py
 */

@Getter
@Setter
public class SearchResult extends Base {
    private String uri;
    private Track[] tracks;
    private Artist[] artists;
    private Album[] albums;
}