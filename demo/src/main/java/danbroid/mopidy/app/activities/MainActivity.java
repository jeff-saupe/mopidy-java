package danbroid.mopidy.app.activities;

import android.net.Uri;
import android.net.nsd.NsdServiceInfo;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;

import danbroid.mopidy.app.R;
import danbroid.mopidy.app.content.ContentProvider;
import danbroid.mopidy.app.fragments.ContentListFragment;
import danbroid.mopidy.app.interfaces.ContentView;
import danbroid.mopidy.app.interfaces.MainView;
import danbroid.mopidy.app.util.MopidyServerDiscovery;
import danbroid.mopidy.model.Base;
import danbroid.mopidy.model.Ref;

@EActivity(R.layout.activity_main)
public class MainActivity extends AppCompatActivity implements MainView {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MainActivity.class);


	@ViewById(R.id.toolbar)
	Toolbar toolbar;

	@Bean
	ContentProvider contentProvider;

	@Bean
	MopidyServerDiscovery serverDiscovery;

	@AfterViews
	void init() {
		log.info("init()");
		setSupportActionBar(toolbar);

		if (getContent() == null) {
			showContent(ContentProvider.URI_SERVERS);
		}
	}

	public void showContent(String uri) {
		log.trace("showContent(): {}", uri);
		setContent(ContentListFragment.newInstance(uri));
	}

	@Override
	protected void onResume() {
		super.onResume();
		serverDiscovery.start();
	}

	@Override
	protected void onPause() {
		super.onPause();
		serverDiscovery.stop();
	}

	public Fragment getContent() {
		return getSupportFragmentManager().findFragmentById(R.id.content_container);
	}

	public void setContent(Fragment fragment) {
		getSupportFragmentManager().beginTransaction()
				.replace(R.id.content_container, fragment)
				.addToBackStack(null)
				.commit();
	}


	@Override
	public void onItemSelected(Base item) {
		log.info("onItemSelected(): {}", item);
		if (item instanceof Ref) {
			Ref ref = (Ref) item;

			if (ref.getType().equals(Ref.TYPE_DIRECTORY)) {
				String uri = ref.getUri();
				showContent(uri);
			}
		}

	}

	@Override
	public void browse(String uri, ContentView contentView) {
		log.debug("browse(): {}", uri);

		if (ContentProvider.URI_SERVERS.equals(uri)) {

			ArrayList<Ref> servers = new ArrayList<>();

			for (NsdServiceInfo serviceInfo : serverDiscovery.getServerInfo().values()) {
				Ref ref = new Ref();
				ref.setType(Ref.TYPE_DIRECTORY);
				ref.setName(serviceInfo.getServiceName());
				ref.setUri(ContentProvider.URI_SERVER + serviceInfo.getHost().toString() + ":" + serviceInfo.getPort());
				servers.add(ref);
			}

			contentView.setContent(servers.toArray(new Ref[]{}));

			return;
		}


		contentProvider.browse(uri, contentView);
	}


	@Override
	public void onBackPressed() {
		FragmentManager fm = getSupportFragmentManager();

		if (fm.getBackStackEntryCount() > 1) {
			super.onBackPressed();
			return;
		}
		Uri.parse("");

		finish();
	}

	@Override
	protected void onStart() {
		super.onStart();
		contentProvider.start();
	}

	@Override
	protected void onStop() {
		super.onStop();
		contentProvider.stop();
	}


}
