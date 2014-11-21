package de.robotik.nao.communicator.core.sections;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceListener;

import de.robotik.nao.communicator.R;
import de.robotik.nao.communicator.MainActivity;
import de.robotik.nao.communicator.core.RemoteNAO;
import de.robotik.nao.communicator.core.widgets.RemoteDevice;
import de.robotik.nao.communicator.network.NAOConnector;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.MulticastLock;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.NetworkOnMainThreadException;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

public class SectionConnect extends Section implements OnRefreshListener {
	
	private static SectionConnect INSTANCE;
	
	private MulticastLock lock;
	private JmDNS jmDNS = null;
	private ServiceListener jmDNSServiceListener = null;
		
	private String TAG = getClass().getName();
	private static List<RemoteDevice> devices = new ArrayList<RemoteDevice>();
	private static List<String> servicesProcessing = new ArrayList<String>();
	
	private SwipeRefreshLayout swipeConnect;
	
	private LinearLayout lstNetworkDevices;
	private Button btnScanDevices;
	
	/**
	 * Constructor
	 */
	public SectionConnect() {
		super();
	}
	
	/**
	 * Constrctor
	 * @param title
	 */
	public SectionConnect(String title) {
		super(title);
	}

	/**
	 * Called to create view
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.page_connect, container, false);
		
		// get ui widgets
		swipeConnect = (SwipeRefreshLayout) findViewById(R.id.swipeConnect);
		Button btnAddDevice = (Button) findViewById(R.id.btnConnect);
		btnScanDevices = (Button) findViewById(R.id.btnScanDevices);
		EditText txtHost = (EditText) findViewById(R.id.txtConnectHost);
		lstNetworkDevices = (LinearLayout) findViewById(R.id.lstConnectDevices);
		
		// set devices from list
		lstNetworkDevices.removeAllViews();
		for( RemoteDevice device : devices ){
			ViewGroup parent = (ViewGroup) device.getView().getParent();
			if( parent != null ){
				parent.removeView( device.getView() );
			}
			lstNetworkDevices.addView( device.getView() );
		}
		
		// set swipe layout
		swipeConnect.setOnRefreshListener(this);
		swipeConnect.setColorSchemeResources(
				R.color.darkerblue,
				R.color.darkblue,
				R.color.blue,
				R.color.lighterblue);
		
		// add default host and port
		txtHost.setText( NAOConnector.defaultHost );
		
		// connect ui widgets
		btnAddDevice.setOnClickListener( new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				String vHost = ((EditText) findViewById(R.id.txtConnectHost)).getText().toString().trim();
				if( addDevice(vHost) ){
					swipeConnect.setRefreshing(false);
				}				
			}
		} );
		
		btnScanDevices.setOnClickListener( new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				restartJmDNSService();
			}
		} );
		
		// Set instance
		INSTANCE = this;
		
		return rootView;
	}
	
	/**
	 * @return	{@link SectionConnect} instance.
	 */
	public static SectionConnect getInstance(){
		if( INSTANCE == null ){
			INSTANCE = new SectionConnect();
		}
		
		return INSTANCE;
	}
	
	/**
	 * Clears list of devices, except connected devices.
	 */
	public void clearDevicesList(){
		List<RemoteDevice> vDevicesBackup = new ArrayList<RemoteDevice>(devices);
		for( RemoteDevice device : vDevicesBackup ){
			if( !device.getNao().isConnected() ){
				devices.remove(device);
				lstNetworkDevices.removeView( device.getView() );
			}		
		}
	}
	
	/**
	 * Updates devices backgrounds
	 */
	public static void updateRemoteDevicesBackgrounds(){
		for( RemoteDevice device : devices ){
			device.updateDeviceBackground();
		}
	}
	
