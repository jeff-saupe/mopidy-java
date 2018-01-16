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
public class Library extends Api {


	protected Library(Api parent) {
		super(parent, "library.");
	}

	public Call<Ref[]> browse(final String uri) {
		return createCall("browse", Ref[].class).addParam(Constants.Key.URI, uri);
	}


	public Call<Track[]> lookup(String uris[]) {
		return createCall("lookup", Track[].class)
				.addParam(Constants.Key.URIS,getGson().toJsonTree(uris));
	}

	public Call<Map<String, Image[]>> getImages(final String uris[]) {

		Call<Map<String, Image[]>> call = createCall("get_images");
		call.setResultType(new TypeToken<Map<String, Image[]>>() {
		});

		return call.addParam(Constants.Key.URIS, getGson().toJsonTree(uris));
	}


}
