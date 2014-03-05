package de.robotik.nao.communicator.core.interfaces;

public interface NAOInterface {

	/**
	 * Connects to NAO
	 */
	public void connect();
	
	/**
	 * Disconnect from NAO
	 */
	public void disconnect();
	
	/**
	 * @return Name of NAO
	 */
	public String getName();
	
}
