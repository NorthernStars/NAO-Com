package de.robotik.nao.communicator.network.data.request;

import de.robotik.nao.communicator.network.data.NAOCommands;

public class DataRequestPackage {

	/**
	 * Command to execute
	 */
	public NAOCommands command;
	public String[] commandArguments;
	
	/**
	 * Constructor
	 */
	public DataRequestPackage() {
		command = NAOCommands.SYS_GET_INFO;
		commandArguments = new String[0];
	}
	
	
	public DataRequestPackage(NAOCommands aCommand, String[] aArgs) {
		command = aCommand;
		commandArguments = aArgs;
	}
	
	public String toString(){
		String ret = "" + command;
		for( String s : commandArguments ){
			ret += "\n\t" + s;
		}		
		
		return ret;
	}
	
}
