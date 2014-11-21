package de.robotik.nao.communicator.core.widgets.programming.settings;

import de.robotik.nao.communicator.R;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

public class SettingWalkTo extends AbstractSettingsContent {

	private static final String KEY__X = "x";
	private static final String KEY__Y = "y";
	private static final String KEY_THETA = "theta";
	private static final String KEY_USE_ARMS = "arms";
	
	private EditText txtSettingsWalkToX;
	private EditText txtSettingsWalkToY;
	private EditText txtSettingsWalkToTheta;
	private CheckBox chkSettingsWalkToArmsEnabled;
	
	@Override
	public void generateView(ViewGroup root) {
		mResource = R.layout.programming_setting_walk_to;
		super.generateView(root);
		
		// get widgets
		txtSettingsWalkToTheta = (EditText) findViewById(R.id.txtSettingsWalkToTheta);
		txtSettingsWalkToX = (EditText) findViewById(R.id.txtSettingsWalkToX);
		txtSettingsWalkToY = (EditText) findViewById(R.id.txtSettingsWalkToY);
		chkSettingsWalkToArmsEnabled = (CheckBox) findViewById(R.id.chkSettingsWalkToArmsEnabled);
	}
	
	@Override
	public void updateSettings() {
		mSettings.put( KEY__X, txtSettingsWalkToX.getText().toString() );
		mSettings.put( KEY__Y, txtSettingsWalkToY.getText().toString() );
		mSettings.put( KEY_THETA, txtSettingsWalkToTheta.getText().toString() );
		mSettings.put( KEY_USE_ARMS, (chkSettingsWalkToArmsEnabled.isChecked() ? "True" : "False") );
	}

	@Override
	public void updateText(TextView txtText) {
		txtText.setText( "(" + txtSettingsWalkToX.getText().toString()
				+ "m, " + txtSettingsWalkToY.getText().toString()
				+ "m, " + txtSettingsWalkToTheta.getText().toString()
				+ "°) " + (chkSettingsWalkToArmsEnabled.isChecked() ? "arms" : ""));
	}

}
