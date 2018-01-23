package danbroid.mopidy.fragments;

import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.annotation.DrawableRes;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import danbroid.mopidy.R;


/**
 * Created by dan on 17/11/17.
 */

@EFragment
public class MediaControlsFragment extends MediaFragment {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MediaFragment.class);


	@DrawableRes
	protected int pauseImage = R.drawable.ic_media_pause_dark;

	@DrawableRes
	protected int playImage = R.drawable.ic_media_play_dark;

	@ViewById(resName = "play_pause")
	protected ImageView playPauseImage;

	@ViewById(resName = "seekbar_start_text")
	protected TextView seekStartText;

	@ViewById(resName = "seekbar_end_text")
	protected TextView seekEndText;

	@ViewById(resName = "seek_bar")
	protected SeekBar seekBar;

	@ViewById(resName = "seek_bar_container")
	protected View seekBarContainer;

	private static final int MSG_UPDATE_PROGRESS = 1;

	private final Handler handler = new Handler(new Handler.Callback() {
		@Override
		public boolean handleMessage(Message msg) {
			switch (msg.what) {
				case MSG_UPDATE_PROGRESS:
					updateProgress();
					break;
			}
			return false;
		}
	});

	protected void updateProgress() {
		if (state == null || seekBar == null) {
			return;
		}

		//log.trace("updateProgress(): {}", state);

		handler.sendEmptyMessageDelayed(MSG_UPDATE_PROGRESS, PROGRESS_UPDATE_INTERNAL);

		long currentPosition = state.getPosition();
		if (state.getState() == PlaybackStateCompat.STATE_PLAYING) {
			// Calculate the elapsed time between the last position update and now and unless
			// paused, we can assume (delta * speed) + current position is approximately the
			// latest position. This ensure that we do not repeatedly call the getPlaybackState()
			// on MediaControllerCompat.
			long timeDelta = SystemClock.elapsedRealtime() -
					state.getLastPositionUpdateTime();
			currentPosition += (int) timeDelta * state.getPlaybackSpeed();
		}


		//log.trace("setting progress to: {}", DateUtils.formatElapsedTime(currentPosition / 1000L));
		seekBar.setProgress((int) currentPosition);
	}


	private static final long PROGRESS_UPDATE_INTERNAL = 1000;
	private static final long PROGRESS_UPDATE_INITIAL_INTERVAL = 100;

	private void scheduleSeekbarUpdate() {
		if (seekBar == null || seekBarContainer == null || seekBarContainer.getVisibility() != View.VISIBLE)
			return;

		log.warn("scheduleSeekbarUpdate()");
		stopSeekbarUpdate();
		handler.sendEmptyMessageDelayed(MSG_UPDATE_PROGRESS, PROGRESS_UPDATE_INITIAL_INTERVAL);
	}

	private void stopSeekbarUpdate() {
		if (seekBar == null) return;
		handler.removeMessages(MSG_UPDATE_PROGRESS);
	}


	protected void init() {
		super.init();

		if (seekBar != null) {
			seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
				@Override
				public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
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


	@Override
	public void onStop() {
		super.onStop();
		stopSeekbarUpdate();
	}


	protected void onConnected() {
		super.onConnected();

		updateProgress();

		if (state != null && (state.getState() == PlaybackStateCompat.STATE_PLAYING ||
				state.getState() == PlaybackStateCompat.STATE_BUFFERING)) {
			scheduleSeekbarUpdate();
		}
	}

	protected void onPlaybackStateChanged(PlaybackStateCompat state) {
		super.onPlaybackStateChanged(state);

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
				if (playPauseImage != null)
					playPauseImage.setImageDrawable(getResources().getDrawable(pauseImage));
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
		super.onMetadataChanged(metadata);

		if (metadata == null) {
			if (seekStartText != null) seekStartText.setText("");
			if (seekEndText != null) seekEndText.setText("");
			if (seekBarContainer != null)
				seekBarContainer.setVisibility(View.GONE);
			return;
		}

		int duration = (int) metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION);

		if (duration == 0) {
			if (seekBarContainer != null)
				seekBarContainer.setVisibility(View.GONE);
		} else {

			if (seekBar != null)
				seekBar.setMax(duration);

			if (seekEndText != null)
				seekEndText.setText(DateUtils.formatElapsedTime(duration / 1000));

			if (seekStartText != null)
				seekStartText.setText("");
		}


	}

	@Click(resName = "play_pause")
	public void onPlayPause() {
		log.trace("onPlayPause()");
		state = getController().getPlaybackState();


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
