package danbroid.mopidy.service;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaBrowserServiceCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import java.util.List;

import danbroid.mopidy.util.MediaIds;
import danbroid.mopidy.util.PackageValidator;

/**
 * Created by dan on 23/12/17.
 */

public abstract class AbstractMopidyService extends MediaBrowserServiceCompat {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AbstractMopidyService.class);

	protected MopidyBackend backend;
	private PackageValidator packageValidator;
	private MopidyContentManager_ contentManager;
	private MediaSessionCompat session;
	private Bundle sessionExtras = new Bundle();

	public static String SESSION_TAG = "MopidyService";

	@Override
	public void onCreate() {
		super.onCreate();
		log.info("onCreate()");

		backend = createBackend();
		packageValidator = new PackageValidator(this);
		contentManager = MopidyContentManager_.getInstance_(this);

		session = new MediaSessionCompat(this, SESSION_TAG);

		setSessionToken(session.getSessionToken());

		session.setFlags(
				MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
						MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS
		);

		session.setExtras(sessionExtras);

		session.setCallback(backend.createSessionCallback());

		backend.init(this);
	}

	protected MopidyBackend createBackend() {
		return MopidyBackend_.getInstance_(this);
	}


	public void setSessionActivity(Class<? extends Activity> activityClass) {
		Intent intent = new Intent(this, activityClass);
		PendingIntent pi = PendingIntent.getActivity(this, 99 /*request code*/,
				intent, PendingIntent.FLAG_UPDATE_CURRENT);
		session.setSessionActivity(pi);
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
			return new BrowserRoot(MediaIds.EMPTY_ROOT, null);
		}

		return new BrowserRoot(MediaIds.ROOT, null);
	}

	@Override
	public void onLoadChildren(@NonNull String parentId, @NonNull Result<List<MediaBrowserCompat.MediaItem>> result) {
		contentManager.onLoadChildren(parentId, result);
	}

	@Override
	public void onDestroy() {
		log.info("onDestroy()");
		super.onDestroy();
		backend.onDestroy();
	}


	public void onPlaybackStarted() {
		log.warn("onPlaybackStarted()");
		session.setActive(true);

		startService(new Intent(getApplicationContext(), getClass()));
	}


	public void onPlaybackStopped() {
		log.warn("onPlaybackStopped()");
		session.setActive(false);

		stopForeground(true);
	}

	public MediaSessionCompat getSession() {
		return session;
	}

	public void onMetadataChanged(MediaMetadataCompat md) {
		session.setMetadata(md);
	}

	public void onPlaybackStateChanged(PlaybackStateCompat state) {
		session.setPlaybackState(state);
	}
}
