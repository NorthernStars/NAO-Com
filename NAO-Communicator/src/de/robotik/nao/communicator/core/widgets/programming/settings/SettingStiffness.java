package de.robotik.nao.communicator.core.widgets.programming.settings;

import de.northernstars.naocom.R;
import android.view.ViewGroup;
import android.widget.TextView;

public class SettingStiffness extends AbstractSettingsContent {

	@Override
	public void generateView(ViewGroup root) {
		mResource = R.layout.programming_setting_stiffness;
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