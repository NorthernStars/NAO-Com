package de.robotik.nao.communicator.core;

import java.util.HashMap;
import java.util.Map;

import android.net.nsd.NsdServiceInfo;
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

	private static final String naoDefaultName = "unknown NAO"; 
	public static final String naoNetworkServiceToken = "_nao._tcp.local.";
	public static final String naoqiNetworkServiceToken = "_naoqi._tcp.local.";
	public static final String sshNetworkServiceToken = "_ssh._tcp.local.";
	public static final String sftpNetworkServiceToken = "_sftp-ssh._tcp.local.";
	
	
	private NAOConnector connector = null;
	private Map<String, Boolean> services = new HashMap<String, Boolean>();	
	private String name = null;
	
	/**
	 * Constructor using {@link NsdServiceInfo} to set remote device information
	 * @param serviceInfo
	 */
	public RemoteNAO(NsdServiceInfo serviceInfo){
		connector = new NAOConnector( serviceInfo );
		addNetworkService(serviceInfo);
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
	public void connect(){
		connector.start();
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
	public void addNetworkService(NsdServiceInfo service) {
		String serviceType = service.getServiceType();
		services.put(serviceType, true);
		
		if( serviceType.contains(NAOConnector.serverNetworkServiceToken) ){
			name = service.getServiceName();
		}
		else if( serviceType.contains(naoNetworkServiceToken) ){
			name = naoDefaultName;			
		}
	}

	@Override
	public void removeNetworkService(NsdServiceInfo service) {
		services.remove(service.getServiceType());
	}

	@Override
	public String getHostAdress() {
		return connector.getHost();
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
