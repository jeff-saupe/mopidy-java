package danbroid.mopidy.service;

import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import com.google.gson.JsonObject;

import danbroid.mopidy.AndroidMopidyConnection;
import danbroid.mopidy.interfaces.EventListener;
import danbroid.mopidy.interfaces.PlaybackState;
import danbroid.mopidy.model.TlTrack;
import danbroid.mopidy.model.Track;

/**
 * Created by dan on 28/12/17.
 */
public class MopidyEventManager implements EventListener {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MopidyEventManager.class);

	private final MediaSessionCompat session;
	private final AndroidMopidyConnection connection;

	private int state;
	private float playBackSpeed = 1;
	private long position = 0;

	public MopidyEventManager(MediaSessionCompat session, AndroidMopidyConnection connection) {
		this.session = session;
		this.connection = connection;
		connection.setEventListener(this);
	}

	@Override
	public void onTrackPlaybackEnded(JsonObject tl_track, long time_position) {
		this.position = time_position;
		updateState();
	}

	@Override
	public void onTrackPlaybackResumed(JsonObject tl_track, long time_position) {
		this.position = time_position;
		updateState();
	}

	@Override
	public void onTrackPlaybackStarted(JsonObject tl_track) {
		this.position = 0;
		updateState();
		updateMetadata(tl_track);
	}

	protected void updateMetadata(JsonObject json) {
		log.trace("updateMetadata(): {}", json);
		TlTrack tlTrack = connection.getGson().fromJson(json, TlTrack.class);
		Track track = tlTrack.getTrack();
		String description = null;
		String artist = null;
		String album = null;

		if (track.getComment() != null)
			description = track.getComment();

		if (track.getArtists() != null && track.getArtists().length > 0)
			artist = track.getArtists()[0].getName();

		if (track.getAlbum() != null) {
			album = track.getAlbum().getName();
		}

		if (artist != null)
			description = (description == null) ? artist : description + " " + artist;

		if (album != null)
			description = (description == null) ? album : description + " - " + album;

		MediaMetadataCompat.Builder md = new MediaMetadataCompat.Builder()
				.putText(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE, track.getName())
				.putText(MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE, description);

		if (track.getLength() != null)
			md.putLong(MediaMetadataCompat.METADATA_KEY_DURATION, track.getLength());

		if (track.getAlbum() != null) {
			if (track.getAlbum().getImages() != null && track.getAlbum().getImages().length > 0) {
				md.putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, track.getAlbum().getImages()[0]);
			}
		}


		session.setMetadata(md.build());
	}

	@Override
	public void onTrackPlaybackPaused(JsonObject tl_track, long time_position) {
		this.position = time_position;
		updateState();
	}

	@Override
	public void onPlaybackStateChanged(PlaybackState oldState, PlaybackState newState) {
		switch (newState) {
			case PAUSED:
				state = PlaybackStateCompat.STATE_PAUSED;
				break;
			case PLAYING:
				state = PlaybackStateCompat.STATE_PLAYING;
				break;
			case STOPPED:
				state = PlaybackStateCompat.STATE_STOPPED;
				break;
			default:
				log.error("unhandled state: " + newState);
				return;
		}

		updateState();
	}

	private void updateState() {
		PlaybackStateCompat.Builder builder = new PlaybackStateCompat.Builder();

		builder.setState(state, position, playBackSpeed, System.currentTimeMillis());
		session.setPlaybackState(builder.build());
	}

	public void onOptionsChanged() {
		log.trace("onOptionsChanged()");
	}

	public void onVolumeChanged(int volume) {
		log.trace("onVolumeChanged(): {}", volume);
	}

	public void onMuteChanged(boolean mute) {
		log.trace("onMuteChanged(): {}", mute);
	}

	public void onSeeked(long time_position) {
		log.trace("onStreamSeeked(): {}", time_position);
	}

	public void onStreamTitleChanged(String title) {
		log.trace("onStreamTitleChanged(): {}", title);

	}


	public void onTracklistChanged() {
		log.trace("onTracklistChanged() ");
	}

	// Called when playlists are loaded or refreshed.
	public void onPlaylistsLoaded() {
		log.trace("onPlaylistsLoaded() ");
	}


	//    Called whenever a playlist is changed.
	public void onPlaylistChanged(JsonObject playlist) {
		log.trace("onPlaylistChanged() :{}", playlist);
	}

	//Called whenever a playlist is deleted.
	public void onPlaylistDeleted(String uri) {
		log.trace("onPLaylistDeleted(): {}", uri);
	}
}

