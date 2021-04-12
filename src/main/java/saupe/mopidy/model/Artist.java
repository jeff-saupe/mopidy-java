package saupe.mopidy.model;

import lombok.Getter;

/*
 * See: https://github.com/mopidy/mopidy/blob/develop/mopidy/models/__init__.py
 */

@Getter
public class Artist extends Base {
	private String uri;
	private String name;
	private String sortname;
	private String musicbrainz_id;
}