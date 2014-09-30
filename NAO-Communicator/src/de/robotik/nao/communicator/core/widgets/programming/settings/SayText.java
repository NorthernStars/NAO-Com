package de.robotik.nao.communicator.core.widgets.programming.settings;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import de.northernstars.naocom.R;
import de.robotik.nao.communicator.core.MainActivity;
import de.robotik.nao.communicator.core.interfaces.SettingsContent;

public class SayText implements SettingsContent {
	
	private static final String KEY_TEXT = "text";
	
	private View mView;
	private TextView txtSayText;
	
	private Map<String, String> mSettings = new HashMap<String, String>();
	
	@Override
	public void generateView(ViewGroup root) {
		final int vResource = R.layout.setting_say_text;
		final LayoutInflater vInflater = (LayoutInflater) MainActivity.getInstance()
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mView = vInflater.inflate(vResource, root);
		txtSayText = (TextView) getView().findViewById(R.id.txtSayText);
		
		mSettings.put(KEY_TEXT, "\"Hallo I'am NAO\"");
		
		txtSayText.setText( mSettings.get(KEY_TEXT).replace("\"", "") );
	}

	@Override
	public View getView() {
		return mView;
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

	@Override
	public Map<String, String> getSettings() {
		return mSettings;
	}

	@Override
	public String toJson(){
		String vJson = "{";
		
		for( String key : mSettings.keySet() ){
			vJson += String.format( "'%s':%s", key, mSettings.get(key) );
		}
			
		return vJson + "}";
	}

}
