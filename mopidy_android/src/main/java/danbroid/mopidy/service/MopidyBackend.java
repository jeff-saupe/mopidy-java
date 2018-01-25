package danbroid.mopidy.service;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.media.MediaBrowserServiceCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;

import com.google.gson.JsonElement;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.sharedpreferences.Pref;

import java.util.List;

import danbroid.mopidy.AndroidMopidyConnection;
import danbroid.mopidy.ResponseHandler;
import danbroid.mopidy.interfaces.CallContext;
import danbroid.mopidy.interfaces.MopidyPrefs_;
import danbroid.mopidy.model.TlTrack;
import danbroid.mopidy.util.MediaIds;
import danbroid.mopidy.util.MopidyServerFinder;

/**
 * Created by dan on 26/12/17.
 */

@EBean(scope = EBean.Scope.Singleton)
public class MopidyBackend {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MopidyBackend.class);
	public static final String COMMAND_CONNECT = "MopidyBackend.COMMAND_CONNECT";
	public static final String COMMAND_ADD_TO_TRACKLIST = "MopidyBackend.COMMAND_ADD_TO_TRACKLIST";
	public static final String COMMAND_SHUFFLE_PLAYLIST = "MopidyBackend.COMMAND_SHUFFLE_PLAYLIST";

	public static final String COMMAND_TRACKLIST_CLEAR = "MopidyBackend.COMMAND_TRACKLIST_CLEAR";

	public static final String SESSION_EVENT_BUSY = "MopidyBackend.EVENT_BUSY";
	public static final String SESSION_EVENT_NOT_BUSY = "MopidyBackend.EVENT_NOT_BUSY";
	public static final String SESSION_EVENT_CONNECTED = "MopidyBackend.EVENT_CONNECTED";

	public static final String ARG_MESSAGE = "message";

	public static final String ARG_VERSION = "version";
	public static final String ARG_URL = "url";
	public static final String ARG_URI = "uri";
	public static final String ARG_URIS = "uris";
	public static final String ARG_MEDIAID = "mediaid";
	public static final String ARG_REPLACE = "replace";
	public static final String ARG_POSITION = "position";
	public static final String ARG_PLAYABLE = "playable";

	public static final int RESULT_CODE_SUCCESS = 0;

	public static final String INTENT_MOPIDY_CONNECTED = MopidyBackend.class.getName() + ".INTENT_MOPIDY_CONNECTED";


	protected AbstractMopidyService service;


	@Bean
	protected MopidyContentManager contentManager;

	@Pref
	protected MopidyPrefs_ prefs;

	@Bean
	protected AndroidMopidyConnection conn;

	@Bean
	protected MopidyServerFinder discoveryHelper;


	public void init(AbstractMopidyService service) {
		log.trace("init()");
		this.service = service;

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			discoveryHelper.start();
		}

		new MopidyEventManager(conn, service);


	}

	public void onMopidyConnected() {
		LocalBroadcastManager.getInstance(service).sendBroadcast(new Intent(INTENT_MOPIDY_CONNECTED));
		service.getSession().sendSessionEvent(MopidyBackend.SESSION_EVENT_CONNECTED, null);
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
		public void onSkipToNext() {
			conn.getPlayback().next().call();
		}

		@Override
		public void onSkipToPrevious() {
			conn.getPlayback().previous().call();
		}

		@Override
		public void onPlayFromMediaId(String mediaId, Bundle extras) {
			playFromMediaID(mediaId);
		}

		@Override
		public void onSeekTo(long pos) {
			log.trace("onSeekTo(): {}", pos);
			conn.getPlayback().seek(pos).call();
		}

		@Override
		public void onCommand(String command, Bundle extras, final ResultReceiver cb) {

			switch (command) {
				case COMMAND_TRACKLIST_CLEAR:
					conn.getTrackList().clear().call(new ResponseHandler<Void>() {
						@Override
						public void onResponse(CallContext context, Void result) {
							cb.send(RESULT_CODE_SUCCESS, null);
						}
					});
					return;

				case COMMAND_ADD_TO_TRACKLIST:
					String mediaID = extras.getString(ARG_MEDIAID);
					boolean replace = extras.getBoolean(ARG_REPLACE);
					boolean playable = extras.getBoolean(ARG_PLAYABLE);
					addToTracklist(mediaID, replace, playable, cb);
					return;

				case COMMAND_CONNECT:
					String url = extras.getString(ARG_URL);
					log.trace("connecting to {}", url);
					conn.start(url).call(new ResponseHandler<String>() {
						@Override
						public void onError(int code, String message, JsonElement data) {
							cb.send(code, bundle(ARG_MESSAGE, message));
						}

						@Override
						public void onResponse(CallContext context, String version) {
							cb.send(RESULT_CODE_SUCCESS, bundle(ARG_VERSION, version));
						}
					});
					break;

				case COMMAND_SHUFFLE_PLAYLIST:
					Long start = (Long) extras.get("start");
					Long end = (Long) extras.get("end");
					conn.getTrackList().shuffle(start, end).call(new ResponseHandler<Void>() {
						@Override
						public void onResponse(CallContext context, Void result) {
							service.notifyChildrenChanged(MediaIds.TRACKLIST);
							cb.send(RESULT_CODE_SUCCESS, null);
						}
					});
					break;
			}

		}


	}

	private void addToTracklist(final String mediaID, boolean replace, final boolean playable, final ResultReceiver cb) {
		log.info("addToTracklist() {} replace: " + replace + " playable: " + playable, mediaID);
		if (replace) {
			conn.getTrackList().clear().call(new ResponseHandler<Void>() {
				@Override
				public void onResponse(CallContext context, Void result) {
					appendToTracklist(mediaID, playable, cb);
				}
			});
		} else {
			appendToTracklist(mediaID, playable, cb);
		}
	}

	private void appendToTracklist(String mediaID, boolean playable, final ResultReceiver cb) {
		log.trace("appendToTracklist(): {} playable: {}", mediaID, playable);

		if (playable) {
			conn.getTrackList().add(mediaID).call(new ResponseHandler<TlTrack[]>() {
				@Override
				public void onResponse(CallContext context, TlTrack[] result) {
					cb.send(RESULT_CODE_SUCCESS, bundle(ARG_MESSAGE, "Added " + result.length + " tracks to tracklist"));
				}
			});
			return;
		}

		contentManager.getTracks(mediaID, new MopidyContentManager.Result<List<String>>() {
			@Override
			public void onResult(List<String> result) {
				conn.getTrackList().add(result.toArray(new String[]{})).call(new ResponseHandler<TlTrack[]>() {
					@Override
					public void onResponse(CallContext context, TlTrack[] result) {
						cb.send(RESULT_CODE_SUCCESS, bundle(ARG_MESSAGE, "added " + result.length + " tracks to the tracklist"));
					}
				});
			}
		});

	}

	private static Bundle bundle(String name, String value) {
		Bundle b = new Bundle();
		b.putString(name, value);
		return b;
	}


	public void playFromMediaID(String mediaId) {
		log.debug("playFromMediaID(): {}", mediaId);

		if (mediaId.startsWith(MediaIds.TRACKLIST)) {
			int i = mediaId.lastIndexOf(':');
			int tlid = Integer.parseInt(mediaId.substring(i + 1));
			conn.getPlayback().play(tlid, null).call();
			return;
		}

		conn.getTrackList().add(mediaId)
				.call(new ResponseHandler<TlTrack[]>() {
					@Override
					public void onResponse(CallContext context, TlTrack[] result) {
						if (result.length != 1) {
							log.error("unexpected result length: " + result.length);
							return;
						}

						log.trace("tracklist length: " + result.length + " playing {}", result[0]);
						conn.getPlayback().play(result[0].getTlid(), null).call();
					}
				});

	}

	public MediaSessionCompat.Callback createSessionCallback() {
		return new SessionCallback();
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

	public static void connect(Activity activity, String url, ResultReceiver resultReceiver) {
		MediaControllerCompat controller = MediaControllerCompat.getMediaController(activity);

		if (controller == null) {
			log.error("controller is null");
			return;
		}

		controller.sendCommand(COMMAND_CONNECT, bundle(ARG_URL, url), resultReceiver);
	}


}
