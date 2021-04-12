package danbroid.mopidy.api;

import danbroid.mopidy.MopidyClient;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Slf4j
@Getter
public class Core extends Api {
	private final Library 	library 	= new Library(this);
	private final History 	history 	= new History(this);
	private final Mixer 	mixer 		= new Mixer(this);
	private final Playback 	playback 	= new Playback(this);
	private final Tracklist tracklist 	= new Tracklist(this);
	private final Playlists	playlists	= new Playlists(this);

	public Core(MopidyClient client) {
		super(client, "core.");
	}

	public Call<String[]> getUriSchemes() {
		return createCall("get_uri_schemes", String[].class);
	}

	public Call<String> getVersion() {
		return createCall("get_version", String.class);
	}
}