package danbroid.mopidy.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.widget.TextView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import danbroid.mopidy.interfaces.MainView;
import danbroid.mopidy.service.MopidyBackend;


/**
 * Created by dan on 17/11/17.
 */

@EFragment
public class MediaFragment extends Fragment {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MediaFragment.class);

	private final MediaControllerCompat.Callback callback = createCallback();


	@ViewById(resName = "title")
	protected TextView titleText;

	@ViewById(resName = "sub_title")
	protected TextView subTitleText;


	protected PlaybackStateCompat state;


	@AfterViews
	protected void init() {
		if (titleText != null) titleText.setText("");
		if (subTitleText != null) subTitleText.setText("");
	}


	protected MediaControllerCompat.Callback createCallback() {
		return new MediaControllerCompat.Callback() {

			@Override
			public void onSessionEvent(final String event, Bundle extras) {
				log.trace("onSessionEvent(): {}", event);

				switch (event) {
					case MopidyBackend.SESSION_EVENT_NOT_BUSY:
						onBusy(false);
						break;
					case MopidyBackend.SESSION_EVENT_BUSY:
						onBusy(true);
						break;
					case MopidyBackend.SESSION_EVENT_CONNECTED:
						onMopidyConnected();
						break;
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

	protected void onMopidyConnected() {
		log.warn("onMopidyConnected()");
	}

	protected void onBusy(boolean busy) {
		log.error("onBusy(): {}", busy);
	}

	private BroadcastReceiver broadcastReceiver;

	@Override
	public void onStart() {
		log.trace("onStart() :{}", getClass());
		super.onStart();

		if (getController() != null) {
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


	protected void onConnected() {
		log.trace("onConnected(): {}",getClass().getSimpleName());
		MediaControllerCompat controller = getController();
		controller.registerCallback(callback);

		MediaMetadataCompat metadata = controller.getMetadata();
		onMetadataChanged(metadata);

		state = controller.getPlaybackState();
		onPlaybackStateChanged(state);
	}

	protected void onPlaybackStateChanged(PlaybackStateCompat state) {
		log.trace("onPlaybackStateChanged(): {}", state);
		this.state = state;


/*		switch (state.getState()) {
			case PlaybackStateCompat.STATE_PLAYING:
				mLoading.setVisibility(INVISIBLE);
				mPlayPause.setVisibility(VISIBLE);
				mPlayPause.setImageDrawable(mPauseDrawable);
				mControllers.setVisibility(VISIBLE);
				scheduleSeekbarUpdate();
				break;
			case PlaybackStateCompat.STATE_PAUSED:
				mControllers.setVisibility(VISIBLE);
				mLoading.setVisibility(INVISIBLE);
				mPlayPause.setVisibility(VISIBLE);
				mPlayPause.setImageDrawable(mPlayDrawable);
				stopSeekbarUpdate();
				break;
			case PlaybackStateCompat.STATE_NONE:
			case PlaybackStateCompat.STATE_STOPPED:
				mLoading.setVisibility(INVISIBLE);
				mPlayPause.setVisibility(VISIBLE);
				mPlayPause.setImageDrawable(mPlayDrawable);
				stopSeekbarUpdate();
				break;
			case PlaybackStateCompat.STATE_BUFFERING:
				mPlayPause.setVisibility(INVISIBLE);
				mLoading.setVisibility(VISIBLE);
				mLine3.setText(R.string.loading);
				stopSeekbarUpdate();
				break;
			default:
				LogHelper.d(TAG, "Unhandled state ", state.getState());
		}*/

	}


	protected void onMetadataChanged(MediaMetadataCompat metadata) {

		if (metadata == null) {
			log.trace("onMetadataChanged(): null");
			if (titleText != null) titleText.setText("");
			if (subTitleText != null) subTitleText.setText("");
			return;
		}

		MediaDescriptionCompat desc = metadata.getDescription();
		log.trace("onMetadataChanged(): title: {} subtitle: {}", desc.getTitle(), desc.getSubtitle());

		if (titleText != null)
			titleText.setText(desc.getTitle());

		if (subTitleText != null)
			subTitleText.setText(desc.getSubtitle());

	}

	public MainView getMainView() {
		return (MainView) getActivity();
	}

	protected MediaControllerCompat getController() {
		return MediaControllerCompat.getMediaController(getActivity());
	}



}
