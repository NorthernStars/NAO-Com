package de.robotik.nao.communicator.core.sections;

import java.io.IOException;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;

import de.northernstars.naocom.R;
import de.robotik.nao.communicator.core.InstallActivity;
import de.robotik.nao.communicator.core.widgets.RemoteDevice;
import de.robotik.nao.communicator.network.NAOConnector;
import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.MulticastLock;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

public class SectionConnect extends Section {
	
	private String TAG = getClass().getName();
	
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
				// TODO
			}
		} );
		
		return rootView;
	}
	
//	/**
//	 * Called if activity visibility changes
//	 */
//	@Override
//	public void setUserVisibleHint(boolean isVisibleToUser) {
//		if( isVisibleToUser ){
//			// TODO: starting network discovery
//		}
//		else{
//			// TODO stop net
//		}
//	}
	
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
