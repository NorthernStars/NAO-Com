package de.robotik.nao.communicator.core.sections;

import de.northernstars.naocom.R;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class SectionStatus extends Section{

	public SectionStatus() {}
	
	public SectionStatus(String title) {
		super(title);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {		
		// connect ui widgets
		rootView = inflater.inflate(R.layout.page_status, container, false);
		
		
		return rootView;
	}

}
