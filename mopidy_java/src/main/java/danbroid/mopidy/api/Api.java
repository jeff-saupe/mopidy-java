package danbroid.mopidy.api;

/**
 * Created by dan on 11/12/17.
 */
public class Api {

	private final Api parent;
	protected final String methodPrefix;

	protected Api(Api parent, String methodPrefix) {
		this.parent = parent;
		this.methodPrefix = parent.methodPrefix + methodPrefix;
	}

	protected Api(String methodPrefix) {
		this.parent = null;
		this.methodPrefix = methodPrefix;
	}

	protected void call(Call call) {
		parent.call(call);
	}

	protected String getMethodPrefix() {
		return methodPrefix;
	}
}
