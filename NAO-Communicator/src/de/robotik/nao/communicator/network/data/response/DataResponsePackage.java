package de.robotik.nao.communicator.network.data.response;

import de.robotik.nao.communicator.network.data.AudioData;
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
	
	/**
	 * Requested data
	 */
	public String naoName;
	public int batteryLevel;
	public StiffnessData stiffnessData;
	public AudioData audioData;
	
}
