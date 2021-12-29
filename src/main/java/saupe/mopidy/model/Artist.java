package saupe.mopidy.model;

import lombok.Getter;

/*
 * See: https://github.com/mopidy/mopidy/blob/develop/mopidy/models/__init__.py#L111-L134
 */

@Getter
public class Artist extends Base {
    // Artist URI
    private String uri;
    // Artist name
    private String name;
    // Artist name for sorting
    private String sortname;
    // MusicBrainz ID
    private String musicbrainz_id;
}