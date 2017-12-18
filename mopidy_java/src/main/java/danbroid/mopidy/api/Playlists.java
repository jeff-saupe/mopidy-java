package danbroid.mopidy.api;

import danbroid.mopidy.ResponseHandler;
import danbroid.mopidy.model.Ref;

/**
 * Created by dan on 18/12/17.
 */
public class Playlists extends Api {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Playlists.class);

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
	public void asList(ResponseHandler<Ref[]> handler) {
		call(new Call<Ref[]>(methodPrefix + "as_list")
				.setResultType(Ref[].class)
				.setHandler(handler));
	}


	public void getItems(String uri, ResponseHandler<Ref[]> handler) {
		call(new Call<Ref[]>(methodPrefix + "get_items")
				.addParam("uri", uri)
				.setResultType(Ref[].class)
				.setHandler(handler));
	}

}
