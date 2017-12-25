package danbroid.mopidy.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import danbroid.mopidy.interfaces.MainView;
import danbroid.mopidy.service.AbstractMopidyService;


/**
 * Created by dan on 17/11/17.
 */

public class MediaFragment extends Fragment {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MediaFragment.class);


	private final MediaControllerCompat.Callback callback = createCallback();

	protected MediaControllerCompat.Callback createCallback() {
		return new MediaControllerCompat.Callback() {

			@Override
			public void onSessionEvent(String event, Bundle extras) {
				log.trace("onSessionEvent(): {}", event);
				if (event.equals(AbstractMopidyService.SESSION_EVENT_BUSY)) {
					onBusy(true);
				} else if (event.equals(AbstractMopidyService.SESSION_EVENT_NOT_BUSY)) {
					onBusy(false);
				}
			}

			@Override
			public void onMetadataChanged(MediaMetadataCompat metadata) {
				MediaFragment.this.onMetadataChanged(metadata);
			}

			@Override
			public void onPlaybackStateChanged(PlaybackStateCompat state) {
				MediaFragment.this.onPlaybackStateChanged(state);
			}
		};
	}

	protected void onBusy(boolean busy) {
		log.error("onBusy(): {}", busy);
	}

	private BroadcastReceiver broadcastReceiver;

	@Override
	public void onStart() {
		log.trace("onStart() :{}", getClass());
		super.onStart();
		MediaControllerCompat controller = getController();
		if (controller != null) {
			onConnected();
		} else {
			log.warn("controller is null");
			broadcastReceiver = new BroadcastReceiver() {
				@Override
				public void onReceive(Context context, Intent intent) {
					log.trace("onReceive(): {}", intent);
					if (intent.getAction().equals(MainView.ACTION_CONTROLLER_CONNECTED)) {
						onConnected();
					}
				}
			};
			LocalBroadcastManager.getInstance(getActivity())
					.registerReceiver(broadcastReceiver, new IntentFilter(MainView.ACTION_CONTROLLER_CONNECTED));
		}
	}

	@Override
	public void onStop() {
		log.trace("onStop() :{}", getClass());
		super.onStop();
		MediaControllerCompat controller = getController();
		if (controller != null)
			controller.unregisterCallback(callback);

		if (broadcastReceiver != null) {
			LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(broadcastReceiver);
			broadcastReceiver = null;
		}
	}


	protected MainView getMainView() {
		return (MainView) getActivity();
	}

	protected void onConnected() {
		log.trace("onConnected()");
		MediaControllerCompat controller = getController();
		onMetadataChanged(controller.getMetadata());
		onPlaybackStateChanged(controller.getPlaybackState());
		controller.registerCallback(callback);
	}

	protected void onPlaybackStateChanged(PlaybackStateCompat state) {
		log.trace("onPlaybackStateChanged(): {}",state);
	}

	protected void onMetadataChanged(MediaMetadataCompat metadata) {
		log.trace("onMetadataChanged(): {}",metadata);
	}

	protected MediaControllerCompat getController() {
		return MediaControllerCompat.getMediaController(getActivity());
	}
}
