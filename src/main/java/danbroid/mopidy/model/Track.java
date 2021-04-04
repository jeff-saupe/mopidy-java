package danbroid.mopidy.model;

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
public class Track extends Base {
	private String name;

	// The track URI.
	private String uri;
	private Integer track_no;
	private Integer disc_no;

	//The track release date (YYYY or YYYY-MM-DD)
	private String date;
	private Artist artists[] = {};
	private Album album;
	private Artist composers[];
	private String genre;

	// track length in milliseconds
	private Long length;

	//bitrate in kbit/s
	private Integer bitrate;

	//track comment
	private String comment;

	private String musicbrainz_id;

	private Long last_modified;


	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Track[");
		sb.append("name=" + name);
		sb.append(" uri=" + uri);
		sb.append("]");
		return sb.toString();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public Integer getTrackNo() {
		return track_no;
	}

	public void setTrackNo(Integer track_no) {
		this.track_no = track_no;
	}

	public Integer getDiscNo() {
		return disc_no;
	}

	public void setDiscNo(Integer disc_no) {
		this.disc_no = disc_no;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public Artist[] getArtists() {
		return artists;
	}

	public void setArtists(Artist[] artists) {
		this.artists = artists;
	}

	public Album getAlbum() {
		return album;
	}

	public void setAlbum(Album album) {
		this.album = album;
	}

	public Artist[] getComposers() {
		return composers;
	}

	public void setComposers(Artist[] composers) {
		this.composers = composers;
	}

	public String getGenre() {
		return genre;
	}

	public void setGenre(String genre) {
		this.genre = genre;
	}

	public Long getLength() {
		return length;
	}

	public void setLength(Long length) {
		this.length = length;
	}

	public Integer getBitrate() {
		return bitrate;
	}

	public void setBitrate(Integer bitrate) {
		this.bitrate = bitrate;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getMusicbrainz_id() {
		return musicbrainz_id;
	}

	public void setMusicbrainz_id(String musicbrainz_id) {
		this.musicbrainz_id = musicbrainz_id;
	}

	public Long getLast_modified() {
		return last_modified;
	}

	public void setLast_modified(Long last_modified) {
		this.last_modified = last_modified;
	}
}
