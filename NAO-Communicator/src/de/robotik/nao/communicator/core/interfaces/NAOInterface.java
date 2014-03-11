package de.robotik.nao.communicator.core.interfaces;

import java.util.List;

public interface NAOInterface {

	/**
	 * Connects to NAO
	 * @return {@code true} if connecting process started, {@code false} otherwise
	 */
	public boolean connect();
	
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
	 * @return Adress of remote host
	 */
	public List<String> getHostAdresses();
	
}
