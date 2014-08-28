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
import de.robotik.nao.communicator.network.data.response.DataResponsePackage;
import de.robotik.nao.communicator.network.interfaces.NetworkDataRecievedListener;
import de.robotik.nao.communicator.network.interfaces.NetworkDataSender;

public class NAOConnector extends Thread implements NetworkDataSender {

	public static final String TAG = NAOConnector.class.getName();
	public static final String defaultHost = "nao.local";
	public static final int defaultPort = 5050;
	public static final int defaultReadTimeout = 3000;
	public static final int connectionMaxTries = 3;
	public static final String serverNetworkServiceToken = "_naocom._tcp.local.";
	
	
	private List<String> hostAdresses = new ArrayList<String>();
	private String host;
	private int port = defaultPort;
	private Gson gson = new Gson();
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
	 * Try to connect to one of the available remote addresses.
	 * @return	{@code true} if successful, {@code false} otherwise.
	 */
	private boolean connect(){
		
		for( String h : hostAdresses ){
			try{
				host = h;
				// create socket				
				socket = new Socket( InetAddress.getByName(host).getHostAddress(), port ); 
				socket.setSoTimeout(defaultReadTimeout);
				in = new BufferedReader( new InputStreamReader(socket.getInputStream()) );
				out = socket.getOutputStream();		

				// try to connect
				int tries = connectionMaxTries;
				while(!stop && socket != null && state == ConnectionState.CONNECTION_INIT && tries > 0){
					
					// send connection request
					DataRequestPackage p = new DataRequestPackage(
							NAOCommands.SYS_CONNECT,
							new String[0]);
					String data = gson.toJson(p);
					out.write(data.getBytes());
					
					// wait for data
					data = in.readLine();
					
					DataResponsePackage response = gson.fromJson(data, DataResponsePackage.class);
					if( response.request.command == NAOCommands.SYS_CONNECT
							&& response.requestSuccessfull){
						state = ConnectionState.CONNECTION_ESTABLISHED;
						notifyDataRecievedListeners(response);
						return true;
					}
					
					tries--;
				}
				
			}catch(UnknownHostException e){
				state = ConnectionState.CONNECTION_UNKNOWN_HOST;
				Log.w(TAG, "Host unknown " + host);
				
				new Handler(Looper.getMainLooper()).post(new Runnable() {				
					@Override
					public void run() {
						Toast.makeText(MainActivity.getInstance().getApplicationContext(),
								R.string.net_unknown_host, Toast.LENGTH_SHORT).show();
					}
				});
			} catch (IOException e) {
				state = ConnectionState.CONNECTION_ESTABLISHED_FAILED;
				Log.w(TAG, "IO Exception on connnection with " + host + ":" + port);
				e.printStackTrace();
			}
		}
		
		return false;
	}
	
	/**
	 * Read data until thread gets stopped or connection gets an error
	 */
	private void readData(){
		while( !stop && socket != null && state == ConnectionState.CONNECTION_ESTABLISHED ){
			
			// handle socket_CLOSED
			try{
				
				String data = in.readLine();
				if( data != null ){
					DataResponsePackage p = gson.fromJson(data, DataResponsePackage.class);				
					notifyDataRecievedListeners( p );
				}
				
			} catch( SocketTimeoutException e ){				
			} catch (IOException e) {
				Log.e(TAG, "IOException on connnection with " + host + ":" + port);
				state = ConnectionState.CONNECTION_ERROR;
			}
			
		}
	}
	
	/**
	 * Try to disconnect.
	 * @return	{@code true} if successful, {@code false} otherwise.
	 */
	private boolean disconnect(){
		
		try{
			
			// create disconnect request
			DataRequestPackage p = new DataRequestPackage(
					NAOCommands.SYS_DISCONNECT,
					new String[0]);
			
			String data = gson.toJson(p);
			out.write(data.getBytes());
			
			// wait for data
			data = in.readLine();
			
			// analyse request
			DataResponsePackage response = gson.fromJson(data, DataResponsePackage.class);
			if( response.request.command == NAOCommands.SYS_DISCONNECT
					&& response.requestSuccessfull){
				
				notifyDataRecievedListeners(response);
				
				// Close connections
				in.close();
				out.close();
				socket.close();
				socket = null;
				state = ConnectionState.CONNECTION_CLOSED;
				
				return true;	
			}
			
		} catch(IOException e) {
			Log.w(TAG, "IOException on connnection with " + host + ":" + port);
			state = ConnectionState.CONNECTION_ERROR;
		} catch( NullPointerException e ){
			Log.w(TAG, "NullPointerException on disconnect from " + host + ":" + port);
			state = ConnectionState.CONNECTION_ERROR;
		}
		
		return false;
	}
	
	/**
	 * Function if thread started
	 */
	@Override
	public void run() {
		
		// try to connect
		if( connect() ){
			
			// connected
			new Handler(Looper.getMainLooper()).post(new Runnable() {						
				@Override
				public void run() {
					Toast.makeText(MainActivity.getInstance().getApplicationContext(),
							R.string.net_connected, Toast.LENGTH_SHORT).show();
				}
			});
			
			// read data
			readData();
			
			// try to disconnect
			if( disconnect() ){
				MainActivity.getInstance().updateTitle("[offline] NAO Communicator");
				
				new Handler(Looper.getMainLooper()).post(new Runnable() {						
					@Override
					public void run() {
						Toast.makeText(MainActivity.getInstance().getApplicationContext(),
								R.string.net_disconnected, Toast.LENGTH_SHORT).show();
					}
				});
			}
			
		} else {
			
			Log.e(TAG, "Establishing connection to " + host + ":" + port + " failed.");
			
			new Handler(Looper.getMainLooper()).post(new Runnable() {				
				@Override
				public void run() {
					Toast.makeText(MainActivity.getInstance().getApplicationContext(),
							R.string.net_connection_failed, Toast.LENGTH_SHORT).show();
				}
			});
		}
		
		
	}
	
	@Override
	public void addNetworkDataRecievedListener(NetworkDataRecievedListener listener){
		dataRecievedListener.add(listener);
	}
	
	@Override
	public void removeNetworkDataRecievedListener(NetworkDataRecievedListener listener){
		if( listener == null ){
			dataRecievedListener.clear();
		}
		else{
			dataRecievedListener.remove(listener);
		}
	}
	
	@Override
	public void notifyDataRecievedListeners(DataResponsePackage data){
		for( NetworkDataRecievedListener listener : dataRecievedListener ){
			Runnable r = new NetworkDataRecievedListenerNotifier(listener, data);
			new Thread(r).start();
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
