package de.robotik.nao.communicator.network;

import android.net.nsd.NsdServiceInfo;

public interface NetworkServiceHandler {

	/**
	 * Adds a resolved {@link NsdServiceInfo} network service
	 * @param service Resolved {@link NsdServiceInfo}
	 */
	public void addNetworkService(NsdServiceInfo service);
	
	/**
	 * Removes a resolved {@link NsdServiceInfo} network service
	 * @param service Resolved {@link NsdServiceInfo}
	 */
	public void removeNetworkService(NsdServiceInfo service);
	
}
