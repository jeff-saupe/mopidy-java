package danbroid.mopidy.util;

import android.net.Uri;

/**
 * Created by dan on 26/12/17.
 */
public class MediaIds {
	//private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MediaIds.class);

	public static final String EMPTY_ROOT = "__EMPTY_ROOT__";
	public static final String ROOT = "__ROOT__";
	public static final String PROFILES = "__PROFILES__";
	public static final String TRACKLIST = "__TRACKLIST__";
	public static final String PLAYLISTS = "__PLAYLISTS__";
	public static final String PARENT_FOLDER = "__PARENT_FOLDER__";

	public static final String M3U = "m3u";

	public static String encode(String prefix, String uri) {
		return prefix + ":" + Uri.encode(uri);
	}

	public static String[] decode(String mediaID) {
		int i = mediaID.indexOf(':');
		if (i > 0) {
			return new String[]{mediaID.substring(0, i), Uri.decode(mediaID.substring(i + 1))};
		} else {
			return new String[]{mediaID};
		}
	}

	public static String profileID(String mopidyURL) {
		return encode(PROFILES, mopidyURL);
	}

	public static String trackListID(int tlid) {
		return TRACKLIST + ":" + tlid;
	}


	public static String prependParentID(String parentID, String mediaID) {
		return parentID + "->" + mediaID;
	}

	public static String extractParentID(String mediaID) {
		int i = mediaID.lastIndexOf("->");
		if (i > -1)
			return mediaID.substring(0, i);
		return null;
	}

	public static String extractChildID(String mediaID) {
		int i = mediaID.lastIndexOf("->");
		if (i > -1)
			return mediaID.substring(i + 2);
		return mediaID;
	}
}
