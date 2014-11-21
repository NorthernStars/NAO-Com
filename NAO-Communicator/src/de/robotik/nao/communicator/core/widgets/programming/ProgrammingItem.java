package de.robotik.nao.communicator.core.widgets.programming;

import de.robotik.nao.communicator.R;
import de.robotik.nao.communicator.R.drawable;
import de.robotik.nao.communicator.MainActivity;
import de.robotik.nao.communicator.core.interfaces.SettingsContent;
import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.Context;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.View.OnDragListener;

/**
 * Abstract class for generating programming item
 * @author hannes
 *
 */
public class ProgrammingItem extends LinearLayout implements
	OnClickListener,
	OnTouchListener,
	OnDragListener{
	
	private TextView txtNumber;
	private TextView txtName;
	private TextView txtText;
	private ImageView imgIcon;
	private ImageButton btnSettings;
	private ImageButton btnRemove;
	
	private SettingsContent mSettingsContent;
	
	/**
	 * Constructor
	 * @param context			{@link Context}
	 * @param name				{@link Integer} resource id of items name.
	 * @param icon				{@link Integer} resource id of items icon {@link drawable}.
	 * @param settingsContent	{@link SettingsContent}
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
	@SuppressLint("ClickableViewAccessibility")
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
		
		// set listener
		btnRemove.setOnClickListener(this);	
		imgIcon.setOnTouchListener(this);
		setOnDragListener(this);
		
		if( mSettingsContent != null ){
			
			btnSettings.setOnClickListener(this);
			
			// update text
			mSettingsContent.generateView(null);
			mSettingsContent.updateText(txtText);
			
		} else {
			btnSettings.setVisibility( View.GONE );
		}
		
	}
	
	/**
	 * Shows settings dialog
	 */
	private void openSettings(){
		
		if( mSettingsContent != null ){			
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
		
	}
	
	/**
	 * Sets position number of item.
	 * That's position in parent {@link LinearLayout} {@code +1}.
	 * @param aPosition	{@link Integer} position.
	 */
	public void setPosition(int aPosition){
		txtNumber.setText( String.format("%02d", aPosition) );
	}
	
	/**
	 * @return	{@link Integer} position of element in parent {@link LinearLayout}.
	 * 			Returns {@code -1} if position not found.
	 */
	public int getPosition(){
		ViewGroup vParent = (ViewGroup) getParent();
		if( vParent != null ){
			
			for( int i=0; i < vParent.getChildCount(); i++ ){
				if( vParent.getChildAt(i) == this ){
					return i;
				}
			}
			
		}
		
		return -1;
	}
	
	/**
	 * Removes item from list.
	 * @param aItem	{@link ProgrammingItem} to remove.
	 */
	public static synchronized void removeItem(ProgrammingItem aItem){
		// remove from parent
		ViewGroup vParent = (ViewGroup) aItem.getParent();
		vParent.removeView(aItem);
		
		// update positions
		updatePositions(vParent);
	}
	
	/**
	 * Removes a {@link ProgrammingItem} item from parent
	 * and adds it again at a new position.
	 * @param aItem			{@link ProgrammingItem} to move.
	 * @param aPosition		{@link Integer} position where to move the item.
	 */
	public static synchronized void moveItemTo(ProgrammingItem aItem, int aPosition){
		// remove from parent
		ViewGroup vParent = (ViewGroup) aItem.getParent();
		removeItem(aItem);
		
		// add at new position
		vParent.addView(aItem, aPosition);
		aItem.setVisibility( View.VISIBLE );
		
		// update positions
		updatePositions(aItem);
	}
	
	/**
	 * Updates all {@link ProgrammingItem} childs in parent of a {@link ProgrammingItem}.
	 * @param aItem		{@link ProgrammingItem} to take parent from.
	 */
	public static synchronized void updatePositions(ProgrammingItem aItem){
		updatePositions( (ViewGroup) aItem.getParent() );
	}
	
	/**
	 * Updates all {@link ProgrammingItem} childs in {@link ViewGroup} parent.
	 * @param aParent	{@link ViewGroup} where to update childs.
	 */
	public static synchronized void updatePositions(ViewGroup aParent){		
		// update other item numbers
		if( aParent != null ){
			for( int i=0; i < aParent.getChildCount(); i++ ){
				View vView = aParent.getChildAt(i);
				if( vView.getClass() == ProgrammingItem.class ){
					((ProgrammingItem) vView).setPosition(i+1);
				}
			}
		}
	}
	
	/**
	 * Set {@link ProgrammingItem} active or inactive.
	 * @param aSelected	{@link Boolean} {@code true} if item currently active, {@code false} othwerise.
	 */
	public void setActive(boolean aSelected){
		if( aSelected ){
			
			MainActivity.getInstance().runOnUiThread(new Runnable() {				
				@Override
				public void run() {
					setBackgroundColor( getResources().getColor(R.color.active_orange) );
				}
			});
			
		} else {
			
			MainActivity.getInstance().runOnUiThread(new Runnable() {				
				@Override
				public void run() {
					setBackgroundColor( getResources().getColor(R.color.lightgray) );
				}
			});
			
		}
	}
	
	/**
	 * @return JSON {@link String} of item.
	 */
	public String toJson(){		
		String data = "{}";		
		if( mSettingsContent != null ){
			data = mSettingsContent.toJson();
		}
		
		return "{\"name\": \"" + txtName.getText().toString() + "\", \"data\": " + data + "}";
	}
	

	@Override
	public void onClick(View v) {
		if( v == btnSettings ){
			openSettings();
		} else if( v == btnRemove ){
			removeItem(this);
		}
	}
	
	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		View vParent = (View) v.getParent().getParent();
		if( event.getAction() == MotionEvent.ACTION_DOWN ){
			ClipData vData = ClipData.newPlainText(
					"position",
					Integer.toString(getPosition()) );
			
			DragShadowBuilder vBuilder = new DragShadowBuilder(v);
			v.startDrag(vData, vBuilder, vParent, 0);
			return true;
		}
		return false;
	}

	@Override
	public boolean onDrag(View v, DragEvent event) {	
		ProgrammingItem vView = (ProgrammingItem) event.getLocalState();
		int vPosition;
		
		switch(event.getAction()){
		case DragEvent.ACTION_DRAG_ENTERED:
			setBackgroundResource( R.drawable.shape_drop_target );
			break;
			
		case DragEvent.ACTION_DRAG_EXITED:
			setBackgroundResource( R.drawable.shape_default );
			break;
			
		case DragEvent.ACTION_DROP:
			vPosition = getPosition();
			
			if( vPosition >= 0 ){
				moveItemTo(vView, vPosition);
			}
			
		case DragEvent.ACTION_DRAG_ENDED:			
			setBackgroundResource( R.drawable.shape_default );
		}
		return true;
	}	

}
