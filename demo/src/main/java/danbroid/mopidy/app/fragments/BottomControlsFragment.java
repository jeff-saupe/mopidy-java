package danbroid.mopidy.app.fragments;

import android.support.v4.view.GestureDetectorCompat;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonObject;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import danbroid.mopidy.app.MopidyConnection;
import danbroid.mopidy.app.R;
import danbroid.mopidy.app.interfaces.MainView;
import danbroid.mopidy.interfaces.CallContext;
import danbroid.mopidy.interfaces.PlaybackState;
import danbroid.mopidy.model.TlTrack;
import danbroid.mopidy.model.Track;
import danbroid.mopidy.util.UIResponseHandler;

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

	@Bean
	MopidyConnection conn;

	private boolean paused = false;

	@ViewById(R.id.pause_button)
	ImageView pauseButton;

	@ViewById(R.id.chevron_up)
	View chevronUp;
	private GestureDetectorCompat gestureDetector;


	@AfterViews
	void init() {
		descriptionText.setSelected(true);
		titleText.setText("");
		descriptionText.setText("");
		pauseButton.setVisibility(View.INVISIBLE);
		chevronUp.setVisibility(View.INVISIBLE);


		gestureDetector = new GestureDetectorCompat(getContext(), new GestureDetector.OnGestureListener() {
			@Override
			public boolean onDown(MotionEvent e) {
				return false;
			}

			@Override
			public void onShowPress(MotionEvent e) {

			}

			@Override
			public boolean onSingleTapUp(MotionEvent e) {
				return false;
			}

			@Override
			public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
				return false;
			}

			@Override
			public void onLongPress(MotionEvent e) {

			}

			@Override
			public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
				log.error("onFling(): {} -> {}", velocityX, velocityY);
				if (Math.abs(velocityX) < Math.abs(velocityY) && velocityY < -500)
					showFullControls();

				return false;
			}
		});

		getView().setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				gestureDetector.onTouchEvent(event);
				return false;
			}
		});
	}

	@Click(R.id.chevron_up)
	protected void showFullControls() {
		log.trace("showFullControls()");
		((MainView) getActivity()).showFullControls();
	}


	@Override
	public void onConnect() {
		log.info("onConnect()");
		Toast.makeText(getContext(), "Connected", Toast.LENGTH_SHORT).show();

		conn.getPlayback().getCurrentTlTrack(new UIResponseHandler<TlTrack>() {
			@Override
			public void onUIResponse(CallContext context, TlTrack result) {
				displayTrack(result);
			}
		});

		conn.getPlayback().getState(new UIResponseHandler<PlaybackState>() {
			@Override
			public void onUIResponse(CallContext context, PlaybackState result) {
				onPlaybackStateChanged(null, result);
			}
		});


	}

	@Override
	public void onTracklistChanged() {
		log.error("onTracklistChanged()");
	}

	@UiThread
	@Override
	public void onTrackPlaybackPaused(JsonObject tl_track, long time_position) {
		log.trace("onTrackPlaybackPause(): {} pos: {}", tl_track, time_position);

		//pauseButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_play));
	}

	@UiThread
	@Override
	public void onTrackPlaybackStarted(JsonObject tl_track) {
		//pauseButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_pause));
		displayTrack(conn.getGson().fromJson(tl_track, TlTrack.class));

	}

	@UiThread
	@Override
	public void onTrackPlaybackResumed(JsonObject tl_track, long time_position) {
		super.onTrackPlaybackResumed(tl_track, time_position);
		//pauseButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_pause));
		displayTrack(conn.getGson().fromJson(tl_track, TlTrack.class));
	}

	public void displayTrack(TlTrack tlTrack) {
		log.debug("displayTrack(): {}", tlTrack);
		if (tlTrack == null) {
			titleText.setText("");
			descriptionText.setText("");
			pauseButton.setVisibility(View.INVISIBLE);
			chevronUp.setVisibility(View.INVISIBLE);
			return;
		}

		pauseButton.setVisibility(View.VISIBLE);
		chevronUp.setVisibility(View.VISIBLE);
		Track track = tlTrack.getTrack();
		titleText.setText(track.getName());
		String description = null;

		if (track.getArtists().length > 0) {
			description = track.getArtists()[0].name;
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

		pauseButton.setImageDrawable(getResources().getDrawable(paused ? R.drawable.ic_play : R.drawable.ic_pause));

	}

	@Click(R.id.pause_button)
	void pauseClicked() {
		log.trace("pauseClicked() paused: " + paused);
		if (paused) conn.getPlayback().play();
		else conn.getPlayback().pause();
	}
}
