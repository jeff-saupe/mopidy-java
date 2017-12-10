package danbroid.mopidy.api;

import danbroid.mopidy.ResponseHandler;

/**
 * Created by dan on 10/12/17.
 */
public class Core extends Api {
	private static final String METHOD_PREFIX = "core.";

	public Core(Api parent) {
		super(parent);
	}

	public void getVersion(ResponseHandler<String> handler) {
		call(new Call(METHOD_PREFIX + "get_version", String.class)
				.setHandler(handler));
	}


	public void getUriScemes(ResponseHandler<String[]> handler) {
		call(new Call<>(METHOD_PREFIX + "get_uri_schemes", String[].class)
				.setHandler(handler));
	}

	private Library library = new Library(this);

	public Library getLibrary() {
		return library;
	}

}
