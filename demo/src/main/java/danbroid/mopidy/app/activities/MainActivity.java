package danbroid.mopidy.app.activities;

import android.net.nsd.NsdServiceInfo;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;

import danbroid.mopidy.MopidyConnection;
import danbroid.mopidy.app.R;
import danbroid.mopidy.app.content.ContentProvider;
import danbroid.mopidy.app.fragments.ContentListFragment;
import danbroid.mopidy.app.interfaces.MainView;
import danbroid.mopidy.app.util.MopidyServerDiscovery;
import danbroid.mopidy.model.Base;
import danbroid.mopidy.model.Ref;

@EActivity(R.layout.activity_main)
public class MainActivity extends AppCompatActivity implements MainView {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MainActivity.class);


	@ViewById(R.id.toolbar)
	Toolbar toolbar;

	String url = "ws://192.168.1.2:6680/mopidy/ws";

	MopidyConnection conn;

	@Bean
	ContentProvider contentProvider;

	@Bean
	MopidyServerDiscovery serverDiscovery;


	@AfterViews
	void init() {
		log.info("init()");
		setSupportActionBar(toolbar);


		if (getContent() == null) {
			browse(ContentProvider.URI_SERVERS);
		}
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
				browse(uri);
			}
		}

	}

	public void browse(String uri) {
		log.info("browse(): {}", uri);
		if (uri.startsWith(ContentProvider.URI_SERVER)) {
			connectTo(uri);
			return;
		}

		if (uri.equals(ContentProvider.URI_SERVERS)) {
			ArrayList<Ref> servers = new ArrayList<>();

			for (NsdServiceInfo serviceInfo : serverDiscovery.getServerInfo().values()) {
				Ref ref = new Ref();
				ref.setType(Ref.TYPE_DIRECTORY);
				ref.setName(serviceInfo.getServiceName());
				ref.setUri(ContentProvider.URI_SERVER + serviceInfo.getHost() + ":" + serviceInfo.getPort());
				servers.add(ref);
			}

			view.setContent(servers.toArray(new Ref[]{}));
			return;
		}

		setContent(ContentListFragment.newInstance(uri));
	}

	public void connectTo(String uri) {
		log.debug("connectTo(): {}", uri);

	}
}
