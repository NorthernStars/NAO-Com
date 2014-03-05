package de.robotik.nao.communicator.network.data.request;

public class DataRequestPackage {

	public RequestType naoName = RequestType.SKIP;
	public RequestType batteryLevel = RequestType.SKIP;
	public RequestType stiffnessData = RequestType.SKIP;
	public AudioRequests audioRequests = new AudioRequests();
	
	
}
