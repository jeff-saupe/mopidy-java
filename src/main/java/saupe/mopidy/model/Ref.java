package saupe.mopidy.model;

import lombok.Getter;
import lombok.Setter;

/*
 * Model to represent URI references with a human friendly name and type attached.
 * This is intended to have a lightweight object "free" of metadata that can be passed around,
 * instead of using full-blown models.
 *
 * See: https://github.com/mopidy/mopidy/blob/develop/mopidy/models/__init__.py#L21-L90
 */

@Getter
@Setter
public class Ref extends Base {
    private static final String TYPE_ALBUM = "album";
    private static final String TYPE_ARTIST = "artist";
    private static final String TYPE_PLAYLIST = "playlist";
    private static final String TYPE_DIRECTORY = "directory";
    private static final String TYPE_TRACK = "track";

    // Object URI
    private String uri;
    // Object name
    private String name;
    // Object type
    private String type;

    private Ref(String uri, String name, String type) {
        super();

        this.uri = uri;
        this.name = name;
        this.type = type;
    }

    public static Ref album(String uri, String name) {
        return new Ref(uri, name, TYPE_ALBUM);
    }

    public static Ref artist(String uri, String name) {
        return new Ref(uri, name, TYPE_ARTIST);
    }

    public static Ref directory(String uri, String name) {
        return new Ref(uri, name, TYPE_DIRECTORY);
    }

    public static Ref playlist(String uri, String name) {
        return new Ref(uri, name, TYPE_PLAYLIST);
    }

    public static Ref track(String name, String uri) {
        return new Ref(uri, name, TYPE_TRACK);
    }

    @Override
    public String toString() {
        return String.format("Ref[%s:%s:<%s>]", type, name, uri);
    }
}