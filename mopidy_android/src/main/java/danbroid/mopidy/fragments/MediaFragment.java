package danbroid.mopidy.fragments;

import android.support.v4.app.Fragment;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.widget.TextView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import danbroid.mopidy.interfaces.MainView;
import danbroid.mopidy.interfaces.MopidyListener;


/**
 * Created by dan on 17/11/17.
 */

@EFragment
public class MediaFragment extends Fragment implements MopidyListener {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MediaFragment.class);


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


	@Override
	public void onStart() {
		log.trace("onStart() :{}", getClass());
		super.onStart();

		getMainView().getEventManager().addListener(this);
		MediaControllerCompat controller = getController();

		if (controller != null) {
			onMetadataChanged(controller.getMetadata());
			onPlaybackStateChanged(controller.getPlaybackState());

		}

	}

	@Override
	public void onStop() {
		log.trace("onStop() :{}", getClass());
		super.onStop();
		getMainView().getEventManager().removeListener(this);
	}


	public void onPlaybackStateChanged(PlaybackStateCompat state) {
		//log.trace("onPlaybackStateChanged(): {}", state);
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


	public void onMetadataChanged(MediaMetadataCompat metadata) {

		if (metadata == null) {
			//log.trace("onMetadataChanged(): null");
			if (titleText != null) titleText.setText("");
			if (subTitleText != null) subTitleText.setText("");
			return;
		}

		MediaDescriptionCompat desc = metadata.getDescription();
		//log.trace("onMetadataChanged(): title: {} subtitle: {}", desc.getTitle(), desc.getSubtitle());

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


	@Override
	public void onServerConnected(String url) {

	}

	@Override
	public void onServerDisconnected(String url) {

	}
}
