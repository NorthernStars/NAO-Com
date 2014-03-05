package de.robotik.nao.communicator.network.hotspot;

/*
 * Copyright 2013 WhiteByte (Nick Russler, Ahmet Yueksektepe).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
 
import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.util.Log;
 

public class WifiApManager {
	private final WifiManager mWifiManager;
	private boolean wasWifiEnabled = false;

	public WifiApManager(Context context) {
		mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
	}

	/**
     * Start AccessPoint mode with the specified
     * configuration. If the radio is already running in
     * AP mode, update the new configuration
     * Note that starting in access point mode disables station
     * mode operation
     * @param wifiConfig SSID, security and channel details as part of WifiConfiguration
     * @return {@code true} if the operation succeeds, {@code false} otherwise
     */
	public boolean setWifiApEnabled(WifiConfiguration wifiConfig, boolean enabled) {
		try {
			if (enabled) {
				wasWifiEnabled = mWifiManager.isWifiEnabled();
				mWifiManager.setWifiEnabled(false);
			}
			else{
				mWifiManager.setWifiEnabled(wasWifiEnabled);
				mWifiManager.reconnect();
			}

			Method method = mWifiManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
			return (Boolean) method.invoke(mWifiManager, wifiConfig, enabled);
		} catch (Exception e) {
			Log.e(this.getClass().toString(), "", e);
			return false;
		}
	}

	/**
     * Gets the Wi-Fi enabled state.
     * @return {@link WifiStates}
     * @see #isWifiApEnabled()
     */
	public WifiStates getWifiApState() {
		try {
			Method method = mWifiManager.getClass().getMethod("getWifiApState");

			int tmp = ((Integer)method.invoke(mWifiManager));

			// Fix for Android 4
			if (tmp > 10) {
				tmp -= 10;
			}
			
			if( tmp < WifiStates.class.getEnumConstants().length ){
				return WifiStates.class.getEnumConstants()[tmp];
			}
		} catch (Exception e) {
			Log.e(this.getClass().toString(), "", e);
		}
		return WifiStates.WIFI_AP_STATE_FAILED;
	}

	/**
     * Return whether Wi-Fi AP is enabled or disabled.
     * @return {@code true} if Wi-Fi AP is enabled
     * @see #getWifiApState()
     *
     * @hide Dont open yet
     */
    public boolean isWifiApEnabled() {
        return getWifiApState() == WifiStates.WIFI_AP_STATE_ENABLED;
    }
    
    /**
     * Gets the Wi-Fi AP Configuration.
     * @return AP details in {@link WifiConfiguration}
     */
    public WifiConfiguration getWifiApConfiguration() {
		try {
			Method method = mWifiManager.getClass().getMethod("getWifiApConfiguration");
			return (WifiConfiguration) method.invoke(mWifiManager);
		} catch (Exception e) {
			Log.e(this.getClass().toString(), "", e);
			return null;
		}
    }
    
    /**
     * Sets the Wi-Fi AP Configuration.
     * @return {@code true} if the operation succeeded, {@code false} otherwise
     */
    public boolean setWifiApConfiguration(WifiConfiguration wifiConfig) {
    	try {
			Method method = mWifiManager.getClass().getMethod("setWifiApConfiguration", WifiConfiguration.class);
			return (Boolean) method.invoke(mWifiManager, wifiConfig);
		} catch (Exception e) {
			Log.e(this.getClass().toString(), "", e);
			return false;
		}
	}
    
	/**
     * Gets a list of the clients connected to the Hotspot, reachable timeout is 300
     * @param onlyReachables {@code false} if the list should contain unreachable (probably disconnected) clients, {@code true} otherwise
     * @return {@link List} of {@link ClientScanResult}
     */
    public static List<ClientScanResult> getClientList(boolean onlyReachables) {
    	return getClientList(onlyReachables, 300);
    }
    
	/**
     * Gets a list of the clients connected to the Hotspot 
     * @param onlyReachables {@code false} if the list should contain unreachable (probably disconnected) clients, {@code true} otherwise
     * @param reachableTimeout Reachable Timout in miliseconds
     * @return {@link List} of {@link ClientScanResult}
     */
	public static List<ClientScanResult> getClientList(boolean onlyReachables, int reachableTimeout) {
		BufferedReader br = null;
		List<ClientScanResult> result = null;

		try {
			result = new ArrayList<ClientScanResult>();
			br = new BufferedReader(new FileReader("/proc/net/arp"));
			String line;
			while ((line = br.readLine()) != null) {
				String[] splitted = line.split(" +");

				if ((splitted != null) && (splitted.length >= 4)) {
					// Basic sanity check
					String mac = splitted[3];

					if (mac.matches("..:..:..:..:..:..")) {
						boolean isReachable = InetAddress.getByName(splitted[0]).isReachable(reachableTimeout);

						if (!onlyReachables || isReachable) {
							result.add(new ClientScanResult(splitted[0], splitted[3], splitted[5], isReachable));
						}
					}
				}
			}
		} catch (Exception e) {
			String msg = e.getMessage();
			if( msg != null)
				Log.e("getClientList", e.getMessage());
			else
				e.printStackTrace();
		} finally {
			try {
				br.close();
			} catch (IOException e) {
				Log.e("getClientList", e.getMessage());
			}
		}

		return result;
	}
}