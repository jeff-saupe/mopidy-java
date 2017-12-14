package danbroid.mopidy.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import danbroid.mopidy.RuntimeTypeAdapterFactory;
import danbroid.mopidy.model.Album;
import danbroid.mopidy.model.Artist;
import danbroid.mopidy.model.Base;
import danbroid.mopidy.model.Image;
import danbroid.mopidy.model.Ref;
import danbroid.mopidy.model.TlTrack;
import danbroid.mopidy.model.Track;

/**
 * Created by dan on 11/12/17.
 */
public class Api {

	private final Api parent;
	protected final String methodPrefix;
	private Gson gson;

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

	public Gson getGson() {
		return gson == null ? gson = getGsonBuilder().create() : gson;
	}

	public GsonBuilder getGsonBuilder() {
		RuntimeTypeAdapterFactory<Base> runtimeTypeAdapterFactory = RuntimeTypeAdapterFactory
				.of(Base.class, "__model__");

		for (Class<Base> clz : new Class[]{Album.class, Artist.class, Image.class, Ref.class, Track.class, TlTrack.class}) {
			runtimeTypeAdapterFactory.registerSubtype(clz, clz.getSimpleName());
		}
			/*	.registerSubtype(Image.class, "Image")
				.registerSubtype(Ref.class, "Ref");
*/
		return new GsonBuilder()
				.registerTypeAdapterFactory(runtimeTypeAdapterFactory);
	}

}
