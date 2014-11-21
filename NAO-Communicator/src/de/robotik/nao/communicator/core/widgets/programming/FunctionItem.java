package de.robotik.nao.communicator.core.widgets.programming;

import de.robotik.nao.communicator.R;
import de.robotik.nao.communicator.MainActivity;
import de.robotik.nao.communicator.core.RemoteNAO;
import de.robotik.nao.communicator.network.data.NAOCommands;
import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.view.View.OnClickListener;;

public class FunctionItem extends LinearLayout implements
	OnClickListener{

	private TextView txtKey;
	private TextView txtName;
	private ImageButton btnRemove;
	
	private String mKey;
	private String mName;
	private boolean removed = false;
	
	/**
	 * Constructor
	 * @param context	{@link Context}
	 * @param key		{@link String} of items ALMemory key.
	 * @param name		{@link String} of items name.
	 */
	public FunctionItem(Context context, String key, String name) {
		super(context);
		
		LayoutInflater vInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		vInflater.inflate(R.layout.custom_event_item, this);
		
		createItem(key, name);
	}
	
	/**
	 * Updates the key and name of the item.
	 * @param aKey	{@link String} of items ALMemory key.
	 * @param aName	{@link String} of items name.
	 */
	public void update(String aKey, String aName){
		mKey = aKey;
		mName = aName;
		
		// set context
		MainActivity.getInstance().runOnUiThread(new Runnable() {			
			@Override
			public void run() {
				txtKey.setText( mKey );
				txtName.setText( mName );
			}
		});
	}
	
	/**
	 * @return	{@link String} of items ALMemory key.
	 */
	public String getKey(){
		return mKey;
	}
	
	/**
	 * @return	{@link String} of items name.
	 */
	public String getName(){
		return mName;
	}
	
	public boolean isRemoved(){
		return removed;
	}
	
	/**
	 * Creates items context
	 * @param aKey	{@link String} of items ALMemory key.
	 * @param aName	{@link String} of items name.
	 */
	@SuppressLint("ClickableViewAccessibility")
	private void createItem(String aKey, String aName){
		// get view
		txtKey = (TextView) findViewById(R.id.txtFunctionItemKey);
		txtName = (TextView) findViewById(R.id.txtFunctionItemName);
		btnRemove = (ImageButton) findViewById(R.id.btnFunctionItemRemove);
		setBackgroundResource(R.drawable.background_click);
				
		// set context
		update(aKey, aName);
		
		// set listener
		btnRemove.setOnClickListener(this);	
		setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		if( v == btnRemove ){
			RemoteNAO.sendCommand( NAOCommands.MEMORY_EVENT_REMOVE,
					new String[]{ txtKey.getText().toString() } );
			removed = true;
		} else {
			RemoteNAO.sendCommand( NAOCommands.MEMORY_EVENT_RAISE,
					new String[]{ txtKey.getText().toString() } );
		}
	}

}
