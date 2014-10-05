package de.robotik.nao.communicator.core.widgets.programming.settings;

import android.view.ViewGroup;
import android.widget.TextView;
import de.northernstars.naocom.R;

public class SettingSayText extends AbstractSettingsContent {
	
	private static final String KEY_TEXT = "text";	
	private TextView txtSayText;
	
	@Override
	public void generateView(ViewGroup root) {
		mResource = R.layout.programming_setting_say_text;
		super.generateView(root);
					
		mSettings.put(KEY_TEXT, "\"Hallo I'am NAO\"");
		txtSayText = (TextView) findViewById(R.id.txtSayText);	
		txtSayText.setText( mSettings.get(KEY_TEXT).replace("\"", "") );
	}

	@Override
	public void updateSettings() {
		String vText = txtSayText.getText().toString();		
		mSettings.put(KEY_TEXT, "\"" + vText + "\"");
	}

	@Override
	public void updateText(TextView txtText) {
		txtText.setText( mSettings.get(KEY_TEXT) );
	}



}
