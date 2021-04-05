package danbroid.mopidy.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Ref extends Base {
	public static final String TYPE_ALBUM 		= "album";
	public static final String TYPE_ARTIST 		= "artist";
	public static final String TYPE_DIRECTORY 	= "directory";
	public static final String TYPE_PLAYLIST 	= "playlist";
	public static final String TYPE_TRACK 		= "track";

	private String type;
	private String name;
	private String uri;

	public Ref(String type, String name, String uri) {
		super();

		this.type = type;
		this.name = name;
		this.uri = uri;
	}

	public static Ref directory(String name, String uri) {
		return new Ref(TYPE_DIRECTORY, name, uri);
	}

	public static Ref track(String name, String uri) {
		return new Ref(TYPE_TRACK, name, uri);
	}

	public static Ref playlist(String name, String uri) {
		return new Ref(TYPE_PLAYLIST, name, uri);
	}

	public static Ref album(String name, String uri) {
		return new Ref(TYPE_ALBUM, name, uri);
	}

	public static Ref artist(String name, String uri) {
		return new Ref(TYPE_ARTIST, name, uri);
	}

	@Override
	public String toString() {
		return "Ref[" + type + ":" + name + ":<" + uri + ">]";
	}
}