package de.robotik.nao.communicator.core.sections;

import de.northernstars.naocom.R;
import de.robotik.nao.communicator.core.MainActivity;
import de.robotik.nao.communicator.core.RemoteNAO;
import de.robotik.nao.communicator.core.widgets.programming.ProgrammingItem;
import de.robotik.nao.communicator.core.widgets.programming.settings.SayText;
import de.robotik.nao.communicator.network.data.NAOCommands;
import de.robotik.nao.communicator.network.data.response.DataResponsePackage;
import de.robotik.nao.communicator.network.interfaces.NetworkDataRecievedListener;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SectionProgramming extends Section implements
	OnClickListener,
	NetworkDataRecievedListener{
	
	private ImageButton btnProgrammingSayText;
	private ImageButton btnProgrammingChangeLanguage;
	private ImageButton btnProgrammingPlaySound;
	private ImageButton btnProgrammingWait;
	private ImageButton btnProgrammingStandUp;
	private ImageButton btnProgrammingSitDown;
	private ImageButton btnProgrammingLedEyes;
	private ImageButton btnProgrammingHello;
	private ImageButton btnProgrammingWalkTo;
	private ImageButton btnProgrammingStiffness;
	private ImageButton btnProgrammingIf;
	private ImageButton btnProgrammingWhile;
	
	private ImageButton btnProgrammingPlay;
	private ImageButton btnProgrammingStop;
	private TextView lblProgrammingStatus;
	
	private LinearLayout divProgramming;
	
	/**
	 * Constructor
	 */
	public SectionProgramming() {}
	
	/**
	 * Constructor
	 * @param aTitle	{@link String} title of section
	 */
	public SectionProgramming(String aTitle) {
		super(aTitle);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.page_programming, container, false);
		
		// get views
		btnProgrammingSayText = (ImageButton) findViewById(R.id.btnProgrammingSayText);
		btnProgrammingChangeLanguage = (ImageButton) findViewById(R.id.btnProgrammingChangeLanguage);
		btnProgrammingPlaySound = (ImageButton) findViewById(R.id.btnProgrammingPlaySound);
		btnProgrammingWait = (ImageButton) findViewById(R.id.btnProgrammingWait);
		btnProgrammingStandUp = (ImageButton) findViewById(R.id.btnProgrammingStandUp);
		btnProgrammingSitDown = (ImageButton) findViewById(R.id.btnProgrammingSitDown);
		btnProgrammingLedEyes = (ImageButton) findViewById(R.id.btnProgrammingLedEyes);
		btnProgrammingHello = (ImageButton) findViewById(R.id.btnProgrammingHello);
		btnProgrammingWalkTo = (ImageButton) findViewById(R.id.btnProgrammingWalkTo);
		btnProgrammingStiffness = (ImageButton) findViewById(R.id.btnProgrammingStiffness);
		btnProgrammingIf = (ImageButton) findViewById(R.id.btnProgrammingIf);
		btnProgrammingWhile = (ImageButton) findViewById(R.id.btnProgrammingWhile);
		
		btnProgrammingPlay = (ImageButton) findViewById(R.id.btnProgrammingPlay);
		btnProgrammingStop = (ImageButton) findViewById(R.id.btnProgrammingStop);
		lblProgrammingStatus = (TextView) findViewById(R.id.lblProgrammingStatus);
		
		divProgramming = (LinearLayout) findViewById(R.id.divProgramming);
		
		// set listener
		btnProgrammingSayText.setOnClickListener(this);
		btnProgrammingChangeLanguage.setOnClickListener(this);
		btnProgrammingPlaySound.setOnClickListener(this);
		btnProgrammingWait.setOnClickListener(this);
		btnProgrammingStandUp.setOnClickListener(this);
		btnProgrammingSitDown.setOnClickListener(this);
		btnProgrammingLedEyes.setOnClickListener(this);
		btnProgrammingHello.setOnClickListener(this);
		btnProgrammingWalkTo.setOnClickListener(this);
		btnProgrammingStiffness.setOnClickListener(this);
		btnProgrammingIf.setOnClickListener(this);
		btnProgrammingWhile.setOnClickListener(this);
		
		btnProgrammingPlay.setOnClickListener(this);
		btnProgrammingStop.setOnClickListener(this);
		
		// connect network listener
		MainActivity.getInstance().addNetworkDataRecievedListener(this);
		
		return rootView;
	}
	
	/**
	 * Adds a new item.
	 * @param aItem	{@link ProgrammingItem} to add.
	 */
	private void addItem(ProgrammingItem aItem){
		if( aItem != null ){
			divProgramming.addView(aItem);
			aItem.setPosition( divProgramming.getChildCount() );
		}
	}
	
	/**
	 * Generates program data and sends play command
	 */
	private void playProgram(){
		
		// get arguments list of programming items
		String[] vArguments = new String[divProgramming.getChildCount()];		
		for( int i=0; i < divProgramming.getChildCount(); i++ ){
			ProgrammingItem vItem = (ProgrammingItem) divProgramming.getChildAt(i);
			vArguments[i] = vItem.toJson();
			System.out.println( "ITEM " + i + ": " + vItem.toJson() );
		}
		
		// send command
		RemoteNAO.sendCommand(NAOCommands.PLAY_PROGRAM, vArguments);
		
	}

	@Override
	public void onClick(View v) {
		
		ProgrammingItem vItem = null;
		
		// programming icons
		if( v == btnProgrammingSayText ){
			
			vItem = new ProgrammingItem(getActivity(),
					R.string.programming_SayText,
					R.drawable.say,
					new SayText());
			
		} else if( v == btnProgrammingChangeLanguage ) {
			
		} else if( v == btnProgrammingPlaySound ) {
			
		} else if( v == btnProgrammingWait ) {
			
		} else if( v == btnProgrammingStandUp ) {
			
		} else if( v == btnProgrammingSitDown ) {
			
		} else if( v == btnProgrammingLedEyes ) {
			
		} else if( v == btnProgrammingHello ) {
			
		} else if( v == btnProgrammingWalkTo ) {
			
		} else if( v == btnProgrammingStiffness ) {
			
		} else if( v == btnProgrammingIf ) {
			
		} else if( v == btnProgrammingWhile ) {
			
		} 
		
		// control buttons
		else if( v == btnProgrammingPlay ){			
			playProgram();			
		} else if( v == btnProgrammingStop ){
			RemoteNAO.sendCommand(NAOCommands.STOP_PROGRAM);
		}
		
		// add new item
		if( vItem != null ){
			addItem(vItem);
		}
	}

	@Override
	public void onNetworkDataRecieved(DataResponsePackage data) {
		if( data.request.command == NAOCommands.PROGRAM_STATUS ){
			
			// check for playing
			if( data.requestSuccessfull ){
				lblProgrammingStatus.setText(R.string.programming_playing);
			} else {
				lblProgrammingStatus.setText(R.string.programming_stopped);
			}
			
			// select active item
			if( data.request.commandArguments.length > 0 ){
				int activeItem = Integer.parseInt( data.request.commandArguments[0] );
				for( int i=0; i < divProgramming.getChildCount(); i++ ){
					
					if( i == activeItem ){
						((ProgrammingItem) divProgramming.getChildAt(i)).setActive(true);
					} else {
						((ProgrammingItem) divProgramming.getChildAt(i)).setActive(false);
					}
					
				}
			}
		}
	}

}