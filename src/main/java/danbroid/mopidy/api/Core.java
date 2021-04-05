package danbroid.mopidy.api;

import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Getter
public class Core extends Api {
	private static final Logger log = LoggerFactory.getLogger(Core.class);

	private final Library 	library 	= new Library(this);
	private final History 	history 	= new History(this);
	private final Mixer 	mixer 		= new Mixer(this);
	private final Playback 	playback 	= new Playback(this);
	private final Tracklist trackList 	= new Tracklist(this);
	private final Playlists	playlists	= new Playlists(this);

	public Core() {
		super("core.");
	}

	public Call<String[]> getUriSchemes() {
		return createCall("get_uri_schemes", String[].class);
	}
}