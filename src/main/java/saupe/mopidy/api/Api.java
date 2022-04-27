package saupe.mopidy.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import lombok.Getter;
import saupe.mopidy.RuntimeTypeAdapterFactory;
import saupe.mopidy.model.Album;
import saupe.mopidy.model.Artist;
import saupe.mopidy.model.Base;
import saupe.mopidy.model.Image;
import saupe.mopidy.model.Playlist;
import saupe.mopidy.model.Ref;
import saupe.mopidy.model.TlTrack;
import saupe.mopidy.model.Track;

import java.util.Arrays;

public class Api {
    @Getter
    protected final Gson gson = getGsonBuilder().create();
    protected final String prefix;

    private static final Class<? extends Base>[] subClasses = new Class[]{
        Album.class,
        Artist.class,
        Image.class,
        Playlist.class,
        Ref.class,
        TlTrack.class,
        Track.class
    };

    // ---- Parent constructor ----
    protected Api(String prefix) {
        this.prefix = prefix;
    }

    // ---- Child constructor ----
    protected Api(Api parent, String prefix) {
        this.prefix = parent.prefix + prefix;
    }

    public <T> Call<T> createCall(String method) {
        return new Call<T>(prefix + method);
    }

    public <T> Call<T> createCall(String method, Class<T> resultType) {
        Call<T> call = createCall(method);
        call.setResultType(resultType);
        return call;
    }

    private GsonBuilder getGsonBuilder() {
        RuntimeTypeAdapterFactory<Base> factory = RuntimeTypeAdapterFactory.of(Base.class, "__model__");
        Arrays.stream(subClasses).forEach(clz -> factory.registerSubtype(clz, clz.getSimpleName()));
        return new GsonBuilder().registerTypeAdapterFactory(factory);
    }
}