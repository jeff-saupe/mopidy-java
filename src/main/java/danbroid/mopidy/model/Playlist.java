package danbroid.mopidy.model;

import lombok.Getter;
import lombok.Setter;

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
@Getter
@Setter
public class Playlist extends Base {
	private String uri;
	private String name;
	private Track[] tracks;
	private long lastModified;
}