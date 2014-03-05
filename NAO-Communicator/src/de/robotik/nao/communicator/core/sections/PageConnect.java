package de.robotik.nao.communicator.core.sections;

import java.util.List;

import de.northernstars.naocom.R;
import de.robotik.nao.communicator.core.InstallActivity;
import de.robotik.nao.communicator.core.widgets.RemoteDevice;
import de.robotik.nao.communicator.network.NAOConnector;
import de.robotik.nao.communicator.network.hotspot.WifiApManager;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

public class PageConnect extends Page {
	
	private Handler backgroundHandler = new Handler();
	private Runnable backgroundRunnable = null;
		
	private static final long backgroundTaskDelay = 2000;
	
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
				backgroundHandler.post( new BackgroundRunnable(false) );
			}
		} );
		
		// activate background task
		if( backgroundRunnable == null ){
			backgroundRunnable = new BackgroundRunnable();
		}
		backgroundHandler.post( new BackgroundRunnable(false) );
		backgroundHandler.postDelayed( backgroundRunnable, backgroundTaskDelay );
		
		return rootView;
	}
	
	/**
	 * Called if activity is hidden
	 */
	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
		if( isVisibleToUser ){			
			// start new background task
			if( backgroundRunnable == null ){
				backgroundRunnable = new BackgroundRunnable();
				backgroundHandler.postDelayed( backgroundRunnable, backgroundTaskDelay );
			}			
		}
		else{			
			// stop background task
			backgroundHandler.removeCallbacks(null);
			backgroundRunnable = null;			
		}
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
			
			Log.i(getClass().getName(), "Connecting to " + host + ":" + port);
			
			// try to connect to nao
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
