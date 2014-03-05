package de.robotik.nao.communicator.core;

import java.util.HashMap;
import java.util.Map;

import android.net.nsd.NsdServiceInfo;
import de.robotik.nao.communicator.core.interfaces.NAOInterface;
import de.robotik.nao.communicator.network.NAOConnector;
import de.robotik.nao.communicator.network.NetworkDataRecievedListener;
import de.robotik.nao.communicator.network.NetworkServiceHandler;

/**
 * Class for handle connection to remote nao
 * and call functions
 * @author Hannes Eilers
 *
 */
public class RemoteNAO implements NAOInterface, NetworkDataRecievedListener, NetworkServiceHandler {

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
	public String getName() {
		return name;
	}

	@Override
	public void addNetworkService(NsdServiceInfo service) {
		services.put(service.getServiceType(), true);
		
		if( service.getServiceType().contains(NAOConnector.serverNetworkServiceToken) ){
			name = service.getServiceName();
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
	
}
