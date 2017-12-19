package danbroid.mopidy.app.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.gson.JsonObject;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.ViewById;

import danbroid.mopidy.app.MopidyConnection;
import danbroid.mopidy.app.R;
import danbroid.mopidy.app.activities.Playback;
import danbroid.mopidy.app.interfaces.MainView;
import danbroid.mopidy.interfaces.CallContext;
import danbroid.mopidy.interfaces.EventListener;
import danbroid.mopidy.interfaces.PlaybackState;
import danbroid.mopidy.model.TlTrack;
import danbroid.mopidy.util.UIResponseHandler;

/**
 * Created by dan on 14/12/17.
 * A fragment that automatically hooks into playback events
 */
@EBean
public abstract class PlaybackFragment extends Fragment implements EventListener {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PlaybackFragment.class);

	private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			onConnect();
		}
	};
	@Bean
	protected Playback playback;

	@ViewById(R.id.play_pause)
	protected ImageView playButton;


	@ViewById(R.id.seek_bar_container)
	View seekBarContainer;

	@ViewById(R.id.startText)
	TextView seekBarStartText;

	@ViewById(R.id.seek_bar)
	SeekBar seekBar;

	@ViewById(R.id.endText)
	TextView seekBarEndText;

	@AfterViews
	protected void init() {
		log.trace("init()");
		initPlaybackState();
	}

	public void onConnect() {
		log.trace("onConnect()");
		initPlaybackState();
	}

	protected void initPlaybackState() {
		log.trace("initPlaybackState()");

		getConnection().getPlayback().getCurrentTlTrack(new UIResponseHandler<TlTrack>() {
			@Override
			public void onUIResponse(CallContext context, TlTrack result) {
				displayTrack(result);
			}
		});

		getConnection().getPlayback().getState(new UIResponseHandler<PlaybackState>() {
			@Override
			public void onUIResponse(CallContext context, PlaybackState result) {
				onPlaybackStateChanged(null, result);
			}
		});
	}

	protected void displayTrack(TlTrack tlTrack) {
		log.error("displayTrack(): {}", tlTrack);
		if (tlTrack == null) return;

		if (seekBarContainer != null) {
			Long length = tlTrack.getTrack().getLength();
			if (length == null) {
				seekBarContainer.setVisibility(View.INVISIBLE);
			} else {
				initSeekBar(length);
			}

		}
	}

	protected void initSeekBar(long length) {
		log.trace("initSeekBar(): length: " + length);
		seekBarContainer.setVisibility(View.VISIBLE);
		if (seekBarEndText != null)
			seekBarEndText.setText(DateUtils.formatElapsedTime(length / 1000));

		if (seekBar != null)
			seekBar.setMax((int) length);

		displayPosition(0);


	}

	@Click(R.id.play_pause)
	public void togglePlay() {
		playback.togglePlay();
	}

	@Override
	public void onResume() {
		super.onResume();
		playback.addListener(this);
		LocalBroadcastManager.getInstance(getContext())
				.registerReceiver(broadcastReceiver, new IntentFilter(MopidyConnection.INTENT_SERVER_CONNECTED));
	}

	@Override
	public void onPause() {
		super.onPause();
		playback.removeListener(this);
		LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(broadcastReceiver);

	}


	protected MopidyConnection getConnection() {
		return playback.getConnection();
	}

	@Override
	public void onOptionsChanged() {
	}

	@Override
	public void onVolumeChanged(int volume) {
	}

	@Override
	public void onMuteChanged(boolean mute) {
	}

	@Override
	public void onSeeked(long time_position) {
		displayPosition(time_position);
	}

	protected void displayPosition(long position) {
		if (seekBarStartText != null)
			seekBarStartText.setText(DateUtils.formatElapsedTime(position / 1000));

		if (seekBar != null)
			seekBar.setProgress((int) position);
	}

	@Override
	public void onStreamTitleChanged(String title) {
	}

	@Override
	public void onTrackPlaybackPaused(JsonObject tl_track, long time_position) {
		if (playButton != null)
			playButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_play));
		displayPosition(time_position);

	}

	@Override
	public void onTrackPlaybackResumed(JsonObject tl_track, long time_position) {
		if (playButton != null)
			playButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_pause));
		displayPosition(time_position);
	}

	@Override
	public void onTrackPlaybackStarted(JsonObject tl_track) {
	}

	@Override
	public void onTrackPlaybackEnded(JsonObject tl_track, long time_position) {
	}

	@Override
	public void onPlaybackStateChanged(PlaybackState oldState, PlaybackState newState) {
	}

	@Override
	public void onTracklistChanged() {
	}

	@Override
	public void onPlaylistsLoaded() {
	}

	@Override
	public void onPlaylistChanged(JsonObject playlist) {
	}

	@Override
	public void onPlaylistDeleted(String uri) {
	}

	public MainView getMainView() {
		return (MainView) getActivity();
	}
}
