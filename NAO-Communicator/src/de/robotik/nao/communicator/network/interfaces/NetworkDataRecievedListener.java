package de.robotik.nao.communicator.network.interfaces;

import de.robotik.nao.communicator.network.data.response.DataResponsePackage;

public interface NetworkDataRecievedListener {

	public void onNetworkDataRecieved(DataResponsePackage data);
	
}
