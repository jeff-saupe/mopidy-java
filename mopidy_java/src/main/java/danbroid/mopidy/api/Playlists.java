package danbroid.mopidy.api;

import danbroid.mopidy.ResponseHandler;
import danbroid.mopidy.interfaces.Constants;
import danbroid.mopidy.model.Ref;
import danbroid.mopidy.model.Track;

/**
 * Created by dan on 18/12/17.
 */
public class Playlists extends Api {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Playlists.class);

	protected Playlists(Api parent) {
		super(parent, "playlists.");
	}

	public void asList(ResponseHandler<Ref[]> handler) {
		call(new Call<Ref[]>(methodPrefix + "as_list")
				.setResultType(Ref[].class)
				.setHandler(handler));
	}

}
