package de.robotik.nao.communicator.core.widgets.programming.settings;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.preference.PreferenceManager.OnActivityResultListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import de.northernstars.naocom.R;
import de.robotik.nao.communicator.core.MainActivity;

public class SettingPlaySound extends AbstractSettingsContent implements
	OnClickListener,
	OnActivityResultListener{
	
	private static final String KEY_FILE = "file";
	private static final int INTENT_REQUEST_CODE = (int)(Math.random() * 1000);
	
	private Button btnSettingsSelectSoundFile;
	private TextView txtSettingsPlaySoundFile;
	private ProgressBar pgbSettingsPlaySoundLoading;
	
	/**
	 * Uploads a file to remote nao.
	 * @param aUri	{@link Uri} of file.
	 * @return		{@link String} of uploaded filename or {@code null} if not successfull.
	 */
	private String uploadFile( Uri aUri ){
		return aUri.getPath();
	}
	
	@Override
	public void generateView(ViewGroup root) {
		mResource = R.layout.programming_setting_play_sound;
		super.generateView(root);
		
		// get widgets
		txtSettingsPlaySoundFile = (TextView) findViewById(R.id.txtSettingsPlaySoundFile);
		btnSettingsSelectSoundFile = (Button) findViewById(R.id.btnSettingsSelectSoundFile);
		pgbSettingsPlaySoundLoading = (ProgressBar) findViewById(R.id.pgbSettingsPlaySoundLoading);
		
		// set listener
		btnSettingsSelectSoundFile.setOnClickListener(this);
	}

	@Override
	public void updateSettings() {
		mSettings.put( KEY_FILE, txtSettingsPlaySoundFile.getText().toString() );
	}

	@Override
	public void updateText(TextView txtText) {
		txtText.setText( txtSettingsPlaySoundFile.getText() );
	}

	@Override
	public void onClick(View v) {
		// create intend
		Intent vIntent = new Intent();
		vIntent.setType( "audio/*" );
		vIntent.setAction( Intent.ACTION_GET_CONTENT );
		
		// register listener and start intent
		MainActivity.getInstance().addOnActivityResultListener(this);
		MainActivity.getInstance().startActivityForResult(vIntent, INTENT_REQUEST_CODE);
		
		// start loading bar
		pgbSettingsPlaySoundLoading.setVisibility( View.VISIBLE );
	}

	@Override
	public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
		
		if( requestCode == INTENT_REQUEST_CODE ){
			if( resultCode == Activity.RESULT_OK ){
				
				Uri vFile = data.getData();				
				txtSettingsPlaySoundFile.setText( uploadFile(vFile) );
				
			}
			
			// hide loading bar and remove listener
			pgbSettingsPlaySoundLoading.setVisibility( View.GONE );
			MainActivity.getInstance().removeOnActivityResultListener(this);			
		}
			
		return true;
	}
	

}
