package danbroid.mopidy.api;

import danbroid.mopidy.model.Playlist;
import danbroid.mopidy.model.Ref;
import danbroid.mopidy.transport.WebSocketTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by dan on 18/12/17.
 * Unfinished
 */
public class Playlists extends Api {
	private static final Logger log = LoggerFactory.getLogger(Playlists.class);

	protected Playlists(Api parent) {
		super(parent, "playlists.");
	}

	/*
	Get the items in a playlist specified by ``uri``.

  Returns a list of :class:`~mopidy.models.Ref` objects referring to the
  playlist's items.

  If a playlist with the given ``uri`` doesn't exist, it returns
  :class:`None`.
	 */
	public Call<Ref[]> asList() {
		return createCall("as_list", Ref[].class);
	}

	/*
	        Get the items in a playlist specified by ``uri``.

        Returns a list of :class:`~mopidy.models.Ref` objects referring to the
        playlist's items.

        If a playlist with the given ``uri`` doesn't exist, it returns
        :class:`None`.

        :rtype: list of :class:`mopidy.models.Ref`, or :class:`None`
	 *

	public void getItems(String uri, ResponseHandler<Ref[]> handler) {
		call(new Call<Ref[]>(methodPrefix + "get_items")
				.addParam("uri", uri)
				.setResultType(Ref[].class)
				.setHandler(handler));
	}

	//Get the list of URI schemes that support playlists.
	public void get_uri_schemes(ResponseHandler<String[]> handler) {
		call(new Call<String[]>(methodPrefix + "get_uri_schemes")
				.setResultType(String[].class)
				.setHandler(handler));
	}


	/*
	Get the available playlists.

        :rtype: list of :class:`mopidy.models.Playlist`

        .. versionchanged:: 1.0
            If you call the method with ``include_tracks=False``, the
            :attr:`~mopidy.models.Playlist.last_modified` field of the returned
            playlists is no longer set.
	 */
	public Call<Playlist[]> getPlaylists(boolean include_tracks) {
		return createCall("get_playlists", Playlist[].class)
				.addParam("include_tracks", include_tracks);
	}

	/*
	        Get the items in a playlist specified by ``uri``.

        Returns a list of :class:`~mopidy.models.Ref` objects referring to the
        playlist's items.

        If a playlist with the given ``uri`` doesn't exist, it returns
        :class:`None`.

        :rtype: list of :class:`mopidy.models.Ref`, or :class:`None`
	 */
	public Call<Ref[]> getItems(String uri) {
		return createCall("get_items", Ref[].class)
				.addParam("uri", uri);
	}

	/*
	     Create a new playlist.

        If ``uri_scheme`` matches an URI scheme handled by a current backend,
        that backend is asked to create the playlist. If ``uri_scheme`` is
        :class:`None` or doesn't match a current backend, the first backend is
        asked to create the playlist.

        All new playlists must be created by calling this method, and **not**
        by creating new instances of :class:`mopidy.models.Playlist`.

        :param name: name of the new playlist
        :type name: string
        :param uri_scheme: use the backend matching the URI scheme
        :type uri_scheme: string
        :rtype: :class:`mopidy.models.Playlist` or :class:`None`
	 */
	public Call<Playlist> create(String name, String uri_scheme) {
		return createCall("create", Playlist.class)
				.addParam("name", name)
				.addParam("uri_scheme", uri_scheme);
	}

