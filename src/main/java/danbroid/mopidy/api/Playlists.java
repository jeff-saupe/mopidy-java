package danbroid.mopidy.api;

import danbroid.mopidy.model.Playlist;
import danbroid.mopidy.model.Ref;
import lombok.extern.slf4j.Slf4j;

/*
 * See: https://github.com/mopidy/mopidy/blob/develop/mopidy/core/history.py
 * TODO: lookup, refresh, save
 */

@Slf4j
public class Playlists extends Api {
    protected Playlists(Api parent) {
        super(parent, "playlists.");
    }

    /**
     * Get the list of URI schemes that support playlists.
     *
     * @return Array of String
     */
    public Call<String[]> getUriSchemes() {
        return createCall("get_uri_schemes", String[].class);
    }

    /**
     * Get a list of the currently available playlists.
     * <p>
     * No information about the playlists' content is given.
     *
     * @return Array of {@link Ref} objects referring to the playlists
     */
    public Call<Ref[]> asList() {
        return createCall("as_list", Ref[].class);
    }

    /**
     * Get the items in a playlist specified by {@code uri}
     * <p>
     * If a playlist with the given {@code uri} doesn't exist, it returns Null
     *
     * @param uri
     * @return Array of {@link Ref} objects referring to the playlist's items or Null
     */
    public Call<Ref[]> getItems(String uri) {
        return createCall("get_items", Ref[].class)
                .addParam("uri", uri);
    }

    /**
     * Create a new playlist.
     * <p>
     * All new playlists must be created by calling this method, and not by creating new instances of {@link Playlist}.
     *
     * @param name       Name of the new playlist
     * @param uri_scheme URI scheme of playlist
     * @return {@link Playlist} or Null
     */
    public Call<Playlist> create(String name, String uri_scheme) {
        return createCall("create", Playlist.class)
                .addParam("name", name)
                .addParam("uri_scheme", uri_scheme);
    }

    /**
     * Delete playlist identified by the URI.
     *
     * @param uri URI of the playlist to delete
     * @return True if deleted, False otherwise
     */
    public Call<Boolean> delete(String uri) {
        return createCall("delete", Boolean.class)
                .addParam("uri", uri);
    }
}