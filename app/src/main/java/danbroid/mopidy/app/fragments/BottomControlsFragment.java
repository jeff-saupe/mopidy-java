package danbroid.mopidy.app.fragments;

import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v4.view.GestureDetectorCompat;
import android.view.MotionEvent;
import android.view.View;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import danbroid.mopidy.app.R;
import danbroid.mopidy.app.util.FlingDetector;
import danbroid.mopidy.fragments.MediaFragment;

/**
 * Created by dan on 11/12/17.
 */
@EFragment(R.layout.bottom_controls)
public class BottomControlsFragment extends MediaFragment {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(BottomControlsFragment.class);


	@ViewById(R.id.chevron_up)
	View chevronUp;

	private GestureDetectorCompat gestureDetector;


	protected void init() {
		super.init();

		chevronUp.setVisibility(View.INVISIBLE);

		getView().setOnTouchListener(new FlingDetector(getContext()) {
			@Override
			protected boolean onFlingUp(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
				showFullControls();
				return true;
			}
		});


	}


	@Override
	protected void onPlaybackStateChanged(PlaybackStateCompat state) {
		super.onPlaybackStateChanged(state);
		log.error("onPlaybackStateChanged(): {}", state);
	}

	@Override
	protected void onMetadataChanged(MediaMetadataCompat metadata) {
		super.onMetadataChanged(metadata);
		descriptionText.setSelected(true);
	}

	@Click(R.id.chevron_up)
	protected void showFullControls() {
		log.trace("showFullControls()");
		((danbroid.mopidy.app.interfaces.MainView) getMainView()).showFullControls();
	}

}
