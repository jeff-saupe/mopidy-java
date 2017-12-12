package danbroid.mopidy.app.fragments;

import android.support.v4.app.Fragment;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;

import danbroid.mopidy.app.R;
import danbroid.mopidy.app.activities.MainActivity;

/**
 * Created by dan on 12/12/17.
 */
@EFragment(R.layout.test_fragment)
public class TestFragment extends Fragment {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TestFragment.class);

	@Click(R.id.chevron_down)
	void close() {
		((MainActivity) getActivity()).hideFullControls();
	}
}
