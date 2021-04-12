package danbroid.mopidy.api;

import com.google.gson.reflect.TypeToken;

import java.util.Map;

import danbroid.mopidy.misc.JSONKeywords;
import danbroid.mopidy.model.Image;
import danbroid.mopidy.model.Ref;
import danbroid.mopidy.model.Track;
import lombok.extern.slf4j.Slf4j;

/*
 * See: https://github.com/mopidy/mopidy/blob/develop/mopidy/core/library.py
 * TODO: get_distinct, refresh, search
 */

@Slf4j
public class Library extends Api {
    protected Library(Api parent) {
        super(parent, "library.");
    }

    /**
     * Browse directories and tracks at the given {@code uri}.
     * <p>
     * {@code uri} is a string which represents some directory belonging to a backend. To get the initial root
     * directories for backends pass Null as the URI.
     *
     * @param uri URI to browse
     * @return Array of {@link Ref} of objects for the directories and tracks at the given {@code uri}.
     */
    public Call<Ref[]> browse(String uri) {
        return createCall("browse", Ref[].class).addParam(JSONKeywords.URI, uri);
    }

    /**
     * Lookup the images for the given URIs.
     * <p>
     * Unknown URIs or URIs the corresponding backend couldn't find anything for will simply return an empty list for
     * that URI.
     *
     * @param uris List of URIs to find images for
     * @return Map of {@link Image}
     */
    public Call<Map<String, Image[]>> getImages(String[] uris) {

        Call<Map<String, Image[]>> call = createCall("get_images");
        call.setResultType(new TypeToken<Map<String, Image[]>>() {
        });

        return call.addParam(JSONKeywords.URIS, getGson().toJsonTree(uris));
    }

    /**
     * Lookup the given URIs.
     * <p>
     * If the URI expands to multiple tracks, the returned list will contain them all.
     *
     * @param uris Track URIs
     * @return Array of {@link Track}
     */
    public Call<Track[]> lookup(String[] uris) {
        return createCall("lookup", Track[].class)
                .addParam(JSONKeywords.URIS, getGson().toJsonTree(uris));
    }
}