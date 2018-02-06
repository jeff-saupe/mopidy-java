package danbroid.mopidy.activities;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

import java.util.LinkedList;

import danbroid.mopidy.interfaces.MopidyListener;
import danbroid.mopidy.service.MopidyBackend;

/**
 * Created by dan on 3/02/18.
 */
@EBean
public class ClientEventManager {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ClientEventManager.class);


	protected final MediaControllerCompat.Callback controllerCallback =
			new MediaControllerCompat.Callback() {

				@Override
				public void onSessionEvent(String event, Bundle extras) {
					log.error("onSessionEvent(): {} extras: {}", event, extras);
					switch (event) {
						case MopidyBackend.SESSION_EVENT_CONNECTED:
							onServerConnected(extras.getString(MopidyBackend.ARG_URL));
							break;
						case MopidyBackend.SESSION_EVENT_DISCONNECTED:
							onServerDisconnected(extras.getString(MopidyBackend.ARG_URL));
							break;
					}
				}

				@Override
				public void onSessionReady() {
					log.error("onSessionReady()");
				}

				@Override
				public void onSessionDestroyed() {
					log.error("onSessionDestroyed()");
				}

				@Override
				public void onPlaybackStateChanged(@NonNull PlaybackStateCompat state) {
					ClientEventManager.this.onPlaybackStateChanged(state);
				}

				@Override
				public void onMetadataChanged(MediaMetadataCompat metadata) {
					ClientEventManager.this.onMetadataChanged(metadata);
				}
			};
	private MediaMetadataCompat metadata;
	private PlaybackStateCompat state;
	private boolean serverConnected = false;


	@RootContext
	protected MopidyActivity activity;

	private final LinkedList<MopidyListener> listeners = new LinkedList<>();


	protected void onServerConnected(String url) {
		this.serverConnected = true;
		for (MopidyListener listener : listeners) {
			listener.onServerConnected(url);
		}
	}

	protected void onServerDisconnected(String url) {
		this.serverConnected = false;
		for (MopidyListener listener : listeners) {
			listener.onServerDisconnected(url);
		}
	}

	public void addListener(MopidyListener listener) {
		if (listeners.contains(listener)) return;
		listeners.add(listener);

		initListener(listener);
	}

	protected void initListener(MopidyListener listener) {
	}


	public void removeListener(MopidyListener listener) {
		listeners.remove(listener);
	}

	public MediaControllerCompat.Callback getControllerCallback() {
		return controllerCallback;
	}

	protected void onMetadataChanged(MediaMetadataCompat metadata) {
		this.metadata = metadata;
		for (MopidyListener listener : listeners) {
			listener.onMetadataChanged(metadata);
		}
	}

	protected void onPlaybackStateChanged(PlaybackStateCompat state) {
		this.state = state;
		for (MopidyListener listener : listeners) {
			listener.onPlaybackStateChanged(state);
		}
	}
}
