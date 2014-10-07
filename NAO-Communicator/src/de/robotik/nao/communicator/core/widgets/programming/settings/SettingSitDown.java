package de.robotik.nao.communicator.core.widgets.programming.settings;

import de.northernstars.naocom.R;
import android.view.ViewGroup;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

public class SettingSitDown extends AbstractSettingsContent {

private static final String KEY_POSTURE = "posture";
	
	private Spinner spSettingsPosture;
	
	@Override
	public void generateView(ViewGroup root) {
		mResource = R.layout.programming_setting_posture;
		super.generateView(root);
		
		// add default key
		mSettings.put(KEY_POSTURE, "Sit");

		// get widgets
		spSettingsPosture = (Spinner) findViewById(R.id.spSettingsPosture);
		
		// select default posture
		SpinnerAdapter vAdapter = spSettingsPosture.getAdapter();
		for( int i=0; i < vAdapter.getCount(); i++ ){
			if( ((String) vAdapter.getItem(i)).equals( mSettings.get(KEY_POSTURE) ) ){
				spSettingsPosture.setSelection(i);
				break;
			}
		}
		
	}
	
	@Override
	public void updateSettings() {
		mSettings.put( KEY_POSTURE, "\"" + (String) spSettingsPosture.getSelectedItem() + "\"");
	}

	@Override
	public void updateText(TextView txtText) {
		txtText.setText( mSettings.get(KEY_POSTURE) );
	}

}
