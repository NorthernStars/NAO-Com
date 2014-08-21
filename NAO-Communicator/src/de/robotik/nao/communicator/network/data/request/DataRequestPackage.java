package de.robotik.nao.communicator.network.data.request;

import de.robotik.nao.communicator.network.data.NAOCommands;

public class DataRequestPackage {

	/**
	 * Command to execute
	 */
	public RequestType type;
	public NAOCommands command;
	public String[] commandArguments;
	
	/**
	 * Constructor
	 */
	public DataRequestPackage() {
		type = RequestType.REQUEST;
		command = NAOCommands.SYS_GET_INFO;
		commandArguments = new String[0];
	}
	
	
	public DataRequestPackage(RequestType aType, NAOCommands aCommand, String[] aArgs) {
		type = aType;
		command = aCommand;
		commandArguments = aArgs;
	}
	
}
