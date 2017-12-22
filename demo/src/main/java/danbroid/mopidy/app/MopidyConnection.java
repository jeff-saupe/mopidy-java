package danbroid.mopidy.app;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.sharedpreferences.Pref;

import danbroid.mopidy.app.interfaces.MainPrefs_;

/**
 * Created by dan on 14/12/17.
 */
@EBean(scope = EBean.Scope.Singleton)
public class MopidyConnection extends danbroid.mopidy.MopidyConnection {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MopidyConnection.class);
	public static final String INTENT_SERVER_CONNECTED = MopidyConnection.class.getName() + ".INTENT_SERVER_CONNECTED";

	@Pref
	MainPrefs_ prefs;

	@RootContext
	Context context;

	@AfterInject
	void init() {
		String url = prefs.lastConnectionURL().getOr(null);
		if (url != null) {
			setUrl(url);
		}
	}

	@Override
	public void setUrl(String url) {
		super.setUrl(url);
		prefs.edit().lastConnectionURL().put(url).apply();
	}

	@Override
	protected void onConnect() {
		log.info("onConnect()");
		LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(INTENT_SERVER_CONNECTED));
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
}
