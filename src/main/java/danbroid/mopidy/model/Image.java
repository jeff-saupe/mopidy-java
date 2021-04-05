package danbroid.mopidy.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Image extends Base {
	private String uri;

	@Override
	public String toString() {
		return "Image[" + uri + "]";
	}
}