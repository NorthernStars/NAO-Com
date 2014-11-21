package de.robotik.nao.communicator.core.sections;

import de.robotik.nao.communicator.R;
import de.robotik.nao.communicator.network.hotspot.WifiApManager;
import de.robotik.nao.communicator.network.hotspot.WifiStates;
import android.net.wifi.WifiConfiguration;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class SectionHotspot extends Section {
	
	private Handler backgroundHandler = new Handler();
	private Runnable backgroundRunnable = null;
	private static final long backgroundTaskDelay = 200;
	private static final long backgroundTaskCheckSwitchDeleay = 5000;
	
	private Switch swHotspot;
	private WifiApManager wifiManager;
	private View rootView;
	private TextView lblHotspotStatus;

	public SectionHotspot() {
		super();
	}
	
	/**
	 * Constrctor
	 * @param title
	 */
	public SectionHotspot(String title) {
		super(title);
	}
	
	/**
	 * Called to create view
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		// create wifi manager
		wifiManager = new WifiApManager(getActivity());
		
		// connect ui widgets
		rootView = inflater.inflate(R.layout.page_wifi, container, false);	
		swHotspot = (Switch) rootView.findViewById(R.id.swHotspot);
		swHotspot.setOnCheckedChangeListener( createOnCheckedChangeListener() );
		lblHotspotStatus = (TextView) rootView.findViewById(R.id.lblHotspotStatus);
		
		// set wifi button
		swHotspot.setChecked( wifiManager.isWifiApEnabled() );
		
		// activate background task
		if( backgroundRunnable == null ){
			backgroundRunnable = createBackgroundRunnable();
		}
		backgroundHandler.postDelayed( backgroundRunnable, backgroundTaskDelay );
		
		return rootView;
	}
	
	
	/**
	 * Called if activity is hidden
	 */
	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
		super.setUserVisibleHint(isVisibleToUser);
		
		if( isVisibleToUser ){			
			// start new background task
			if( backgroundRunnable == null ){
				backgroundRunnable = createBackgroundRunnable();
				backgroundHandler.postDelayed( backgroundRunnable, backgroundTaskDelay );
			}			
		}
		else{			
			// stop background task
			backgroundHandler.removeCallbacks(null);
			backgroundRunnable = null;			
		}
	}
	
	/**
	 * @return {@link Runnable} for background task
	 */
	private Runnable createBackgroundRunnable(){
		return new Runnable() {
			
			private long count = 0;
			
			@Override
			public void run() {
				// show status of hotspot
				if( wifiManager.isWifiApEnabled() ){
					lblHotspotStatus.setText(R.string.net_hotspot_enabled);
				}
				else{
					lblHotspotStatus.setText(R.string.net_hotspot_disabled);
				}
				
				// check if to check status of switch button
				WifiStates wifiState = wifiManager.getWifiApState();
				if( swHotspot.isChecked()
						&& (wifiState == WifiStates.WIFI_AP_STATE_DISABLED || wifiState == WifiStates.WIFI_AP_STATE_FAILED)){
					if( count >= backgroundTaskCheckSwitchDeleay ){
						swHotspot.setChecked(false);
						count = 0;
					}
					else{
						count += backgroundTaskDelay;
					}
				}
				else if( !swHotspot.isChecked()
						&& wifiState == WifiStates.WIFI_AP_STATE_ENABLED ){
					if( count >= backgroundTaskCheckSwitchDeleay ){
						swHotspot.setChecked(true);
						count = 0;
					}
					else{
						count += backgroundTaskDelay;
					}
				}
				else{
					count = 0;
				}
				
				// check if to start runnable again
				if( backgroundRunnable != null ){
					backgroundHandler.postDelayed( backgroundRunnable, backgroundTaskDelay );
				}
			}
		};
	}
	
	/**
	 * @return {@link CompoundButton.OnCheckedChangeListener} for switch button
	 */
	private CompoundButton.OnCheckedChangeListener createOnCheckedChangeListener(){
		return new CompoundButton.OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				
				// Set wifi configuration
				WifiConfiguration wifiConfig = new WifiConfiguration();
				wifiConfig.SSID = ((TextView) rootView.findViewById(R.id.txtSSID)).getText().toString();
				wifiConfig.preSharedKey = ((TextView) rootView.findViewById(R.id.txtWPAKey)).getText().toString();
				wifiConfig.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
				wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
				wifiConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);	
				
				int toastResID = -1;
				if( isChecked ){
					
					// Enable wifi hotspot	
					toastResID = R.string.net_hotspot_enable_success;
					if( !wifiManager.isWifiApEnabled() && !wifiManager.setWifiApEnabled(wifiConfig, true) ){
						toastResID = R.string.net_hotspot_enabled_failed;
					}

				}
				else{
					
					// Disable wifi hotspot
					toastResID = R.string.net_hotspot_disable_success;
					if( !wifiManager.setWifiApEnabled(wifiConfig, false) ){
						toastResID = R.string.net_hotspot__disabled_failed;
					}
					
				}
				
				// Show toast
				if( toastResID >= 0 ){
					Toast toast = Toast.makeText( getActivity(), toastResID, Toast.LENGTH_SHORT );
					toast.show();
				}
				
			}
		};
	}

}
