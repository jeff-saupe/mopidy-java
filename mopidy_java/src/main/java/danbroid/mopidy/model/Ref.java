package danbroid.mopidy.model;

/**
 */
public class Ref extends Base {

	public static final String TYPE_ALBUM = "album";
	public static final String TYPE_ARTIST = "artist";
	public static final String TYPE_DIRECTORY = "directory";
	public static final String TYPE_PLAYLIST = "playlist";
	public static final String TYPE_TRACK = "track";


	private String type;
	private String name;
	private String uri;

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
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

	@Override
	public String toString() {
		return "Ref[" + type + ":" + name + ":<" + uri + ">]";
	}
}
