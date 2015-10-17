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
import de.robotik.nao.communicator.core.widgets.RemoteDevice;
import de.robotik.nao.communicator.network.NAOConnector;
import android.annotation.SuppressLint;
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

public class SectionConnect extends Section
	implements OnRefreshListener {
	
	private String TAG = getClass().getName();
	
	private JmDNS jmDNS = null;
	private ServiceListener jmDNSServiceListener = null;	
	
	/**
	 * Layout
	 */
	private SwipeRefreshLayout swipeConnect;	
	private EditText txtHost;
	private Button btnAddDevice;
	private LinearLayout lstNetworkDevices;
	private Button btnScanDevices;
	
	/**
	 * Listener lists
	 */
	private static List<RemoteDevice> devices = new ArrayList<RemoteDevice>();
	private static List<String> servicesProcessing = new ArrayList<String>();
	
	/**
	 * Constructor
	 */
	public SectionConnect() {
		super();
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
		btnScanDevices = (Button) findViewById(R.id.btnScanDevices);
		lstNetworkDevices = (LinearLayout) findViewById(R.id.lstConnectDevices);
		btnAddDevice = (Button) findViewById(R.id.btnConnect);
		txtHost = (EditText) findViewById(R.id.txtConnectHost);
		
		// set devices from list
		lstNetworkDevices.removeAllViews();
		for( RemoteDevice device : devices ){
			ViewGroup parent = (ViewGroup) device.getParent();
			if( parent != null ){
				parent.removeView( device );
			}
			lstNetworkDevices.addView( device );
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
		
		return rootView;
	}
	
	/**
	 * Clears list of devices, except connected devices.
	 */
	private void clearDevicesList(){
		List<RemoteDevice> vDevicesBackup = new ArrayList<RemoteDevice>(devices);
		for( RemoteDevice device : vDevicesBackup ){
			if( !device.isConnected() ){
				devices.remove(device);
				lstNetworkDevices.removeView( device );
			}		
		}
	}
	
	/**
	 * Updates {@link RemoteDevice}s.
	 */
	public static void updateRemoteDevices(){
		for( RemoteDevice device : devices ){
			device.updateView();
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
        
        try {
        	if( jmDNS == null ){
        		jmDNS = JmDNS.create();
        	}
        	
        	// Start discovery for every requested service type
        	jmDNS.addServiceListener(RemoteDevice.workstationNetworkServiceToken, jmDNSServiceListener);
        	jmDNS.addServiceListener(RemoteDevice.naoNetworkServiceToken, jmDNSServiceListener);
        	jmDNS.addServiceListener(RemoteDevice.naoqiNetworkServiceToken, jmDNSServiceListener);
        	jmDNS.addServiceListener(RemoteDevice.sshNetworkServiceToken, jmDNSServiceListener);
        	jmDNS.addServiceListener(NAOConnector.serverNetworkServiceToken, jmDNSServiceListener);
        	
        } catch (IOException e) {
            e.printStackTrace();
        }
        
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
	@SuppressLint("InflateParams")
	@SuppressWarnings("unused")
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
        	RemoteDevice vDevice = null; //getDevice(vHost);
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
							
							RemoteDevice vDevice = new RemoteDevice(getActivity());
							vDevice.addNetworkService( params[0] );
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
					ViewGroup vParent = (ViewGroup) vDevice.getParent();
					if( vParent != null ){
						vParent.removeView(vDevice);
					}
					
					lstNetworkDevices.addView( vDevice );
					servicesProcessing.remove( vDevice.getHostAdresses().get(0) );
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
							RemoteDevice vDevice = new RemoteDevice(getActivity());
							vDevice.setWorkstationName( params[0] );
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

	@Override
	public void onRefresh() {
		restartJmDNSService();
	}
	
}
