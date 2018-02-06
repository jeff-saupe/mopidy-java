package danbroid.mopidy.app.interfaces;

import android.support.v4.media.MediaBrowserCompat;

/**
 * Created by dan on 12/01/18.
 */
public interface MainView extends danbroid.mopidy.interfaces.MainView {

	void showFullControls();

	void hideFullControls();

	MediaBrowserCompat getMediaBrowser();
}
