package de.robotik.nao.communicator.core.widgets;

import javax.jmdns.ServiceEvent;

import de.northernstars.naocom.R;
import de.robotik.nao.communicator.core.MainActivity;
import de.robotik.nao.communicator.core.RemoteNAO;
import de.robotik.nao.communicator.network.data.response.DataResponsePackage;
import de.robotik.nao.communicator.network.interfaces.NetworkDataRecievedListener;
import de.robotik.nao.communicator.network.interfaces.NetworkServiceHandler;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class RemoteDevice implements
	NetworkServiceHandler,
	NetworkDataRecievedListener,
	OnClickListener {

	public static final String workstationNetworkServiceToken = "_workstation._tcp.local.";
	public static final String networkServiceLocalToken = ".local.";
	
	private RemoteNAO nao;
	private String workstationName = null;
	
	private View mView;
	private TextView txtName;
	private TextView txtNAOqi;
	private TextView txtSSH;
	private TextView txtSFTP;
	private ImageView imgLogo;
	private ProgressBar pgbLoading;
	
	/**
	 * Constructor
	 * @param context	Layout {@link Context}
	 * @param service	{@link ServiceEvent}
	 */
	public RemoteDevice(Context context, ServiceEvent service) {
		this(context);
		
		// add network service
		addNetworkService(service);
	}
	
	/**
	 * Constructor
	 * @param aContext	Layout {@link Context}
	 * @param aHost		{@link String} of remote host name
	 */
	public RemoteDevice(Context aContext, String aHost){
		this(aContext);		
		txtName.setText(aHost);
		addAdress(aHost);
	}
	
	/**
	 * Default Constructor
	 * @param context	Layout {@link Context}
	 */
	@SuppressLint("InflateParams")
	public RemoteDevice(Context context){
		nao = new RemoteNAO();
		
		// inflate detail layout
		LayoutInflater inflater = LayoutInflater.from(context);
		mView = inflater.inflate( R.layout.remote_device, null );
		
		// get components
		txtName = (TextView) mView.findViewById(R.id.txtDevicename);
		imgLogo = (ImageView) mView.findViewById(R.id.imgDevice);
		txtNAOqi = (TextView) mView.findViewById(R.id.txtNAOqi);
		txtSSH = (TextView) mView.findViewById(R.id.txtSSH);
		txtSFTP = (TextView) mView.findViewById(R.id.txtSFTP);
		pgbLoading = (ProgressBar) mView.findViewById(R.id.pgbSettingsPlaySoundLoading);
		
		mView.setOnClickListener(this);
	}
	
	/**
	 * @return {@link View} of the remote device
	 */
	public View getView(){
		return mView;
	}
	
	/**
	 * Updates view's components like device text
	 */
	public void updateView(){	
		
		new AsyncTask<Void, Void, Void>(){

			@Override
			protected Void doInBackground(Void... params) {
				return null;
			}
			
			protected void onPostExecute(Void result) {
				
				String name = nao.getName();
				String ip = "";
				int imgRes = R.drawable.unknown_device;
				
				// check name
				if( name == null ){
					if( getWorkstationName() == null ){
						name = mView.getResources().getString(R.string.net_unknown_device);
					}
					else{
						name = getWorkstationName();
					}
				}
				
				// check ip
				if( nao.getHostAdresses().size() > 0 ){
					ip = " [" + nao.getHostAdresses().get(0) + "]";
				}
				
				// check image
				if( nao.isNAO() ){
					imgRes = R.drawable.robot_off;
				}
				if( nao.hasCommunicationServer() ){
					imgRes = R.drawable.robot_on;
				}		
				
				// set device name and image
				txtName.setText( name + ip );
				imgLogo.setImageResource(imgRes);
				
				// check services
				if( nao.hasNAOqi() ){
					txtNAOqi.setTextColor( mView.getResources().getColor(R.color.darkblue) );
				}
				else{
					txtNAOqi.setTextColor(  mView.getResources().getColor(R.color.inactive_text) );
				}
				
				if( nao.hasSSH() ){
					txtSSH.setTextColor(  mView.getResources().getColor(R.color.darkblue) );
				}
				else{
					txtSSH.setTextColor(  mView.getResources().getColor(R.color.inactive_text) );
				}
				
				if( nao.hasSFTP() ){
					txtSFTP.setTextColor(  mView.getResources().getColor(R.color.darkblue) );
				}
				else{
					txtSFTP.setTextColor(  mView.getResources().getColor(R.color.inactive_text) );
				}
				
			}
			}.execute();
		
	}

	/**
	 * @return Underlying {@link RemoteNAO} object
	 */
	public RemoteNAO getNao() {
		return nao;
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
		workstationName = name.substring(0, name.indexOf(networkServiceLocalToken));
	}
	

	/**
	 * @param adress {@link String}
	 * @return {@code true} if device has network device with {@code adress}, {@code false} otherwise
	 */
	public boolean hasAdress(String adress){
		for( String host : nao.getHostAdresses() ){
			if( host.contains(adress) ){
				return true;
			}
		}		
		return false;
	}
	
	/**
	 * Adds a new host address.
	 * @param adress	{@link String} host address.
	 */
	public void addAdress(String adress){
		if( !hasAdress(adress) ){
			nao.addHostAdress(adress);
		}
	}
	
	/**
	 * Connects the device to remote server
	 * @return	{@code true} if successful, {@code false} otherwise.
	 */
	public boolean connect(){
		// check if to disconnect from other NAO
		RemoteDevice remoteDevice = MainActivity.getInstance().getConnectedDevice();
		if( remoteDevice != null ){
			remoteDevice.getNao().disconnect();
			MainActivity.getInstance().setConnectedDevice( null );
		}
		
		if( remoteDevice != this && getNao().connect() ){
			MainActivity.getInstance().setConnectedDevice( this );
			imgLogo.setVisibility( View.GONE );
			pgbLoading.setVisibility( View.VISIBLE );	
			return true;
		}
		
		return false;
	}
	

	@Override
	public void addNetworkService(ServiceEvent service) {
		nao.addNetworkService(service);
		if( nao.getName() == null ){
			setWorkstationName(service.getInfo().getServer());
		}

		updateView();
	}

	@Override
	public void removeNetworkService(ServiceEvent service) {
		nao.removeNetworkService(service);
		updateView();
	}

	@Override
	public void onClick(View v) {
		connect();
	}

	/**
	 * Updates device background depending on its connection state.
	 */
	public void updateDeviceBackground() {	
			MainActivity.getInstance().runOnUiThread(new Runnable() {				
				@Override
				public void run() {
					// check if connected > set background color
					if( getNao().isConnected() ) {
						getView().setBackgroundColor(
								getView().getResources().getColor(R.color.lighterlightblue) );
					} else {
						// set transparent background
						getView().setBackgroundColor( Color.TRANSPARENT );
					}
					
					// disable loading progress bar
					imgLogo.setVisibility( View.VISIBLE );
					pgbLoading.setVisibility( View.GONE );
				}
			});
	}

	@Override
	public void onNetworkDataRecieved(DataResponsePackage data) {
		int vOnlineRevision = MainActivity.getInstance().getOnlineRevision();
		if( vOnlineRevision >= 0 && data.revision < vOnlineRevision ){
			System.out.println("update");
		}
	}

}
