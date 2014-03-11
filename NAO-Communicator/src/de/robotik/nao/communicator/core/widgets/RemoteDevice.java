package de.robotik.nao.communicator.core.widgets;

import javax.jmdns.ServiceEvent;

import de.northernstars.naocom.R;
import de.robotik.nao.communicator.core.RemoteNAO;
import de.robotik.nao.communicator.network.interfaces.NetworkServiceHandler;
import android.content.Context;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class RemoteDevice implements NetworkServiceHandler {

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
	
	public RemoteDevice(Context context, ServiceEvent service) {
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
		
		// add network service
		addNetworkService(service);
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
	 * @return Underlaying {@link RemoteNAO} object
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

}
