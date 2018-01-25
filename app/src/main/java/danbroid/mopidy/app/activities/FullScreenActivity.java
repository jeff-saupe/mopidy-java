package danbroid.mopidy.app.activities;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.support.v4.media.MediaBrowserCompat;
import android.widget.Toast;

import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.sharedpreferences.Pref;

import java.util.List;

import danbroid.mopidy.activities.MopidyActivity;
import danbroid.mopidy.app.R;
import danbroid.mopidy.app.fragments.FullScreenControlsFragment;
import danbroid.mopidy.app.service.MopidyService_;
import danbroid.mopidy.fragments.MediaFragment;
import danbroid.mopidy.interfaces.MopidyPrefs_;
import danbroid.mopidy.service.AbstractMopidyService;
import danbroid.mopidy.service.MopidyBackend;
import danbroid.mopidy.util.MediaIds;

/**
 * Created by dan on 16/01/18.
 */
@EActivity(R.layout.fullscreen_activity)
public class FullScreenActivity extends MopidyActivity {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(FullScreenActivity.class);


	protected void init() {
		super.init();
		getSupportFragmentManager().beginTransaction()
				.replace(R.id.fragment_container, FullScreenControlsFragment.newInstance())
				.commit();

	}

	@Override
	protected Class<? extends AbstractMopidyService> getServiceClass() {
		return MopidyService_.class;
	}

	@Override
	protected void setContent(MediaFragment instance) {
		log.error("not implemented");
	}


	@Pref
	protected MopidyPrefs_ prefs;


	@Override
	protected void onConnected() {
		log.info("onConnected()");

		String lastURL = prefs.lastConnectionURL().getOr(null);

		if (lastURL != null) {
			connectTo(lastURL);
		}

	}


	public void connectTo(String lastURL) {
		MopidyBackend.connect(this, lastURL,
				new ResultReceiver(new Handler(Looper.getMainLooper())) {
					@Override
					protected void onReceiveResult(int resultCode, Bundle resultData) {
						log.debug("connected code: " + resultCode + " data: {}", resultData);
						if (resultCode != MopidyBackend.RESULT_CODE_SUCCESS) {
							Toast.makeText(getApplicationContext(), "ERROR: " + resultData.getString(MopidyBackend.ARG_MESSAGE),
									Toast.LENGTH_SHORT).show();
							return;
						}
						onMopidyConnected();
					}
				});
	}

	private void onMopidyConnected() {
		getMediaBrowser().subscribe(MediaIds.TRACKLIST, new MediaBrowserCompat.SubscriptionCallback() {
			@Override
			public void onChildrenLoaded(@NonNull String parentId, @NonNull List<MediaBrowserCompat.MediaItem> children) {
				log.trace("onChildrenLoaded(): {}",children.size());

			}
		});
	}
}
