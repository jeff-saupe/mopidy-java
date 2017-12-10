package danbroid.mopidy.app.content;

import android.net.nsd.NsdServiceInfo;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;

import java.util.ArrayList;

import danbroid.mopidy.app.fragments.ContentListFragment;
import danbroid.mopidy.app.util.MopidyServerDiscovery;
import danbroid.mopidy.model.Ref;

/**
 * Created by dan on 10/12/17.
 */
@EBean(scope = EBean.Scope.Singleton)
public class ContentProvider {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ContentProvider.class);

	public static final String URI_PREFIX = "android_mopidy_demo:";

	public static String URI_SERVERS = URI_PREFIX + "servers";
	public static String URI_SERVER = URI_PREFIX + "server";




	public void browse(String uri, ContentListFragment view) {
		log.trace("browse(): {}", uri);



		throw new IllegalArgumentException("Invalid uri: " + uri);
	}




}
