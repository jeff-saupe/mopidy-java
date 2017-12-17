package danbroid.mopidy.app.fragments;

import android.support.v4.app.Fragment;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;

import danbroid.mopidy.ResponseHandler;
import danbroid.mopidy.app.MopidyConnection;
import danbroid.mopidy.app.R;
import danbroid.mopidy.app.activities.MainActivity;
import danbroid.mopidy.interfaces.CallContext;
import danbroid.mopidy.model.Ref;

/**
 * Created by dan on 12/12/17.
 */
@EFragment(R.layout.test_fragment)
public class TestFragment extends Fragment {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TestFragment.class);

	@Bean
	MopidyConnection conn;

	@Click(R.id.chevron_down)
	void close() {
		((MainActivity) getActivity()).hideFullControls();
	}

	@Click(R.id.test1)
	void test1() {
		log.debug("test1()");
		conn.getPlaylists().asList(new ResponseHandler<Ref[]>() {
			@Override
			public void onResponse(CallContext context, Ref[] result) {
				log.debug("result count: " + result.length);
			}
		});
	}
}
