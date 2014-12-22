package de.robotik.nao.communicator.network;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

import javax.jmdns.ServiceEvent;

import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

import de.robotik.nao.communicator.R;
import de.robotik.nao.communicator.MainActivity;
import de.robotik.nao.communicator.core.sections.SectionConnect;
import de.robotik.nao.communicator.core.widgets.LoginDialog;
import de.robotik.nao.communicator.network.data.NAOCommands;
import de.robotik.nao.communicator.network.data.request.DataRequestPackage;
import de.robotik.nao.communicator.network.data.response.DataResponsePackage;
import de.robotik.nao.communicator.network.interfaces.NetworkDataRecievedListener;
import de.robotik.nao.communicator.network.interfaces.NetworkDataSender;

public class NAOConnector extends Thread implements NetworkDataSender {

	public static final String TAG = NAOConnector.class.getName();
	public static final String defaultHost = "192.168.0.100";
	public static final int defaultPort = 5050;
	public static final int defaultReadTimeout = 3000;
	public static final int minReadTimeout = 100;
	public static final int connectionMaxTries = 3;
	public static final int connectionMaxTimeouts = 30;
	public static final String serverNetworkServiceToken = "_naocom._tcp.local.";
	
	private static final String SSH_COMMAND_SERVER_START = "naocom/start.sh";
	
	private static final String SSH_CHANNEL_EXEC = "exec";
	private static final String SSH_CHANNEL_SFTP = "sftp";
	private static final String PREFERENCES_SSH_USER = "ssh_default_usr";
	private static final String PREFERENCES_SSH_PASSWORD = "ssh_default_pw";
	
	private List<String> hostAdresses = new ArrayList<String>();
	private String host;
	private int port = defaultPort;
	private Gson gson = new Gson();
	private BufferedReader in = null;
	private OutputStream out = null;
	private int timeoutCounter = 0;
	
	private JSch mSSH = new JSch();
	private String mSSHUser = "nao";
	private String mSSHPassword = "nao";
	private boolean mUseCustomLoginData = false;
	
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
	 * @param aHost
	 * @param aPort
	 */
	public NAOConnector(String aHost, int aPort) {
		hostAdresses.add(aHost);
		port = aPort;
	}
	
	/**
	 * Constructor
	 * @param connector	{@link NAOConnector} to copy settings (host adresses and port) from.
	 */
	public NAOConnector(NAOConnector connector) {
		hostAdresses = connector.hostAdresses;
		port = connector.port;
		mUseCustomLoginData = connector.mUseCustomLoginData;
		mSSHUser = connector.mSSHUser;
		mSSHPassword = connector.mSSHPassword;
		dataRecievedListener = connector.dataRecievedListener;
	}
	
	/**
	 * Try to connect to one of the available remote addresses.
	 * @return	{@code true} if successful, {@code false} otherwise.
	 */
	private boolean connect(){		
		state = ConnectionState.CONNECTION_INIT;
		
		for( String h : hostAdresses ){
			try{
				host = h;
				// create socket				
				socket = new Socket( InetAddress.getByName(host).getHostAddress(), port ); 
				socket.setSoTimeout(defaultReadTimeout);
				in = new BufferedReader( new InputStreamReader( new BufferedInputStream(socket.getInputStream()) ) );
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
				
				MainActivity.getInstance().runOnUiThread( new Runnable() {				
					@Override
					public void run() {
						Toast.makeText(MainActivity.getInstance().getApplicationContext(),
								R.string.net_unknown_host, Toast.LENGTH_SHORT).show();
					}
				});
			} catch (IOException e) {
				state = ConnectionState.CONNECTION_ESTABLISHED_FAILED;
				Log.w(TAG, "IO Exception on connnection with " + host + ":" + port);
			}
		}
		
		return false;
	}
	
