package danbroid.mopidy.service;

import android.net.Uri;
import android.net.nsd.NsdServiceInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaBrowserServiceCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.session.MediaSessionCompat;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.sharedpreferences.Pref;

import java.util.LinkedList;
import java.util.List;

import danbroid.mopidy.BuildConfig;
import danbroid.mopidy.interfaces.MopidyPrefs_;
import danbroid.mopidy.util.MediaIds;
import danbroid.mopidy.util.MopidyServerFinder;

/**
 * Created by dan on 26/12/17.
 */

@EBean(scope = EBean.Scope.Singleton)
public class MopidyBackend {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MopidyBackend.class);
	public static String SESSION_TAG = "MopidyService";

	protected MediaBrowserServiceCompat service;

	protected MediaSessionCompat session;
	protected Bundle sessionExtras = new Bundle();


	@Pref
	MopidyPrefs_ prefs;

	@Bean
	protected MopidyServerFinder discoveryHelper;

	public void init(MediaBrowserServiceCompat service) {
		this.service = service;

		initSession();

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			discoveryHelper.start();
		}
	}

	protected void initSession() {
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
		@Override
		public void onPlayFromUri(Uri uri, Bundle extras) {
			log.error("onPlayFromUri(): {} NOT IMPLEMENTED", uri);//TODO
		}
	}

	protected MediaSessionCompat.Callback createSessionCallback() {
		return new SessionCallback();
	}

	public MediaSessionCompat getSession() {
		return session;
	}

	public void onLoadChildren(@NonNull String parentId, @NonNull MediaBrowserServiceCompat.Result<List<MediaBrowserCompat.MediaItem>> result) {
		log.trace("onLoadChildren(): {}", parentId);
		int i = parentId.indexOf('/');
		String type = i == -1 ? parentId : parentId.substring(0, i);
		String data = i == -1 ? "" : Uri.decode(parentId.substring(i + 1));

		switch (type) {
			case MediaIds.ROOT:
				loadRoot(result);
				break;
			case MediaIds.SERVER:
				loadServer(data, result);
				break;
			default:
				log.error("Unhandled parentID: " + parentId);
				break;
		}
	}

	protected void loadServer(String data, MediaBrowserServiceCompat.Result<List<MediaBrowserCompat.MediaItem>> result) {
		log.trace("loadServer(): {}", data);
	}

	protected void loadRoot(MediaBrowserServiceCompat.Result<List<MediaBrowserCompat.MediaItem>> result) {
		log.trace("loadRoot()");
		List<MediaBrowserCompat.MediaItem> items = new LinkedList<>();

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			for (String serviceName : discoveryHelper.getServices().keySet()) {

				NsdServiceInfo service = discoveryHelper.getServices().get(serviceName);

				MediaDescriptionCompat.Builder desc = new MediaDescriptionCompat.Builder();

				desc.setTitle(service.getServiceName());

				String host = service.getHost().toString();
				int port = service.getPort();

				if (host.startsWith("/")) host = host.substring(1);
				String description = host + ":" + port;

				desc.setDescription(description);
				String id = MediaIds.idServer(host, port);

				desc.setMediaId(id);

				items.add(new MediaBrowserCompat.MediaItem(desc.build(), MediaBrowserCompat.MediaItem.FLAG_BROWSABLE));
			}
		}

		if (BuildConfig.DEBUG) {
			MediaDescriptionCompat.Builder desc = new MediaDescriptionCompat.Builder();
			desc.setTitle("Dan");
			desc.setMediaId(MediaIds.idServer("192.168.1.2", 6680));
			desc.setDescription("dans mopidy server");
			items.add(new MediaBrowserCompat.MediaItem(desc.build(), MediaBrowserCompat.MediaItem.FLAG_BROWSABLE));
		}


		result.sendResult(items);
	}

	public void onDestroy() {
		log.info("onDestroy()");
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			discoveryHelper.stop();
		}
	}
}
