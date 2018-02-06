package danbroid.mopidy.interfaces;

import android.support.v4.media.MediaBrowserCompat;

import danbroid.mopidy.activities.ClientEventManager;
import danbroid.mopidy.activities.MopidyClient;

/**
 * Created by dan on 24/12/17.
 */
public interface MainView {
	//String ACTION_CONTROLLER_CONNECTED = MainView.class.getName() + ".ACTION_CONNECTED";

	MediaBrowserCompat getMediaBrowser();

	void onMediaItemSelected(MediaBrowserCompat.MediaItem item);

	void showContent(String mopidyURL);

	MopidyClient getMopidyClient();

	ClientEventManager getEventManager();
}
