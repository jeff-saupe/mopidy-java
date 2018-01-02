package danbroid.mopidy.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import danbroid.mopidy.R;
import danbroid.mopidy.interfaces.MainView;
import danbroid.mopidy.service.MopidyBackend;


/**
 * Created by dan on 17/11/17.
 */

@EFragment
public class MediaFragment extends Fragment {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MediaFragment.class);


	private final MediaControllerCompat.Callback callback = createCallback();

	protected PlaybackStateCompat state;

	@DrawableRes
	protected int pauseImage = R.drawable.ic_media_pause_dark;

	@DrawableRes
	protected int playImage = R.drawable.ic_media_play_dark;

	@ViewById(resName = "title")
	protected TextView titleText;

	@ViewById(resName = "description")
	protected TextView descriptionText;

	@ViewById(resName = "play_pause")
	protected ImageView playPauseImage;

	@AfterViews
	protected void init() {
		if (titleText != null) titleText.setText("");
		if (descriptionText != null) descriptionText.setText("");
		if (playPauseImage != null)
			playPauseImage.setVisibility(View.INVISIBLE);
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
		//log.trace("onPlaybackStateChanged(): {}", state);

		if (state == null) {
			if (playPauseImage != null)

				playPauseImage.setVisibility(View.INVISIBLE);
			return;
		}

		if (playPauseImage != null)

			playPauseImage.setVisibility(View.VISIBLE);
		this.state = state;

		switch (state.getState()) {
			case PlaybackStateCompat.STATE_PAUSED:
				if (playPauseImage != null)
					playPauseImage.setImageDrawable(getResources().getDrawable(playImage));
				break;
			default:
				if (playPauseImage != null)
					playPauseImage.setImageDrawable(getResources().getDrawable(pauseImage));
				break;
		}
	}

	protected void onMetadataChanged(MediaMetadataCompat metadata) {
		log.trace("onMetadataChanged(): {}", metadata);
		if (metadata == null) {
			if (titleText != null) titleText.setText("");
			if (descriptionText != null) descriptionText.setText("");
			return;
		}

		MediaDescriptionCompat desc = metadata.getDescription();

		if (titleText != null)
			titleText.setText(desc.getTitle());
		if (descriptionText != null) {
			descriptionText.setText(desc.getSubtitle());
		}

	}

	protected MediaControllerCompat getController() {
		return MediaControllerCompat.getMediaController(getActivity());
	}

	@Click(resName = "play_pause")
	public void onPlayPause(View view) {
		if (state != null && PlaybackStateCompat.STATE_PAUSED == state.getState()) {
			log.trace("calling play");
			getController().getTransportControls().play();
		} else {
			log.trace("calling pause");
			getController().getTransportControls().pause();
		}
	}
}
