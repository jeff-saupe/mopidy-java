package saupe.mopidy.model;

import lombok.Getter;
import lombok.Setter;

/*
 * See: https://github.com/mopidy/mopidy/blob/develop/mopidy/models/__init__.py#L178-L259
 */

@Getter
@Setter
public class Track extends Base {
    // Track URI
    private String uri;
    // Track name
    private String name;
    // Track artists
    private Artist[] artists;
    // Track album
    private Album album;
    // Track composers
    private Artist[] composers;
    // Track performers
    private Artist[] performers;
    // Track genre
    private String genre;
    // Track number in album or Null if unknown
    private Integer trackNo;
    // Disc number in album ot Null if unknown
    private Integer discNo;
    // Track release date (YYYY or YYYY-MM-DD)
    private String date;
    // Track length in milliseconds or Null if there is no duration
    private Long length;
    // Bitrate in kBit/s
    private Integer bitrate;
    // Track comment
    private String comment;
    // MusicBrainz ID
    private String musicbrainzId;
    // Represents last modification time or Null if unknown
    private Long last_modified;

    @Override
    public String toString() {
        return String.format("Track[name=%s, uri=%s]", name, uri);
    }
}