package danbroid.mopidy.app.activities;

import android.support.v7.app.AppCompatActivity;

import com.google.gson.JsonObject;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;

import danbroid.mopidy.interfaces.EventListener;
import danbroid.mopidy.interfaces.PlaybackState;

/**
 * Created by dan on 14/12/17.
 */
@EActivity
public class PlaybackActivity extends AppCompatActivity implements EventListener {

	@Bean
	protected Playback playback;

	@Override
	public void onResume() {
		super.onResume();
		playback.addListener(this);
	}

	@Override
	public void onPause() {
		super.onPause();
		playback.removeListener(this);
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
