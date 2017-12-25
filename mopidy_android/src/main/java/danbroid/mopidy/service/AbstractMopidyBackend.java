package danbroid.mopidy.service;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaBrowserServiceCompat;
import android.support.v4.media.session.MediaSessionCompat;

import java.util.List;

/**
 * Created by dan on 26/12/17.
 */

public abstract class AbstractMopidyBackend {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AbstractMopidyBackend.class);
	public static String SESSION_TAG = "AbstractMopidyService";

	private MediaBrowserServiceCompat service;

	private MediaSessionCompat session;
	private Bundle sessionExtras = new Bundle();

	public void init(MediaBrowserServiceCompat service) {
		this.service = service;
		session = new MediaSessionCompat(service, SESSION_TAG);

		service.setSessionToken(session.getSessionToken());

		session.setFlags(
				MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
						MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS
		);

		session.setExtras(sessionExtras);


		session.setCallback(createSessionCallback());
	}

	protected class SessionCallback extends MediaSessionCompat.Callback {


	}

	protected MediaSessionCompat.Callback createSessionCallback() {
		return new SessionCallback();
	}

	public MediaSessionCompat getSession() {
		return session;
	}


	public void onLoadChildren(@NonNull String parentId, @NonNull MediaBrowserServiceCompat.Result<List<MediaBrowserCompat.MediaItem>> result) {
		log.trace("onLoadChildren(): {}", parentId);
	}
}
