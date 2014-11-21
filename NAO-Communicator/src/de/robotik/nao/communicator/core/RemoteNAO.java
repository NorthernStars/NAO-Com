package de.robotik.nao.communicator.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jmdns.ServiceEvent;

import de.robotik.nao.communicator.MainActivity;
import de.robotik.nao.communicator.core.interfaces.NAOInterface;
import de.robotik.nao.communicator.core.widgets.RemoteDevice;
import de.robotik.nao.communicator.network.ConnectionState;
import de.robotik.nao.communicator.network.NAOConnector;
import de.robotik.nao.communicator.network.NetworkDataRecievedListenerNotifier;
import de.robotik.nao.communicator.network.data.NAOCommands;
import de.robotik.nao.communicator.network.data.response.DataResponsePackage;
import de.robotik.nao.communicator.network.interfaces.NetworkDataRecievedListener;
import de.robotik.nao.communicator.network.interfaces.NetworkDataSender;
import de.robotik.nao.communicator.network.interfaces.NetworkServiceHandler;

/**
 * Class for handle connection to remote nao
 * and call functions
 * @author Hannes Eilers
 *
 */
public class RemoteNAO implements NAOInterface, NetworkDataSender, NetworkDataRecievedListener, NetworkServiceHandler {

	public static final String naoNetworkServiceToken = "_nao._tcp.local.";
	public static final String naoqiNetworkServiceToken = "_naoqi._tcp.local.";
	public static final String sshNetworkServiceToken = "_ssh._tcp.local.";
	public static final String sftpNetworkServiceToken = "_sftp-ssh._tcp.local.";
	
	
	private NAOConnector connector = null;
	private Map<String, Boolean> services = new HashMap<String, Boolean>();	
	private String name = null;
	private List<NetworkDataRecievedListener> dataRecievedListener = new ArrayList<NetworkDataRecievedListener>();
	
	public RemoteNAO() {
	}
	
	/**
	 * Constructor using {@link ServiceEvent} to set remote device information
	 * @param serviceInfo
	 */
	public RemoteNAO(ServiceEvent service){
		addNetworkService(service);
	}
	
	/**
	 * Constructor to set remote device
	 * @param host
	 * @param port
	 */
	public RemoteNAO(String host, int port) {
		connector = new NAOConnector(host, port);
	}
	
	/**
	 * @return	{@code true} if remote NAO is connected, {@code false} otherwise.
	 */
	public boolean isConnected(){
		if( connector != null
				&& connector.getConnectionState() == ConnectionState.CONNECTION_ESTABLISHED ){
			return true;
		}
		
		return false;
	}
	
	@Override
	public void addNetworkDataRecievedListener(NetworkDataRecievedListener listener){
		dataRecievedListener.add(listener);
	}
	
	@Override
	public void removeNetworkDataRecievedListener(NetworkDataRecievedListener listener){
		if( listener == null ){
			dataRecievedListener.clear();
		}
		else{
			dataRecievedListener.remove(listener);
		}
	}
	
	@Override
	public void notifyDataRecievedListeners(DataResponsePackage data){
		for( NetworkDataRecievedListener listener : dataRecievedListener ){
			Runnable r = new NetworkDataRecievedListenerNotifier(listener, data);
			new Thread(r).start();
		}
	}
	
	@Override
	public boolean connect(){
		if( connector != null ){
			
			if( connector.getConnectionState() != ConnectionState.CONNECTION_INIT ){

				disconnect();
				connector = new NAOConnector(connector);
				
			} 
			
			connector.addNetworkDataRecievedListener(this);
			connector.start();			
			return true;
		}
		
		return false;
	}
	
	@Override
	public boolean reconnect() {		
		if( connector != null ){
			// disconnect existing connection
			disconnect();
			
			// reset connector
			connector = new NAOConnector(connector);
		}	
		
		// connect and return result
		return connect();
	}
	
	@Override
	public void disconnect(){
		if( connector != null ){
			connector.removeNetworkDataRecievedListener(this);
			connector.stopConnector();
		}
	}

	@Override
	public void onNetworkDataRecieved(DataResponsePackage data) {
		notifyDataRecievedListeners(data);
		
		if( data.request.command == NAOCommands.SYS_DISCONNECT
				&& data.requestSuccessfull ){
			MainActivity.getInstance().updateTitle( "[offline] NAO Communicator" );
		}
	}

	@Override
	public void addNetworkService(ServiceEvent service) {
		String serviceType = service.getType();
		
		synchronized (services) {
			services.put(serviceType, true);
		}		
		
		// check connector and add host adresses
		if(connector == null){
			connector = new NAOConnector(service);
		}
		
		connector.addHostAdresses( service.getInfo().getHostAddresses() );
		
		// check for communication server or only nao
		if( serviceType.contains(NAOConnector.serverNetworkServiceToken) ){
			name = service.getName();
		}		
	}

	@Override
	public void removeNetworkService(ServiceEvent service) {
		synchronized (services) {
			services.remove(service.getType());
		}
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
	public String getName() {
		return name;
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
	public boolean hasSFTP() {
		return getServiceStatus(sftpNetworkServiceToken);
	}
	
	@Override
	public boolean isNAO() {
		return getServiceStatus(naoNetworkServiceToken);
	}
	
	@Override
	public boolean hasCommunicationServer() {
		return getServiceStatus( NAOConnector.serverNetworkServiceToken );
	}
	
	/**
	 * @param serviceToken
	 * @return Status of the service
	 */
	private boolean getServiceStatus(String serviceToken){
		synchronized (services) {
			for( String key : services.keySet() ){
				if( key.contains(serviceToken) ){
					return services.get(key);
				}
			}
		}		
		
		return false;
	}
	
	/**
	 * @return	Currently connected {@link RemoteNAO} or {@code null} if none connected.
	 */
	public static synchronized RemoteNAO getCurrentRemoteNao(){
		RemoteDevice vRemoteDevice = MainActivity.getInstance().getConnectedDevice();
		if( vRemoteDevice != null
				&& vRemoteDevice.getNao() != null ){
			return vRemoteDevice.getNao();
		}
		
		return null;
	}
	
	/**
	 * Send {@link NAOCommands} to remote NAO.
	 * @param aCommand	{@link NAOCommands} to send.
	 * @param aArgs		Array of {@link String} arguments to for {@code aCommand}.
	 * @return			{@code true} if successful, {@code false} otherwise.
	 */
	public static boolean sendCommand(NAOCommands aCommand, String[] aArgs){
		RemoteNAO nao = getCurrentRemoteNao();
		if( nao != null ){
			return nao.connector.sendCommand(aCommand, aArgs);
		}
		
		return false;
	}
	
	/**
	 * Send {@link NAOCommands} to remote NAO.
	 * @param aCommand	{@link NAOCommands} to send.
	 * @return			{@code true} if successful, {@code false} otherwise.
	 */
	public static boolean sendCommand(NAOCommands aCommand){
		return sendCommand(aCommand, new String[]{});
	}

	/**
	 * @return {@link NAOConnector} of this {@link RemoteNAO}.
	 */
	public NAOConnector getConnector() {
		return connector;
	}
	
}
