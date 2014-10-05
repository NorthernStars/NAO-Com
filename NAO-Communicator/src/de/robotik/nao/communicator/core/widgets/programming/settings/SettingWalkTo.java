package de.robotik.nao.communicator.core.widgets.programming.settings;

import de.northernstars.naocom.R;
import android.view.ViewGroup;
import android.widget.TextView;

public class SettingWalkTo extends AbstractSettingsContent {

	@Override
	public void generateView(ViewGroup root) {
		mResource = R.layout.programming_setting_walk_to;
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
