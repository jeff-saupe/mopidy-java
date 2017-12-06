package danbroid.mopidy.app.activities;

import android.net.nsd.NsdServiceInfo;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.SupposeUiThread;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.util.HashMap;

import danbroid.mopidy.MopidyConnection;
import danbroid.mopidy.app.R;
import danbroid.mopidy.util.ServiceDiscoveryHelper;

@EActivity(R.layout.activity_main)
public class MainActivity extends AppCompatActivity implements ServiceDiscoveryHelper.Listener {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MainActivity.class);


	@ViewById(R.id.toolbar)
	Toolbar toolbar;

	String url = "ws://192.168.1.2:6680/mopidy/ws";

	ServiceDiscoveryHelper serviceDiscoveryHelper;


	MopidyConnection conn;

	@AfterViews
	void init() {
		log.info("init()");

		setSupportActionBar(toolbar);

		serviceDiscoveryHelper = new ServiceDiscoveryHelper(this, this);

	}

	@Click(R.id.start)
	void startSocket() {
		log.debug("startSocket()");
		conn = new MopidyConnection("192.168.1.2", 6680);
		conn.start();

	}

	@Click(R.id.stop)
	void stopSocket() {
		log.debug("stopSocket()");
		if (conn != null) {
			conn.stop();
			conn = null;
		}
	}


	@Override
	protected void onResume() {
		super.onResume();
		serviceDiscoveryHelper.start();
	}

	@Override
	protected void onPause() {
		super.onPause();
		serviceDiscoveryHelper.stop();
	}

	private final HashMap<String,NsdServiceInfo> serverInfo = new HashMap<>();

	@UiThread
	@Override
	public void onServiceAdded(NsdServiceInfo serviceInfo) {
		if (!serverInfo.containsKey(serviceInfo.getServiceName())) {
			log.warn("onServiceAdded(): {}",serviceInfo);
			serverInfo.put(serviceInfo.getServiceName(), serviceInfo);
		}
	}

	@UiThread
	@Override
	public void onServiceRemoved(NsdServiceInfo serviceInfo) {
		if (serverInfo.containsKey(serviceInfo.getServiceName())){
			log.warn("onServiceRemoved(): {}",serviceInfo);
			serverInfo.remove(serviceInfo.getServiceName());
		}
	}
}
