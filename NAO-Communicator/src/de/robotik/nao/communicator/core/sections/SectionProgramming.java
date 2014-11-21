package de.robotik.nao.communicator.core.sections;

import java.util.ArrayList;
import java.util.List;

import de.robotik.nao.communicator.R;
import de.robotik.nao.communicator.MainActivity;
import de.robotik.nao.communicator.core.RemoteNAO;
import de.robotik.nao.communicator.core.widgets.programming.ProgrammingItem;
import de.robotik.nao.communicator.core.widgets.programming.settings.SettingChangeLanguage;
import de.robotik.nao.communicator.core.widgets.programming.settings.SettingSensor;
import de.robotik.nao.communicator.core.widgets.programming.settings.SettingLedEyes;
import de.robotik.nao.communicator.core.widgets.programming.settings.SettingPlaySound;
import de.robotik.nao.communicator.core.widgets.programming.settings.SettingSayText;
import de.robotik.nao.communicator.core.widgets.programming.settings.SettingSitDown;
import de.robotik.nao.communicator.core.widgets.programming.settings.SettingStandUp;
import de.robotik.nao.communicator.core.widgets.programming.settings.SettingStiffness;
import de.robotik.nao.communicator.core.widgets.programming.settings.SettingWait;
import de.robotik.nao.communicator.core.widgets.programming.settings.SettingWalkTo;
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
	
	private static List<ProgrammingItem> mActiveProgram = new ArrayList<ProgrammingItem>();
	
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
	private ImageButton btnProgrammingSensor;
	
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
		btnProgrammingSensor = (ImageButton) findViewById(R.id.btnProgrammingSensor);
		
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
		btnProgrammingSensor.setOnClickListener(this);
		
		btnProgrammingPlay.setOnClickListener(this);
		btnProgrammingStop.setOnClickListener(this);
		
		// connect network listener
		MainActivity.getInstance().addNetworkDataRecievedListener(this);
		
		// restore programming items
		if( mActiveProgram != null ){
			for( ProgrammingItem vItem : mActiveProgram ){
				ViewGroup parent = (ViewGroup) vItem.getParent();
				parent.removeView(vItem);
				addItem(vItem);
			}
		}
		
		return rootView;
	}
	
	@Override
	public void onDestroyView() {
		// save program
		mActiveProgram.clear();
		for( int i=0; i < divProgramming.getChildCount(); i++ ){
			ProgrammingItem vItem = (ProgrammingItem) divProgramming.getChildAt(i);
			mActiveProgram.add(vItem);
		}
		
		super.onDestroyView();
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
					new SettingSayText());
			
		} else if( v == btnProgrammingChangeLanguage ) {
			
			vItem = new ProgrammingItem(getActivity(),
					R.string.programming_ChangeLanguage,
					R.drawable.flag,
					new SettingChangeLanguage());
			
		} else if( v == btnProgrammingPlaySound ) {
			
			vItem = new ProgrammingItem(getActivity(),
					R.string.programming_PlaySound,
					R.drawable.play_music,
					new SettingPlaySound());
			
		} else if( v == btnProgrammingWait ) {
			
			vItem = new ProgrammingItem(getActivity(),
					R.string.programming_Wait,
					R.drawable.wait,
					new SettingWait());
			
		} else if( v == btnProgrammingStandUp ) {
			
			vItem = new ProgrammingItem(getActivity(),
					R.string.programming_StandUp,
					R.drawable.stand,
					new SettingStandUp());
			
		} else if( v == btnProgrammingSitDown ) {
			
			vItem = new ProgrammingItem(getActivity(),
					R.string.programming_SitDown,
					R.drawable.sit_ground,
					new SettingSitDown());
			
		} else if( v == btnProgrammingLedEyes ) {
			
			vItem = new ProgrammingItem(getActivity(),
					R.string.programming_LedEyes,
					R.drawable.led,
					new SettingLedEyes());
			
		} else if( v == btnProgrammingHello ) {
			
			vItem = new ProgrammingItem(getActivity(),
					R.string.programming_Hello,
					R.drawable.move,
					null);
			
		} else if( v == btnProgrammingWalkTo ) {
			
			vItem = new ProgrammingItem(getActivity(),
					R.string.programming_WalkTo,
					R.drawable.walk_to_target,
					new SettingWalkTo());
			
		} else if( v == btnProgrammingStiffness ) {
			
			vItem = new ProgrammingItem(getActivity(),
					R.string.programming_Stiffness,
					R.drawable.stiffness,
					new SettingStiffness());
			
		} else if( v == btnProgrammingSensor ) {
			
			vItem = new ProgrammingItem(getActivity(),
					R.string.programming_sensor,
					R.drawable.box_diagram,
					new SettingSensor());
			
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
				
				MainActivity.getInstance().runOnUiThread(new Runnable() {					
					@Override
					public void run() {
						lblProgrammingStatus.setText(R.string.programming_playing);
					}
				});
				
			} else {
				
				MainActivity.getInstance().runOnUiThread(new Runnable() {					
					@Override
					public void run() {
						lblProgrammingStatus.setText(R.string.programming_stopped);
					}
				});
				
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
