package danbroid.mopidy.app.fragments;

import android.support.v4.media.MediaBrowserCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;

import danbroid.mopidy.app.R;
import danbroid.mopidy.fragments.MediaListFragment;
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
		getMainView().getMopidyClient().shuffleTracklist().call();
	}

	@OptionsItem(R.id.action_clear)
	public void clear() {
		log.debug("clear()");
		getMainView().getMopidyClient().clearTracklist().call();
	}

	@Override
	protected void showLongClickMenu(View view, MediaBrowserCompat.MediaItem item) {
		PopupMenu popupMenu = new PopupMenu(getContext(), view);
		Menu menu = popupMenu.getMenu();

		menu.add(danbroid.mopidy.R.string.tracklist_remove_from).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem menuItem) {
				getMainView().getMopidyClient().removeFromTracklist(new MediaBrowserCompat.MediaItem[]{item}).call();
				return true;
			}
		});


		popupMenu.show();
	}
}
