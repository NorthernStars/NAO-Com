package de.robotik.nao.communicator.network.data.response;

import java.util.Map;

import de.robotik.nao.communicator.network.data.AudioData;
import de.robotik.nao.communicator.network.data.NAOAutonomousLifeStates;
import de.robotik.nao.communicator.network.data.StiffnessData;
import de.robotik.nao.communicator.network.data.request.DataRequestPackage;

/**
 * Class for storing servers response
 * @author Hannes Eilers
 *
 */
public class DataResponsePackage {

	public DataRequestPackage request;
	public boolean requestSuccessfull;
	
	public DataResponsePackage() {}
	
	public DataResponsePackage(DataRequestPackage aRequest, boolean aSuccessfull){
		request = aRequest;
		requestSuccessfull = aSuccessfull;
	}
	
	/**
	 * Requested data
	 */
	public long revision;
	public String naoName;
	public int batteryLevel;
	public NAOAutonomousLifeStates lifeState;
	public StiffnessData stiffnessData;
	public AudioData audioData;
	public Map<String, String> customMemoryEvents;
	
	public String toString(){
		String ret = "";
		
		ret += "REQUEST: " + request;
		ret += "\nrevision: " + revision;
		ret += "\nnaoName: " + naoName + "\tbatterylevel: " + batteryLevel;
		ret += "\nstiffnessData:\n" + stiffnessData;
		ret += "\naudioData:\n" + audioData;
		
		ret += "\ncustom memory events:";
		for( String key : customMemoryEvents.keySet() ){
			ret += "\n\t" + key + ": " + customMemoryEvents.get(key);
		}
		
		return ret;
	}
	
}
