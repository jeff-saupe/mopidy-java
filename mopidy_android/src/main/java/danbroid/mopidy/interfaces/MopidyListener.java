package danbroid.mopidy.interfaces;

import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.PlaybackStateCompat;

/**
 * Created by dan on 2/02/18.
 */
public interface MopidyListener {
	void onServerConnected(String url);

	void onServerDisconnected(String url);

	void onMetadataChanged(MediaMetadataCompat metadata);

	void onPlaybackStateChanged(PlaybackStateCompat state);
}
