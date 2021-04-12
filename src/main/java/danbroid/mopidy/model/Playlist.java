package danbroid.mopidy.model;

import lombok.Getter;
import lombok.Setter;

/**
 * See: https://github.com/mopidy/mopidy/blob/develop/mopidy/models/__init__.py
 */

@Getter
@Setter
public class Playlist extends Base {
	private String uri;
	private String name;
	private Track[] tracks;
	private long lastModified;
}