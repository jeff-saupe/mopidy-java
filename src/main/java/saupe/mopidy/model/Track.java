package saupe.mopidy.model;

import lombok.Getter;
import lombok.Setter;

/*
 * See: https://github.com/mopidy/mopidy/blob/develop/mopidy/models/__init__.py
 */

@Getter
@Setter
public class Track extends Base {
    private String uri;
    private String name;
    private Artist[] artists;
    private Album album;
    private Artist[] composers;
    private Artist[] performers;
    private String genre;
    private Integer trackNo;
    private Integer discNo;
    private String date;            // Release date (YYYY or YYYY-MM-DD)
    private Long length;            // In milliseconds
    private Integer bitrate;        // In kBit/s
    private String comment;
    private String musicbrainzId;
    private Long lastModified;

    @Override
    public String toString() {
        return "Track[" + "name=" + name + " uri=" + uri + "]";
    }
}