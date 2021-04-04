package danbroid.mopidy.model;

public class Album extends Base {
	private String[] images;
	private String name;
	private Artist[] artists;
	private String date;
	private String uri;
	private Integer num_tracks;
	private Integer num_discs;
	private String musicbrainz_id;

	public String[] getImages() {
		return images;
	}

	public void setImages(String[] images) {
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

	public Integer getNumTracks() {
		return num_tracks;
	}

	public void setNumTracks(Integer num_tracks) {
		this.num_tracks = num_tracks;
	}

	public Integer getNumDiscs() {
		return num_discs;
	}

	public void setNumDiscs(Integer num_discs) {
		this.num_discs = num_discs;
	}

	public String getMusicbrainzID() {
		return musicbrainz_id;
	}

	public void setMusicbrainzID(String musicbrainz_id) {
		this.musicbrainz_id = musicbrainz_id;
	}
}

