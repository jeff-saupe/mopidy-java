package danbroid.mopidy.app;

import android.app.Application;

import org.androidannotations.annotations.EApplication;

import danbroid.mopidy.lastfm.LastFMCall;

/**
 * Created by dan on 3/01/18.
 */
@EApplication
public class MopidyApplication extends Application {
	//private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MopidyApplication.class);

	@Override
	public void onCreate() {
		super.onCreate();
		LastFMCall.API_KEY = getString(R.string.lastfm_api_key);
	}
}
