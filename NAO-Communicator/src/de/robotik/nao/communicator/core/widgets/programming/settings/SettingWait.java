package de.robotik.nao.communicator.core.widgets.programming.settings;

import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import de.robotik.nao.communicator.R;

public class SettingWait extends AbstractSettingsContent {

	private static final String KEY_TIME = "time";
	
	private EditText txtSettingsWait;
	
	@Override
	public void generateView(ViewGroup root) {
		mResource = R.layout.programming_setting_wait;
		super.generateView(root);
		
		// get widgets
		txtSettingsWait = (EditText) findViewById(R.id.txtSettingsWait);
	}
	
	@Override
	public void updateSettings() {
		mSettings.put( KEY_TIME, txtSettingsWait.getText().toString() );
	}

	@Override
	public void updateText(TextView txtText) {
		txtText.setText( txtSettingsWait.getText().toString() + "s" );
	}

}
