package danbroid.mopidy.model;

/**
 * Created by dan on 8/12/17.
 */
public class Album extends Base {
	private Image images[];
	private String name;
	private Artist artists[];
	private String date;
	private String uri;
	private Integer num_tracks;
	private Integer num_discs;
	private String musicbrainz_id;

	public Image[] getImages() {
		return images;
	}

	public void setImages(Image[] images) {
		this.images = images;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Artist[] getArtists() {
		return artists;
	}

	public void setArtists(Artist[] artists) {
		this.artists = artists;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public Integer getNum_tracks() {
		return num_tracks;
	}

	public void setNum_tracks(Integer num_tracks) {
		this.num_tracks = num_tracks;
	}

	public Integer getNum_discs() {
		return num_discs;
	}

	public void setNum_discs(Integer num_discs) {
		this.num_discs = num_discs;
	}

	public String getMusicbrainzID() {
		return musicbrainz_id;
	}

	public void setMusicbrainzID(String musicbrainz_id) {
		this.musicbrainz_id = musicbrainz_id;
	}
}

