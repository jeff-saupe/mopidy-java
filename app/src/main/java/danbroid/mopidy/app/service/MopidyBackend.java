package danbroid.mopidy.app.service;

import android.support.v4.media.MediaBrowserServiceCompat;

import org.androidannotations.annotations.EBean;

import danbroid.mopidy.service.AbstractMopidyBackend;

/**
 * Created by dan on 26/12/17.
 */
@EBean(scope = EBean.Scope.Singleton)
public class MopidyBackend extends AbstractMopidyBackend {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MopidyBackend.class);

	@Override
	public void init(MediaBrowserServiceCompat service) {
		super.init(service);

	}
}
