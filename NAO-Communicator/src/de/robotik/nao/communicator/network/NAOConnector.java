package de.robotik.nao.communicator.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import javax.jmdns.ServiceEvent;

import android.util.Log;

import com.google.gson.Gson;

import de.robotik.nao.communicator.network.data.NAOCommands;
import de.robotik.nao.communicator.network.data.request.DataRequestPackage;
import de.robotik.nao.communicator.network.data.request.RequestType;
import de.robotik.nao.communicator.network.interfaces.NetworkDataRecievedListener;

public class NAOConnector extends Thread {

	public static final String TAG = NAOConnector.class.getName();
	public static final String defaultHost = "nao.local";
	public static final int defaultPort = 5050;
	public static final int defaultReadTimeout = 300;
	public static final String serverNetworkServiceToken = "_naocom._tcp.local.";
	
	
	private List<String> hostAdresses = new ArrayList<String>();
	private int port = defaultPort;
	private BufferedReader in = null;
	private OutputStreamWriter out = null;
	
	private List<NetworkDataRecievedListener> dataRecievedListener = new ArrayList<NetworkDataRecievedListener>();
	
	private ConnectionState state = ConnectionState.CONNECTION_INIT;
	private boolean stop = false;
	private Socket socket = null;
	
	/**
	 * Constructor
	 * @param service Resolved {@link ServiceEvent}
	 */
	public NAOConnector(ServiceEvent service) {
		if( service.getType().contains(serverNetworkServiceToken) && service.getInfo().getHostAddresses().length > 0 ){
			addHostAdresses(service.getInfo().getHostAddresses());
			port = service.getInfo().getPort();
		}
	}
	
	/**
	 * Constructor
	 * @param host
	 * @param port
	 */
	public NAOConnector(String host, int port) {
		this.hostAdresses.add(host);
		this.port = port;
	}
	
	public NAOConnector(NAOConnector connector) {
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * Function if thread started
	 */
	@Override
	public void run() {
		
		String host = "";
		
		try{
			
			for( String h : hostAdresses ){
				host = h;
				// create socket
				System.out.println("try: " + host + ":" + port);
				
				System.out.println( "inet: " + InetAddress.getByName(host).getHostAddress() );
				System.out.println( "inet: " + InetAddress.getByName(host).getHostName() );
				
				socket = new Socket( InetAddress.getByName(host).getHostAddress(), port ); 
				socket.setSoTimeout(defaultReadTimeout);
				in = new BufferedReader( new InputStreamReader(socket.getInputStream()) );
				out = new OutputStreamWriter(socket.getOutputStream());
				Gson gson = new Gson();
				
				
				// try to connect
				int tries = 3;
				while(!stop && socket != null && state == ConnectionState.CONNECTION_INIT && tries > 0){
					
					// send connection request
					DataRequestPackage p = new DataRequestPackage(RequestType.CONNECT, NAOCommands.SYS_CONNECT, new String[0]);
					String data = gson.toJson(p);
					System.out.println("send: " + data);
					out.write( data );
					
					// wait for data
					data = in.readLine();
					
					System.out.println("recv: s"  + data);
					
					tries--;
				}
				
				// Check if connecting was successfull
				if( state != ConnectionState.CONNECTION_ESTABLISHED ){
					throw new IOException("Could not connect to remote server.");
				}
				
				while( !stop && socket != null && state == ConnectionState.CONNECTION_ESTABLISHED ){
					
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
				state = ConnectionState.CONNECTION_CLOSED;
				break;
			}
			
		}catch(UnknownHostException e){
			state = ConnectionState.CONNECTION_UNKNOWN_HOST;
			e.printStackTrace();
			Log.w(TAG, "Host unknown " + host);
			return;
		}catch(IOException e){
			state = ConnectionState.CONNECTION_ESTABLISHED_FAILED;
			e.printStackTrace();
			Log.e(TAG, "Establishing connection to " + host + ":" + port + " failed.");
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
	 * Class of interface {@link Runnable} to notify a {@link NetworkDataRecievedListener}
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
	 * Adds host adresses to internal list
	 * @param hostAdresses	{@link List} of {@link String} host addresses
	 */
	public void addHostAdresses(List<String> hostAdresses){
		
		for( String host : hostAdresses ){
			if( !this.hostAdresses.contains(host) ){
				this.hostAdresses.add(host);
			}
		}
	}
	
	/**
	 * Adds host adresses to internal list
	 * @param hostAdresses	Array of {@link String} host addresses
	 */
	public void addHostAdresses(String[] hostAdresses){
		
		List<String> hosts = new ArrayList<String>();
		for( String host : hostAdresses ){
			hosts.add(host);
		}
		
		addHostAdresses(hosts);
	}

	/**
	 * @return the host
	 */
	public List<String> getHostAdresses() {
		return hostAdresses;
	}

	/**
	 * @return the port
	 */
	public int getPort() {
		return port;
	}	
	
}
