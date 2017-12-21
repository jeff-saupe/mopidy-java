package danbroid.mopidy.lastfm;

import com.google.gson.annotations.SerializedName;

/**
 * Created by dan on 21/12/17.
 */
public class Album {


	public String name;
	public String album;
	public String mdid;

	public enum ImageSize {
		SMALL, MEDIUM, LARGE, EXTRA_LARGE, MEGA, DEFAULT;
	}

	public static class Image {
		@SerializedName("#text")
		public String url;

		public String size;
	}

	public Image[] image;

	public String getImage(String size) {
		if (image == null) return null;
		for (Image i : image) {
			if (size.equals(i.size)) return i.url;
		}
		return null;
	}

	public String getImage(ImageSize size) {
		return getImage(size == ImageSize.DEFAULT ? "" : size.name().toLowerCase());
	}


	public static class Tag {
		public String name;
		public String url;
	}

	public static class TagInfo {
		public Tag tag[];
	}

	public TagInfo tags;

	public static class Wiki {
		public String published;
		public String summary;
		public String content;

	}

	public Wiki wiki;

	/*
		"tracks": {
			"track": [
				{
					"name": "Highway to Hell",
					"url": "https://www.last.fm/music/AC%2FDC/_/Highway+to+Hell",
					"duration": "206",
					"@attr": {
						"rank": "1"
					},
					"streamable": {
						"#text": "0",
						"fulltrack": "0"
					},
					"artist": {
						"name": "AC/DC",
						"mbid": "66c662b6-6e2f-4930-8610-912e24c63ed1",
						"url": "https://www.last.fm/music/AC%2FDC"
					}
				},
				{
	 */

	public static class Track {
		public String name;
		public String url;
		public String duration;
		public Artist artist;

	}

	public static class TracksInfo {
		public Track[] track;
	}

	public TracksInfo tracks;
}
