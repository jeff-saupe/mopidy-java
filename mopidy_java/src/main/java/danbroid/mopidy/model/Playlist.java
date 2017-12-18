package danbroid.mopidy.model;

/**
 * Created by dan on 18/12/17.
 * <p>
 * """
 * :param uri: playlist URI
 * :type uri: string
 * :param name: playlist name
 * :type name: string
 * :param tracks: playlist's tracks
 * :type tracks: list of :class:`Track` elements
 * :param last_modified:
 * playlist's modification time in milliseconds since Unix epoch
 * :type last_modified: int
 * """
 * <p>
 * #: The playlist URI. Read-only.
 * uri = fields.URI()
 * <p>
 * #: The playlist name. Read-only.
 * name = fields.String()
 * <p>
 * #: The playlist's tracks. Read-only.
 * tracks = fields.Collection(type=Track, container=tuple)
 * <p>
 * #: The playlist modification time in milliseconds since Unix epoch.
 * #: Read-only.
 * #:
 * #: Integer, or :class:`None` if unknown.
 * last_modified = fields.Integer(min=0)
 */
public class Playlist extends Base {
	private String uri;

	private String name;

	private Track tracks[];

	private long last_modified;

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Track[] getTracks() {
		return tracks;
	}

	public void setTracks(Track[] tracks) {
		this.tracks = tracks;
	}

	public long getLastModified() {
		return last_modified;
	}

	public void setLastModified(long last_modified) {
		this.last_modified = last_modified;
	}
}
