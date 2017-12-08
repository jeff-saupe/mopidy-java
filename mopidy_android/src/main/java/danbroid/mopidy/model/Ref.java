package danbroid.mopidy.model;

/**
 */
public class Ref extends Base {

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
