package danbroid.mopidy.model;

/**
 * Created by dan on 8/12/17.
 */
public abstract class Base {

	private String __model__ = getClass().getSimpleName();

	//extra field attaching non-json derived data
	private transient Object extra;

	public Object getExtra() {
		return extra;
	}

	public void setExtra(Object extra) {
		this.extra = extra;
	}
}
