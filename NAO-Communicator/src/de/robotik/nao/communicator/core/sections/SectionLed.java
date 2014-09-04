package de.robotik.nao.communicator.core.sections;

import de.northernstars.naocom.R;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class SectionLed extends Section {

	public SectionLed() {}
	
	public SectionLed(String aTitle){
		super(aTitle);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.page_led, container, false);
		return rootView;
	}

}
