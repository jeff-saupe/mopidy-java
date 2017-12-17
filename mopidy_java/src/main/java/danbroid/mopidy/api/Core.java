package danbroid.mopidy.api;

import danbroid.mopidy.ResponseHandler;

/**
 * Created by dan on 10/12/17.
 */
public class Core extends Api {

	public Core() {
		super("core.");
	}

	public void getVersion(ResponseHandler<String> handler) {
		call(new Call<String>(methodPrefix + "get_version")
				.setResultType(String.class)
				.setHandler(handler));
	}

	public void getUriScemes(ResponseHandler<String[]> handler) {
		call(new Call<String[]>(methodPrefix + "get_uri_schemes")
				.setResultType(String[].class)
				.setHandler(handler));
	}

	private Library library = new Library(this);
	private History history = new History(this);
	private Mixer mixer = new Mixer(this);
	private Playback playback = new Playback(this);
	private TrackList trackList = new TrackList(this);
	private Playlists playlists = new Playlists(this);

	public Library getLibrary() {
		return library;
	}

	public History getHistory() {
		return history;
	}

	public Mixer getMixer() {
		return mixer;
	}

	public Playback getPlayback() {
		return playback;
	}

	public TrackList getTrackList() {
		return trackList;
	}

	public Playlists getPlaylists() {
		return playlists;
	}
}