	/**
	 * Read data until thread gets stopped or connection gets an error
	 */
	private void readData(){
		// handle socket_CLOSED
		try{
			
			String data = in.readLine();
			if( data != null ){
				timeoutCounter = 0;
				DataResponsePackage p = gson.fromJson(data, DataResponsePackage.class);
				notifyDataRecievedListeners( p );
			} else {
				timeoutCounter++;
				if( timeoutCounter >= connectionMaxTimeouts ){
					Log.i(TAG, "");
					stopConnector();
				}
			}
			
		} catch( SocketTimeoutException e ){
		} catch (IOException e) {
			Log.e(TAG, "IOException on connnection with " + host + ":" + port);
			state = ConnectionState.CONNECTION_ERROR;
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
				
				MainActivity.getInstance().setConnectedDevice(null);
				
				return true;	
			}
			
		} catch(IOException e) {
			Log.w(TAG, "IOException on connnection with " + host + ":" + port);
			state = ConnectionState.CONNECTION_ERROR;
		} catch( NullPointerException e ){
			// no response on socket
			Log.w(TAG, "NullPointerException on disconnect from " + host + ":" + port);
			notifyDataRecievedListeners( new DataResponsePackage(
					new DataRequestPackage(NAOCommands.SYS_DISCONNECT, new String[]{}), true) );
			state = ConnectionState.CONNECTION_CLOSED;
			MainActivity.getInstance().setConnectedDevice(null);
			return true;
		}
		
