package danbroid.mopidy.api;

import com.google.gson.reflect.TypeToken;

import java.util.Map;

import danbroid.mopidy.interfaces.JsonKeywords;
import danbroid.mopidy.model.Image;
import danbroid.mopidy.model.Ref;
import danbroid.mopidy.model.Track;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Library extends Api {
	protected Library(Api parent) {
		super(parent, "library.");
	}

	public Call<Ref[]> browse(final String uri) {
		return createCall("browse", Ref[].class).addParam(JsonKeywords.URI, uri);
	}

	public Call<Track[]> lookup(String uris[]) {
		return createCall("lookup", Track[].class)
				.addParam(JsonKeywords.URIS,getGson().toJsonTree(uris));
	}

	public Call<Map<String, Image[]>> getImages(final String uris[]) {

		Call<Map<String, Image[]>> call = createCall("get_images");
		call.setResultType(new TypeToken<Map<String, Image[]>>() {
		});

		return call.addParam(JsonKeywords.URIS, getGson().toJsonTree(uris));
	}
}