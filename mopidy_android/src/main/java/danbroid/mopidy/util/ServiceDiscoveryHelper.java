package danbroid.mopidy.util;

import android.annotation.TargetApi;
import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdManager.DiscoveryListener;
import android.net.nsd.NsdServiceInfo;
import android.os.Build;


/**
 * Created by dan on 6/12/17.
 * Helper class to find Mopidy servers
 */

@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class ServiceDiscoveryHelper {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ServiceDiscoveryHelper.class);
	private Listener listener;


	NsdManager nsdManager;
	String serviceType;

	public interface Listener {
		void onServiceAdded(NsdServiceInfo serviceInfo);

		void onServiceRemoved(NsdServiceInfo serviceInfo);
	}

	private final DiscoveryListener discoveryListener = new DiscoveryListener() {
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
					log.error("onResolvedFailed(): code: {} :{}", errorCode, serviceInfo);
				}

				@Override
				public void onServiceResolved(NsdServiceInfo serviceInfo) {
					log.debug("onServiceResolved(): {}", serviceInfo);
					listener.onServiceAdded(serviceInfo);
				}
			});
		}


		@Override
		public void onServiceLost(NsdServiceInfo serviceInfo) {
			listener.onServiceRemoved(serviceInfo);
		}
	};

	public ServiceDiscoveryHelper(Context context, Listener listener) {
		this(context, "_mopidy-http._tcp", listener);
	}

	public ServiceDiscoveryHelper(Context context, String serviceType, Listener listener) {
		nsdManager = (NsdManager) context.getSystemService(Context.NSD_SERVICE);
		this.serviceType = serviceType;
		this.listener = listener;
	}

	public void start() {
		log.debug("start()");
		nsdManager.discoverServices(serviceType, NsdManager.PROTOCOL_DNS_SD, discoveryListener);
	}

	public void stop() {
		log.debug("stop()");
		nsdManager.stopServiceDiscovery(discoveryListener);
	}

	public void setListener(Listener listener) {
		this.listener = listener;
	}
}
