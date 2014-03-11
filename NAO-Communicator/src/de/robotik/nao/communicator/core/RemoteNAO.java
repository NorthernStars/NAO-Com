package de.robotik.nao.communicator.core;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jmdns.ServiceEvent;

import de.robotik.nao.communicator.core.interfaces.NAOInterface;
import de.robotik.nao.communicator.network.NAOConnector;
import de.robotik.nao.communicator.network.interfaces.NetworkDataRecievedListener;
import de.robotik.nao.communicator.network.interfaces.NetworkServiceHandler;

/**
 * Class for handle connection to remote nao
 * and call functions
 * @author Hannes Eilers
 *
 */
public class RemoteNAO implements NAOInterface, NetworkDataRecievedListener, NetworkServiceHandler {

	public static final String naoNetworkServiceToken = "_nao._tcp.local.";
	public static final String naoqiNetworkServiceToken = "_naoqi._tcp.local.";
	public static final String sshNetworkServiceToken = "_ssh._tcp.local.";
	public static final String sftpNetworkServiceToken = "_sftp-ssh._tcp.local.";
	
	
	private NAOConnector connector = null;
	private Map<String, Boolean> services = new HashMap<String, Boolean>();	
	private String name = null;
	
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

	
	@Override
	public boolean connect(){
		if( connector != null ){
			connector.start();
			return true;
		}
		return false;
	}
	@Override
	public void disconnect(){
		connector.stopConnector();
	}

	@Override
	public void onNetworkDataRecieved(String data) {
		System.out.println("NEW DATA: " + data);
	}

	@Override
	public void addNetworkService(ServiceEvent service) {
		String serviceType = service.getType();
		services.put(serviceType, true);
		
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
		services.remove(service.getType());
	}

	@Override
	public List<String> getHostAdresses() {
		return connector.getHostAdresses();
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
		for( String key : services.keySet() ){
			if( key.contains(serviceToken) ){
				return services.get(key);
			}
		}
		
		return false;
	}
	
}
