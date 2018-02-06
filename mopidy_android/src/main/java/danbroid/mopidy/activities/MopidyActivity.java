package danbroid.mopidy.activities;

import android.content.ComponentName;
import android.os.Process;
import android.os.RemoteException;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;

import danbroid.mopidy.BuildConfig;
import danbroid.mopidy.R;
import danbroid.mopidy.fragments.MediaFragment;
import danbroid.mopidy.fragments.MediaListFragment;
import danbroid.mopidy.interfaces.MainView;
import danbroid.mopidy.interfaces.MopidyListener;
import danbroid.mopidy.service.MopidyService;

/**
 * Created by dan on 24/12/17.
 */
@EActivity
public abstract class MopidyActivity extends AppCompatActivity implements MainView, MopidyListener {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MopidyActivity.class);

	protected final MediaBrowserCompat.ConnectionCallback connectionCallback =
			new MediaBrowserCompat.ConnectionCallback() {

				@Override
				public void onConnected() {
					try {
						connectToSession(mediaBrowser.getSessionToken());
					} catch (RemoteException e) {
						log.error("could not connect media controller", e);
					}
				}

				@Override
				public void onConnectionSuspended() {
					log.warn("ON CONNECTION SUSPENDED!!!!!");
				}

				@Override
				public void onConnectionFailed() {
					log.error("onConnectionFailed()");
					Toast.makeText(getApplicationContext(), R.string.error_connection_failed, Toast.LENGTH_SHORT).show();
				}
			};

	protected MediaBrowserCompat mediaBrowser;
	protected MopidyClient mopidyClient;

	@Bean
	protected ClientEventManager eventManager;

	@AfterViews
	protected void init() {
		log.info("init()");


		mopidyClient = new MopidyClient(this);


		mediaBrowser = new MediaBrowserCompat(this,
				new ComponentName(this, getServiceClass()), connectionCallback, null);

	}

	protected Class<? extends MopidyService> getServiceClass() {
		return MopidyService.class;
	}

	private void connectToSession(MediaSessionCompat.Token token) throws RemoteException {
		log.info("connectToSession(): {}", token);

		MediaControllerCompat mediaController = new MediaControllerCompat(this, token);
		mediaController.registerCallback(eventManager.getControllerCallback());
		MediaControllerCompat.setMediaController(this, mediaController);



		/*LocalBroadcastManager bm = LocalBroadcastManager.getInstance(this);
		bm.sendBroadcast(new Intent(MainView.ACTION_CONTROLLER_CONNECTED));*/


	}

	@Override
	public ClientEventManager getEventManager() {
		return eventManager;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (BuildConfig.DEBUG) {
			menu.add("Test").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
				@Override
				public boolean onMenuItemClick(MenuItem item) {
					log.debug("activity pid: " + Process.myPid());
					getSupportMediaController().sendCommand("test", null, null);
					return true;
				}
			});
		}
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public MopidyClient getMopidyClient() {
		return mopidyClient;
	}

	@Override
	protected void onStart() {
		super.onStart();
		log.debug("onStart()");
		eventManager.addListener(this);
		mediaBrowser.connect();
	}

	@Override
	protected void onStop() {
		super.onStop();

		log.debug("onStop()");
		eventManager.removeListener(this);
		MediaControllerCompat controllerCompat = getSupportMediaController();

		if (controllerCompat != null) {
			controllerCompat.unregisterCallback(eventManager.getControllerCallback());
		}
		mediaBrowser.disconnect();
	}

	public MediaControllerCompat getSupportMediaController() {
		return MediaControllerCompat.getMediaController(this);
	}


	@Override
	public MediaBrowserCompat getMediaBrowser() {
		return mediaBrowser;
	}

	@Override
	public void onMediaItemSelected(MediaBrowserCompat.MediaItem item) {
		String mediaID = item.getMediaId();
		log.trace("onMediaItemSelected() mediaId: {}", mediaID);

		if (item.isPlayable()) {
			getSupportMediaController().getTransportControls()
					.playFromMediaId(item.getMediaId(), null);

		} else if (item.isBrowsable()) {
			showContent(item.getMediaId());
		} else {
			log.warn("Ignoring MediaItem that is neither browsable nor playable: mediaId: {}", item.getMediaId());
		}
	}


	protected void setContent(MediaFragment instance) {
		log.error("setContent() not implemented");
	}

	@Override
	public void showContent(String mediaID) {
		setContent(MediaListFragment.newInstance(mediaID));
	}


	@Override
	public void onServerConnected(String url) {
		log.trace("onServerConnected() {}", url);
	}

	@Override
	public void onServerDisconnected(String url) {
		log.trace("onServerDisconnected(): {}", url);
	}

	@Override
	public void onMetadataChanged(MediaMetadataCompat metadata) {
		log.trace("onMetadataChanged(): {}", metadata);
	}

	@Override
	public void onPlaybackStateChanged(PlaybackStateCompat state) {
		log.trace("onPlaybackStateChanged(): {}", state);
	}
}
