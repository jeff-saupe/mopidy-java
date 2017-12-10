package danbroid.mopidy.api;

import com.google.gson.JsonArray;
import com.google.gson.reflect.TypeToken;

import java.util.Map;

import danbroid.mopidy.ResponseHandler;
import danbroid.mopidy.interfaces.Constants;
import danbroid.mopidy.model.Image;
import danbroid.mopidy.model.Ref;
import danbroid.mopidy.model.Track;

/**
 * Created by dan on 10/12/17.
 */
public class Library extends Api {

	private static final String METHOD_PREFIX = "core.library.";

	protected Library(Api parent) {
		super(parent);
	}

	public void browse(final String uri, ResponseHandler<Ref[]> handler) {
		call(new Call<>(METHOD_PREFIX + "browse", Ref[].class)
				.addParam(Constants.Key.URI, uri)
				.setHandler(handler));
	}

	public void lookup(String uris, ResponseHandler<Track[]> handler) {
		call(new Call<>(METHOD_PREFIX + "lookup", Track[].class)
				.addParam(Constants.Key.URIS, uris)
				.setHandler(handler));
	}

	public void getImages(final String uris[], ResponseHandler<Map<String, Image[]>> handler) {
		JsonArray uriArray = new JsonArray();
		for (String uri : uris) {
			uriArray.add(uri);
		}

		call(
				new Call(METHOD_PREFIX + "get_images", new TypeToken<Map<String, Image[]>>() {
				}).addParam(Constants.Key.URIS, uriArray)
						.setHandler(handler));
	}


}
