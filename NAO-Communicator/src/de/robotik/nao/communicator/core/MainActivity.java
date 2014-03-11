package de.robotik.nao.communicator.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;

import de.northernstars.naocom.R;
import de.robotik.nao.communicator.core.sections.Section;
import de.robotik.nao.communicator.core.sections.SectionConnect;
import de.robotik.nao.communicator.core.sections.SectionSpeech;
import de.robotik.nao.communicator.core.sections.SectionStatus;
import de.robotik.nao.communicator.core.sections.SectionWifi;
import de.robotik.nao.communicator.core.widgets.RemoteDevice;
import de.robotik.nao.communicator.network.NAOConnector;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.MulticastLock;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;

public class MainActivity extends FragmentActivity {

	private static List<Section> sections = new ArrayList<Section>();
	
	private String TAG = getClass().getName();
	
	private SectionsPagerAdapter mSectionsPagerAdapter;
	private ViewPager mViewPager;
	
	private MulticastLock lock;
	private JmDNS jmDNS = null;
	private ServiceListener jmDNSServiceListener = null;

	/**
	 * Called if activity created
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);		
		
		// add layouts
		createPageFragmentLayouts();
		
		// Create the adapter that will return a fragment for each of the three
		// primary sections of the app.
		mSectionsPagerAdapter = new SectionsPagerAdapter(
				getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);
		
		(new AsyncTask<Void, Void, Void>() {
			@Override
			protected Void doInBackground(Void... params) {
				discoverNetworkServices();
				return null;
			}
		}).execute();

	}
	
	@Override
	protected void onStop() {
		super.onStop();
		if( jmDNS != null ){
			jmDNS.unregisterAllServices();
			try {
				jmDNS.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		jmDNS = null;
	}
	
	/**
	 * @param sectionName
	 * @return {@link Section} with parameter {@code sectionName} as title.
	 */
	public static Section getSection(String sectionName){
		for( Section section : sections ){
			if( section.getTitle().equals(sectionName) ){
				return section;
			}
		}
		return null;
	}
	
	/**
	 * Starts discovering network services
	 */
	private synchronized void discoverNetworkServices(){		
		
		if(jmDNSServiceListener == null){
			jmDNSServiceListener = initializeDiscoveryListener();
		}

		WifiManager wifi = (WifiManager)getSystemService(Context.WIFI_SERVICE);
        lock = wifi.createMulticastLock(TAG);
        lock.setReferenceCounted(true);
        lock.acquire();
        
        try {
        	if( jmDNS == null ){
        		jmDNS = JmDNS.create();
        	}
        	
        	// Start diuscovery for every requested service type
        	jmDNS.addServiceListener(RemoteDevice.workstationNetworkServiceToken, jmDNSServiceListener);
        	jmDNS.addServiceListener(RemoteNAO.naoNetworkServiceToken, jmDNSServiceListener);
        	jmDNS.addServiceListener(RemoteNAO.naoqiNetworkServiceToken, jmDNSServiceListener);
        	jmDNS.addServiceListener(RemoteNAO.sshNetworkServiceToken, jmDNSServiceListener);
        	jmDNS.addServiceListener(RemoteNAO.sftpNetworkServiceToken, jmDNSServiceListener);
        	jmDNS.addServiceListener(NAOConnector.serverNetworkServiceToken, jmDNSServiceListener);
        	
        	ServiceInfo info = ServiceInfo.create(RemoteDevice.workstationNetworkServiceToken, "TEST", 9696, "teststring");
        	jmDNS.registerService(info);
        	
        } catch (IOException e) {
            e.printStackTrace();
        }
        
//        lock.release();	
        
	}
	
	/**
	 * Initalizes {@link NsdManager.DiscoveryListener} for callback on {@link NsdManager} discovery.
	 * @return {@link NsdManager.DiscoveryListener} 
	 */
	private ServiceListener initializeDiscoveryListener() {
		// Instantiate a new DiscoveryListener
		return new ServiceListener() {
	        public void serviceResolved(ServiceEvent service) {
	            Log.i(TAG, "Service name: " + service.getName());
	            Log.i(TAG, "Service type: " + service.getType());
	            Log.i(TAG, "Service host: " + service.getInfo().getServer());
	            for( String adrr : service.getInfo().getHostAddresses() ){
	            	Log.i(TAG, "Service host adress: " + adrr);
	            }
	        }
	        public void serviceRemoved(ServiceEvent service) {
	        	Log.i(TAG, "Service removed: " + service.getName());
	        }
	        public void serviceAdded(ServiceEvent service) {
	            // Required to force serviceResolved to be called again
	            // (after the first search)
	        	Log.i(TAG, "Service added: " + service.getType() + " : " + service.getName());
	            jmDNS.requestServiceInfo(service.getType(), service.getName(), 1);
	        }
	    };
	}
	
	
	
	/**
	 * Adds layouts for fragment pages
	 */
	private void createPageFragmentLayouts(){
		if( sections.size() == 0 ){
			
			// Add all new sections here
			sections.add( new SectionConnect("Connect") );
			sections.add( new SectionWifi("Hotspot") );
			sections.add( new SectionStatus("NAO") );
			sections.add( new SectionSpeech("Speech") );
			
		}
	}

	/**
	 * @return the sections
	 */
	public static List<Section> getSections() {
		return sections;
	}

}
