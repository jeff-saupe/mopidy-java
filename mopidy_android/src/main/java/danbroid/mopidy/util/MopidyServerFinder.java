package danbroid.mopidy.util;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.Build;
import android.support.annotation.RequiresApi;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.SystemService;
import org.androidannotations.annotations.UiThread;

import java.util.HashMap;

/**
 * Created by dan on 26/12/17.
 */
@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
@EBean(scope = EBean.Scope.Singleton)
public class MopidyServerFinder implements NsdManager.DiscoveryListener {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MopidyServerFinder.class);


	@RootContext
	Context context;

	@SystemService
	NsdManager nsdManager;

	private HashMap<String, NsdServiceInfo> services = new HashMap<>();


	public void start() {
		nsdManager.discoverServices("_mopidy-http._tcp", NsdManager.PROTOCOL_DNS_SD, this);
	}

	public void stop() {
		nsdManager.stopServiceDiscovery(this);
	}

	@Override
	public void onStartDiscoveryFailed(String serviceType, int errorCode) {

	}

	@Override
	public void onStopDiscoveryFailed(String serviceType, int errorCode) {

	}

	@Override
	public void onDiscoveryStarted(String serviceType) {

	}

	@Override
	public void onDiscoveryStopped(String serviceType) {

	}


	@Override
	public void onServiceFound(NsdServiceInfo serviceInfo) {
		log.trace("onServiceFound(): {}", serviceInfo);
		nsdManager.resolveService(serviceInfo, new NsdManager.ResolveListener() {
			@Override
			public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
				log.debug("onResolvedFailed(): code: {} :{}", errorCode, serviceInfo);
				onServiceRemoved(serviceInfo);

			}

			@Override
			public void onServiceResolved(NsdServiceInfo serviceInfo) {
				log.debug("onServiceResolved(): {}", serviceInfo);
				onServiceAdded(serviceInfo);
			}
		});
	}

	protected void onServiceAdded(NsdServiceInfo serviceInfo) {
		if (!services.containsKey(serviceInfo.getServiceName())) {
			services.put(serviceInfo.getServiceName(), serviceInfo);
		}
	}


	@UiThread
	protected void onServiceRemoved(NsdServiceInfo serviceInfo) {
		if (services.containsKey(serviceInfo.getServiceName())) {
			services.remove(serviceInfo.getServiceName());
		}
	}

	@UiThread
	@Override
	public void onServiceLost(NsdServiceInfo serviceInfo) {
		onServiceRemoved(serviceInfo);
	}

	public HashMap<String, NsdServiceInfo> getServices() {
		return services;
	}
}
