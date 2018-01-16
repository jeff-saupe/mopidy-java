package danbroid.mopidy.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.DrawableRes;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

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


	@DrawableRes
	protected int pauseImage = R.drawable.ic_media_pause_dark;

	@DrawableRes
	protected int playImage = R.drawable.ic_media_play_dark;

	@ViewById(resName = "title")
	protected TextView titleText;

	@ViewById(resName = "sub_title")
	protected TextView subTitleText;

	@ViewById(resName = "play_pause")
	protected ImageView playPauseImage;

	@ViewById(resName = "seekbar_start_text")

	protected TextView seekStartText;

	@ViewById(resName = "seekbar_end_text")
	protected TextView seekEndText;

	@ViewById(resName = "seek_bar")
	protected SeekBar seekBar;


	private final Runnable mUpdateProgressTask = new Runnable() {
		@Override
		public void run() {
			updateProgress();
		}
	};
	private ScheduledFuture<?> mScheduleFuture;
	private PlaybackStateCompat mLastPlaybackState;

	private void updateProgress() {
		if (mLastPlaybackState == null) {
			return;
		}
		long currentPosition = mLastPlaybackState.getPosition();
		if (mLastPlaybackState.getState() == PlaybackStateCompat.STATE_PLAYING) {
			// Calculate the elapsed time between the last position update and now and unless
			// paused, we can assume (delta * speed) + current position is approximately the
			// latest position. This ensure that we do not repeatedly call the getPlaybackState()
			// on MediaControllerCompat.
			long timeDelta = SystemClock.elapsedRealtime() -
					mLastPlaybackState.getLastPositionUpdateTime();
			currentPosition += (int) timeDelta * mLastPlaybackState.getPlaybackSpeed();
		}

		seekBar.setProgress((int) currentPosition);
	}

	private final ScheduledExecutorService mExecutorService =
			Executors.newSingleThreadScheduledExecutor();
	private final Handler mHandler = new Handler();
	private static final long PROGRESS_UPDATE_INTERNAL = 1000;
	private static final long PROGRESS_UPDATE_INITIAL_INTERVAL = 100;

	private void scheduleSeekbarUpdate() {
		if (seekBar == null) return;
		stopSeekbarUpdate();
		if (!mExecutorService.isShutdown()) {
			log.error("scehduiling update");
			mScheduleFuture = mExecutorService.scheduleAtFixedRate(
					new Runnable() {
						@Override
						public void run() {
							mHandler.post(mUpdateProgressTask);
						}
					}, PROGRESS_UPDATE_INITIAL_INTERVAL,
					PROGRESS_UPDATE_INTERNAL, TimeUnit.MILLISECONDS);
		}
	}

	private void stopSeekbarUpdate() {
		if (mScheduleFuture != null) {
			mScheduleFuture.cancel(false);
		}
	}

	@AfterViews
	protected void init() {
		if (titleText != null) titleText.setText("");
		if (subTitleText != null) subTitleText.setText("");
		if (playPauseImage != null)
			playPauseImage.setVisibility(View.INVISIBLE);


		if (seekBar != null) {
			seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
				@Override
				public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
					log.trace("onProgressChanged(): " + progress);
					if (seekStartText != null)
						seekStartText.setText(DateUtils.formatElapsedTime(progress / 1000));
				}

				@Override
				public void onStartTrackingTouch(SeekBar seekBar) {
					stopSeekbarUpdate();
				}

				@Override
				public void onStopTrackingTouch(SeekBar seekBar) {
					getController().getTransportControls().seekTo(seekBar.getProgress());
					//scheduleSeekbarUpdate();

				}
			});
		}

	}


	@Click(resName = "skip_next")
	public void onSkipNext() {
		log.trace("onSkipNext()");
		getController().getTransportControls().skipToNext();
	}

	@Click(resName = "skip_prev")
	public void onSkipPrev() {
		log.trace("onSkipPrev()");
		getController().getTransportControls().skipToPrevious();
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


	protected void onConnected() {
		log.trace("onConnected()");
		MediaControllerCompat controller = getController();
		controller.registerCallback(callback);

		MediaMetadataCompat metadata = controller.getMetadata();
		onMetadataChanged(metadata);

		PlaybackStateCompat state = controller.getPlaybackState();
		onPlaybackStateChanged(state);

		updateProgress();

		if (state != null && (state.getState() == PlaybackStateCompat.STATE_PLAYING ||
				state.getState() == PlaybackStateCompat.STATE_BUFFERING)) {
			scheduleSeekbarUpdate();
		}
	}

	protected void onPlaybackStateChanged(PlaybackStateCompat state) {
		log.trace("onPlaybackStateChanged(): {}", state);
		this.mLastPlaybackState = state;

		if (state == null) {
			if (playPauseImage != null)
				playPauseImage.setVisibility(View.INVISIBLE);
			return;
		}

		if (playPauseImage != null)
			playPauseImage.setVisibility(View.VISIBLE);

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

		switch (state.getState()) {
			case PlaybackStateCompat.STATE_PLAYING:

				scheduleSeekbarUpdate();
				break;
			case PlaybackStateCompat.STATE_PAUSED:
				if (playPauseImage != null)
					playPauseImage.setImageDrawable(getResources().getDrawable(playImage));
				stopSeekbarUpdate();
				break;
			case PlaybackStateCompat.STATE_NONE:
			case PlaybackStateCompat.STATE_STOPPED:
				stopSeekbarUpdate();
				break;
			case PlaybackStateCompat.STATE_BUFFERING:
				stopSeekbarUpdate();
				break;
		}
	}


	protected void onMetadataChanged(MediaMetadataCompat metadata) {

		if (metadata == null) {
			log.trace("onMetadataChanged(): null");
			if (titleText != null) titleText.setText("");
			if (subTitleText != null) subTitleText.setText("");
			if (seekStartText != null) seekStartText.setText("");
			if (seekEndText != null) seekEndText.setText("");
			return;
		}

		MediaDescriptionCompat desc = metadata.getDescription();
		log.trace("onMetadataChanged(): title: {} subtitle: {}", desc.getTitle(), desc.getSubtitle());

		if (titleText != null)
			titleText.setText(desc.getTitle());

		if (subTitleText != null)
			subTitleText.setText(desc.getSubtitle());

		int duration = (int) metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION);


		if (seekBar != null)
			seekBar.setMax(duration);

		if (seekEndText != null)
			seekEndText.setText(DateUtils.formatElapsedTime(duration / 1000));

	}

	public MainView getMainView() {
		return (MainView) getActivity();
	}

	protected MediaControllerCompat getController() {
		return MediaControllerCompat.getMediaController(getActivity());
	}

	@Click(resName = "play_pause")
	public void onPlayPause() {
		log.trace("onPlayPause()");
		PlaybackStateCompat state = getController().getPlaybackState();
		if (state == null) return;

		switch (state.getState()) {
			case PlaybackStateCompat.STATE_BUFFERING:
			case PlaybackStateCompat.STATE_PLAYING:
				stopSeekbarUpdate();
				getController().getTransportControls().pause();
				break;
			case PlaybackStateCompat.STATE_PAUSED:
			case PlaybackStateCompat.STATE_STOPPED:
				getController().getTransportControls().play();
				scheduleSeekbarUpdate();
				break;
		}
	}


}
