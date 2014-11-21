package de.robotik.nao.communicator.core.widgets.programming.settings;

import de.robotik.nao.communicator.R;
import de.robotik.nao.communicator.MainActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

public class SettingSensor extends AbstractSettingsContent implements
	OnItemSelectedListener {

	private static final String KEY_TYPE = "type";
	private static final String KEY_VALUE = "value";
	
	private Spinner spSettingsSensorType;
	private Spinner spSettingsSensorValue;
	
	@Override
	public void generateView(ViewGroup root) {
		mResource = R.layout.programming_setting_sensor;
		super.generateView(root);
		
		mSettings.put(KEY_TYPE,
				"\"" + MainActivity.getInstance().getResources().getStringArray(R.array.nao_sensors)[0] + "\"" );
		
		// get widgets
		spSettingsSensorType = (Spinner) findViewById(R.id.spSettingsSensorType);
		spSettingsSensorValue = (Spinner) findViewById(R.id.spSettingsSensorValue);
		
		// set listener
		spSettingsSensorType.setOnItemSelectedListener(this);
		
		// update value entries
		selectValueEntries();
		updateSettings();
	}
	
	private void selectValueEntries(){
		String vType = mSettings.get(KEY_TYPE);
		ArrayAdapter<CharSequence> vAdapter = null;
		
		// select entries
		if( vType.contains("Tactile")  ){
			
			vAdapter = ArrayAdapter.createFromResource(getView().getContext(),
					R.array.nao_sensor_tactile_values,
					android.R.layout.simple_spinner_item);
			
		} else if( vType.contains("Bumper")  ){
			
			vAdapter = ArrayAdapter.createFromResource(getView().getContext(),
					R.array.nao_sensor_bumper_values,
					android.R.layout.simple_spinner_item);
			
//		} else if( vType.contains("Chest button")  ){
//			
//			vAdapter = ArrayAdapter.createFromResource(getView().getContext(),
//					R.array.nao_sensor_chest_button_values,
//					android.R.layout.simple_spinner_item);
//			
		} else if( vType.contains("Sonar")  ){
			
			vAdapter = ArrayAdapter.createFromResource(getView().getContext(),
					R.array.nao_sensor_sonar_values,
					android.R.layout.simple_spinner_item);
			
		}
		
		// update entries
		if( vAdapter != null ){
			spSettingsSensorValue.setAdapter(vAdapter);
		}
	}
	
	@Override
	public void updateSettings() {
		int vPosition = spSettingsSensorValue.getSelectedItemPosition();
		mSettings.put( KEY_VALUE, "\""
				+ (String) spSettingsSensorValue.getAdapter().getItem(vPosition) + "\"" );
	}

	@Override
	public void updateText(TextView txtText) {
		String vText = mSettings.get(KEY_TYPE) + "\n" + mSettings.get(KEY_VALUE);
		txtText.setText( vText );
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position,
			long id) {
		mSettings.put( KEY_TYPE, "\""
				+ (String) parent.getAdapter().getItem(position) + "\"" );
		System.out.println("Selected new item");
		selectValueEntries();
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {}

}
