package danbroid.mopidy.api;

import com.google.gson.reflect.TypeToken;

import java.util.Map;

import danbroid.mopidy.interfaces.JSONConstants;
import danbroid.mopidy.model.Image;
import danbroid.mopidy.model.Ref;
import danbroid.mopidy.model.Track;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Library extends Api {
	private static final Logger log = LoggerFactory.getLogger(Library.class);

	protected Library(Api parent) {
		super(parent, "library.");
	}

	public Call<Ref[]> browse(final String uri) {
		return createCall("browse", Ref[].class).addParam(JSONConstants.URI, uri);
	}

	public Call<Track[]> lookup(String uris[]) {
		return createCall("lookup", Track[].class)
				.addParam(JSONConstants.URIS,getGson().toJsonTree(uris));
	}

	public Call<Map<String, Image[]>> getImages(final String uris[]) {

		Call<Map<String, Image[]>> call = createCall("get_images");
		call.setResultType(new TypeToken<Map<String, Image[]>>() {
		});

		return call.addParam(JSONConstants.URIS, getGson().toJsonTree(uris));
	}
}