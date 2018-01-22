package danbroid.mopidy;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.sharedpreferences.Pref;

import danbroid.mopidy.interfaces.MopidyPrefs_;
import danbroid.mopidy.service.MopidyBackend;
import danbroid.mopidy.transport.Transport;


/**
 * Created by dan on 14/12/17.
 */
@EBean(scope = EBean.Scope.Singleton)
public class AndroidMopidyConnection extends danbroid.mopidy.MopidyConnection {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AndroidMopidyConnection.class);

	@Pref
	MopidyPrefs_ prefs;

	@RootContext
	Context context;

	@Bean
	MopidyBackend backend;

	@AfterInject
	void init() {
		String url = prefs.lastConnectionURL().getOr(null);
		if (url != null) {
			setURL(url);
		}
	}

	@Override
	public void setURL(String url) {
		super.setURL(url);
		prefs.edit().lastConnectionURL().put(url).apply();
	}

	@Override
	protected void onConnect() {

		backend.onMopidyConnected();

	}

	/**
	 * Move all message processing to the UI thread
	 * @param text The received message
	 */
	@UiThread
	@Override
	protected void processMessage(String text) {
		super.processMessage(text);
	}


	@Override
	protected Transport createTransport() {
		return AndroidWebSocket_.getInstance_(context).setCallback(this);
	}
}
