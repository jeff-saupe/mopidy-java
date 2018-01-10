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
import android.text.TextUtils;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.sharedpreferences.Pref;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import danbroid.mopidy.AndroidMopidyConnection;
import danbroid.mopidy.BuildConfig;
import danbroid.mopidy.ResponseHandler;
import danbroid.mopidy.interfaces.CallContext;
import danbroid.mopidy.interfaces.MopidyPrefs_;
import danbroid.mopidy.model.Image;
import danbroid.mopidy.model.Ref;
import danbroid.mopidy.model.TlTrack;
import danbroid.mopidy.util.MediaIds;
import danbroid.mopidy.util.MopidyServerFinder;

/**
 * Created by dan on 26/12/17.
 */

@EBean(scope = EBean.Scope.Singleton)
public class MopidyBackend {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MopidyBackend.class);

	public static final String SESSION_EVENT_BUSY = "MopidyBackend.EVENT_BUSY";
	public static final String SESSION_EVENT_NOT_BUSY = "MopidyBackend.EVENT_NOT_BUSY";
	public static final String SESSION_EVENT_CONNECTED = "MopidyBackend.EVENT_CONNECTED";

	public static String SESSION_TAG = "MopidyService";

	protected MediaBrowserServiceCompat service;

	protected MediaSessionCompat session;
	protected Bundle sessionExtras = new Bundle();


	@Pref
	protected MopidyPrefs_ prefs;


	@Bean
	protected AndroidMopidyConnection conn;

	@Bean
	protected MopidyServerFinder discoveryHelper;


	public void init(MediaBrowserServiceCompat service) {
		this.service = service;

		initSession();

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			discoveryHelper.start();
		}

		new MopidyEventManager(session, conn);

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
		public void onPause() {
			conn.getPlayback().pause().call();
		}

		@Override
		public void onPlay() {
			conn.getPlayback().play(null, null).call();
		}

		@Override
		public void onPlayFromUri(Uri uri, Bundle extras) {
			log.error("onPlayFromUri(): {} NOT IMPLEMENTED", uri);//TODO
		}

		@Override
		public void onPlayFromMediaId(String mediaId, Bundle extras) {
			playFromMediaID(mediaId);
		}
	}

	public void playFromMediaID(String mediaId) {
		log.debug("playFromMediaID(): {}", mediaId);
		conn.getTrackList().add(mediaId, null, null, null)
				.call(new ResponseHandler<TlTrack[]>() {
					@Override
					public void onResponse(CallContext context, TlTrack[] result) {
						if (result.length != 1) {
							log.error("unexpected result length: " + result.length);
							return;
						}
						conn.getPlayback().play(result[0].getTlid(), null).call();
					}
				});
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
				return;
			case MediaIds.SERVER:
				loadServer(data, result);
				return;
			case MediaIds.MOPIDY_ROOT:
				parentId = null;
				break;
		}

		loadMopidy(parentId, result);
	}

	protected void loadMopidy(String id, @NonNull final MediaBrowserServiceCompat.Result<List<MediaBrowserCompat.MediaItem>> result) {
		log.trace("loadMopidy(): {}", id);
		if (TextUtils.isEmpty(id)) id = null;
		result.detach();

		conn.getLibrary().browse(id).call(new ResponseHandler<Ref[]>() {
			@Override
			public void onResponse(CallContext context, Ref[] refs) {
				resolveImages(refs, result);
			}
		});

	}

	protected void resolveImages(final Ref[] refs, @NonNull final MediaBrowserServiceCompat.Result<List<MediaBrowserCompat.MediaItem>> result) {
		log.trace("resolveImages()");
		HashSet<String> uris = new HashSet<>();
		for (Ref ref : refs) {
			if (ref.getType().equals(Ref.TYPE_TRACK)) {
				if (!uris.contains(ref.getUri())) uris.add(ref.getUri());
			}
		}

		if (uris.isEmpty()) {
			result.sendResult(toMediaItems(refs));
			return;
		}

		conn.getLibrary().getImages(uris.toArray(new String[]{})).call(new ResponseHandler<Map<String, Image[]>>() {
			@Override
			public void onResponse(CallContext context, Map<String, Image[]> imgMap) {
				for (Ref ref : refs) {
					if (ref.getType().equals(Ref.TYPE_TRACK)) {
						if (imgMap.containsKey(ref.getUri())) {
							Image images[] = imgMap.get(ref.getUri());
							if (images.length > 0)
								ref.setExtra(images[0]);
						}
					}
				}
				result.sendResult(toMediaItems(refs));

			}
		});
	}


	public List<MediaBrowserCompat.MediaItem> toMediaItems(Ref[] refs) {
		LinkedList<MediaBrowserCompat.MediaItem> items = new LinkedList<>();
		for (Ref ref : refs)
			items.add(toMediaItem(ref));
		return items;
	}

	public MediaBrowserCompat.MediaItem toMediaItem(Ref ref) {
		MediaDescriptionCompat.Builder desc = new MediaDescriptionCompat.Builder();
		String name = ref.getName();
		desc.setTitle(name);
		String uri = ref.getUri();
		desc.setMediaId(uri);

		log.trace("toMediaItem(): " + ref.getType() + " {}:{}", name, uri);
		desc.setDescription(uri);

		if (ref.getExtra() != null) {
			Image img = (Image) ref.getExtra();
			log.trace("FOUND IMAGE {} -> {}", ref.getUri(), img.getUri());
			desc.setIconUri(Uri.parse(img.getUri()));
		}

		int flags = 0;
		if (ref.getType().equals(Ref.TYPE_TRACK)) flags = MediaBrowserCompat.MediaItem.FLAG_PLAYABLE;
		else flags = MediaBrowserCompat.MediaItem.FLAG_BROWSABLE;
		return new MediaBrowserCompat.MediaItem(desc.build(), flags);
	}

	protected void loadServer(String data, final MediaBrowserServiceCompat.Result<List<MediaBrowserCompat.MediaItem>> result) {
		log.trace("loadServer(): {}", data);
		int i = data.indexOf(':');
		String host = data.substring(0, i);
		int port = Integer.parseInt(data.substring(i + 1));
		conn.start(host, port);

		loadMopidy(null, result);
	}

	protected void loadRoot(MediaBrowserServiceCompat.Result<List<MediaBrowserCompat.MediaItem>> result) {
		log.trace("loadRoot()");
		List<MediaBrowserCompat.MediaItem> items = new LinkedList<>();

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			for (String serviceName : discoveryHelper.getServices().keySet()) {
				log.error("FOUND SERVICE: " + serviceName);

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

		if (BuildConfig.DEBUG) { //TODO REMOVE THIS SECTION
			MediaDescriptionCompat.Builder desc = new MediaDescriptionCompat.Builder();
			desc.setTitle("Dan");
			desc.setMediaId(MediaIds.idServer("192.168.1.2", 6680));
			desc.setDescription("dans mopidy server");
			items.add(new MediaBrowserCompat.MediaItem(desc.build(), MediaBrowserCompat.MediaItem.FLAG_BROWSABLE));

			desc = new MediaDescriptionCompat.Builder();
			desc.setTitle("Rip");
			desc.setMediaId(MediaIds.idServer("192.168.1.4", 6680));
			desc.setDescription("rip mopidy server");
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

	public MediaBrowserServiceCompat getService() {
		return service;
	}
}
