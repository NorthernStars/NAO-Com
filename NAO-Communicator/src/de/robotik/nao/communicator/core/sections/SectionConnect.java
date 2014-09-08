package de.robotik.nao.communicator.core.sections;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceListener;

import de.northernstars.naocom.R;
import de.robotik.nao.communicator.core.MainActivity;
import de.robotik.nao.communicator.core.RemoteNAO;
import de.robotik.nao.communicator.core.widgets.RemoteDevice;
import de.robotik.nao.communicator.network.NAOConnector;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.MulticastLock;
import android.os.AsyncTask;
import android.os.Bundle;
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
		Button btnConnect = (Button) findViewById(R.id.btnConnect);
		btnScanDevices = (Button) findViewById(R.id.btnScanDevices);
		EditText txtHost = (EditText) findViewById(R.id.txtConnectHost);
		EditText txtPort = (EditText) findViewById(R.id.txtConnectPort);
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
		txtPort.setText( Integer.toString(NAOConnector.defaultPort) );
		
		// connect ui widgets
		btnConnect.setOnClickListener( new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				connect(true);
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
	        	
	        	if( service.getInfo().getHostAddresses().length > 0 ){
	        		
	        		// try to resolve correct hostname
	        		String host = service.getInfo().getHostAddresses()[0];
					try {
						host = InetAddress.getByName(host).getHostAddress();
					} catch (UnknownHostException e) {
						Log.e(TAG, "Resolving " + host + " was not successfull.");
						e.printStackTrace();
						return;
					}
	        		
	        		// check if service already processing
		        	while( servicesProcessing.contains(host) );
		        	servicesProcessing.add(host);
	        		
	        		// search for devices in list of devices
		        	boolean found = false;
		        	for( RemoteDevice device : devices ){
		        		if( device.hasAdress(host) ){
		        			device.addNetworkService(service);
		        			servicesProcessing.remove(host);
		        			found = true;
		        			break;
		        		}
		        	}
		        	
		        	// device not found > adding new device	
		        	if( !found ){
			        	new AsyncTask<ServiceEvent, Void, RemoteDevice>(){
	
							@Override
							protected RemoteDevice doInBackground(
									ServiceEvent... params) {
								if( params.length > 0 ){
									ServiceEvent service = params[0];
									RemoteDevice device = new RemoteDevice(getActivity(), service);
									devices.add(device);
									return device;
								}
								return null;
							}
							
							protected void onPostExecute(RemoteDevice device) {
								if( device != null ){
									lstNetworkDevices.addView( device.getView() );
								}
								servicesProcessing.remove( device.getNao().getHostAdresses().get(0) );
							};
							
			        	}.execute(new ServiceEvent[]{service});
		        	}
		        	
	        	}
	        	else{
	        		Log.e(TAG, "service contains no host adresse: " + service.getInfo().getHostAddresses().length);
	        	}
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
	 * Connect to nao
	 * @param manualConnect {@code true} if connect with manual host and port, {@code false} otherwise
	 */
	public void connect(boolean manualConnect){
		if( manualConnect ){
			
			// manual connection
			String host = ((EditText) findViewById(R.id.txtConnectHost)).getText().toString().trim();
			int port = Integer.parseInt( ((EditText) findViewById(R.id.txtConnectPort)).getText().toString().trim() );
			
			Log.i(TAG, "Connecting to " + host + ":" + port);
			
			// try to connect to nao
			
			// search for devices in list of devices
        	boolean found = false;
        	RemoteDevice device = null;
        	for( RemoteDevice dev : devices ){
        		if( dev.hasAdress(host) ){
        			device = dev;
        			found = true;
        			break;
        		}
        	}
        	
        	if( !found ){
        		
        		device = new RemoteDevice(getActivity(), host, port);
        		new AsyncTask<RemoteDevice, Void, RemoteDevice>(){
        			
					@Override
					protected RemoteDevice doInBackground(
							RemoteDevice... params) {
						if( params.length > 0 ){
							RemoteDevice device = params[0];
							devices.add(device);
							return device;
						}
						return null;
					}
					
					protected void onPostExecute(RemoteDevice device) {
						if( device != null ){
							lstNetworkDevices.addView( device.getView() );
						}
						if( device.getNao() != null && device.getNao().getHostAdresses().size() > 0 ){
							servicesProcessing.remove( device.getNao().getHostAdresses().get(0) );
						}
					};
					
	        	}.execute(new RemoteDevice[]{device});

        	}
        	
			MainActivity.getInstance().setConnectedDevice( device );
			MainActivity.getInstance().getConnectedDevice().getNao().connect();
			
			// TODO: Doing install on connection thread
			//showAksForServerInstallDialog(host, port);
			
		}
		else{
			
			// connecting to found device
			System.out.println("nope");
			
		}

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
