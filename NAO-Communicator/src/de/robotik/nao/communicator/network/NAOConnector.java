package de.robotik.nao.communicator.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import de.robotik.nao.communicator.network.interfaces.NetworkDataRecievedListener;

import android.net.nsd.NsdServiceInfo;

public class NAOConnector extends Thread {

	public static final String defaultHost = "nao.local";
	public static final int defaultPort = 9696;
	public static final int defaultReadTimeout = 300;
	public static final String serverNetworkServiceToken = "_naocomserver._tcp.local.";
	
	
	private String host = defaultHost;
	private int port = defaultPort;
	private BufferedReader in = null;
	private OutputStreamWriter out = null;
	
	private List<NetworkDataRecievedListener> dataRecievedListener = new ArrayList<NetworkDataRecievedListener>();
	
	private ConnectionState state = ConnectionState.CONNECTION_INIT;
	private boolean stop = false;
	private Socket socket = null;
	
	/**
	 * Constructor
	 * @param service Resolved {@link NsdServiceInfo}
	 */
	public NAOConnector(NsdServiceInfo service) {
		if( service.getServiceType().contains(serverNetworkServiceToken) ){
			host = service.getHost().getHostAddress();
			port = service.getPort();
		}
	}
	
	/**
	 * Constructor
	 * @param host
	 * @param port
	 */
	public NAOConnector(String host, int port) {
		this.host = host;
		this.port = port;
	}
	
	/**
	 * Function if thread started
	 */
	@Override
	public void run() {
		
		try{
			// create socket
			socket = new Socket(host, port); 
			socket.setSoTimeout(defaultReadTimeout);
			in = new BufferedReader( new InputStreamReader(socket.getInputStream()) );
			out = new OutputStreamWriter(socket.getOutputStream());
			state = ConnectionState.CONNECTION_ESTABLISHED;
			
			while( !stop && socket != null ){
				
				// handle socket
				String data = in.readLine();
				if( data != null ){
					notifyDataRecievedListeners( data.trim() );
				}
				
			}
			
			// Close connections
			in.close();
			out.close();
			socket.close();
			socket = null;
			
		}catch(UnknownHostException e){
			state = ConnectionState.CONNECTION_UNKNOWN_HOST;
			return;
		}catch(IOException e){
			state = ConnectionState.CONNECTION_ESTABLISHED_FAILED;
			return;
		}
		
		// set stop state
		state = ConnectionState.CONNECTION_CLOSED;
	}
	
	/**
	 * Notifies all listeners
	 * @param data
	 */
	private void notifyDataRecievedListeners(String data){
		for( NetworkDataRecievedListener listener : dataRecievedListener ){
			Runnable r = new NetworkDataRecievedListenerNotifier(listener, data);
			new Thread(r).start();
		}
	}
	
	/**
	 * Class of inrterface {@link Runnable} to notifiy a {@link NetworkDataRecievedListener}
	 * @author Hannes Eilers
	 *
	 */
	private class NetworkDataRecievedListenerNotifier implements Runnable{

		private String data = "";
		private NetworkDataRecievedListener listener = null;
		
		public NetworkDataRecievedListenerNotifier(NetworkDataRecievedListener aListener, String aData) {
			data = aData;
			listener = aListener;
		}
		
		@Override
		public void run() {
			listener.onNetworkDataRecieved(data);
		}
		
	}
	
	
	/**
	 * Adds a {@link NetworkDataRecievedListener} to this connector
	 * @param listener {@link NetworkDataRecievedListener} to add
	 */
	public void addNetworkDataRecievedListener(NetworkDataRecievedListener listener){
		dataRecievedListener.add(listener);
	}
	
	/**
	 * Removes a {@link NetworkDataRecievedListener} from this connector.
	 * If {@code listener} is {@code null} all listeners are removed.
	 * @param listener {@link NetworkDataRecievedListener} to remove or {@code null}
	 */
	public void removeNetworkDataRecievedListener(NetworkDataRecievedListener listener){
		if( listener == null ){
			dataRecievedListener.clear();
		}
		else{
			dataRecievedListener.remove(listener);
		}
	}
	
	public void stopConnector(){
		stop = true;
	}

	public boolean isStopped(){
		return (state == ConnectionState.CONNECTION_CLOSED ? true : false);
	}
	
	public ConnectionState getConnectionState(){
		return state;
	}

	/**
	 * @return the host
	 */
	public String getHost() {
		return host;
	}

	/**
	 * @return the port
	 */
	public int getPort() {
		return port;
	}
	
	
	
}
