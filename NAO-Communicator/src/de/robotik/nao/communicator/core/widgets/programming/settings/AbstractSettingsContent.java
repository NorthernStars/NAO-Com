package de.robotik.nao.communicator.core.widgets.programming.settings;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import de.robotik.nao.communicator.MainActivity;
import de.robotik.nao.communicator.core.interfaces.SettingsContent;

public abstract class AbstractSettingsContent implements SettingsContent {

	protected int mResource;
	protected View mView;	
	protected Map<String, String> mSettings = new HashMap<String, String>();
	
	/**
	 * Gets {@link View} by its ID.
	 * @param id	{@link Integer} ID.
	 * @return		{@link View} or  {@code null} if not found.
	 */
	protected View findViewById(int id){
		if( getView() != null ){
			return getView().findViewById(id);
		}
		
		return null;
	}
	
	@Override
	public void generateView(ViewGroup root) {
		final LayoutInflater vInflater = (LayoutInflater) MainActivity.getInstance()
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mView = vInflater.inflate(mResource, root);
	}

	@Override
	public View getView() {
		return mView;
	}

	@Override
	public Map<String, String> getSettings() {
		return mSettings;
	}

	@Override
	public String toJson() {
		String vJson = "{";
		
		updateSettings();
		for( String key : mSettings.keySet() ){
			if( vJson.length() > 1 ){
				vJson += ",";
			}
			vJson += String.format( "\"%s\":%s", key, mSettings.get(key) );
		}
			
		return vJson + "}";
	}

}
