package danbroid.mopidy.app.fragments;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.JsonObject;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import danbroid.mopidy.app.MopidyConnection;
import danbroid.mopidy.app.R;
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


	@ViewById(R.id.pause_button)
	ImageView pauseButton;

	@ViewById(R.id.chevron_up)
	View chevronUp;


	@AfterViews
	void init() {
		descriptionText.setSelected(true);
		titleText.setText("");
		descriptionText.setText("");
		pauseButton.setVisibility(View.INVISIBLE);
		chevronUp.setVisibility(View.INVISIBLE);
		conn.getPlayback().getCurrentTlTrack(new UIResponseHandler<TlTrack>() {
			@Override
			protected void onUIResponse(CallContext context, TlTrack result) {
				displayTrack(result);
			}
		});
	}

	@UiThread
	@Override
	public void onTrackPlaybackPaused(JsonObject tl_track, long time_position) {
		log.trace("onTrackPlaybackPause(): {} pos: {}", tl_track, time_position);

		pauseButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_play));
	}

	@UiThread
	@Override
	public void onTrackPlaybackStarted(JsonObject tl_track) {
		pauseButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_pause));
		displayTrack(conn.getGson().fromJson(tl_track, TlTrack.class));

	}

	@UiThread
	@Override
	public void onTrackPlaybackResumed(JsonObject tl_track, long time_position) {
		super.onTrackPlaybackResumed(tl_track, time_position);
		pauseButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_pause));
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

	}

	@Click(R.id.pause_button)
	void pauseClicked() {
		boolean paused = PlaybackState.PAUSED.equals(playback.getState());
		log.trace("pauseClicked() paused: " + paused);
		if (paused) conn.getPlayback().play();
		else conn.getPlayback().pause();
	}
}
