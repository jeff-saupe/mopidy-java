package danbroid.mopidy.model;

/**
 * Created by dan on 13/12/17.
 * Track-List Track.
 * (So you can have multiple tracks in a track-list)
 */
public class TlTrack extends Base {

	private Track track;
	//track list id
	private int tlid;

	public int getTlid() {
		return tlid;
	}

	public void setTlid(int tlid) {
		this.tlid = tlid;
	}

	public Track getTrack() {
		return track;
	}

	public void setTrack(Track track) {
		this.track = track;
	}

	@Override
	public String toString() {
		return "TlTrack[" + tlid + "," + track + "]";
	}
}
