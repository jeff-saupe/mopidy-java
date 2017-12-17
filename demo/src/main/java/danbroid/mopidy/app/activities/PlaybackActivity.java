package danbroid.mopidy.app.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;

import com.google.gson.JsonObject;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;

import danbroid.mopidy.app.MopidyConnection;
import danbroid.mopidy.interfaces.EventListener;
import danbroid.mopidy.interfaces.PlaybackState;

/**
 * Created by dan on 14/12/17.
 */
@EActivity
public abstract class PlaybackActivity extends AppCompatActivity implements EventListener {

	@Bean
	protected Playback playback;

	private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			onConnect();
		}
	};

	public void onConnect() {
	}

	@Override
	public void onResume() {
		super.onResume();
		playback.addListener(this);
		LocalBroadcastManager.getInstance(this)
				.registerReceiver(broadcastReceiver, new IntentFilter(MopidyConnection.INTENT_SERVER_CONNECTED));
	}

	@Override
	public void onPause() {
		super.onPause();
		playback.removeListener(this);
		LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
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
