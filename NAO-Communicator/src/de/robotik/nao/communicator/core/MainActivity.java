package de.robotik.nao.communicator.core;

import java.util.ArrayList;
import java.util.List;
import de.northernstars.naocom.R;
import de.robotik.nao.communicator.core.sections.Section;
import de.robotik.nao.communicator.core.sections.SectionConnect;
import de.robotik.nao.communicator.core.sections.SectionSpeech;
import de.robotik.nao.communicator.core.sections.SectionStatus;
import de.robotik.nao.communicator.core.sections.SectionWifi;
import de.robotik.nao.communicator.core.widgets.RemoteDevice;
import de.robotik.nao.communicator.network.NetworkDataRecievedListenerNotifier;
import de.robotik.nao.communicator.network.data.NAOCommands;
import de.robotik.nao.communicator.network.data.response.DataResponsePackage;
import de.robotik.nao.communicator.network.interfaces.NetworkDataRecievedListener;
import de.robotik.nao.communicator.network.interfaces.NetworkDataSender;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;

public class MainActivity extends FragmentActivity implements NetworkDataRecievedListener, NetworkDataSender {
	
	private static MainActivity INSTANCE;
	
	private List<Section> mSections = new ArrayList<Section>();	
	private RemoteDevice mConnectedDevice = null;
	private List<NetworkDataRecievedListener> dataRecievedListener = new ArrayList<NetworkDataRecievedListener>();
	
	private String mTitle = "[offline] NAO Communicator";
	private SectionsPagerAdapter mSectionsPagerAdapter;
	private ViewPager mViewPager;

	/**
	 * Called if activity created
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		INSTANCE = this;
		
		setContentView(R.layout.activity_main);	
		updateTitle(mTitle);
		
		// add layouts
		createPageFragmentLayouts();
		
		// Create the adapter that will return a fragment for each of the three
		// primary sections of the app.
		mSectionsPagerAdapter = new SectionsPagerAdapter(
				getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);

	}
	
	
	
	/**
	 * @param aSectionName
	 * @return {@link Section} with parameter {@code sectionName} as title.
	 */
	public Section getSection(String aSectionName){
		for( Section section : mSections ){
			if( section.getTitle().equals(aSectionName) ){
				return section;
			}
		}
		return null;
	}
	
	
	
	/**
	 * Adds layouts for fragment pages
	 */
	private void createPageFragmentLayouts(){
		if( mSections.size() == 0 ){
			
			// Add all new sections here
			mSections.add( new SectionConnect("Connect") );
			mSections.add( new SectionWifi("Hotspot") );
			mSections.add( new SectionStatus("NAO") );
			mSections.add( new SectionSpeech("Speech") );
			
		}
	}

	/**
	 * @return the sections
	 */
	public List<Section> getSections() {
		return mSections;
	}

	/**
	 * @return the connectedDevice
	 */
	public RemoteDevice getConnectedDevice() {
		return mConnectedDevice;
	}

	/**
	 * @param aConnectedDevice the connectedDevice to set
	 */
	public void setConnectedDevice(RemoteDevice aConnectedDevice) {
		if( mConnectedDevice != null ){
			mConnectedDevice.getNao().removeNetworkDataRecievedListener(this);
		}
		mConnectedDevice = aConnectedDevice;
		
		if( mConnectedDevice != null ){
			mConnectedDevice.getNao().addNetworkDataRecievedListener(this);
		}
	}
	
	/**
	 * Update action bar title
	 * @param aTitle	{@link String} for new title
	 */
	public void updateTitle(String aTitle){
		mTitle = aTitle;
		new Handler(Looper.getMainLooper()).post(new Runnable() {			
			@Override
			public void run() {
				setTitle( mTitle );
			}
		});
	}
	
	@Override
	public void onNetworkDataRecieved(DataResponsePackage data) {
		if( data.request.command == NAOCommands.SYS_DISCONNECT && data.requestSuccessfull ){
			updateTitle( "[offline] NAO Communicator" );
		} else {
			updateTitle( "[" + data.batteryLevel + "%] " + data.naoName );
		}
		notifyDataRecievedListeners(data);
	}

	/**
	 * @return Current {@link MainActivity} instance.
	 */
	public static MainActivity getInstance() {
		return INSTANCE;
	}
	
	/**
	 * @return Current {@link ViewPager}
	 */
	public ViewPager getViewPager() {
		return mViewPager;
	}


	@Override
	protected void onStop() {
		super.onStop();
		RemoteNAO nao = RemoteNAO.getCurrentRemoteNao();
		if( nao != null ){
			nao.disconnect();
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
			System.out.println("notify " + listener);
			new Thread(r).start();
		}
	}

}
