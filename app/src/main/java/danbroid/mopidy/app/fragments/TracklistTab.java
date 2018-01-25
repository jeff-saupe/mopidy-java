package danbroid.mopidy.app.fragments;

import android.support.v4.media.MediaBrowserCompat;
import android.view.View;
import android.widget.Toast;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;

import danbroid.mopidy.app.R;
import danbroid.mopidy.fragments.MediaListFragment;
import danbroid.mopidy.service.MopidyClient;
import danbroid.mopidy.util.MediaIds;

/**
 * Created by dan on 19/01/18.
 */

@EFragment(resName = "refreshable_list")
@OptionsMenu(R.menu.tracklist)
public class TracklistTab extends MediaListFragment {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TracklistTab.class);

	public static TracklistTab newInstance() {
		return TracklistTab_.builder().arg(ARG_MEDIA_ID, MediaIds.TRACKLIST).build();
	}

	@OptionsItem(R.id.action_shuffle)
	public void shuffle() {
		log.debug("shuffle()");

		new MopidyClient.ShuffleTracklist(getActivity())
				.call();
	}

	@Override
	protected void showLongClickMenu(View view, MediaBrowserCompat.MediaItem item) {
	}
}
