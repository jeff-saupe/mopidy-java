package danbroid.mopidy.model;

import lombok.Getter;
import lombok.Setter;

/**
 * Track-List Track.
 * (So you can have multiple tracks in a track-list)
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