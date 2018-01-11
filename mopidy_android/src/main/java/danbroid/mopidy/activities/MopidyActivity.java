package danbroid.mopidy.activities;

import android.content.ComponentName;
import android.content.Intent;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.AppCompatActivity;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;

import danbroid.mopidy.fragments.MediaFragment;
import danbroid.mopidy.fragments.MediaListFragment;
import danbroid.mopidy.interfaces.MainView;
import danbroid.mopidy.service.AbstractMopidyService;

/**
 * Created by dan on 24/12/17.
 */
@EActivity
public abstract class MopidyActivity extends AppCompatActivity implements MainView {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MopidyActivity.class);


	protected final MediaBrowserCompat.ConnectionCallback connectionCallback =
			new MediaBrowserCompat.ConnectionCallback() {

				@Override
				public void onConnected() {
					log.debug("onConnected()");
					try {
						connectToSession(mediaBrowser.getSessionToken());
					} catch (RemoteException e) {
						log.error("could not connect media controller", e);
					}
				}

				@Override
				public void onConnectionFailed() {
					log.error("onConnectionFailed()");
				}
			};


	protected final MediaControllerCompat.Callback controllerCallback =
			new MediaControllerCompat.Callback() {
				@Override
				public void onPlaybackStateChanged(@NonNull PlaybackStateCompat state) {
					log.error("onPlaybackStateCHanged(): " + state.getState());
					MopidyActivity.this.onPlaybackStateChanged(state);
				}

				@Override
				public void onMetadataChanged(MediaMetadataCompat metadata) {
					MopidyActivity.this.onMetadataChanged(metadata);
				}
			};

	protected MediaBrowserCompat mediaBrowser;


	@AfterViews
	protected void init() {
		log.info("init()");

		mediaBrowser = new MediaBrowserCompat(this,
				new ComponentName(this, getServiceClass()), connectionCallback, null);
	}

	protected abstract Class<? extends AbstractMopidyService> getServiceClass();

	private void connectToSession(MediaSessionCompat.Token token) throws RemoteException {
		log.info("connectToSession(): {}", token);
		MediaControllerCompat mediaController = new MediaControllerCompat(this, token);
		MediaControllerCompat.setMediaController(this, mediaController);
		mediaController.registerCallback(controllerCallback);

		LocalBroadcastManager bm = LocalBroadcastManager.getInstance(this);
		bm.sendBroadcast(new Intent(MainView.ACTION_CONTROLLER_CONNECTED));
		PlaybackStateCompat playbackState = mediaController.getPlaybackState();

		if (playbackState != null) {
			switch (playbackState.getState()) {
				case PlaybackStateCompat.STATE_NONE:
				case PlaybackStateCompat.STATE_STOPPED:
					break;
				default:
					log.warn("unhandled playback state: " + playbackState.getState());
					//		showBottomControls(false);
					break;
			}
			onPlaybackStateChanged(playbackState);
		} else {
			log.warn("playback state is null");
		}

	}

	@Override
	protected void onStart() {
		super.onStart();
		log.debug("onStart()");
		mediaBrowser.connect();
	}

	@Override
	protected void onStop() {
		super.onStop();
		log.debug("onStop()");
		MediaControllerCompat controllerCompat = getSupportMediaController();
		if (controllerCompat != null) {
			controllerCompat.unregisterCallback(controllerCallback);
		}
		mediaBrowser.disconnect();
	}

	public MediaControllerCompat getSupportMediaController() {
		return MediaControllerCompat.getMediaController(this);
	}

	protected void onPlaybackStateChanged(PlaybackStateCompat state) {
		log.trace("onPlaybackStateChanged(): {}", state.getState());
	}

	protected void onMetadataChanged(MediaMetadataCompat metadata) {
		log.trace("onMetadataChanged(): {}", metadata);
	}

	@Override
	public MediaBrowserCompat getMediaBrowser() {
		return mediaBrowser;
	}

	@Override
	public void onMediaItemSelected(MediaBrowserCompat.MediaItem item) {
		String mediaID = item.getMediaId();
		log.debug("onMediaItemSelected() mediaId: {}", mediaID);


		if (item.isPlayable()) {
			log.error("PLAYABLE!");
			getSupportMediaController().getTransportControls()
					.playFromMediaId(item.getMediaId(), null);

		} else if (item.isBrowsable()) {
			showContent(item.getMediaId());
		} else {
			log.warn("Ignoring MediaItem that is neither browsable nor playable: mediaId: {}", item.getMediaId());
		}
	}

	protected abstract void setContent(MediaFragment instance);

	@Override
	public void showContent(String mediaID) {
		setContent(MediaListFragment.getInstance(mediaID));
	}
}
