package de.robotik.nao.communicator.core.widgets.programming.settings;

import de.robotik.nao.communicator.R;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;

public class SettingStiffness extends AbstractSettingsContent {

	private static final String KEY_JOINT = "joint";
	private static final String KEY_MOTOR = "status";
	
	private Spinner spSettingsStiffnessJoint;
	private CheckBox chkSettingsStiffnessOn;
	
	@Override
	public void generateView(ViewGroup root) {
		mResource = R.layout.programming_setting_stiffness;
		super.generateView(root);
		
		// get widgets
		spSettingsStiffnessJoint = (Spinner) findViewById(R.id.spSettingsStiffnessJoint);
		chkSettingsStiffnessOn = (CheckBox) findViewById(R.id.chkSettingsStiffnessOn);
	}
	
	@Override
	public void updateSettings() {
		mSettings.put(KEY_JOINT, "\"" + (String) spSettingsStiffnessJoint.getSelectedItem() + "\"" );
		mSettings.put(KEY_MOTOR, (chkSettingsStiffnessOn.isChecked() ? "True" : "False") ); 
	}

	@Override
	public void updateText(TextView txtText) {
		txtText.setText( (String) spSettingsStiffnessJoint.getSelectedItem() + " "
				+ (chkSettingsStiffnessOn.isChecked() ? "on" : "off"));
	}

}
