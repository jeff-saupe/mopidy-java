package danbroid.mopidy.app.util;

import android.content.Context;
import android.net.Uri;
import android.net.nsd.NsdServiceInfo;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.SupposeUiThread;
import org.androidannotations.annotations.UiThread;

import java.util.HashMap;
import java.util.Map;

import danbroid.mopidy.app.content.ContentProvider;
import danbroid.mopidy.util.ServiceDiscoveryHelper;

/**
 * Created by dan on 10/12/17.
 */
@EBean(scope = EBean.Scope.Singleton)
public class MopidyServerDiscovery implements ServiceDiscoveryHelper.Listener {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MopidyServerDiscovery.class);

	//List of mopidy server information
	private final HashMap<String, NsdServiceInfo> serverInfo = new HashMap<>();
	ServiceDiscoveryHelper discoveryHelper;

	private final Uri URI = Uri.parse(ContentProvider.URI_SERVERS);

	@RootContext
	Context context;


	@SupposeUiThread
	public void start() {
		discoveryHelper = new ServiceDiscoveryHelper(context, this);
		discoveryHelper.start();
	}

	@SupposeUiThread
	public void stop() {
		if (discoveryHelper != null) {
			discoveryHelper.stop();
			discoveryHelper = null;
		}
	}


	/**
	 * Add the service info to our list of available mopidy servers
	 *
	 * @param serviceInfo
	 */
	@UiThread
	@Override
	public void onServiceAdded(NsdServiceInfo serviceInfo) {
		if (!serverInfo.containsKey(serviceInfo.getServiceName())) {
			log.warn("onServiceAdded(): {}", serviceInfo);
			serverInfo.put(serviceInfo.getServiceName(), serviceInfo);
			context.getContentResolver().notifyChange(URI, null);
		}

	}


	@UiThread
	@Override
	public void onServiceRemoved(NsdServiceInfo serviceInfo) {
		if (serverInfo.containsKey(serviceInfo.getServiceName())) {
			log.warn("onServiceRemoved(): {}", serviceInfo);
			serverInfo.remove(serviceInfo.getServiceName());
			context.getContentResolver().notifyChange(URI, null);
		}

	}

	public Map<String, NsdServiceInfo> getServerInfo() {
		return serverInfo;
	}

}
