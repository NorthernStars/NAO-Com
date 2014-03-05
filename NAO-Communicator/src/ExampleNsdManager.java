

import android.app.Activity;
import android.app.Fragment;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class ExampleNsdManager extends Activity {

	private String TAG = getClass().getName();
	private NsdManager mNsdManager;
	private NsdManager.DiscoveryListener mDiscoveryListener;
	private NsdManager.ResolveListener mResolveListener;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}

		mNsdManager = (NsdManager) getSystemService(NSD_SERVICE);
		initalizeResolveListener();
		initializeDiscoveryListener();
		
		mNsdManager.discoverServices("_nao._tcp",
				NsdManager.PROTOCOL_DNS_SD, mDiscoveryListener);
		mNsdManager.discoverServices("_naoqi._tcp",
				NsdManager.PROTOCOL_DNS_SD, mDiscoveryListener);

	}
	
	@Override
	protected void onStop() {
		super.onStop();
		mNsdManager.stopServiceDiscovery(mDiscoveryListener);
	}
	
	/**
	 * Initalizes {@link NsdManager.ResolveListener} for resolving {@link NsdServiceInfo}
	 */
	private void initalizeResolveListener(){		
		// Instantiate new Resolve Listener
		mResolveListener = new NsdManager.ResolveListener() {
			
			@Override
			public void onServiceResolved(NsdServiceInfo serviceInfo) {
				Log.d(TAG, "---- SERVICE FOUND ----");
				Log.d(TAG, "Service Type: " + serviceInfo.getServiceType());
				Log.d(TAG, "Service Name: " + serviceInfo.getServiceName());
				Log.d(TAG, "Service Host: " + serviceInfo.getHost().getHostAddress());				
			}
			
			@Override
			public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {}
		};		
	}
	
	/**
	 * Initalizes {@link NsdManager.DiscoveryListener} for callback on {@link NsdManager} discovery.
	 */
	private void initializeDiscoveryListener() {
		// Instantiate a new DiscoveryListener
		mDiscoveryListener = new NsdManager.DiscoveryListener() {

			// Called as soon as service discovery begins.
			@Override
			public void onDiscoveryStarted(String regType) {}

			@Override
			public void onServiceFound(NsdServiceInfo serviceInfo) {
				// A service was found! Do something with it.
				mNsdManager.resolveService(serviceInfo, mResolveListener);
			}

			@Override
			public void onServiceLost(NsdServiceInfo serviceInfo) {
				// When the network service is no longer available.
				// Internal bookkeeping code goes here.
				Log.d(TAG, "service lost: " + serviceInfo);
			}

			@Override
			public void onDiscoveryStopped(String serviceType) {}

			@Override
			public void onStartDiscoveryFailed(String serviceType, int errorCode) {
				mNsdManager.stopServiceDiscovery(this);
			}

			@Override
			public void onStopDiscoveryFailed(String serviceType, int errorCode) {	}
		};
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container,
					false);
			return rootView;
		}
	}

}