	/**
	 * Restarts network service discovery
	 */
	private void restartJmDNSService(){
		// clear lists
		clearDevicesList();
		
		// start discovering of network services
		(new AsyncTask<Void, Void, Void>() {					
			protected void onPreExecute() {
				btnScanDevices.setEnabled(false);
				btnScanDevices.setText(R.string.net_restart_network_service);
			};
			
			@Override
			protected Void doInBackground(Void... params) {
				discoverNetworkServices();
				return null;
			}
			
			protected void onPostExecute(Void result) {
				btnScanDevices.setText(R.string.net_scan_devices);
				btnScanDevices.setEnabled(true);
			};
		}).execute();
	}
	
	
	@Override
	public void onStop() {
		super.onStop();
		if( jmDNS != null ){
			jmDNS.unregisterAllServices();
			try {
				jmDNS.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		jmDNS = null;
	}
	
	/**
	 * Called if activity visibility changes
	 */
	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
		super.setUserVisibleHint(isVisibleToUser);
		
		if( isVisibleToUser ){
			// start discovering of network services
			(new AsyncTask<Void, Void, Void>() {
				@Override
				protected Void doInBackground(Void... params) {
					discoverNetworkServices();
					return null;
				}
			}).execute();
		}
		else{
			// stop network service discovery
			new AsyncTask<Void, Void, Void>(){
				@Override
				protected Void doInBackground(Void... params) {
					stopNetworkServiceDiscovery();
					return null;
				}				
			}.execute();
			
		}
	}
	
	/**
	 * Stops discovering of network services
	 */
	private void stopNetworkServiceDiscovery(){	
		Log.d(TAG, "stop discovering network services");
		
		if( jmDNS != null ){
			jmDNS.unregisterAllServices();
			try {
				jmDNS.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		jmDNS = null;
	}
	
	/**
	 * Starts discovering network services
	 */
	private synchronized void discoverNetworkServices(){	

		// stop running discoveries
		stopNetworkServiceDiscovery();
		
		Log.d(TAG, "discover network services");
		
		if(jmDNSServiceListener == null){
			jmDNSServiceListener = initializeDiscoveryListener();
		}
		
		if( getActivity() == null ){
			return;
		}

		WifiManager wifi = (WifiManager) getActivity().getSystemService(Context.WIFI_SERVICE);
        lock = wifi.createMulticastLock(TAG);
        lock.setReferenceCounted(true);
        lock.acquire();
        
        try {
        	if( jmDNS == null ){
        		jmDNS = JmDNS.create();
        	}
        	
        	// Start discovery for every requested service type
        	jmDNS.addServiceListener(RemoteDevice.workstationNetworkServiceToken, jmDNSServiceListener);
        	jmDNS.addServiceListener(RemoteNAO.naoNetworkServiceToken, jmDNSServiceListener);
        	jmDNS.addServiceListener(RemoteNAO.naoqiNetworkServiceToken, jmDNSServiceListener);
        	jmDNS.addServiceListener(RemoteNAO.sshNetworkServiceToken, jmDNSServiceListener);
        	jmDNS.addServiceListener(RemoteNAO.sftpNetworkServiceToken, jmDNSServiceListener);
        	jmDNS.addServiceListener(NAOConnector.serverNetworkServiceToken, jmDNSServiceListener);
        	
        	// Register test service
//        	ServiceInfo info = ServiceInfo.create(RemoteDevice.workstationNetworkServiceToken, "TEST", 9696, "teststring");
//        	jmDNS.registerService(info);
        	
        } catch (IOException e) {
            e.printStackTrace();
        }
        
//        lock.release();	
        
	}
	
	/**
	 * Initalizes {@link ServiceListener} for callback on {@link ServiceEvent} discovery.
	 * @return {@link ServiceListener} 
	 */
	private ServiceListener initializeDiscoveryListener() {
		// Instantiate a new DiscoveryListener
		return new ServiceListener() {
	        public void serviceResolved(ServiceEvent service) {	        	
	        	// adding remote device to list
//	        	Log.i(TAG, "----------------------");
//	            Log.i(TAG, "Service name: " + service.getName());
//	            Log.i(TAG, "Service type: " + service.getType());
//	            Log.i(TAG, "Service host: " + service.getInfo().getServer());
//	            for( String adrr : service.getInfo().getHostAddresses() ){
//	            	Log.i(TAG, "Service host adress: " + adrr);
//	            }
	        	
	        	swipeConnect.setRefreshing(false);	        	
	        	addDevice(service);
	        }
	        
	        public void serviceRemoved(ServiceEvent service) {
	        	Log.d(TAG, "Service removed: " + service.getName());
	        }
	        
	        public void serviceAdded(ServiceEvent service) {
	            // Required to force serviceResolved to be called again
	            // (after the first search)
	        	Log.d(TAG, "Service added: " + service.getType() + " : " + service.getName());
	            jmDNS.requestServiceInfo(service.getType(), service.getName(), 1);
	        }
	    };
	}
	
	/**
	 * Adds {@link RemoteDevice} to device list
	 * @param aService	{@link ServiceEvent} of {@link RemoteDevice}
	 * @return			{@code true} if successful, {@code false} otherwise.
	 */
	private boolean addDevice(ServiceEvent aService){
		
		if( aService.getInfo().getHostAddresses().length > 0 ){
			
			// try to resolve correct hostname
			String vHost = aService.getInfo().getHostAddresses()[0];
			try {
				vHost = InetAddress.getByName(vHost).getHostAddress();
			} catch (UnknownHostException e) {
				Log.e(TAG, "Resolving " + vHost + " was not successfull.");
				return false;
			}
			
			// check if service already processing
        	while( servicesProcessing.contains(vHost) );
        	servicesProcessing.add(vHost);
        	
        	// try to get existing device
        	RemoteDevice vDevice = getDevice(vHost);
        	if( vDevice != null ){
        		
        		// add service to existing device
        		vDevice.addNetworkService(aService);
    			servicesProcessing.remove(vHost);
    			
        	} else {
        		
        		// add new device
        		new AsyncTask<ServiceEvent, Void, Void>(){    				
					@Override
					protected Void doInBackground(
							ServiceEvent... params) {
						if( params.length > 0 ){
							ServiceEvent vService = params[0];
							RemoteDevice vDevice = new RemoteDevice(getActivity(), vService);
							addRemoteDevice(vDevice);
						}
						return null;
					}				
	        	}.execute(new ServiceEvent[]{aService});
	        	
        	}
        	
        	return true;
        	
		}
		
		Log.e(TAG, "service contains no host adresses: " + aService.getInfo().getHostAddresses().length);
		return false;
	}
	
	/**
	 * Adds a {@link RemoteDevice} to device list.
	 * @param aDevice	{@link RemoteDevice} to add.
	 */
	public void addRemoteDevice(RemoteDevice aDevice){
		
		if( !devices.contains(aDevice) ){
			devices.add(aDevice);
			MainActivity.getInstance().runOnUiThread( new Runnable() {			
				@Override
				public void run() {
					RemoteDevice vDevice = devices.get( devices.size()-1 );
					
					// check if to remove from previous parent
					View vView = vDevice.getView();
					ViewGroup vParent = (ViewGroup) vView.getParent();
					if( vParent != null ){
						vParent.removeView(vView);
					}
					
					lstNetworkDevices.addView( vView );
					servicesProcessing.remove( vDevice.getNao().getHostAdresses().get(0) );
				}
			} );
		}
		
	}
	
	/**
	 * Adds {@link RemoteDevice} to device list.
	 * @param aHost	{@link String} host address.
	 * @return		{@code true} if successful, {@code false} otherwise.
	 */
	private boolean addDevice(String aHost){
		
		try {
			
			// resolve host address
			aHost = InetAddress.getByName(aHost).getHostAddress();
			
			// check if service already processing
        	while( servicesProcessing.contains(aHost) );
        	servicesProcessing.add(aHost);
			
			// try to get existing device
			if( getDevice(aHost) == null ){
				
				// add new device
        		new AsyncTask<String, Void, Void>(){    				
					@Override
					protected Void doInBackground(
							String... params) {
						if( params.length > 0 ){
							String vHost = params[0];
							RemoteDevice vDevice = new RemoteDevice(getActivity(), vHost);
							addRemoteDevice(vDevice);
						}
						return null;
					}				
	        	}.execute(new String[]{aHost});
	        	
	        	return true;				
			}
			
			
		} catch (UnknownHostException e) {
			Log.e(TAG, "Resolving " + aHost + " was not successfull.");
		} catch (NetworkOnMainThreadException e) {
			Log.e(TAG, "Resolving " + aHost + " was not successfull.");
		}
		
		return false;
	}
	
	/**
	 * Gets {@link RemoteDevice} by host name.
	 * @param host	{@link String} host name
	 * @return		{@link RemoteDevice} if found, {@code null} othwerise.
	 */
	private RemoteDevice getDevice(String host){
		
		// search for devices in list of devices
    	for( RemoteDevice device : devices ){
    		if( device.hasAdress(host) ){
    			return device;
    		}
    	}
    	
    	return null;
	}
	
	
	/**
	 * Shows dialog to ask for installation of nao communication server 
	 * @param host Hostname or IP of NAO
	 * @param port Port of NAO server
	 */
	public void showAksForServerInstallDialog(String host, int port){
		
	}

	@Override
	public void onRefresh() {
		restartJmDNSService();
	}
	
}
