package de.robotik.nao.communicator.network.interfaces;

import de.robotik.nao.communicator.network.data.response.DataResponsePackage;

public interface NetworkDataSender {

	/**
	 * Adds a {@link NetworkDataRecievedListener} to this connector
	 * @param listener {@link NetworkDataRecievedListener} to add
	 */
	public void addNetworkDataRecievedListener(NetworkDataRecievedListener listener);
	
	/**
	 * Removes a {@link NetworkDataRecievedListener} from this connector.
	 * If {@code listener} is {@code null} all listeners are removed.
	 * @param listener {@link NetworkDataRecievedListener} to remove or {@code null}
	 */
	public void removeNetworkDataRecievedListener(NetworkDataRecievedListener listener);
	
	/**
	 * Notifies all listeners
	 * @param data
	 */
	public void notifyDataRecievedListeners(DataResponsePackage data);
	
}
