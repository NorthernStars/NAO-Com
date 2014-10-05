package de.robotik.nao.communicator.core.widgets.programming.settings;

import android.view.ViewGroup;
import android.widget.TextView;
import de.northernstars.naocom.R;

public class SettingStandUp extends AbstractSettingsContent {

	@Override
	public void generateView(ViewGroup root) {
		mResource = R.layout.programming_setting_posture;
		super.generateView(root);
	}
	
	@Override
	public void updateSettings() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateText(TextView txtText) {
		// TODO Auto-generated method stub
		
	}

}
