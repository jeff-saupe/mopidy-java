package danbroid.mopidy.app.fragments;

import android.support.v4.app.Fragment;
import android.widget.TextView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import danbroid.mopidy.app.R;

/**
 * Created by dan on 11/12/17.
 */
@EFragment(R.layout.bottom_controls)
public class BottomControlsFragment extends Fragment {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(BottomControlsFragment.class);

	@ViewById(R.id.artist)
	TextView artist;

	@AfterViews
	void init(){
		artist.setSelected(true);
	}
}
