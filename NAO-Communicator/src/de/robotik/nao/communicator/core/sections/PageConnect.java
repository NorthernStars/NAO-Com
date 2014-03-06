package de.robotik.nao.communicator.core.sections;

import java.io.IOException;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceListener;

import de.northernstars.naocom.R;
import de.robotik.nao.communicator.core.InstallActivity;
import de.robotik.nao.communicator.core.RemoteNAO;
import de.robotik.nao.communicator.core.widgets.RemoteDevice;
import de.robotik.nao.communicator.network.NAOConnector;
import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.MulticastLock;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

public class PageConnect extends Page {
	
	private String TAG = getClass().getName();
	
	private MulticastLock lock;
	private Handler backgroundHandler = new Handler();
	private JmDNS jmDNS = null;
	private ServiceListener jmDNSServiceListener = null;
	
	/**
	 * Constructor
	 */
	public PageConnect() {
		super();
	}
	
	/**
	 * Constrctor
	 * @param title
	 */
	public PageConnect(String title) {
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
		Button btnConnect = (Button) findViewById(R.id.btnConnect);
		Button btnScanDevices = (Button) findViewById(R.id.btnScanDevices);
		EditText txtHost = (EditText) findViewById(R.id.txtHost);
		EditText txtPort = (EditText) findViewById(R.id.txtPort);
		
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
				discoverNetworkServices();
			}
		} );
		
		// start network service discovery
		Log.i(TAG, "START");
		(new AsyncTask<Void, Void, Void>(){

			@Override
			protected Void doInBackground(Void... params) {
				discoverNetworkServices();
				return null;
			}
			
		}).execute();
		
		return rootView;
	}
	
	/**
	 * Called if activity visibility changes
	 */
	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
		if( isVisibleToUser ){
			// TODO: starting network discovery
		}
		else{
			stopNetworkServiceDiscovery();
		}
	}
	
	
	/**
	 * Starts discovering network services
	 */
	private synchronized void discoverNetworkServices(){		
		
		if(jmDNSServiceListener == null){
			jmDNSServiceListener = initializeDiscoveryListener();
		}

		if( getActivity() != null ){
			WifiManager wifi = (WifiManager) getActivity().getSystemService(Context.WIFI_SERVICE);
	        lock = wifi.createMulticastLock(TAG);
	        lock.setReferenceCounted(true);
	        lock.acquire();
	        try {
	        	if( jmDNS == null ){
	        		jmDNS = JmDNS.create();
	        	}
	        	Log.i(TAG, "DISCOVER");
	        	jmDNS.addServiceListener(RemoteDevice.workstationNetworkServiceToken, jmDNSServiceListener);
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	        lock.release();
	    }		
	}
	
	/**
	 * Stops discovering of all network service
	 */
	private synchronized void stopNetworkServiceDiscovery(){
//		if( jmDNS != null ){
//			jmDNS.unregisterAllServices();
//			try {
//	            jmDNS.close();
//	        } catch (IOException e) {
//	            e.printStackTrace();
//	        }
//		}
//		jmDNS = null;
	}
	
	/**
	 * Initalizes {@link NsdManager.DiscoveryListener} for callback on {@link NsdManager} discovery.
	 * @return {@link NsdManager.DiscoveryListener} 
	 */
	private ServiceListener initializeDiscoveryListener() {
		// Instantiate a new DiscoveryListener
		return new ServiceListener() {
	        public void serviceResolved(ServiceEvent service) {
	            Log.i(TAG, "service found: " + service.getInfo().getQualifiedName());
	            Log.i(TAG, service.getName());
	            Log.i(TAG, service.getType());
	            Log.i(TAG, service.getInfo().getKey());
	            Log.i(TAG, service.getInfo().getName());
	            Log.i(TAG, service.getInfo().getProtocol());
	            Log.i(TAG, service.getInfo().getServer());
	            Log.i(TAG, service.getInfo().getSubtype());
	            Log.i(TAG, service.getInfo().getNiceTextString());
	            Log.i(TAG, service.getInfo().getType());
	            Log.i(TAG, service.getInfo().getTypeWithSubtype());
	            Log.i(TAG, service.getInfo().getDomain());
	            Log.i(TAG, service.getInfo().getHostAddresses().toString());
	        }
	        public void serviceRemoved(ServiceEvent service) {
	        	Log.i(TAG, "Service removed: " + service.getName());
	        }
	        public void serviceAdded(ServiceEvent service) {
	            // Required to force serviceResolved to be called again
	            // (after the first search)
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
			String host = ((EditText) findViewById(R.id.txtHost)).getText().toString().trim();
			int port = Integer.parseInt( ((EditText) findViewById(R.id.txtPort)).getText().toString().trim() );
			
			Log.i(TAG, "Connecting to " + host + ":" + port);
			
			// try to connect to nao
			// TODO: Doing install on connection thread
			showAksForServerInstallDialog(host, port);
			
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
		InstallActivity.AskForInstallDialog dialog = new InstallActivity.AskForInstallDialog(host, port);
		dialog.show(getFragmentManager(), "Install");
	}
	
}
