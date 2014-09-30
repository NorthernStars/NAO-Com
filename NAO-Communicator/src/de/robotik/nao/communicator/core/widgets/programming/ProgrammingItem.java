package de.robotik.nao.communicator.core.widgets.programming;

import de.northernstars.naocom.R;
import de.northernstars.naocom.R.drawable;
import de.robotik.nao.communicator.core.MainActivity;
import de.robotik.nao.communicator.core.interfaces.SettingsContent;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.view.View.OnClickListener;

/**
 * Abstract class for generating programming item
 * @author hannes
 *
 */
public class ProgrammingItem extends LinearLayout implements OnClickListener{
	
	private TextView txtNumber;
	private TextView txtName;
	private TextView txtText;
	private ImageView imgIcon;
	private ImageButton btnSettings;
	private ImageButton btnRemove;
	
	private SettingsContent mSettingsContent;
	
	/**
	 * Constructor
	 * @param context	{@link Context}
	 * @param name		{@link Integer} resource id of items name.
	 * @param icon		{@link Integer} resource id of items icon {@link drawable}.
	 */
	public ProgrammingItem(Context context, int name, int icon, SettingsContent settingsContent) {
		super(context);
		
		LayoutInflater vInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		vInflater.inflate(R.layout.programming_item, this);
		
		mSettingsContent = settingsContent;
		createItem(name, icon);
	}
	
	/**
	 * Creates items context
	 * @param aName	{@link Integer} resource id of items name.
	 * @param aIcon	{@link Integer} resource id of items icon {@link drawable}.
	 */
	private void createItem(int aName, int aIcon){
		// get view
		txtNumber = (TextView) findViewById(R.id.txtProgrammingItemNumber);
		txtName = (TextView) findViewById(R.id.txtProgrammingItemName);
		txtText = (TextView) findViewById(R.id.txtProgrammingItemText);
		imgIcon = (ImageView) findViewById(R.id.imgProgrammingItemIcon);
		btnSettings = (ImageButton) findViewById(R.id.btnProgrammingItemSettings);
		btnRemove = (ImageButton) findViewById(R.id.btnProgrammingItemRemove);
		
		// set context
		txtNumber.setText( "-" );
		txtName.setText( aName );
		imgIcon.setImageDrawable( getResources().getDrawable(aIcon) );
		btnSettings.setImageDrawable( getResources().getDrawable(R.drawable.ic_action_settings) );
		btnRemove.setImageDrawable( getResources().getDrawable(R.drawable.ic_action_remove) );
		
		// set onclick listener
		btnSettings.setOnClickListener(this);
		btnRemove.setOnClickListener(this);
		
		// update text
		mSettingsContent.generateView(null);
		mSettingsContent.updateText(txtText);
	}
	
	/**
	 * Shows settings dialog
	 */
	private void openSettings(){
		// generate dialog
		SettingsDialog vDialog = new SettingsDialog(
				mSettingsContent,
				txtNumber.getText().toString() + " " + txtName.getText().toString(),
				txtText);
		
		// show dialog
		vDialog.show(
				MainActivity.getInstance().getSupportFragmentManager(),
				txtName.getText().toString() );		
	}
	
	/**
	 * Sets position number of item.
	 * @param aPosition	{@link Integer} position.
	 */
	public void setPosition(int aPosition){
		txtNumber.setText( String.format("%02d", aPosition) );
	}
	
	/**
	 * Removes item
	 */
	public void removeItem(){
		// remove from parent
		ViewGroup vParent = (ViewGroup) getParent();
		vParent.removeView(this);
		
		// update other item numbers
		for( int i=0; i < vParent.getChildCount(); i++ ){
			((ProgrammingItem) vParent.getChildAt(i)).setPosition(i);
		}
	}
	
	/**
	 * Set {@link ProgrammingItem} active or inactive.
	 * @param vSelected	{@link Boolean} {@code true} if item currently active, {@code false} othwerise.
	 */
	public void setActive(boolean vSelected){
		if( vSelected ){
			setBackgroundColor( getResources().getColor(R.color.active_orange) );
		} else {
			setBackgroundColor( getResources().getColor(R.color.lightgray) );
		}
	}
	
	/**
	 * @return JSON {@link String} of item.
	 */
	public String toJson(){
		return "{'name': '" + txtName.getText().toString() + "', 'data': "
				+ mSettingsContent.toJson() + "}";
	}
	

	@Override
	public void onClick(View v) {
		if( v == btnSettings ){
			openSettings();
		} else if( v == btnRemove ){
			removeItem();
		}
	}

}
