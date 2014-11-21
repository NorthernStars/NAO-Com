package de.robotik.nao.communicator.core.widgets.programming;

import de.robotik.nao.communicator.R;
import de.robotik.nao.communicator.core.interfaces.SettingsContent;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.ViewGroup;
import android.widget.TextView;

public class SettingsDialog extends DialogFragment implements
	OnClickListener{

	private SettingsContent mContent;
	private String mTitle;
	private TextView mTxtText;
	
	/**
	 * Constructor
	 * @param aContent	{@link SettingsContent}
	 * @param aTitle	{@link String} of title.
	 * @param aTxtText	{@link TextView} of items text.
	 */
	public SettingsDialog(SettingsContent aContent, String aTitle, TextView aTxtText) {
		super();
		mContent = aContent;
		mTitle = aTitle;
		mTxtText = aTxtText;
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder vBuilder = new AlertDialog.Builder(getActivity());
		
		// remove view from parent
		if( mContent.getView() != null ){
			if( mContent.getView().getParent() != null ){
				((ViewGroup) mContent.getView().getParent()).removeView( mContent.getView() );
			}
		}
		
		// set content
		vBuilder
			.setTitle(mTitle)
			.setView( mContent.getView() )
			.setPositiveButton(R.string.btnOK, this);
		
		return vBuilder.create();
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		mContent.updateSettings();
		mContent.updateText( mTxtText );
	}
	
}
