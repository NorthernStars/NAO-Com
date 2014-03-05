package de.robotik.nao.communicator.core;

import de.northernstars.naocom.R;
import de.robotik.nao.communicator.network.NAOConnector;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.app.DialogFragment;
import android.widget.TextView;

public class InstallActivity extends Activity {
	
	public static final String HOST = "host";
	public static final String PORT = "port";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_install);
		
		// Show the Up button in the action bar.
        getActionBar().setDisplayHomeAsUpEnabled(true);
		
        // show remote nao information
        Intent intent = getIntent();
        TextView lblNAOInfo = (TextView) findViewById(R.id.lblInstallNAOInformation);
        lblNAOInfo.setText( "Host: " + intent.getStringExtra(HOST)
        		+ "\nPort:" + intent.getIntExtra(PORT, NAOConnector.defaultPort) );
		
	}

	public static class AskForInstallDialog extends DialogFragment {
		
		private String host = NAOConnector.defaultHost;
		private int port = NAOConnector.defaultPort;
		
		public AskForInstallDialog(){}
		
		public AskForInstallDialog(String host, int port){
			this.host = host;
			this.port = port;
		}
		
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			// Use the Builder class for convenient dialog construction
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setTitle(R.install.aks_install_title)
					.setMessage(R.install.ask_install_message)
					.setPositiveButton(R.install.ask_yes,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									
									// Show install activity
									Intent install = new Intent(getActivity(), InstallActivity.class);
									install.putExtra(HOST, host);
									install.putExtra(PORT, port);
									startActivity(install);
									
								}
							})
					.setNegativeButton(R.install.ask_no,
							null);
			// Create the AlertDialog object and return it
			return builder.create();
		}
	}

}
