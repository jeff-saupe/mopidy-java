package danbroid.mopidy.api;

/**
 * Created by dan on 11/12/17.
 */
public class Api {

	private Api parent;

	protected Api(Api parent) {
		this.parent = parent;
	}

	protected void call(Call call) {
		parent.call(call);
	}
}
