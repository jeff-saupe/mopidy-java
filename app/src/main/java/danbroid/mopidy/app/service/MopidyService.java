package danbroid.mopidy.app.service;

import org.androidannotations.annotations.EService;

import danbroid.mopidy.app.activities.MainActivity_;
import danbroid.mopidy.service.MopidyBackend;
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
		setSessionActivity(MainActivity_.class);
	}




}
