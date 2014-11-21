package de.robotik.nao.communicator.core;

import java.util.Map;

import com.google.gson.Gson;

import de.robotik.nao.communicator.R;
import de.robotik.nao.communicator.MainActivity;
import de.robotik.nao.communicator.core.revisions.ServerRevision;
import de.robotik.nao.communicator.network.NAOConnector;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

public class NAOComInstaller extends Activity implements OnClickListener {

	private TextView txtInstallerTitle;
	private TextView txtInstallerDevice;
	private TextView txtInstallerServerVersion;
	private TextView txtInstallerBody;
	private TextView txtInstallerPrerelease;
	private TextView txtInstallerStatus;
	private Button btnInstallerInstall;
	private Button btnInstallerCancel;
	private ProgressBar pgbInstallerProgress;
	private LinearLayout divInstallerButtons;
	
	private boolean mUpdate = false;
	private NAOConnector mNAO;
	private ServerRevision mServerRevision;
	
	private String mStatusMsg = "";
	private int mNewProgress = 0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_naocom_installer);
		
		// get widgets
		txtInstallerBody = (TextView) findViewById(R.id.txtInstallerBody);
		txtInstallerDevice = (TextView) findViewById(R.id.txtInstallerDevice);
		txtInstallerPrerelease = (TextView) findViewById(R.id.txtInstallerPrerelease);
		txtInstallerServerVersion = (TextView) findViewById(R.id.txtInstallerServerVersion);
		txtInstallerTitle = (TextView) findViewById(R.id.txtInstallerTitle);
		txtInstallerStatus = (TextView) findViewById(R.id.txtInstallerStatus);
		btnInstallerCancel = (Button) findViewById(R.id.btnInstallerCancel);
		btnInstallerInstall = (Button) findViewById(R.id.btnInstallerInstall);
		pgbInstallerProgress = (ProgressBar) findViewById(R.id.pgbInstallerProgress);
		divInstallerButtons = (LinearLayout) findViewById(R.id.divInstallerButtons);
		
		// get intent data
		Intent vIntent = getIntent();
		Gson vGson = new Gson();
		
		String vWorkstation = vIntent.getStringExtra( MainActivity.INSTALLER_INTENT_EXTRA_WORKSTATION );
		String vHost = vIntent.getStringExtra( MainActivity.INSTALLER_INTENT_EXTRA_HOST );
		String jsonRevision = vIntent.getStringExtra( MainActivity.INSTALLER_INTENT_EXTRA_REVISION );
		
		mUpdate = vIntent.getBooleanExtra( MainActivity.INSTALLER_INTENT_EXTRA_UPDATE, false );
		mNAO = new NAOConnector(vHost, NAOConnector.defaultPort);		
		mServerRevision = vGson.fromJson( jsonRevision, ServerRevision.class );
		
		// adjust ui
		if( mServerRevision != null ){
			
			txtInstallerServerVersion.setText( mServerRevision.getName() );
			txtInstallerBody.setText( mServerRevision.getBody() );
			if( mServerRevision.isPrerelease() ){
				txtInstallerPrerelease.setVisibility( View.VISIBLE );
			}
			
		}
		
		if( mNAO != null ){
			txtInstallerDevice.setText( vWorkstation );
		}
		
		if( mUpdate ){
			txtInstallerTitle.setText(R.string.installer_title_update);
			btnInstallerInstall.setText(R.string.installer_update);
		}
		
		// add listener
		btnInstallerCancel.setOnClickListener(this);
		btnInstallerInstall.setOnClickListener(this);
		
	}
	
	/**
	 * Show status message
	 * @param msg			{@link String} message
	 * @param progressInc	{@link Intent} incrementation of progress
	 */
	private void status(String msg, int progressInc){
		// set message
		mStatusMsg = msg;
		
		// set progress	
		int vCurrentProgress = pgbInstallerProgress.getProgress();
		int vMaxProgress = pgbInstallerProgress.getMax();
		mNewProgress = vCurrentProgress + progressInc;
		
		if( mNewProgress < 0 ){
			mNewProgress = 0;
		} else if ( mNewProgress > vMaxProgress ){
			mNewProgress = vMaxProgress;
		}
		
		// show status and set progress
		runOnUiThread(new Runnable() {			
			@Override
			public void run() {
				txtInstallerStatus.setText(mStatusMsg);
				pgbInstallerProgress.setProgress( mNewProgress );
			}
		});		
	}
	
	/**
	 * Install / update to new server version
	 */
	private void install(){
		
		(new Thread(new Runnable() {			
			@Override
			public void run() {
				
				Map<String, Integer> vReturn;
				String vCmd;
				boolean vContinue = true;
				
				// Extract filename
				String vUrl = mServerRevision.getDownloadUrl();
				String vFilename = vUrl.substring( vUrl.lastIndexOf('/')+1, vUrl.length() );	
				
				// Download
				status( getResources().getString(R.string.installer_status_downloading), 0 );
				vCmd = "wget " + vUrl + " -O " + vFilename;
				vReturn  = mNAO.sendSSHCommands( new String[]{ vCmd } );
				
				// Close server
				if( vReturn.get(vCmd) != null && vReturn.get(vCmd) >= 0 ){
					status( getResources().getString(R.string.installer_status_extracting), 40 );
					vCmd = "killall communication_server.py";
					vReturn = mNAO.sendSSHCommands( new String[]{ vCmd } );
				} else {
					vContinue = false;
				}
				
				// Prepare for fresh installation
				if( !mUpdate ){
					// Delete existing installations
					if( vContinue && vReturn.get(vCmd) != null && vReturn.get(vCmd) >= 0 ){
						status( getResources().getString(R.string.installer_status_deleting_files), 0 );
						vCmd = "rm -R naocom";
						vReturn = mNAO.sendSSHCommands( new String[]{ vCmd } );
					} else {
						vContinue = false;
					}
					
					// Create new directory for server
					if( vContinue && vReturn.get(vCmd) != null && vReturn.get(vCmd) >= 0 ){
						vCmd = "mkdir naocom";
						vReturn = mNAO.sendSSHCommands( new String[]{ vCmd } );
					} else {
						vContinue = false;
					}
				}
					
				// Extract files
				if( vContinue && vReturn.get(vCmd) != null && vReturn.get(vCmd) >= 0 ){
					status( getResources().getString(R.string.installer_status_extracting), 20 );
					vCmd = "tar -xzf " + vFilename + " -C naocom/";
					vReturn = mNAO.sendSSHCommands( new String[]{ vCmd } );
				} else {
					vContinue = false;
				}
					
				// Set file rights
				if( vContinue && vReturn.get(vCmd) != null && vReturn.get(vCmd) >= 0 ){
					status( getResources().getString(R.string.installer_status_file_right), 10 );
					vCmd = "chmod a+x naocom/start.sh";
					vReturn = mNAO.sendSSHCommands( new String[]{ vCmd } );
				} else {
					vContinue = false;
				}
				
				// Update revision
				if( vContinue && vReturn.get(vCmd) != null && vReturn.get(vCmd) >= 0 ){
					status( getResources().getString(R.string.installer_status_update_revision), 10 );
					vCmd = "sed -i 's/revision.*=.*/revision = "
				+ mServerRevision.getRevision() + "/g' naocom/settings/Settings.py";
					vReturn = mNAO.sendSSHCommands( new String[]{ vCmd } );
				} else {
					vContinue = false;
				}
				
				// Delete files
				if( vContinue && vReturn.get(vCmd) != null && vReturn.get(vCmd) >= 0 ){
					status( getResources().getString(R.string.installer_status_deleting_files), 5 );
					vCmd = "find . -name '*.pyc' -exec rm -f {} \\;";
					vReturn = mNAO.sendSSHCommands( new String[]{ vCmd } );
				} else {
					vContinue = false;
				}
				
				// Delete files
				if( vContinue && vReturn.get(vCmd) != null && vReturn.get(vCmd) >= 0 ){
					status( getResources().getString(R.string.installer_status_deleting_files), 5 );
					vCmd = "rm " + vFilename;
					vReturn = mNAO.sendSSHCommands( new String[]{ vCmd } );
				} else {
					vContinue = false;
				}
				
				// Restart server
				if( vContinue && vReturn.get(vCmd) != null && vReturn.get(vCmd) >= 0 ){
					status( getResources().getString(R.string.installer_status_restart), 10 );
					vCmd = "naocom/start.sh";
					vReturn = mNAO.sendSSHCommands( new String[]{ vCmd } );
				} else {
					vContinue = false;
				}
				
				// Finished
				if( vContinue && vReturn.get(vCmd) != null && vReturn.get(vCmd) >= 0 ){
					if( mUpdate ){
						status( getResources().getString(R.string.installer_status_update_done), 20 );
					} else {
						status( getResources().getString(R.string.installer_status_install_done), 20 );
					}
					
					runOnUiThread(new Runnable() {						
						@Override
						public void run() {
							btnInstallerInstall.setVisibility( View.GONE );
							btnInstallerCancel.setText( R.string.btnExit );
							divInstallerButtons.setVisibility( View.VISIBLE );							
						}
					});
					
				} else {
					if( mUpdate ){
						status( getResources().getString(R.string.installer_status_update_failed)
								+ ": " + vCmd, 0 );
					} else {
						status( getResources().getString(R.string.installer_status_install_failed)
								+ ": " + vCmd, 0 );
					}
				}
				
				
			}
		})).start();

	}

	@Override
	public void onClick(View v) {
		if( v == btnInstallerCancel ){
			finish();
		} else if( v == btnInstallerInstall ){	
			
			// hide buttons and show progress bar
			divInstallerButtons.setVisibility( View.GONE );
			txtInstallerStatus.setVisibility( View.VISIBLE );
			pgbInstallerProgress.setVisibility( View.VISIBLE );
			
			if( mUpdate ){				
				install();				
			} else {				
				install();			
			}
						
		}
	}

}
