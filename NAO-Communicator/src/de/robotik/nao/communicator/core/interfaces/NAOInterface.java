package de.robotik.nao.communicator.core.interfaces;

import java.util.List;

public interface NAOInterface {

	/**
	 * Connects to NAO
	 * @return {@code true} if connecting process started, {@code false} otherwise
	 */
	public boolean connect();
	
	/**
	 * Reconnects to NAO and replaces existing connection.
	 * @return {@code true} if connecting process started, {@code false} otherwise
	 */
	public boolean reconnect();
	
	/**
	 * Disconnect from NAO
	 */
	public void disconnect();
	
	/**
	 * @return Name of NAO
	 */
	public String getName();
	
	/**
	 * @return {@code true} if NAO has naoqi running, {@code false} otherwise
	 */
	public boolean hasNAOqi();
	
	/**
	 * @return {@code true} if NAO has ssh server running, {@code false} otherwise
	 */
	public boolean hasSSH();
	
	/**
	 * @return {@code true} if NAO has sftp server running, {@code false} otherwise
	 */
	public boolean hasSFTP();
	
	/**
	 * @return {@code true} if NAO has nao network service running, {@code false} otherwise
	 */
	public boolean isNAO();
	
	/**
	 * @return {@code true} if NAO has nao communication server running, {@code false} otherwise
	 */
	public boolean hasCommunicationServer();
	
	/**
	 * @return Address of remote host
	 */
	public List<String> getHostAdresses();
	
	/**
	 * Add new host address
	 * @param aAdress	{@link String} host address
	 */
	public void addHostAdress(String aAdress);
	
}
