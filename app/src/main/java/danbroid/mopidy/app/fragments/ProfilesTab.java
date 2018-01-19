package danbroid.mopidy.app.fragments;

import android.support.v4.media.MediaBrowserCompat;
import android.view.View;

import org.androidannotations.annotations.EFragment;

import danbroid.mopidy.fragments.MediaListFragment;
import danbroid.mopidy.service.MopidyClient;
import danbroid.mopidy.util.MediaIds;

/**
 * Created by dan on 19/01/18.
 */

@EFragment(resName = "refreshable_list")
public class ProfilesTab extends MediaListFragment {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ProfilesTab.class);

	public static ProfilesTab newInstance() {
		return ProfilesTab_.builder().arg(ARG_MEDIA_ID, MediaIds.PROFILES).build();
	}

	@Override
	protected void onItemClicked(View view, MediaBrowserCompat.MediaItem item) {
		String url = MediaIds.decode(item.getMediaId());
		log.warn("connecting to {}", url);
		new MopidyClient.Connect(getActivity(), url).call();
	}
}
