package de.robotik.nao.communicator.core.sections;

import com.larswerkman.holocolorpicker.ColorPicker;

import de.robotik.nao.communicator.R;
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

public class SectionLed extends Section implements
	OnClickListener,
	NetworkDataRecievedListener{
	
	private Button btnLedHappy;
	private Button btnLedAngry;
	private Button btnLedLaugh;
	private Button btnLedCautious;
	private Button btnLedThinking;
	private Button btnLedMischievous;
	private Button btnLedDisco;
	private Button btnLedBlink;
	private Button btnLedCircleEyes;
	private Button btnLedFlash;
	
	private ColorPicker pckColor;
	private Button btnLedLeftEye;
	private Button btnLedRightEye;
	
	public SectionLed() {}
	
	public SectionLed(String aTitle){
		super(aTitle);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.page_led, container, false);
		
		// get widgets and add listener
		(btnLedAngry = (Button) findViewById(R.id.btnLedAngry)).setOnClickListener(this);
		(btnLedBlink = (Button) findViewById(R.id.btnLedBlink)).setOnClickListener(this);
		(btnLedCautious = (Button) findViewById(R.id.btnLedCautious)).setOnClickListener(this);
		(btnLedCircleEyes = (Button) findViewById(R.id.btnLedCircleEyes)).setOnClickListener(this);
		(btnLedDisco = (Button) findViewById(R.id.btnLedDisco)).setOnClickListener(this);
		(btnLedFlash = (Button) findViewById(R.id.btnLedFlash)).setOnClickListener(this);
		(btnLedHappy = (Button) findViewById(R.id.btnLedHappy)).setOnClickListener(this);
		(btnLedLaugh = (Button) findViewById(R.id.btnLedLaugh)).setOnClickListener(this);
		(btnLedLeftEye = (Button) findViewById(R.id.btnLedLeftEye)).setOnClickListener(this);
		(btnLedMischievous = (Button) findViewById(R.id.btnLedMischievous)).setOnClickListener(this);
		(btnLedRightEye = (Button) findViewById(R.id.btnLedRightEye)).setOnClickListener(this);
		(btnLedThinking = (Button) findViewById(R.id.btnLedThinking)).setOnClickListener(this);
		
		pckColor = (ColorPicker) findViewById(R.id.pckLedColor);
		
		return rootView;
	}
	
	@Override
	public void onClick(View v) {		
		if( v ==  btnLedAngry){
			RemoteNAO.sendCommand( NAOCommands.LED_ANGRY );
			
		} else if( v == btnLedBlink ){
			RemoteNAO.sendCommand( NAOCommands.LED_BLINK );
			
		} else if( v == btnLedCautious ){
			RemoteNAO.sendCommand( NAOCommands.LED_CAUTIOUS );
			
		} else if( v == btnLedCircleEyes ){
			RemoteNAO.sendCommand( NAOCommands.LED_CIRCLE_EYES );
			
		} else if( v == btnLedDisco ){
			RemoteNAO.sendCommand( NAOCommands.LED_DISCO );
			
		} else if( v == btnLedFlash ){
			RemoteNAO.sendCommand( NAOCommands.LED_FLASH );
			
		} else if( v == btnLedHappy ){
			RemoteNAO.sendCommand( NAOCommands.LED_HAPPY );
			
		} else if( v == btnLedLaugh ){
			RemoteNAO.sendCommand( NAOCommands.LED_LAUGH );
			
		} else if( v == btnLedMischievous ){
			RemoteNAO.sendCommand( NAOCommands.LED_MISCHIEVIOUS );
			
		} else if( v == btnLedThinking ){
			RemoteNAO.sendCommand( NAOCommands.LED_THINKING );
			
		} else if( v == btnLedRightEye ){			
			int vColor = pckColor.getColor();
			RemoteNAO.sendCommand( NAOCommands.LED_SET_EYE,
					new String[]{ "right", Integer.toString(vColor) });
			
		} else if( v == btnLedLeftEye ){			
			int vColor = pckColor.getColor();
			RemoteNAO.sendCommand( NAOCommands.LED_SET_EYE,
					new String[]{ "left", Integer.toString(vColor) });
			
		} 
	}
	
	@Override
	public void onNetworkDataRecieved(DataResponsePackage data) {
		// TODO Auto-generated method stub
		
	}

}
