package danbroid.mopidy.api;

import com.google.gson.JsonArray;
import com.google.gson.reflect.TypeToken;

import java.util.Map;

import danbroid.mopidy.interfaces.Constants;
import danbroid.mopidy.model.Image;
import danbroid.mopidy.model.Ref;
import danbroid.mopidy.model.Track;

/**
 * Created by dan on 10/12/17.
 */
public class Library {

	private static final String METHOD_PREFIX = "core.library.";

	public Call<Ref[]> browse(final String uri) {
		return new Call<>(METHOD_PREFIX + "browse", Ref[].class)
				.addParam(Constants.Key.URI, uri);
	}

	public Call<Track[]> lookup(String uris) {
		return new Call<>(METHOD_PREFIX + "lookup", Track[].class)
				.addParam(Constants.Key.URIS, uris);
	}

	public Call<Map<String, Image[]>> getImages(final String uris[]) {
		JsonArray uriArray = new JsonArray();
		for (String uri : uris) {
			uriArray.add(uri);
		}
		return new Call<>(METHOD_PREFIX + "get_images", new TypeToken<Map<String, Image[]>>() {
		}).addParam(Constants.Key.URIS, uriArray);

	}
}
