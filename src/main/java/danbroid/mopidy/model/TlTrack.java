package danbroid.mopidy.model;

import lombok.Getter;
import lombok.Setter;

/*
 * A tracklist track. Wraps a regular track and it's tracklist ID. It allows the same track to appear multiple times
 * in the tracklist.
 *
 * See: https://github.com/mopidy/mopidy/blob/develop/mopidy/models/__init__.py
 */

@Getter
@Setter
public class TlTrack extends Base {
	private Track track;
	private int tlId;

	@Override
	public String toString() {
		return "TlTrack[" + tlId + "," + track + "]";
	}
}