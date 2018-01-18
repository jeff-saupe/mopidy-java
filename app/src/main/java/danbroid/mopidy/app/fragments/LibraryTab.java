package danbroid.mopidy.app.fragments;

import org.androidannotations.annotations.EFragment;

import danbroid.mopidy.fragments.MediaListFragment;
import danbroid.mopidy.util.MediaIds;

/**
 * Created by dan on 19/01/18.
 */
@EFragment(resName = "refreshable_list")
public class LibraryTab extends MediaListFragment {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LibraryTab.class);

	public static LibraryTab newInstance() {
		return LibraryTab_.builder().arg(ARG_MEDIA_ID, MediaIds.ROOT).build();
	}


}
