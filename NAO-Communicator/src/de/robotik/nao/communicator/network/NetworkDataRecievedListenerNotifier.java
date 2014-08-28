package de.robotik.nao.communicator.network;

import de.robotik.nao.communicator.network.data.response.DataResponsePackage;
import de.robotik.nao.communicator.network.interfaces.NetworkDataRecievedListener;

/**
 * Class of interface {@link Runnable} to notify a {@link NetworkDataRecievedListener}
 * @author Hannes Eilers
 *
 */
public class NetworkDataRecievedListenerNotifier implements Runnable{

	private DataResponsePackage data;
	private NetworkDataRecievedListener listener = null;
	
	public NetworkDataRecievedListenerNotifier(NetworkDataRecievedListener aListener, DataResponsePackage aData) {
		data = aData;
		listener = aListener;
	}
	
	@Override
	public void run() {
		listener.onNetworkDataRecieved(data);
	}
	
}
