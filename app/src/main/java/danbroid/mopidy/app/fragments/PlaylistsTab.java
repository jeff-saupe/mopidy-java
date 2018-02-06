package danbroid.mopidy.app.fragments;

import org.androidannotations.annotations.EFragment;

import danbroid.mopidy.fragments.MediaListFragment;
import danbroid.mopidy.util.MediaIds;

/**
 * Created by dan on 19/01/18.
 */

@EFragment(resName = "refreshable_list")
public class PlaylistsTab extends MediaListFragment {
	public static PlaylistsTab newInstance() {
		return PlaylistsTab_.builder().arg(ARG_MEDIA_ID, MediaIds.PLAYLISTS).build();
	}

}
