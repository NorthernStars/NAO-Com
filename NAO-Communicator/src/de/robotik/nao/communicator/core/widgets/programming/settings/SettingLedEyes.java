package de.robotik.nao.communicator.core.widgets.programming.settings;

import com.larswerkman.holocolorpicker.ColorPicker;

import de.robotik.nao.communicator.R;
import android.view.ViewGroup;
import android.widget.TextView;

public class SettingLedEyes extends AbstractSettingsContent {

	private static final String KEY_COLOR = "color";
	
	private ColorPicker pckSettingsLedEyes;
	
	@Override
	public void generateView(ViewGroup root) {
		mResource = R.layout.programming_setting_led_eyes;
		super.generateView(root);
		
		// get widgets
		pckSettingsLedEyes = (ColorPicker) findViewById(R.id.pckSettingsLedEyes);
	}
	
	@Override
	public void updateSettings() {
		mSettings.put( KEY_COLOR, Integer.toString(pckSettingsLedEyes.getColor()) ); 
	}

	@Override
	public void updateText(TextView txtText) {
		txtText.setBackgroundColor( pckSettingsLedEyes.getColor() );
	}

}
