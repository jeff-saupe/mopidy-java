package danbroid.mopidy.api;

/**
 * Created by dan on 10/12/17.
 */
public class Core {
	private static final String METHOD_PREFIX = "core.";

	public Call<String> getVersion() {
		return new Call<>(METHOD_PREFIX+"get_version", String.class);
	}

	public Call<String[]> getUriScemes(){
		return new Call<>(METHOD_PREFIX+"get_uri_schemes",String[].class);
	}

	private Library library = new Library();

	public Library getLibrary() {
		return library;
	}
}
