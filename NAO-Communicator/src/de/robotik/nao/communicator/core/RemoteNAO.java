package de.robotik.nao.communicator.core;

import de.robotik.nao.communicator.core.interfaces.NAOInterface;
import de.robotik.nao.communicator.network.NAOConnector;
import de.robotik.nao.communicator.network.NetworkDataRecievedListener;
import de.robotik.nao.communicator.network.hotspot.ClientScanResult;

/**
 * Class for handle connection to remote nao
 * and call functions
 * @author Hannes Eilers
 *
 */
public class RemoteNAO implements NAOInterface, NetworkDataRecievedListener {

	private NAOConnector connector = null;
	
	private String name = "";
	
	public RemoteNAO(ClientScanResult client){
		this( client.getIpAddr(), NAOConnector.defaultPort );
	}
	
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
	
}
