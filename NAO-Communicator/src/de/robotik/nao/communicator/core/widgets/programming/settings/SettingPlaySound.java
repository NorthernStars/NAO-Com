package de.robotik.nao.communicator.core.widgets.programming.settings;

import java.io.File;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager.OnActivityResultListener;
import android.provider.MediaStore;
import android.support.v4.content.CursorLoader;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import de.northernstars.naocom.R;
import de.robotik.nao.communicator.core.MainActivity;
import de.robotik.nao.communicator.core.RemoteNAO;
import de.robotik.nao.communicator.network.ConnectionState;
import de.robotik.nao.communicator.network.NAOConnector;

public class SettingPlaySound extends AbstractSettingsContent implements
	OnClickListener,
	OnActivityResultListener{
	
	private static final String KEY_FILE = "file";
	private static final int INTENT_REQUEST_CODE = (int)(Math.random() * 1000);
	private static final String NAO_SOUND_DIRECTORY = "sounds";
	
	private Button btnSettingsSelectSoundFile;
	private TextView txtSettingsPlaySoundFile;
	private ProgressBar pgbSettingsPlaySoundLoading;
	
	/**
	 * Uploads a {@link File} to remote NAO.
	 * @param aUri	{@link File} of file.
	 * @return		{@link String} of uploaded filename or {@code null} if not successful.
	 */
	private String uploadFile( File aFile ){
		
		// start async task to upload file
		new AsyncTask<File, Void, File>() {

			@Override
			protected File doInBackground(File... params) {				
				if( params.length > 0 ){
					
					// get connector and file
					File vFile = (File) params[0];					
					RemoteNAO vRemoteNao = RemoteNAO.getCurrentRemoteNao();
					NAOConnector vConnector = vRemoteNao.getConnector();
					
					// wait for connector to establish connection
					while( vConnector.getConnectionState() != ConnectionState.CONNECTION_ESTABLISHED
							&& vConnector.getConnectionState() == ConnectionState.CONNECTION_INIT ){
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {}
					}		
					
					// upload file
					if( vConnector.uploadSFTP( vFile, NAO_SOUND_DIRECTORY ) ){
						return vFile;
					};
				}
								
				return null;
			}
			
			protected void onPostExecute(File result) {				
				if( result != null ){
					txtSettingsPlaySoundFile.setText( result.getName() );
					Toast.makeText(
							MainActivity.getInstance().getApplicationContext(),
							R.string.net_sftp_success, Toast.LENGTH_SHORT).show();
				} else {
					txtSettingsPlaySoundFile.setText(
							MainActivity.getInstance().getResources().getString(R.string.net_sftp_failed) );
					Toast.makeText(
							MainActivity.getInstance().getApplicationContext(),
							R.string.net_sftp_failed, Toast.LENGTH_SHORT).show();
				}
				pgbSettingsPlaySoundLoading.setVisibility( View.GONE );
			};
			
		}.execute( new File[]{ aFile } );
		
		return aFile.getName();
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
		mSettings.put( KEY_FILE,
				NAO_SOUND_DIRECTORY + "/" + txtSettingsPlaySoundFile.getText().toString() );
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
				
				String vFilePath = getRealPathFromURI(
						MainActivity.getInstance().getApplicationContext(),
						data.getData() );
				File vFile = new File( vFilePath );				
				txtSettingsPlaySoundFile.setText( "uploading " + uploadFile(vFile) );
				
			}
			
			// hide loading bar and remove listener
			MainActivity.getInstance().removeOnActivityResultListener(this);			
		}
			
		return true;
	}
	
	/**
	 * Gets real path from {@link Uri}.
	 * @param context		{@link Context} of application.
	 * @param contentUri	{@link Uri} to get real file path from.
	 * @return				{@link String} of real file path.
	 */
	private String getRealPathFromURI(Context context, Uri contentUri) {
	    String[] proj = { MediaStore.Audio.Media.DATA };
	    CursorLoader loader = new CursorLoader(context, contentUri, proj, null, null, null);
	    Cursor cursor = loader.loadInBackground();
	    int column_index = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
	    cursor.moveToFirst();
	    String vPath = cursor.getString(column_index);
	    cursor.close();
	    return vPath;
	}
	

}
