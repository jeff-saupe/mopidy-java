package danbroid.mopidy.model;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by dan on 8/12/17.
 */

/*
    """




    #: A set of track composers. Read-only.
    composers = fields.Collection(type=Artist, container=frozenset)





    #: The disc number in the album. Read-only.
    disc_no = fields.Integer(min=0)

    #: The track release date. Read-only.
    date = fields.Date()

    #: The track length in milliseconds. Read-only.
    length = fields.Integer(min=0)

    #: The track's bitrate in kbit/s. Read-only.
    bitrate = fields.Integer(min=0)

    #: The track comment. Read-only.
    comment = fields.String()

    #: The MusicBrainz ID of the track. Read-only.
    musicbrainz_id = fields.Identifier()

    #: Integer representing when the track was last modified. Exact meaning
    #: depends on source of track. For local files this is the modification
    #: time in milliseconds since Unix epoch. For other backends it could be an
    #: equivalent timestamp or simply a version counter.
    last_modified = fields.Integer(min=0)
 */
@Getter
@Setter
public class Track extends Base {
	private String name;
	private String uri;
	private Integer trackNo;
	private Integer discNo;
	private String date;			// release date (YYYY or YYYY-MM-DD)
	private Artist[] artists = {};
	private Album album;
	private Artist[] composers;
	private String genre;
	private Long length;			// in milliseconds
	private Integer bitrate; 		// in kbit/s
	private String comment;
	private String musicbrainzId;
	private Long lastModified;

	@Override
	public String toString() {
		return "Track[" + "name=" + name + " uri=" + uri + "]";
	}
}