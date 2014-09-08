package de.robotik.nao.communicator.core.sections;

import de.northernstars.naocom.R;
import de.robotik.nao.communicator.core.MainActivity;
import de.robotik.nao.communicator.core.RemoteNAO;
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
			RemoteNAO.sendCommand( NAOCommands.MEMORY_EVENT_ADD, new String[]{vEventName, vEventKey} );
			
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

	@Override
	public void onNetworkDataRecieved(DataResponsePackage data) {
		// TODO Auto-generated method stub
		
	}
	
}
