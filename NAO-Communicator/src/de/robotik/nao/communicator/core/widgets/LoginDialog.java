package de.robotik.nao.communicator.core.widgets;

import de.robotik.nao.communicator.R;
import de.robotik.nao.communicator.MainActivity;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

public class LoginDialog extends DialogFragment implements
	OnClickListener{

	private View mView = null;
	private Dialog mDialog = null;
	
	private String mUser = null;
	private String mPassword = null;
	
	private boolean mShowing = true;
	
	@SuppressLint("InflateParams")
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		// inflate layout
		LayoutInflater vInflater = (LayoutInflater) MainActivity.getInstance()
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mView = vInflater.inflate(
				R.layout.login_data_dialog,
				null);
		
		// create dialog
		AlertDialog.Builder vBuilder = new AlertDialog.Builder( MainActivity.getInstance() );		
		vBuilder
			.setTitle( MainActivity.getInstance().getResources().getString(R.string.net_login_data_title) )
			.setView( mView )
			.setPositiveButton(R.string.btnOK, this)
			.setNegativeButton(R.string.btnCancel, new OnClickListener() {				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					mUser = null;
					mPassword = null;
				}
			});
		
		mDialog = vBuilder.create();
		return mDialog;
	}
	
	/**
	 * @return	{@code true} if {@link Dialog} is shown, {@code false} otherwise.
	 */
	public boolean isShowing(){
		return mShowing;
	}
	
	/**
	 * @return	{@link String} username.
	 */
	public String getUser(){
		return mUser;
	}
	
	/**	
	 * @return	{@link String} password.
	 */
	public String getPassword(){
		return mPassword;
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {		
		// get widgets
		EditText txtUser = (EditText) mView.findViewById(R.id.txtLoginUser);
		EditText txtPassword = (EditText) mView.findViewById(R.id.txtLoginPassword);
		
		// set username and password
		mUser = txtUser.getText().toString();
		mPassword = txtPassword.getText().toString();
		
		mShowing = false;
	}
	
}
