package de.robotik.nao.communicator.core;

import java.util.HashMap;
import java.util.Map;

import android.app.Fragment;
import android.os.Bundle;

/**
 * {@link Fragment} to store data for configuration change.
 * @author Hannes Eilers
 *
 */
public class DataStorage extends Fragment {

	public static final String TAG = "datastoragefragment";
	private Map<String, Object> mData = new HashMap<String, Object>();
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
	}
	
	/**
	 * Adds data.
	 * @param aKey		{@link String} key.
	 * @param aObject	{@link Object} to add.
	 */
	public void addData(String aKey, Object aObject){
		mData.put(aKey, aObject);
	}
	
	/**
	 * Gets data.
	 * @param aKey	{@link String} key.
	 * @return		{@link Object} if key found, {@code null otherwise}.
	 */
	public Object getData(String aKey){
		return mData.get(aKey);
	}
	
}