	/*
	 Delete playlist identified by the URI.

        If the URI doesn't match the URI schemes handled by the current
        backends, nothing happens.

        :param uri: URI of the playlist to delete
        :type uri: string
	 */
	public Call<Void> delete(String uri) {
		return createCall("delete", Void.class)
				.addParam("uri", uri);
	}
}
/*




    def filter(self, criteria=None, **kwargs):
        """
        Filter playlists by the given criterias.

        Examples::

            # Returns track with name 'a'
            filter({'name': 'a'})

            # Returns track with URI 'xyz'
            filter({'uri': 'xyz'})

            # Returns track with name 'a' and URI 'xyz'
            filter({'name': 'a', 'uri': 'xyz'})

        :param criteria: one or more criteria to match by
        :type criteria: dict
        :rtype: list of :class:`mopidy.models.Playlist`

        .. deprecated:: 1.0
            Use :meth:`as_list` and filter yourself.
        """
        deprecation.warn('core.playlists.filter')

        criteria = criteria or kwargs
        validation.check_query(
            criteria, validation.PLAYLIST_FIELDS, list_values=False)

        matches = self.playlists  # TODO: stop using self playlists
        for (key, value) in criteria.iteritems():
            matches = filter(lambda p: getattr(p, key) == value, matches)
        return matches

    def lookup(self, uri):
        """
        Lookup playlist with given URI in both the set of playlists and in any
        other playlist sources. Returns :class:`None` if not found.

        :param uri: playlist URI
        :type uri: string
        :rtype: :class:`mopidy.models.Playlist` or :class:`None`
        """
        uri_scheme = urllib.parse.urlparse(uri).scheme
        backend = self.backends.with_playlists.get(uri_scheme, None)
        if not backend:
            return None

        with _backend_error_handling(backend):
            playlist = backend.playlists.lookup(uri).get()
            playlist is None or validation.check_instance(playlist, Playlist)
            return playlist

        return None

    # TODO: there is an inconsistency between library.refresh(uri) and this
    # call, not sure how to sort this out.
    def refresh(self, uri_scheme=None):
        """
        Refresh the playlists in :attr:`playlists`.

        If ``uri_scheme`` is :class:`None`, all backends are asked to refresh.
        If ``uri_scheme`` is an URI scheme handled by a backend, only that
        backend is asked to refresh. If ``uri_scheme`` doesn't match any
        current backend, nothing happens.

        :param uri_scheme: limit to the backend matching the URI scheme
        :type uri_scheme: string
        """
        # TODO: check: uri_scheme is None or uri_scheme?

        futures = {}
        backends = {}
        playlists_loaded = False

        for backend_scheme, backend in self.backends.with_playlists.items():
            backends.setdefault(backend, set()).add(backend_scheme)

        for backend, backend_schemes in backends.items():
            if uri_scheme is None or uri_scheme in backend_schemes:
                futures[backend] = backend.playlists.refresh()

        for backend, future in futures.items():
            with _backend_error_handling(backend):
                future.get()
                playlists_loaded = True

        if playlists_loaded:
            listener.CoreListener.send('playlists_loaded')

    def save(self, playlist):
        """
        Save the playlist.

        For a playlist to be saveable, it must have the ``uri`` attribute set.
        You must not set the ``uri`` atribute yourself, but use playlist
        objects returned by :meth:`create` or retrieved from :attr:`playlists`,
        which will always give you saveable playlists.

        The method returns the saved playlist. The return playlist may differ
        from the saved playlist. E.g. if the playlist name was changed, the
        returned playlist may have a different URI. The caller of this method
        must throw away the playlist sent to this method, and use the
        returned playlist instead.

        If the playlist's URI isn't set or doesn't match the URI scheme of a
        current backend, nothing is done and :class:`None` is returned.

        :param playlist: the playlist
        :type playlist: :class:`mopidy.models.Playlist`
        :rtype: :class:`mopidy.models.Playlist` or :class:`None`
        """
        validation.check_instance(playlist, Playlist)

        if playlist.uri is None:
            return  # TODO: log this problem?

        uri_scheme = urllib.parse.urlparse(playlist.uri).scheme
        backend = self.backends.with_playlists.get(uri_scheme, None)
        if not backend:
            return None

        # TODO: we let AssertionError error through due to legacy tests :/
        with _backend_error_handling(backend, reraise=AssertionError):
            playlist = backend.playlists.save(playlist).get()
            playlist is None or validation.check_instance(playlist, Playlist)
            if playlist:
                listener.CoreListener.send(
                    'playlist_changed', playlist=playlist)
            return playlist

        return None

 */