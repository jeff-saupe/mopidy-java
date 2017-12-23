package danbroid.mopidy.app.util;

import android.content.UriMatcher;
import android.net.Uri;

/**
 * Created by dan on 14/12/17.
 */
public class MopidyUris {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MopidyUris.class);


	public static final String AUTHORITY = "mopidy";

	public static final Uri URI_ROOT = Uri.parse("app://" + AUTHORITY);
	public static final Uri URI_SERVERS = Uri.withAppendedPath(URI_ROOT, "servers");

	public static final Uri URI_TRACKLIST = Uri.withAppendedPath(URI_ROOT, "tracklist");
	public static final Uri URI_PLAYLISTS = Uri.withAppendedPath(URI_ROOT, "playlists");


	private static final UriMatcher URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);

	public static final int MATCH_ROOT = 1;
	public static final int MATCH_SERVER = 2;
	public static final int MATCH_SERVERS = 3;
	public static final int MATCH_MOPIDY_URI = 4;
	public static final int MATCH_TRACKLIST = 5;
	public static final int MATCH_TRACKLIST_ITEM = 6;
	public static final int MATCH_PLAYLISTS = 7;
	public static final int MATCH_PLAYLIST = 8;

	static {
		URI_MATCHER.addURI(AUTHORITY, null, MATCH_ROOT);
		URI_MATCHER.addURI(AUTHORITY, "/servers", MATCH_SERVERS);
		URI_MATCHER.addURI(AUTHORITY, "/playlists", MATCH_PLAYLISTS);
		URI_MATCHER.addURI(AUTHORITY, "/playlists/*", MATCH_PLAYLIST);


		URI_MATCHER.addURI(AUTHORITY, "/tracklist", MATCH_TRACKLIST);
		URI_MATCHER.addURI(AUTHORITY, "/tracklist/*", MATCH_TRACKLIST_ITEM);

		URI_MATCHER.addURI(AUTHORITY, "*", MATCH_SERVER);
		URI_MATCHER.addURI(AUTHORITY, "*/*", MATCH_MOPIDY_URI);
	}

	public static int match(String uri) {
		return match(Uri.parse(uri));
	}

	public static int match(Uri uri) {
		return URI_MATCHER.match(uri);
	}

	public static Uri uriServer(String host, int port) {
		return uriServer(host + ":" + port);
	}

	public static Uri uriServer(String address) {
		return URI_ROOT.buildUpon().appendPath(Uri.encode(address)).build();
	}

	public static Uri uriTracklistItem(int tlid) {
		return Uri.withAppendedPath(URI_TRACKLIST, "" + tlid);
	}

	public static Uri uriPlaylist(String uri) {
		return URI_PLAYLISTS.buildUpon().appendPath(Uri.encode(uri)).build();
	}
}

