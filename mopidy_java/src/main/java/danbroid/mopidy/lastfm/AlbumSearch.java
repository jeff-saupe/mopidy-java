package danbroid.mopidy.lastfm;

import danbroid.mopidy.transport.WebSocketTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by dan on 21/12/17.
 */
public class AlbumSearch extends LastFMCall {
	private static final Logger log = LoggerFactory.getLogger(AlbumSearch.class);

	public AlbumSearch() {
		super("album.getinfo");
		getBuilder().addQueryParameter("autocorrect", "1");
	}

	public AlbumSearch artist(String artist) {
		getBuilder().addQueryParameter("artist", artist);
		return this;
	}

	public AlbumSearch album(String album) {
		getBuilder().addQueryParameter("album", album);
		return this;
	}


}
