package danbroid.mopidy.model;

import lombok.Getter;
import lombok.Setter;

/*
 * See: https://github.com/mopidy/mopidy/blob/develop/mopidy/models/__init__.py
 */

@Getter
@Setter
public class Image extends Base {
	private String uri;
	private int width;
	private int height;

	@Override
	public String toString() {
		return "Image[" + uri + "]";
	}
}