		return false;
	}
	
	
	/**
	 * Connecting to ssh server
	 * @return			{@code true} if successful connected, {@code false} otherwise.
	 */
	private Session connectSSH(){
		for( String host : hostAdresses ){			
			try {
				
				// get user information
				SharedPreferences vPreferences = MainActivity.getPreferences();
				String vUser = vPreferences.getString(PREFERENCES_SSH_USER, "nao");
				String vPassword = vPreferences.getString(PREFERENCES_SSH_PASSWORD, "nao");				
				if( mUseCustomLoginData ){
					vUser = mSSHUser;
					vPassword = mSSHPassword;					
				}
				
				// create session
				Session vSession = mSSH.getSession(vUser,
						InetAddress.getByName(host).getHostAddress().toString(),
						22 );
				vSession.setPassword( vPassword );
				
				// avoid asking for key auth
				Properties vProperties = new Properties();
				vProperties.put("StrictHostKeyChecking", "no");
				vSession.setConfig(vProperties);
				
				try{
					// connect to ssh server
					vSession.connect();
				} catch( JSchException err ){
					if( err.getMessage().contains("Auth fail") ){
						
						// ask for custom login data
						if( askForCustomLoginData() ){
							vSession = connectSSH();
						} else {
							return null;
						}
						
					} else {
						System.out.println("EXCEPTION: " + err.getMessage());
						err.printStackTrace();
					}
				}
				
				return vSession;
				
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (JSchException e) {
				e.printStackTrace();
			}
			
		}
		
		return null;
	}
	
	/**
	 * Closes SSH connection.
	 * @param aSession	{@link Session} to close.
	 * @return			{@code true} if successful, {@code false} otherwise.
	 */
	private boolean closeSSH(Session aSession){		
		if( aSession != null ){
			aSession.disconnect();
			return true;
		}
		
		return false;
	}
	
	/**
	 * Function to aks user for custom login data.
	 * @return	{@code true} if new custom login data available, {@code false} otherwise.
	 */
	private synchronized boolean askForCustomLoginData(){
		
		// show dialog
		LoginDialog vDialog = new LoginDialog();
		vDialog.show(
				MainActivity.getInstance().getSupportFragmentManager(),
				getClass().getName());
		
		// wait for dialog to close
		while( vDialog.isShowing() ){
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {}
		}
		
		if( vDialog.getUser() != null && vDialog.getPassword() != null ){
			mUseCustomLoginData = true;
			mSSHUser = vDialog.getUser();
			mSSHPassword = vDialog.getPassword();
			return true;
		}
		
		mUseCustomLoginData = false;
		return false;
		
	}
	
	/**
	 * Send commands to execute via ssh using custom login.
	 * @param aCommands	{@link String} array of commands to execute;
	 * @param aInput	{@link String} array with text to input followed by a return, corresponding to position of executed command.<br>
	 * 					Use one of the following tags to specifiy login data:<br>
	 * 					<ul><li>%%USR%%: Username</li><li>%%PW%%: Password</li></ul>
	 * @return			{@link Map} of exit status for commands.
	 */
	public Map<String, Integer> sendSSHCommands(String[] aCommands, String... aInput){
		
		// onnect to ssh server
		Session vSession = connectSSH();
		Map<String, Integer> vExitStatus = new HashMap<String, Integer>();
						
		if( vSession != null){
			
			// execute commands
			for( int i=0; i < aCommands.length; i++ ){
				String cmd = aCommands[i];
				try{
					// open channel
					Channel vChannel = vSession.openChannel(SSH_CHANNEL_EXEC);
					ChannelExec vChannelExec = (ChannelExec) vChannel;
					OutputStream vOutStream = vChannel.getOutputStream();
					vChannelExec.setCommand(cmd);
					vChannelExec.setOutputStream(System.out);
					vChannelExec.setErrStream(System.err);
					
					// connect
					Log.i(TAG, "sending " + cmd);
					vChannel.connect();
					
					// get user information
					SharedPreferences vPreferences = MainActivity.getPreferences();
					String vUser = vPreferences.getString(PREFERENCES_SSH_USER, "nao");
					String vPassword = vPreferences.getString(PREFERENCES_SSH_PASSWORD, "nao");				
					if( mUseCustomLoginData ){
						vUser = mSSHUser;
						vPassword = mSSHPassword;					
					}
					
					// send input
					if( i < aInput.length ){
						
						// replace tags
						aInput[i] = aInput[i].replace("%%USR%%", vUser);
						aInput[i] = aInput[i].replace("%%PW%%", vPassword);
						
						Log.d(TAG, "writing " + aInput[i]);
						vOutStream.write( (aInput[i]+"\n").getBytes() );
						vOutStream.flush();
					}					
					
					// wait for command to complete
					while( !vChannel.isClosed() ){
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {}
					};
					
					// add exit status
					vExitStatus.put( cmd, vChannel.getExitStatus() );
					vOutStream.close();
					vChannel.disconnect();
					
				} catch(JSchException e){
					Log.e(TAG, e.getMessage());
				} catch (IOException e) {
					Log.e(TAG, e.getMessage());
					e.printStackTrace();
				}				
			}
			
			// close ssh connection
			closeSSH(vSession);						
		}
				
		return vExitStatus;
	}
	
	/**
	 * Uploads a {@link File} to remote NAO.
	 * @param aFile			{@link File} to upload.
	 * @param aRemoteDir	{@link String} of remote directory.
	 * @return				{@code true} if file uploaded successful, {@code false} otherwise.
	 */
	public boolean uploadSFTP(File aFile, String aRemoteDir){
		
		// connect to ssh
		Session vSession = connectSSH();

		if( vSession != null){						
			try{
				
				// open channel
				Channel vChannel = vSession.openChannel(SSH_CHANNEL_SFTP);
				vChannel.connect();
				ChannelSftp vSftpChannel = (ChannelSftp) vChannel;
				
				// Change to remote path or create dir
				try{
					vSftpChannel.cd(aRemoteDir);
				} catch (SftpException e){
					vSftpChannel.mkdir(aRemoteDir);
					vSftpChannel.cd(aRemoteDir);
				}
				
				// upload file
				Log.i(TAG, "Starting file upload");	
				vSftpChannel.put( new FileInputStream(aFile), aFile.getName() );
				Log.i(TAG, "Uploaded file " + aFile.getAbsolutePath());			
				
				// close connection
				vChannel.disconnect();
				closeSSH(vSession);
				
				return true;
				
			} catch(JSchException e){
				Log.e(TAG, e.getMessage());
				e.printStackTrace();
			} catch (SftpException e) {
				Log.e(TAG, e.getMessage());
				e.printStackTrace();
			} catch (FileNotFoundException e) {
				Log.e(TAG, e.getMessage());
				e.printStackTrace();
			}
			
		}
		
		return false;
	}
	
	/**
	 * Gets content of remote directory.
	 * @param aRemoteDir	{@link String} of remote directory.
	 * @return				{@link List} of {@link String} file names in remote directory.
	 */
	public List<String> getSftpDirContent(String aRemoteDir){		
		// connect to ssh
		Session vSession = connectSSH();
		List<String> vDirContent = new ArrayList<String>();

		if( vSession != null){						
			try{
				
				// open channel
				Channel vChannel = vSession.openChannel(SSH_CHANNEL_SFTP);
				vChannel.connect();
				ChannelSftp vSftpChannel = (ChannelSftp) vChannel;
				
				// Change to remote path
				vSftpChannel.cd(aRemoteDir);
				
				// get directory content
				@SuppressWarnings("unchecked")
				Vector<ChannelSftp.LsEntry> vEntryList = vSftpChannel.ls("*");
				for( LsEntry entry : vEntryList ){
					vDirContent.add( entry.getFilename() );
				}
				
				// close connection
				vChannel.disconnect();
				closeSSH(vSession);
				
			} catch(JSchException e){
				Log.e(TAG, e.getMessage());
				e.printStackTrace();
			} catch (SftpException e) {
				Log.e(TAG, e.getMessage());
				e.printStackTrace();
			}			
		}
		
		return vDirContent;		
	}
	
	
	/**
	 * Tries to start remote server using ssh.
	 * @return {@code true} if server started, {@code false} otherwise.
	 */
	private boolean sshServerStart(){
		
		// show message
		MainActivity.getInstance().runOnUiThread( new Runnable() {				
			@Override
			public void run() {
				Toast.makeText(MainActivity.getInstance().getApplicationContext(),
						R.string.net_try_server_start, Toast.LENGTH_SHORT).show();
			}
		});	
		
		// send command
		String[] vCommands = new String[]{ SSH_COMMAND_SERVER_START };		
		if( sendSSHCommands( vCommands ).size() > 0 ){
		
			// wait a few seconds for server to start
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {}
		
			return true;
		}
		
		return false;		
	}
	
	
	/**
	 * Function if thread started
	 */
	@Override
	public void run() {
		
		boolean vRetry = true;
		boolean vTriedSsh = false;
		
		while( vRetry ){
			
			// try to connect
			vRetry = false;
			if( connect() ){
				
				// connected
				SectionConnect.updateRemoteDevicesBackgrounds();
				MainActivity.getInstance().runOnUiThread( new Runnable() {						
					@Override
					public void run() {
						Toast.makeText(MainActivity.getInstance().getApplicationContext(),
								R.string.net_connected, Toast.LENGTH_SHORT).show();
					}
				});
				
				// set new connection timeout
				try {
					socket.setSoTimeout(minReadTimeout);
				} catch (SocketException e) {}
				
				// read data
				while( !stop && socket != null && state == ConnectionState.CONNECTION_ESTABLISHED ){
					readData();
				}
				
				// try to disconnect
				if( disconnect() ){
					MainActivity.getInstance().updateTitle("[offline] NAO Communicator");
					
					MainActivity.getInstance().runOnUiThread( new Runnable() {						
						@Override
						public void run() {
							Toast.makeText(MainActivity.getInstance().getApplicationContext(),
									R.string.net_disconnected, Toast.LENGTH_SHORT).show();
						}
					});
				}
				SectionConnect.updateRemoteDevicesBackgrounds();
				
			} else {
				
				// try to start server using ssh and try to to connect again.
				if( !vTriedSsh && sshServerStart()){				
					
					vRetry = true;
					vTriedSsh = true;
					
				} else {
					
					Log.e(TAG, "Establishing connection to " + host + ":" + port + " failed.");
					SectionConnect.updateRemoteDevicesBackgrounds();
					MainActivity.getInstance().runOnUiThread( new Runnable() {				
						@Override
						public void run() {
							Toast.makeText(MainActivity.getInstance().getApplicationContext(),
									R.string.net_connection_failed, Toast.LENGTH_SHORT).show();
						}
					});
					
				}
			}
			
		}
		
		
		// try to disconnect
		disconnect();
		
	}
	
	/**
	 * Send {@link NAOCommands} to remote NAO.
	 * @param aCommand	{@link NAOCommands} to send.
	 * @param aArgs		Array of {@link String} arguments to for {@code aCommand}.
	 * @return			{@code true} if successful, {@code false} otherwise.
	 */
	public synchronized boolean sendCommand(NAOCommands aCommand, String[] aArgs){
		if( state == ConnectionState.CONNECTION_ESTABLISHED ){
			try{
				
				DataRequestPackage p = new DataRequestPackage(aCommand, aArgs);
				String data = gson.toJson(p);
				out.write( data.getBytes() );
				return true;
				
			} catch(IOException err) {
				Log.e(TAG, "IOException on sending "
						+ aCommand + " to " + host + ":" + port);
				stopConnector();
			}
		}
		
		return false;
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
	
	public void addHostAdress(String aAdress){
		if( !hostAdresses.contains(aAdress) ){
			hostAdresses.add(aAdress);
		}
	}

	/**
	 * @return the port
	 */
	public int getPort() {
		return port;
	}
	
}
