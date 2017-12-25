package danbroid.mopidy.app.service;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.media.MediaBrowserCompat;

import org.androidannotations.annotations.EService;

import java.util.List;

import danbroid.mopidy.app.activities.MainActivity_;
import danbroid.mopidy.service.AbstractMopidyBackend;
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

	@Override
	protected AbstractMopidyBackend createBackend() {
		return MopidyBackend_.getInstance_(this);
	}


	@Nullable
	@Override
	public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, @Nullable Bundle rootHints) {
		return super.onGetRoot(clientPackageName, clientUid, rootHints);
	}

	@Override
	public void onLoadChildren(@NonNull String parentId, @NonNull Result<List<MediaBrowserCompat.MediaItem>> result) {
		super.onLoadChildren(parentId, result);
	}
}
