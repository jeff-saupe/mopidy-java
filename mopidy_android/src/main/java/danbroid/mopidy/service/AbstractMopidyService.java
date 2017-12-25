package danbroid.mopidy.service;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaBrowserServiceCompat;

import java.util.List;

import danbroid.mopidy.util.MediaIDS;
import danbroid.mopidy.util.PackageValidator;

/**
 * Created by dan on 23/12/17.
 */

public abstract class AbstractMopidyService extends MediaBrowserServiceCompat {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AbstractMopidyService.class);

	public static final String SESSION_EVENT_BUSY = AbstractMopidyService.class.getName() + ".EVENT_BUSY";
	public static final String SESSION_EVENT_NOT_BUSY = AbstractMopidyService.class.getName() + ".EVENT_NOT_BUSY";


	protected AbstractMopidyBackend backend;
	private PackageValidator packageValidator;

	@Override
	public void onCreate() {
		super.onCreate();
		log.info("onCreate()");
		backend = createBackend();
		backend.init(this);

		packageValidator = new PackageValidator(this);
	}

	protected abstract AbstractMopidyBackend createBackend();


	public void setSessionActivity(Class<? extends Activity> activityClass) {
		Intent intent = new Intent(this, activityClass);
		PendingIntent pi = PendingIntent.getActivity(this, 99 /*request code*/,
				intent, PendingIntent.FLAG_UPDATE_CURRENT);
		backend.getSession().setSessionActivity(pi);
	}


	@Nullable
	@Override
	public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, @android.support.annotation.Nullable Bundle rootHints) {

		if (!packageValidator.isCallerAllowed(this, clientPackageName, clientUid)) {
			// If the request comes from an untrusted package, return an empty browser root.
			// If you return null, then the media browser will not be able to connect and
			// no further calls will be made to other media browsing methods.
			log.error("OnGetRoot: Browsing NOT ALLOWED for unknown caller. "
					+ "Returning empty browser root so all apps can use MediaController."
					+ clientPackageName);
			return new BrowserRoot(MediaIDS.
					MEDIA_ID_EMPTY_ROOT, null);
		}

		return new BrowserRoot(MediaIDS.MEDIA_ID_ROOT, null);
	}

	@Override
	public void onLoadChildren(@NonNull String parentId, @NonNull Result<List<MediaBrowserCompat.MediaItem>> result) {
		log.trace("onLoadChildren(): {}", parentId);
		backend.onLoadChildren(parentId, result);
	}
}
