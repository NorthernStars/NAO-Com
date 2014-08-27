package de.robotik.nao.communicator.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import javax.jmdns.ServiceEvent;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;

import de.northernstars.naocom.R;
import de.robotik.nao.communicator.core.MainActivity;
import de.robotik.nao.communicator.network.data.NAOCommands;
import de.robotik.nao.communicator.network.data.request.DataRequestPackage;
import de.robotik.nao.communicator.network.data.request.RequestType;
import de.robotik.nao.communicator.network.data.response.DataResponsePackage;
import de.robotik.nao.communicator.network.interfaces.NetworkDataRecievedListener;

public class NAOConnector extends Thread {

	public static final String TAG = NAOConnector.class.getName();
	public static final String defaultHost = "nao.local";
	public static final int defaultPort = 5050;
	public static final int defaultReadTimeout = 3000;
	public static final int connectionMaxTries = 3;
	public static final String serverNetworkServiceToken = "_naocom._tcp.local.";
	
	
	private List<String> hostAdresses = new ArrayList<String>();
	private int port = defaultPort;
	private BufferedReader in = null;
	private OutputStream out = null;
	
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
	
	/**
	 * Constructor
	 * @param connector	{@link NAOConnector} to copy settings (host adresses and port) from.
	 */
	public NAOConnector(NAOConnector connector) {
		hostAdresses = connector.hostAdresses;
		port = connector.port;
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
				out = socket.getOutputStream();
				Gson gson = new Gson();
				
				
				// try to connect
				int tries = connectionMaxTries;
				while(!stop && socket != null && state == ConnectionState.CONNECTION_INIT && tries > 0){
					
					// send connection request
					DataRequestPackage p = new DataRequestPackage(RequestType.CONNECT, NAOCommands.SYS_CONNECT, new String[0]);
					String data = gson.toJson(p);
					out.write(data.getBytes());
					
					// wait for data
					data = in.readLine();
					
					DataResponsePackage response = gson.fromJson(data, DataResponsePackage.class);
					if( p.type == RequestType.CONNECT
							&& response.request.command == NAOCommands.SYS_CONNECT
							&& response.requestSuccessfull == true){
						state = ConnectionState.CONNECTION_ESTABLISHED;
					}
					
					tries--;
				}
				
				// Check if connecting was successfull
				if( state != ConnectionState.CONNECTION_ESTABLISHED ){
					throw new IOException("Could not connect to remote server.");
				} else{
					
					new Handler(Looper.getMainLooper()).post(new Runnable() {						
						@Override
						public void run() {
							Toast.makeText(MainActivity.getContext(), R.string.net_connected, Toast.LENGTH_SHORT).show();
						}
					});
					
				}
				
				while( !stop && socket != null && state == ConnectionState.CONNECTION_ESTABLISHED ){
					
					// handle socket_CLOSED
					try{
						String data = in.readLine();
						if( data != null ){
							
							DataResponsePackage p = gson.fromJson(data, DataResponsePackage.class);
							System.out.println("recieved: " + data);
							
							notifyDataRecievedListeners( p );						
						
						}
					} catch( SocketTimeoutException e ){}
					
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
			
			new Handler(Looper.getMainLooper()).post(new Runnable() {				
				@Override
				public void run() {
					Toast.makeText(MainActivity.getContext(), R.string.net_unknown_host, Toast.LENGTH_SHORT).show();
				}
			});
			
			Log.w(TAG, "Host unknown " + host);
			return;
		}catch(IOException e){
			state = ConnectionState.CONNECTION_ESTABLISHED_FAILED;
			e.printStackTrace();
			
			new Handler(Looper.getMainLooper()).post(new Runnable() {				
				@Override
				public void run() {
					Toast.makeText(MainActivity.getContext(), R.string.net_connection_failed, Toast.LENGTH_SHORT).show();
				}
			});
			
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
	private void notifyDataRecievedListeners(DataResponsePackage data){
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

		private DataResponsePackage data;
		private NetworkDataRecievedListener listener = null;
		
		public NetworkDataRecievedListenerNotifier(NetworkDataRecievedListener aListener, DataResponsePackage aData) {
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
