package de.robotik.nao.communicator.core;

import com.google.gson.Gson;

import de.northernstars.naocom.R;
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
	private ProgressBar pgrInstallerProgress;
	private LinearLayout divInstallerButtons;
	
	private boolean mUpdate = false;
	private NAOConnector mNAO;
	private ServerRevision mServerRevision;
	
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
		pgrInstallerProgress = (ProgressBar) findViewById(R.id.pgbInstallerProgress);
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
	 * @param msg	{@link String} message
	 */
	private void status(String msg){
		txtInstallerStatus.setText(msg);
	}
	
	private void update(){
		// TODO: update
	}
	
	private void install(){
		// TODO: install
	}

	@Override
	public void onClick(View v) {
		if( v == btnInstallerCancel ){
			finish();
		} else if( v == btnInstallerInstall ){
			
			// hide buttons and show progress bar
			divInstallerButtons.setVisibility( View.GONE );
			txtInstallerStatus.setVisibility( View.VISIBLE );
			pgrInstallerProgress.setVisibility( View.VISIBLE );
			
			if( mUpdate ){
				update();
			} else {
				install();
			}
		}
	}

}
