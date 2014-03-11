package de.robotik.nao.communicator.network.interfaces;

import javax.jmdns.ServiceEvent;

public interface NetworkServiceHandler {

	/**
	 * Adds a resolved {@link ServiceEvent} network service
	 * @param service Resolved {@link ServiceEvent}
	 */
	public void addNetworkService(ServiceEvent service);
	
	/**
	 * Removes a resolved {@link ServiceEvent} network service
	 * @param service Resolved {@link ServiceEvent}
	 */
	public void removeNetworkService(ServiceEvent service);
	
}
