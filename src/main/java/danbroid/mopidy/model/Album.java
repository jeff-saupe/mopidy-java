package danbroid.mopidy.model;

import lombok.Getter;
import lombok.Setter;

/**
 * See: https://github.com/mopidy/mopidy/blob/develop/mopidy/models/__init__.py
 */

@Getter
@Setter
public class Album extends Base {
	private String uri;
	private String name;
	private Artist[] artists;
	private Integer numTracks;
	private Integer numDiscs;
	private String date;			// Release date (YYYY or YYYY-MM-DD)
	private String musicbrainzId;
}