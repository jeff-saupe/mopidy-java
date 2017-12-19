package danbroid.mopidy.app.fragments;

import android.support.v4.view.GestureDetectorCompat;
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
import danbroid.mopidy.interfaces.PlaybackState;
import danbroid.mopidy.model.TlTrack;
import danbroid.mopidy.model.Track;

/**
 * Created by dan on 11/12/17.
 */
@EFragment(R.layout.bottom_controls)
public class BottomControlsFragment extends PlaybackFragment {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(BottomControlsFragment.class);

	@ViewById(R.id.title)
	TextView titleText;


	@ViewById(R.id.description)
	TextView descriptionText;


	private boolean paused = false;


	@ViewById(R.id.chevron_up)
	View chevronUp;
	private GestureDetectorCompat gestureDetector;


	protected void init() {
		super.init();
		descriptionText.setSelected(true);
		titleText.setText("");
		descriptionText.setText("");
		playButton.setVisibility(View.INVISIBLE);
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
		getMainView().showFullControls();
	}


	@Override
	public void onTracklistChanged() {
		log.error("onTracklistChanged()");
	}


	@UiThread
	@Override
	public void onTrackPlaybackStarted(JsonObject tl_track) {
		super.onTrackPlaybackStarted(tl_track);
		//pauseButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_pause));
		displayTrack(getConnection().getGson().fromJson(tl_track, TlTrack.class));

	}

	@UiThread
	@Override
	public void onTrackPlaybackResumed(JsonObject tl_track, long time_position) {
		super.onTrackPlaybackResumed(tl_track, time_position);
		//pauseButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_pause));
		displayTrack(getConnection().getGson().fromJson(tl_track, TlTrack.class));
	}

	public void displayTrack(TlTrack tlTrack) {

		super.displayTrack(tlTrack);
		log.debug("displayTrack(): {}", tlTrack);
		if (tlTrack == null) {
			getView().setVisibility(View.INVISIBLE);
			titleText.setText("");
			descriptionText.setText("");
			playButton.setVisibility(View.INVISIBLE);
			chevronUp.setVisibility(View.INVISIBLE);
			return;
		}

		Track track = tlTrack.getTrack();
		getView().setVisibility(View.VISIBLE);
		playButton.setVisibility(View.VISIBLE);
		chevronUp.setVisibility(View.VISIBLE);
		titleText.setText(track.getName());
		String description = null;

		if (track.getArtists().length > 0) {
			description = track.getArtists()[0].getName();
		}

		if (track.getAlbum() != null) {
			description += " - " + track.getAlbum().getName();
		}
		descriptionText.setText(description);

	}


	@UiThread
	@Override
	public void onPlaybackStateChanged(PlaybackState oldState, PlaybackState newState) {
		log.error("{} -> {}", oldState, newState);
		paused = PlaybackState.PAUSED == newState;

		//pauseButton.setImageDrawable(getResources().getDrawable(paused ? R.drawable.ic_play : R.drawable.ic_pause));

	}


}
