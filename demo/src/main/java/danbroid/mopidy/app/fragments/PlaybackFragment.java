package danbroid.mopidy.app.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;

import com.google.gson.JsonObject;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;

import danbroid.mopidy.app.MopidyConnection;
import danbroid.mopidy.app.activities.Playback;
import danbroid.mopidy.interfaces.EventListener;
import danbroid.mopidy.interfaces.PlaybackState;

/**
 * Created by dan on 14/12/17.
 * A fragment that automatically hooks into playback events
 */
@EBean
public abstract class PlaybackFragment extends Fragment implements EventListener {

	private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			onConnect();
		}
	};

	public void onConnect() {
	}

	@Bean
	protected Playback playback;

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
	}

	@Override
	public void onStreamTitleChanged(String title) {
	}

	@Override
	public void onTrackPlaybackPaused(JsonObject tl_track, long time_position) {
	}

	@Override
	public void onTrackPlaybackResumed(JsonObject tl_track, long time_position) {
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
}
