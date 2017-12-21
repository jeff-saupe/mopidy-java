package danbroid.mopidy.lastfm;

/**
 * Created by dan on 21/12/17.
 */
public class AlbumSearch extends LastFMCall {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AlbumSearch.class);

	protected AlbumSearch() {
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
