package danbroid.mopidy.app.fragments;

import android.support.v4.view.GestureDetectorCompat;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.google.gson.JsonObject;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import danbroid.mopidy.app.R;
import danbroid.mopidy.app.util.FlingDetector;
import danbroid.mopidy.fragments.MediaFragment;
import danbroid.mopidy.interfaces.PlaybackState;
import danbroid.mopidy.model.TlTrack;
import danbroid.mopidy.model.Track;

/**
 * Created by dan on 11/12/17.
 */
@EFragment(R.layout.bottom_controls)
public class BottomControlsFragment extends MediaFragment {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(BottomControlsFragment.class);

	@ViewById(R.id.title)
	TextView titleText;


	@ViewById(R.id.description)
	TextView descriptionText;



	@ViewById(R.id.chevron_up)
	View chevronUp;

	private GestureDetectorCompat gestureDetector;


	protected void init() {
		descriptionText.setSelected(true);
		titleText.setText("");
		descriptionText.setText("");
		//playButton.setVisibility(View.INVISIBLE);
		chevronUp.setVisibility(View.INVISIBLE);

		getView().setOnTouchListener(new FlingDetector(getContext()) {
			@Override
			protected boolean onFlingUp(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
				showFullControls();
				return true;
			}
		});
		getView().setVisibility(View.INVISIBLE);


	}

	@Click(R.id.chevron_up)
	protected void showFullControls() {
		log.trace("showFullControls()");
	//TODO	getMainView().showFullControls();
	}




}
