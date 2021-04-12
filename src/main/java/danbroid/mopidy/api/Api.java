package danbroid.mopidy.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import danbroid.mopidy.MopidyClient;
import danbroid.mopidy.RuntimeTypeAdapterFactory;
import danbroid.mopidy.model.Album;
import danbroid.mopidy.model.Artist;
import danbroid.mopidy.model.Base;
import danbroid.mopidy.model.Image;
import danbroid.mopidy.model.Playlist;
import danbroid.mopidy.model.Ref;
import danbroid.mopidy.model.TlTrack;
import danbroid.mopidy.model.Track;

public class Api {
	private final Api parent;

	protected final MopidyClient client;
	protected final String methodPrefix;
	private Gson gson;

	// Parent constructor
	protected Api(MopidyClient client, String methodPrefix) {
		this.parent = null;
		this.client = client;
		this.methodPrefix = methodPrefix;
	}

	// Child constructor
	protected Api(Api parent, String methodPrefix) {
		this.parent = parent;
		this.client = parent.client;
		this.methodPrefix = parent.methodPrefix + methodPrefix;
	}

	public <T> Call<T> createCall(String method) {
		return new Call<T>(methodPrefix + method, client);
	}

	public <T> Call<T> createCall(String method, Class<T> resultType) {
		Call<T> call = createCall(method);
		call.setResultType(resultType);
		return call;
	}

	public Gson getGson() {
		return gson == null ? gson = getGsonBuilder().create() : gson;
	}

	// TODO: Get rid of this
	public GsonBuilder getGsonBuilder() {
		RuntimeTypeAdapterFactory<Base> runtimeTypeAdapterFactory = RuntimeTypeAdapterFactory
				.of(Base.class, "__model__");

		for (Class<Base> clz : new Class[]{Album.class, Artist.class, Image.class, Ref.class, Track.class,
				TlTrack.class, Playlist.class}) {
			runtimeTypeAdapterFactory.registerSubtype(clz, clz.getSimpleName());
		}
			/*	.registerSubtype(Image.class, "Image")
				.registerSubtype(Ref.class, "Ref");
*/
		return new GsonBuilder()
				.registerTypeAdapterFactory(runtimeTypeAdapterFactory);
	}
}