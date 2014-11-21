package de.robotik.nao.communicator.core.widgets.programming.settings;

import java.util.ArrayList;
import java.util.List;

import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import de.robotik.nao.communicator.R;
import de.robotik.nao.communicator.MainActivity;
import de.robotik.nao.communicator.network.data.response.DataResponsePackage;
import de.robotik.nao.communicator.network.interfaces.NetworkDataRecievedListener;

public class SettingChangeLanguage extends AbstractSettingsContent implements
	NetworkDataRecievedListener{
	
	private static final String KEY_LANGUAGE = "language";
		
	private Spinner spSettingsChangeLanguage;
	private List<String> mLanguages = new ArrayList<String>();
	
	@Override
	public void generateView(ViewGroup root) {
			mResource = R.layout.programming_setting_change_language;
			super.generateView(root);
			
			// get widgets
			spSettingsChangeLanguage = (Spinner) findViewById(R.id.spSettingsChangeLanguage);
			
			// connect to network data
			MainActivity.getInstance().addNetworkDataRecievedListener(this);
	}

	@Override
	public void updateSettings() {
		if( spSettingsChangeLanguage.getSelectedItem() != null ){
			mSettings.put( KEY_LANGUAGE, "\""
					+ (String) spSettingsChangeLanguage.getSelectedItem() + "\"" );
		}
	}

	@Override
	public void updateText(TextView txtText) {
		txtText.setText( (String) spSettingsChangeLanguage.getSelectedItem() );
	}

	@Override
	public void onNetworkDataRecieved(DataResponsePackage data) {
		if( data.audioData != null && data.audioData.speechLanguagesList != null ){
			
			// generate languages list if new items available
			if( data.audioData.speechLanguagesList.length != mLanguages.size() ){
				mLanguages.clear();
				for( String lang : data.audioData.speechLanguagesList ){
					mLanguages.add( lang );
				}
				
				// add to spinner
				SpinnerAdapter vAdapter = new ArrayAdapter<String>(
					getView().getContext(),
					android.R.layout.simple_spinner_item,
					mLanguages);
				spSettingsChangeLanguage.setAdapter(vAdapter);
			}
			
		}
	}

}
