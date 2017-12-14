package danbroid.mopidy.app;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.SupposeUiThread;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.sharedpreferences.Pref;

import danbroid.mopidy.app.interfaces.MainPrefs_;

/**
 * Created by dan on 14/12/17.
 */
@EBean(scope = EBean.Scope.Singleton)
public class MopidyConnection extends danbroid.mopidy.MopidyConnection {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MopidyConnection.class);

	@Pref
	MainPrefs_ prefs;

	@AfterInject
	void init() {
		String url = prefs.lastConnectionURL().getOr(null);
		if (url != null) setUrl(url);
	}

	@Override
	public void setUrl(String url) {
		super.setUrl(url);
		prefs.edit().lastConnectionURL().put(url).apply();
	}

	@SupposeUiThread
	@Override
	public void start() {
		super.start();
	}

	@SupposeUiThread
	@Override
	public void stop() {
		super.stop();
	}


}
