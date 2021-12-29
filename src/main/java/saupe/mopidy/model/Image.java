package saupe.mopidy.model;

import lombok.Getter;
import lombok.Setter;

/*
 * See: https://github.com/mopidy/mopidy/blob/develop/mopidy/models/__init__.py#L93-L108
 */

@Getter
@Setter
public class Image extends Base {
    // URI of the image
    private String uri;
    // Optional width of image or Null
    private int width;
    // Optional height of image or Null
    private int height;

    @Override
    public String toString() {
        return String.format("Image[%s]", uri);
    }
}