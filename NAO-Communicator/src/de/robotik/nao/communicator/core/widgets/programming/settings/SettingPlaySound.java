package de.robotik.nao.communicator.core.widgets.programming.settings;

import java.io.File;
import java.util.List;

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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;
import de.robotik.nao.communicator.R;
import de.robotik.nao.communicator.MainActivity;
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
	private ProgressBar pgbSettingsPlaySoundLoading;
	private Spinner spSettingsPlaySoundAvailableFile;
	private ImageButton btnSettingsPlaySoundRefresh;
	private ProgressBar pgbSettingsPlaySoundRemoteFilesLoading;
	
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
					Toast.makeText(
							MainActivity.getInstance().getApplicationContext(),
							R.string.net_sftp_success, Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(
							MainActivity.getInstance().getApplicationContext(),
							R.string.net_sftp_failed, Toast.LENGTH_SHORT).show();
				}
				pgbSettingsPlaySoundLoading.setVisibility( View.GONE );
			};
			
		}.execute( new File[]{ aFile } );
		
		return aFile.getName();
	}
	
	/**
	 * Loads remote files and adds them to spinner
	 */
	private void loadRemoteFiles(){
		
		// start async task to get remote files
		new AsyncTask<Void, Void, SpinnerAdapter>() {
			
			protected void onPreExecute() {
				btnSettingsPlaySoundRefresh.setVisibility( View.GONE );
				pgbSettingsPlaySoundRemoteFilesLoading.setVisibility( View.VISIBLE );
			};

			@Override
			protected SpinnerAdapter doInBackground(Void... params) {
				
				// get connector
				RemoteNAO vRemoteNao = RemoteNAO.getCurrentRemoteNao();
				NAOConnector vConnector = vRemoteNao.getConnector();
				
				List<String> vRemoteFiles = vConnector.getSftpDirContent( NAO_SOUND_DIRECTORY );
				
				SpinnerAdapter vAdapter = new ArrayAdapter<String>(
						MainActivity.getInstance().getApplicationContext(),
						R.layout.spinner_item,
						vRemoteFiles );
			
				return vAdapter;
			}
			
			protected void onPostExecute(SpinnerAdapter result) {				
				spSettingsPlaySoundAvailableFile.setAdapter(result);
				btnSettingsPlaySoundRefresh.setVisibility( View.VISIBLE );
				pgbSettingsPlaySoundRemoteFilesLoading.setVisibility( View.GONE );
			};
			
		}.execute();
		
	}
	
	@Override
	public void generateView(ViewGroup root) {
		mResource = R.layout.programming_setting_play_sound;
		super.generateView(root);
		
		// get widgets
		btnSettingsSelectSoundFile = (Button) findViewById(R.id.btnSettingsSelectSoundFile);
		pgbSettingsPlaySoundLoading = (ProgressBar) findViewById(R.id.pgbSettingsPlaySoundLoading);
		spSettingsPlaySoundAvailableFile = (Spinner) findViewById(R.id.spSettingsPlaySoundAvailableFile);
		btnSettingsPlaySoundRefresh = (ImageButton) findViewById(R.id.btnSettingsPlaySoundRefresh);
		pgbSettingsPlaySoundRemoteFilesLoading = (ProgressBar) findViewById(R.id.pgbSettingsPlaySoundRemoteFilesLoading);
		
		// set listener
		btnSettingsSelectSoundFile.setOnClickListener(this);
		btnSettingsPlaySoundRefresh.setOnClickListener(this);
	}

	@Override
	public void updateSettings() {
		mSettings.put( KEY_FILE,
				"\"" + NAO_SOUND_DIRECTORY
				+ "/" + (String) spSettingsPlaySoundAvailableFile.getSelectedItem() + "\"" );
	}

	@Override
	public void updateText(TextView txtText) {
		txtText.setText( (String) spSettingsPlaySoundAvailableFile.getSelectedItem() );
	}

	@Override
	public void onClick(View v) {
		
		if( v == btnSettingsSelectSoundFile ){
			
			// create intend
			Intent vIntent = new Intent();
			vIntent.setType( "audio/*" );
			vIntent.setAction( Intent.ACTION_GET_CONTENT );
			
			// register listener and start intent
			MainActivity.getInstance().addOnActivityResultListener(this);		
			MainActivity.getInstance().startActivityForResult(vIntent, INTENT_REQUEST_CODE);
			
			// start loading bar
			pgbSettingsPlaySoundLoading.setVisibility( View.VISIBLE );
			
		} else if( v == btnSettingsPlaySoundRefresh ){
			
			// load remote sound files
			loadRemoteFiles();
			
		}
	}

	@Override
	public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
		
		if( requestCode == INTENT_REQUEST_CODE ){
			if( resultCode == Activity.RESULT_OK ){
				
				String vFilePath = getRealPathFromURI(
						MainActivity.getInstance().getApplicationContext(),
						data.getData() );
				File vFile = new File( vFilePath );
				uploadFile(vFile);				
			}
			
			// hide loading bar and remove listener
			MainActivity.getInstance().removeOnActivityResultListener(this);
			
			// load remote sound files
			loadRemoteFiles();
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
