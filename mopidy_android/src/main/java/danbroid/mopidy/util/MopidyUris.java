package danbroid.mopidy.util;

import android.net.Uri;

/**
 * Created by dan on 26/12/17.
 */
public class MopidyUris {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MopidyUris.class);
	public static final String AUTHORITY = "mopidy";

	public static final Uri URI_BASE = Uri.parse("app://" + AUTHORITY);
	public static final Uri URI_SERVERS = Uri.withAppendedPath(URI_BASE,"servers");

	public static Uri getServerURI(String host,int port){
		return URI_SERVERS.buildUpon().appendPath(host).appendPath(String.valueOf(port)).build();
	}

}
