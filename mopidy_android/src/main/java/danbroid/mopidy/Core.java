package danbroid.mopidy;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Map;

import danbroid.mopidy.interfaces.Constants;
import danbroid.mopidy.model.Base;
import danbroid.mopidy.model.Image;
import danbroid.mopidy.model.Ref;

/**
 * Created by dan on 8/12/17.
 */
public class Core {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Core.class);


	public Call<String> getVersion() {
		return new Call<String>(Constants.Method.GET_VERSION) {
			@Override
			public String parseResult(JsonElement response) {
				return response.getAsString();
			}
		};
	}

	public static class Library {
		public static Call<Ref[]> browse(final String uri) {
			return new Call<Ref[]>(Constants.Method.BROWSE) {
				@Override
				public JsonObject toJSON() {
					JsonObject request = super.toJSON();
					JsonObject params = new JsonObject();
					params.addProperty("uri", uri);
					request.add("params", params);

					return request;
				}

				@Override
				public Ref[] parseResult(JsonElement result) {
					return getContext().getGson().fromJson(result, Ref[].class);
				}
			};
		}

		public static Call<Base[]> lookup(final String uri, final String uris) {
			return new Call<Base[]>(Constants.Method.LOOKUP) {
				@Override
				public JsonObject toJSON() {
					JsonObject request = super.toJSON();
					JsonObject params = new JsonObject();
					params.addProperty("uri", uri);
					params.addProperty("uris", uris);
					request.add("params", params);
					return request;
				}

				@Override
				protected Base[] parseResult(JsonElement response) {
					return getContext().getGson().fromJson(response, Base[].class);
				}
			};
		}

		;
	}


	public Call<Map<String, Image[]>> getImages(final String uris[]) {

		return new Call<Map<String, Image[]>>(Constants.Method.GET_IMAGES) {
			@Override
			public JsonObject toJSON() {
				JsonObject request = super.toJSON();
				JsonObject params = new JsonObject();
				JsonArray a = new JsonArray();
				for (String uri : uris) {
					a.add(uri);
				}
				params.add("uris", a);
				request.add("params", params);
				return request;
			}

			@Override
			protected Map<String, Image[]> parseResult(JsonElement response) {
				Type type = new TypeToken<Map<String, Image[]>>() {
				}.getType();
				return getContext().getGson().fromJson(response, type);
			}
		};
	}

}
