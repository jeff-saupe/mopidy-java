package danbroid.mopidy.model;

/**
 * Created by dan on 8/12/17.
 */
public class Image extends Base {
	private String uri;

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	@Override
	public String toString() {
		return "Image[" + uri + "]";
	}
}
