package de.robotik.nao.communicator.core.sections;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import de.robotik.nao.communicator.R;
import de.robotik.nao.communicator.MainActivity;
import de.robotik.nao.communicator.core.RemoteNAO;
import de.robotik.nao.communicator.core.widgets.programming.FunctionItem;
import de.robotik.nao.communicator.network.data.NAOCommands;
import de.robotik.nao.communicator.network.data.response.DataResponsePackage;
import de.robotik.nao.communicator.network.interfaces.NetworkDataRecievedListener;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SectionFunctions extends Section implements
	OnClickListener,
	NetworkDataRecievedListener{

	private Button btnFunctionStandUp;
	private Button btnFuntctionSitDown;
	private Button btnFunctionHello;
	private Button btnFunctionShakeHands;
	private Button btnFunctionWipeForehead;
	private Button btnFunctionsFaceTracker;
	private Button btnFunctionsRedBallTracker;
	
	private Button btnDanceThaiChi;
	private Button btnDanceEvolutionOfDance;
	private Button btnDanceCaravanPalace;
	private Button btnDanceVangelisDance;
	private Button btnDanceGangnameStyle;
	private Button btnDanceEyeOfTheTiger;
	
	private Button btnFunctionsAbort;
	
	private TextView txtFunctionsCustomNewALMemoryKey;
	private TextView txtFunctionsCustomNewName;
	private Button btnFunctionsCustomNewAdd;
	
	private LinearLayout divFunctionsCustom;
	private Map<String, String> mFunctionItemsToUpdate = new HashMap<String, String>();
	private boolean mFunctionsAdding = false;
	
	
	public SectionFunctions(){}
	
	public SectionFunctions(String aTitle){
		super(aTitle);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {		
		rootView = inflater.inflate(R.layout.page_functions, container, false);
		
		// get widgets and set listener
		(btnFunctionHello = (Button) findViewById(R.id.btnFunctionHello)).setOnClickListener(this);
		(btnFunctionsFaceTracker = (Button) findViewById(R.id.btnFunctionsFaceTracker)).setOnClickListener(this);
		(btnFunctionShakeHands = (Button) findViewById(R.id.btnFunctionShakeHands)).setOnClickListener(this);
		(btnFunctionsRedBallTracker = (Button) findViewById(R.id.btnFunctionsRedBallTracker)).setOnClickListener(this);
		(btnFunctionStandUp = (Button) findViewById(R.id.btnFunctionStandUp)).setOnClickListener(this);
		(btnFunctionWipeForehead = (Button) findViewById(R.id.btnFunctionWipeForehead)).setOnClickListener(this);
		(btnFuntctionSitDown = (Button) findViewById(R.id.btnFuntctionSitDown)).setOnClickListener(this);
		(btnFunctionsAbort = (Button) findViewById(R.id.btnFunctionsAbort)).setOnClickListener(this);
		
		(btnDanceCaravanPalace = (Button) findViewById(R.id.btnDanceCaravanPalace)).setOnClickListener(this);
		(btnDanceEvolutionOfDance = (Button) findViewById(R.id.btnDanceEvolutionOfDance)).setOnClickListener(this);
		(btnDanceEyeOfTheTiger = (Button) findViewById(R.id.btnDanceEyeOfTheTiger)).setOnClickListener(this);
		(btnDanceGangnameStyle = (Button) findViewById(R.id.btnDanceGangnameStyle)).setOnClickListener(this);
		(btnDanceThaiChi = (Button) findViewById(R.id.btnDanceThaiChi)).setOnClickListener(this);
		(btnDanceVangelisDance = (Button) findViewById(R.id.btnDanceVangelisDance)).setOnClickListener(this);
		
		txtFunctionsCustomNewALMemoryKey = (TextView) findViewById(R.id.txtFunctionsCustomNewALMemoryKey);
		txtFunctionsCustomNewName = (TextView) findViewById(R.id.txtFunctionsCustomNewName);
		(btnFunctionsCustomNewAdd = (Button) findViewById(R.id.btnFunctionsCustomNewAdd)).setOnClickListener(this);
		
		divFunctionsCustom = (LinearLayout) findViewById(R.id.divFunctionsCustom);
		
		// Register network data listener
		MainActivity.getInstance().addNetworkDataRecievedListener(this);
		
		return rootView;
	}

	@Override
	public void onClick(View v) {
		if( v == btnFunctionHello ){			
			RemoteNAO.sendCommand( NAOCommands.MEMORY_EVENT_RAISE, new String[]{"animationHello"} );
			
		} else if( v == btnFunctionsCustomNewAdd ){
			
			String vEventKey = txtFunctionsCustomNewALMemoryKey.getText().toString();
			String vEventName = txtFunctionsCustomNewName.getText().toString();
			RemoteNAO.sendCommand( NAOCommands.MEMORY_EVENT_ADD, new String[]{vEventKey, vEventName} );
			
		} else if( v == btnFunctionsFaceTracker ){			
			RemoteNAO.sendCommand( NAOCommands.MEMORY_EVENT_RAISE, new String[]{"functionFaceTracker"} );
			
		} else if( v == btnFunctionShakeHands ){
			RemoteNAO.sendCommand( NAOCommands.MEMORY_EVENT_RAISE, new String[]{"animationShakeHands"} );
			
		} else if( v == btnFunctionsRedBallTracker ){
			RemoteNAO.sendCommand( NAOCommands.MEMORY_EVENT_RAISE, new String[]{"functionRedBallTracker"} );
			
		} else if( v == btnFunctionStandUp ){
			RemoteNAO.sendCommand( NAOCommands.STAND_UP );
			
		} else if( v == btnFunctionWipeForehead ){
			RemoteNAO.sendCommand( NAOCommands.MEMORY_EVENT_RAISE, new String[]{"animationWipeForehead"} );
			
		} else if( v == btnFuntctionSitDown ){
			RemoteNAO.sendCommand( NAOCommands.SIT_DOWN );
			
		} else if( v == btnDanceCaravanPalace ){
			RemoteNAO.sendCommand( NAOCommands.MEMORY_EVENT_RAISE, new String[]{"danceCaravanPalace"} );
			
		} else if( v == btnDanceEvolutionOfDance ){
			RemoteNAO.sendCommand( NAOCommands.MEMORY_EVENT_RAISE, new String[]{"danceEvolutionOfDance"} );
			
		} else if( v == btnDanceEyeOfTheTiger ){
			RemoteNAO.sendCommand( NAOCommands.MEMORY_EVENT_RAISE, new String[]{"danceEyeOfTheTiger"} );
			
		} else if( v == btnDanceGangnameStyle ){
			RemoteNAO.sendCommand( NAOCommands.MEMORY_EVENT_RAISE, new String[]{"danceGangnamStyle"} );
			
		} else if( v == btnDanceThaiChi ){
			RemoteNAO.sendCommand( NAOCommands.MEMORY_EVENT_RAISE, new String[]{"danceThaiChi"} );
			
		} else if( v == btnDanceVangelisDance ){
			RemoteNAO.sendCommand( NAOCommands.MEMORY_EVENT_RAISE, new String[]{"danceVangelisDance"} );
			
		} else if( v == btnFunctionsAbort ){
			RemoteNAO.sendCommand( NAOCommands.MEMORY_EVENT_RAISE, new String[]{"naocomAbort"} );
			
		}
	}
	
	/**
	 * Gets {@link FunctionItem} by its key.
	 * @param aKey	{@link String} of items ALMemory key.
	 * @return		{@link FunctionItem} with {@code aKey}, {@code null} if no item found.
	 */
	private FunctionItem getItem(String aKey){
		for( int i=0; i < divFunctionsCustom.getChildCount(); i++ ){
			FunctionItem vItem = (FunctionItem) divFunctionsCustom.getChildAt(i);
			
			if( vItem.getKey().equals(aKey) ){
				return vItem;
			}
		}
		
		return null;
	}

	@Override
	public void onNetworkDataRecieved(DataResponsePackage data) {
		
		// check for new custom events
		synchronized (mFunctionItemsToUpdate) {
			if( !mFunctionsAdding ){
				mFunctionsAdding = true;
				mFunctionItemsToUpdate = new TreeMap<String, String>( data.customMemoryEvents );
				
				MainActivity.getInstance().runOnUiThread(new Runnable() {					
					@Override
					public void run() {
						
						// check if to add, to update item
						for( String key : mFunctionItemsToUpdate.keySet() ){
							FunctionItem vItem = getItem(key);
							if( vItem != null ){
								vItem.update(key, mFunctionItemsToUpdate.get(key));		// update
							} else {
								vItem = new FunctionItem(getActivity(), key,
										mFunctionItemsToUpdate.get(key));
								divFunctionsCustom.addView(vItem);						// add
							}
						}
						
						// remove items not in list
						for( int i=0; i < divFunctionsCustom.getChildCount(); i++ ){
							FunctionItem vItem = (FunctionItem) divFunctionsCustom.getChildAt(i);
							if( !mFunctionItemsToUpdate.containsKey(vItem.getKey()) && vItem.isRemoved() ){
								((ViewGroup) vItem.getParent()).removeView(vItem);
							}
						}
						
						// clear list and reset flag
						mFunctionItemsToUpdate.clear();
						mFunctionsAdding = false;
						
					}					
				});
				
			}				
		}
		
	}
	
}
