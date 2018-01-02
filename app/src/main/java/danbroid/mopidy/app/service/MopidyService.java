package danbroid.mopidy.app.service;

import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.os.Bundle;

import org.androidannotations.annotations.EService;

import danbroid.mopidy.app.activities.MainActivity_;
import danbroid.mopidy.service.AbstractMopidyService;

/**
 * Created by dan on 24/12/17.
 */
@EService
public class MopidyService extends AbstractMopidyService {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MopidyService.class);

	@Override
	public void onCreate() {
		super.onCreate();
		ComponentName myService = new ComponentName(this, this.getClass());
		try {
			Bundle data = getPackageManager().getServiceInfo(myService, PackageManager.GET_META_DATA).metaData;
			log.debug("message: {}", data.getString("message"));
		} catch (PackageManager.NameNotFoundException e) {
			log.error(e.getMessage(), e);
		}

		setSessionActivity(MainActivity_.class);
	}


}
