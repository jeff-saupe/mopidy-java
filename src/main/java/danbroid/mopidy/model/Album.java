package danbroid.mopidy.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Album extends Base {
	private String[] images;
	private String name;
	private Artist[] artists;
	private String date;
	private String uri;
	private Integer numTracks;
	private Integer numDiscs;
	private String musicbrainzId;
}