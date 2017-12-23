package danbroid.mopidy.app.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Looper;
import android.os.Message;
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

	@ViewById(R.id.prev)
	protected ImageView prevButton;

	@ViewById(R.id.next)
	protected ImageView nextButton;

	@ViewById(R.id.play_pause)
	protected ImageView playButton;

	@ViewById(R.id.seek_bar_container)
	protected View seekBarContainer;

	@ViewById(R.id.startText)
	protected TextView seekBarStartText;

	@ViewById(R.id.seek_bar)
	protected SeekBar seekBar;

	@ViewById(R.id.endText)
	protected TextView seekBarEndText;

	private boolean seekBarDragging = false;

	@AfterViews
	protected void init() {
		log.trace("init()");
		initPlaybackState();

		if (seekBar != null)
			seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
				int newPosition = 0;

				@Override
				public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
					log.trace("onProgressChanged(): progress: {} fromUser: {}", progress, fromUser);
					if (!fromUser) return;
					newPosition = progress;
					if (seekBarStartText != null)
						seekBarStartText.setText(DateUtils.formatElapsedTime(progress / 1000));
				}

				@Override
				public void onStartTrackingTouch(SeekBar seekBar) {
					newPosition = -1;
					seekBarDragging = true;
				}

				@Override
				public void onStopTrackingTouch(SeekBar seekBar) {
					seekBarDragging = false;
					log.error("onStopTrackingTouch() newLocation: " + newPosition);
					if (newPosition > -1) {
						playback.getConnection().getPlayback().seek(newPosition).call();
					}
				}
			});


	}


	@Click(R.id.next)
	public void next() {
		log.debug("next()");
		playback.getConnection().getPlayback().next().call();
	}

	@Click(R.id.prev)
	public void previous() {
		log.debug("previous()");
		playback.getConnection().getPlayback().previous().call();
	}

	public void onConnect() {
		log.trace("onConnect()");
		initPlaybackState();
	}

	protected void initPlaybackState() {
		log.trace("initPlaybackState()");


		TlTrack track = playback.getTlTrack();
		if (track != null) displayTrack(track);

		onMuteChanged(playback.getMute());
		onVolumeChanged(playback.getVolume());
		onPlaybackStateChanged(null, playback.getState());
		setPaused(PlaybackState.PAUSED == playback.getState());
		displayPosition(playback.getTimePosition());

		if (PlaybackState.PLAYING == playback.getState()) {
			startPositionUpdates();
		}

	}

	private static final int MSG_UPDATE_POSITION = 1001;
	private final android.os.Handler handler = new android.os.Handler(Looper.getMainLooper()) {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case MSG_UPDATE_POSITION:
					updatePosition();
					break;
			}
		}
	};

	protected void startPositionUpdates() {
		handler.removeMessages(MSG_UPDATE_POSITION);
		handler.sendEmptyMessageDelayed(MSG_UPDATE_POSITION, 1000);
	}

	protected void updatePosition() {
		log.trace("updatePosition()");
		getConnection().getPlayback().getTimePosition().call(new UIResponseHandler<Long>() {
			@Override
			public void onUIResponse(CallContext context, Long result) {
				if (getActivity() == null || !isResumed()) return;
				if (playback.getState() != PlaybackState.PLAYING) return;

				displayPosition(result);
				handler.sendEmptyMessageDelayed(MSG_UPDATE_POSITION, 1000);
			}
		});
	}

	protected void displayTrack(TlTrack tlTrack) {
		log.debug("displayTrack(): {}", tlTrack);
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
		initPlaybackState();

	}

	@Override
	public void onPause() {
		super.onPause();
		playback.removeListener(this);
		LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(broadcastReceiver);
		handler.removeMessages(MSG_UPDATE_POSITION);
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
		if (seekBar == null || seekBarDragging) return;


		seekBarStartText.setText(DateUtils.formatElapsedTime(position / 1000));
		seekBar.setProgress((int) position);
	}

	@Override
	public void onStreamTitleChanged(String title) {
	}

	@Override
	public void onTrackPlaybackPaused(JsonObject tl_track, long time_position) {
		setPaused(true);
		displayPosition(time_position);
		handler.removeMessages(MSG_UPDATE_POSITION);
	}

	@Override
	public void onTrackPlaybackResumed(JsonObject tl_track, long time_position) {
		setPaused(false);
		displayPosition(time_position);
		startPositionUpdates();
	}

	@Override
	public void onTrackPlaybackStarted(JsonObject tl_track) {
		displayTrack(getConnection().getGson().fromJson(tl_track, TlTrack.class));
	}

	@Override
	public void onTrackPlaybackEnded(JsonObject tl_track, long time_position) {
		handler.removeMessages(MSG_UPDATE_POSITION);
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

	protected void setPaused(boolean paused) {
		log.trace("setPaused(): {}", paused);
		playButton.setImageDrawable(getResources().getDrawable(paused ? R.drawable.ic_play : R.drawable.ic_pause));
	}
}
