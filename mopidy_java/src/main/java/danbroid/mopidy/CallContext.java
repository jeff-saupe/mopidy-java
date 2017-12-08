package danbroid.mopidy;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import danbroid.mopidy.model.Album;
import danbroid.mopidy.model.Artist;
import danbroid.mopidy.model.Base;
import danbroid.mopidy.model.Image;
import danbroid.mopidy.model.Ref;
import danbroid.mopidy.model.Track;

/**
 * Created by dan on 8/12/17.
 */
public class CallContext {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CallContext.class);
	private Gson gson;

	public CallContext() {
		gson = getGsonBuilder().create();
	}

	public Gson getGson() {
		return gson;
	}

	public GsonBuilder getGsonBuilder() {
		RuntimeTypeAdapterFactory<Base> runtimeTypeAdapterFactory = RuntimeTypeAdapterFactory
				.of(Base.class, "__model__");

		for (Class<Base> clz : new Class[]{Album.class, Artist.class, Image.class, Ref.class, Track.class}) {
			runtimeTypeAdapterFactory.registerSubtype(clz, clz.getSimpleName());
		}
			/*	.registerSubtype(Image.class, "Image")
				.registerSubtype(Ref.class, "Ref");
*/
		return new GsonBuilder()
				.registerTypeAdapterFactory(runtimeTypeAdapterFactory);
	}
}
