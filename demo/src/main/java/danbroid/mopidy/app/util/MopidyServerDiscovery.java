package danbroid.mopidy.app.util;

import android.content.Context;
import android.net.nsd.NsdServiceInfo;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.SupposeUiThread;
import org.androidannotations.annotations.UiThread;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import danbroid.mopidy.app.activities.MainActivity;
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


	@RootContext
	Context context;

	private ArrayList<ServiceDiscoveryHelper.Listener> listeners = new ArrayList<>();

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


	@SupposeUiThread
	public void addListener(ServiceDiscoveryHelper.Listener listener) {
		if (!listeners.contains(listener)) listeners.add(listener);
	}


	@SupposeUiThread
	public void removeListener(ServiceDiscoveryHelper.Listener listener) {
		if (listeners.contains(listener)) listeners.remove(listener);
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
			for (int i = 0; i < listeners.size(); i++) {
				listeners.get(i).onServiceAdded(serviceInfo);
			}
		}

	}


	@UiThread
	@Override
	public void onServiceRemoved(NsdServiceInfo serviceInfo) {
		if (serverInfo.containsKey(serviceInfo.getServiceName())) {
			log.warn("onServiceRemoved(): {}", serviceInfo);
			serverInfo.remove(serviceInfo.getServiceName());
			for (int i = 0; i < listeners.size(); i++) {
				listeners.get(i).onServiceRemoved(serviceInfo);
			}
		}

	}

	public Map<String, NsdServiceInfo> getServerInfo() {
		return serverInfo;
	}

}
