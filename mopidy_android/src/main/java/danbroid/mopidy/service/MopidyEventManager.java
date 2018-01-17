package danbroid.mopidy.service;

import android.support.v4.media.MediaBrowserServiceCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import com.google.gson.JsonObject;

import danbroid.mopidy.AndroidMopidyConnection;
import danbroid.mopidy.interfaces.EventListener;
import danbroid.mopidy.interfaces.PlaybackState;
import danbroid.mopidy.model.Artist;
import danbroid.mopidy.model.TlTrack;
import danbroid.mopidy.model.Track;
import danbroid.mopidy.util.MediaIds;

/**
 * Created by dan on 28/12/17.
 */
public class MopidyEventManager implements EventListener {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MopidyEventManager.class);

	private final MediaSessionCompat session;
	private final AndroidMopidyConnection connection;
	private final MediaBrowserServiceCompat service;

	private int state;
	private float playBackSpeed = 1;
	private long position = 0;
	private MediaMetadataCompat metadata;

	public MopidyEventManager(MediaSessionCompat session, AndroidMopidyConnection connection, MediaBrowserServiceCompat service) {
		this.session = session;
		this.connection = connection;
		this.service = service;
		connection.setEventListener(this);
	}

	@Override
	public void onTrackPlaybackEnded(JsonObject tl_track, long time_position) {
		log.trace("onPLaybackEnded(): pos:{}", time_position);
		this.position = time_position;
		updateState();
	}

	@Override
	public void onTrackPlaybackResumed(JsonObject tl_track, long time_position) {
		log.trace("onTrackPlaybackResumed(): pos:{}", time_position);
		this.position = time_position;
		updateState();
	}

	@Override
	public void onTrackPlaybackStarted(JsonObject tl_track) {
		log.trace("onTrackPlaybackStarted(): pos:{}", 0);
		this.position = 0;
		updateMetadata(tl_track);
		updateState();
	}

	@Override
	public void onTrackPlaybackPaused(JsonObject tl_track, long time_position) {
		log.warn("onTrackPlaybackPaused(): {} pos: {}", tl_track, time_position);
		this.position = time_position;
		updateState();
	}


	protected void updateMetadata(JsonObject json) {
		log.trace("updateMetadata(): {}", json);
		TlTrack tlTrack = connection.getGson().fromJson(json, TlTrack.class);
		Track track = tlTrack.getTrack();
		String description = null;
		String artist = null;
		String album = null;
		String comment = null;

		if (track.getComment() != null)
			comment = track.getComment();

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

		if (comment != null)
			md.putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_DESCRIPTION, comment);

		if (track.getLength() != null)
			md.putLong(MediaMetadataCompat.METADATA_KEY_DURATION, track.getLength());

		log.error("DURATION IS " + track.getLength());

		if (track.getAlbum() != null) {
			if (track.getAlbum().getImages() != null && track.getAlbum().getImages().length > 0) {
				md.putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, track.getAlbum().getImages()[0]);
			}
			md.putString(MediaMetadataCompat.METADATA_KEY_ALBUM, album);

			if (track.getAlbum().getArtists() != null && track.getAlbum().getArtists().length > 0) {
				String albumArtist = null;
				for (Artist a : track.getAlbum().getArtists()) {
					albumArtist = albumArtist == null ? a.getName() : albumArtist + "," + a.getName();
				}
				md.putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ARTIST, albumArtist);
			}
		}

		if (artist != null) {
			md.putString(MediaMetadataCompat.METADATA_KEY_ARTIST, artist);
		}


		session.setMetadata(metadata = md.build());
	}


	@Override
	public void onPlaybackStateChanged(PlaybackState oldState, PlaybackState newState) {


		log.trace("onPlaybackStateChanged(): {} -> {}  pos: " + position, oldState, newState);
		switch (newState) {
			case PAUSED:
				this.state = PlaybackStateCompat.STATE_PAUSED;
				break;
			case PLAYING:
				this.state = PlaybackStateCompat.STATE_PLAYING;
				break;
			case STOPPED:
				this.state = PlaybackStateCompat.STATE_STOPPED;
				break;
			default:
				log.error("unhandled state: " + newState);
				return;
		}
	}

	private void updateState() {
		//log.trace("updateState()");
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
		this.position = time_position;
		updateState();
	}

	public void onStreamTitleChanged(String title) {
		log.trace("onStreamTitleChanged(): {}", title);

		MediaMetadataCompat.Builder md = metadata == null ? new MediaMetadataCompat.Builder() : new MediaMetadataCompat.Builder(metadata);
		md.putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE, title);
		session.setMetadata(md.build());

	}


	public void onTracklistChanged() {
		log.trace("onTracklistChanged() ");
		service.notifyChildrenChanged(MediaIds.TRACKLIST);
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

