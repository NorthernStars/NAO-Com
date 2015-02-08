package de.robotik.nao.communicator.core.widgets;

import java.util.ArrayList;
import java.util.List;
import javax.jmdns.ServiceEvent;

import de.robotik.nao.communicator.R;
import de.robotik.nao.communicator.MainActivity;
import de.robotik.nao.communicator.core.interfaces.NAOInterface;
import de.robotik.nao.communicator.core.revisions.ServerRevision;
import de.robotik.nao.communicator.network.ConnectionState;
import de.robotik.nao.communicator.network.NAOConnector;
import de.robotik.nao.communicator.network.data.response.DataResponsePackage;
import de.robotik.nao.communicator.network.interfaces.NetworkDataRecievedListener;
import de.robotik.nao.communicator.network.interfaces.NetworkServiceHandler;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

public class RemoteDevice extends LinearLayout implements
	NetworkServiceHandler,
	NetworkDataRecievedListener,
	NAOInterface,
	OnClickListener,
	OnLongClickListener,
	ActionMode.Callback{
	
	/**
	 * Network service token
	 */
	public static final String workstationNetworkServiceToken = "_workstation._tcp.local.";
	public static final String networkTokenLocal= ".local.";
	public static final String naoNetworkServiceToken = "_nao._tcp.local.";
	public static final String naoqiNetworkServiceToken = "_naoqi._tcp.local.";
	public static final String sshNetworkServiceToken = "_ssh._tcp.local.";
	
	/**
	 * Remote device information
	 */
	private NAOConnector connector = null;
	private List<String> services = new ArrayList<String>();	
	
	
	/**
	 * Layout
	 */
	private String workstationName = null;
	
	private LinearLayout mRootView;
	private TextView txtName;
	private TextView txtNAOqi;
	private TextView txtSSH;
	private ImageView imgLogo;
	private ImageView imgUpdate;
	private ProgressBar pgbLoading;
	
	private ActionMode mActionMode;
	
	/**
	 * Default Constructor
	 * @param context	Layout {@link Context}
	 */
	@SuppressLint("InflateParams")
	public RemoteDevice(Context context) {
		super(context);
		setOrientation(LinearLayout.VERTICAL);
		
		// get root view
		mRootView = (LinearLayout) inflate(getContext(), R.layout.remote_device, null);
		addView(mRootView);
		
		// get components
		txtName = (TextView) findViewById(R.id.txtDevicename);
		imgLogo = (ImageView) findViewById(R.id.imgDevice);
		imgUpdate = (ImageView) findViewById(R.id.imgDeviceUpdate);
		txtNAOqi = (TextView) findViewById(R.id.txtNAOqi);
		txtSSH = (TextView) findViewById(R.id.txtSSH);
		pgbLoading = (ProgressBar) findViewById(R.id.pgbSettingsPlaySoundLoading);
		
		// set listener
		MainActivity.getInstance().registerForContextMenu(this);
		setOnClickListener(this);
		setOnLongClickListener(this);
		imgUpdate.setOnClickListener(this);
		
		MainActivity.getInstance().addNetworkDataRecievedListener(this);
	}
	
	/**
	 * Sets if to show loading animation.
	 * @param aLoading	Set {@code true} to show loading animation, {@code false} otherwise.
	 */
	public void setLoading(boolean aLoading){
		if( aLoading ){
			imgLogo.setVisibility(View.GONE);
			pgbLoading.setVisibility(View.VISIBLE);
		} else {
			imgLogo.setVisibility(View.VISIBLE);
			pgbLoading.setVisibility(View.GONE);
		}
	}
	
	/**
	 * Updates view's components like device text
	 */
	public void updateView(){	
		
		new Thread( new Runnable(){			
			public void run() {
				
				String name = getWorkstationName();
				String ip = "";
				int imgRes = R.drawable.unknown_device;
				
				// check name
				if( name == null ){
					name = getResources().getString(R.string.net_unknown_device);
				}
				
				// check ip
				if( getHostAdresses().size() > 0 ){
					ip = " [" + getHostAdresses().get(0) + "]";
				}
				
				// check image
				if( isNAO() ){
					imgRes = R.drawable.robot_off;
				}
				if( hasCommunicationServer() ){
					imgRes = R.drawable.robot_on;
				}		
				
				// set device name and image
				txtName.setText( name + ip );
				imgLogo.setImageResource(imgRes);
				
				// check services
				if( hasNAOqi() ){
					txtNAOqi.setTextColor( getResources().getColor(R.color.darkblue) );
				}
				else{
					txtNAOqi.setTextColor( getResources().getColor(R.color.inactive_text) );
				}
				
				if( hasSSH() ){
					txtSSH.setTextColor( getResources().getColor(R.color.darkblue) );
				}
				else{
					txtSSH.setTextColor( getResources().getColor(R.color.inactive_text) );
				}
				
				// check if connected > set background color
				setLoading( !isConnected() );
				if( isConnected() ) {
					setBackgroundColor(
							getResources().getColor(R.color.lighterlightblue) );
				} else {
					// set transparent background
					setBackgroundColor( Color.TRANSPARENT );
				}
				
			}
		} ).start();
		
	}
	
	/**
	 * @return {@link String} Name of workstation
	 */
	public String getWorkstationName(){
		return workstationName;
	}
	
	/**
	 * @param name {@link String} of workstations name
	 */
	public void setWorkstationName(String name){
		workstationName = name;
		txtName.setText(workstationName);
	}
	
	/**
	 * Gets status of network service.
	 * @param serviceToken	{@link String} of service token
	 * @return 				{@code true} if service is available, {@code false} otherwise.
	 */
	private boolean getServiceStatus(String serviceToken){
		synchronized (services) {
			for( String vService : services ){
				if( vService.equals(serviceToken) ){
					return true;
				}
			}
		}		
		
		return false;
	}
	

	@Override
	public void addNetworkService(ServiceEvent service) {
		String serviceType = service.getType();
		
		synchronized (services) {
			services.add(serviceType);
		}		
		
		// check connector and add host adresses
		if(connector == null){
			connector = new NAOConnector(service);
		}
		
		connector.addHostAdresses( service.getInfo().getHostAddresses() );
		
		// check for communication server or only nao
		if( serviceType.contains(NAOConnector.serverNetworkServiceToken) ){
			setWorkstationName( service.getName() );
		}		

		updateView();
	}

	@Override
	public void removeNetworkService(ServiceEvent service) {
		synchronized (services) {
			services.remove(service.getType());
		}
		updateView();
	}

	@Override
	public void onClick(View v) {
		if( v == this ){
			
			// connect / disconnect
			if( isConnected() ){
				disconnect();
			} else {
				connect();
			}
			
		} else if( v == imgUpdate ) {
			
			// update
			disconnect();
			MainActivity.getInstance().startInstaller(this, true);
			
		}
	}
	
	@Override
	public boolean onLongClick(View v) {
		if( mActionMode != null ){
			return false;
		}
		
		// Start options menu
		mActionMode = MainActivity.getInstance().startActionMode(this);
		setBackgroundColor( getResources().getColor(R.color.lightgray) );
		return true;
	}

	@Override
	public void onNetworkDataRecieved(DataResponsePackage data) {
		// TODO: check for online update
		
//		ServerRevision vOnlineRevision = MainActivity.getInstance().getOnlineRevision();
//		RemoteDevice vConnectedRemoteDevice = MainActivity.getInstance().getConnectedDevice();
//		
//		if( vConnectedRemoteDevice == this && vOnlineRevision != null
//				&& vOnlineRevision.getRevision() >= 0 ){
//			
//			if( data.revision < vOnlineRevision.getRevision()
//				&& imgUpdate.getVisibility() == View.GONE ){			
//				MainActivity.getInstance().runOnUiThread(new Runnable() {				
//					@Override
//					public void run() {
//						imgUpdate.setVisibility( View.VISIBLE );
//					}
//				});			
//			} else if( data.revision >= vOnlineRevision.getRevision()
//					&& imgUpdate.getVisibility() == View.VISIBLE ) {				
//				MainActivity.getInstance().runOnUiThread(new Runnable() {				
//					@Override
//					public void run() {
//						imgUpdate.setVisibility( View.GONE );
//					}
//				});				
//			}
//			
//		}
	}

	@Override
	public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
		switch( item.getItemId() ){
		
		case R.id.itRemoteDeviceServerInstall:
			disconnect();
			MainActivity.getInstance().startInstaller(this, false);
			break;
			
		case R.id.itRemoteDeviceServerUpdate:
			disconnect();
			MainActivity.getInstance().startInstaller(this, true);
			break;
			
		case R.id.itRemoteDeviceReboot:
			new Thread(new Runnable() {				
				@Override
				public void run() {
					connector.sendSSHCommands( new String[]{ "sudo -S -p '' shutdown -r now" },
							new String[]{ "%%PW%%" });
				}
			}).start();			
			break;
			
		case R.id.itRemoteDeviceShutdown:
			new Thread(new Runnable() {				
				@Override
				public void run() {
					connector.sendSSHCommands( new String[]{ "sudo -S -p '' shutdown -h now" },
							new String[]{ "%%PW%%" });
				}
			}).start();	
			break;
			
		default:
			return false;
		}
		
		mode.finish();
		return true;
	}

	@Override
	public boolean onCreateActionMode(ActionMode mode, Menu menu) {
		mode.getMenuInflater().inflate(R.menu.remote_device_options_menu, menu);
		
		// disable server install if server is already installed
		if( hasCommunicationServer() ){
			menu.findItem(R.id.itRemoteDeviceServerInstall).setVisible(false);
		} else {
			menu.findItem(R.id.itRemoteDeviceServerUpdate).setVisible(false);
		}
		return true;
	}

	@Override
	public void onDestroyActionMode(ActionMode mode) {
		setBackgroundColor( Color.TRANSPARENT );
		mActionMode = null;
	}

	@Override
	public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
		return false;
	}
	
	@Override
	public boolean connect() {
		disconnect();
		if( connector != null ){
			
			// check if to restart connector
			if( connector.getConnectionState() != ConnectionState.CONNECTION_INIT ){
				connector = new NAOConnector(connector);
			}
			
			// start connector thread
			connector.start();
			setLoading(true);
			
			return true;
		}
		
		return false;
	}

	@Override
	public void disconnect() {
		if( connector != null ){
			connector.stopConnector();
		}
	}
	
	@Override
	public boolean isConnected() {
		return (connector != null
				&& connector.getConnectionState() == ConnectionState.CONNECTION_ESTABLISHED );
	}

	@Override
	public String getName() {
		return getWorkstationName();
	}

	@Override
	public boolean hasNAOqi() {
		return getServiceStatus(naoqiNetworkServiceToken);
	}

	@Override
	public boolean hasSSH() {
		return getServiceStatus(sshNetworkServiceToken);
	}

	@Override
	public boolean isNAO() {
		return getServiceStatus(naoNetworkServiceToken);
	}

	@Override
	public boolean hasCommunicationServer() {
		return getServiceStatus(NAOConnector.serverNetworkServiceToken);
	}

	@Override
	public List<String> getHostAdresses() {
		if( connector != null ){
			return connector.getHostAdresses();
		}
		return new ArrayList<String>();
	}

	@Override
	public void addHostAdress(String aAdress) {
		if( connector != null ){
			connector.addHostAdress(aAdress);
		} else {
			connector = new NAOConnector(aAdress, NAOConnector.defaultPort);
		}
	}
	
	@Override
	public boolean hasAdress(String aAdress) {
		return connector.getHostAdresses().contains(aAdress);
	}

}
