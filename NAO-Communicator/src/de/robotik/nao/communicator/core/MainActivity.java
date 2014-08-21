package de.robotik.nao.communicator.core;

import java.util.ArrayList;
import java.util.List;

import de.northernstars.naocom.R;
import de.robotik.nao.communicator.core.sections.Section;
import de.robotik.nao.communicator.core.sections.SectionConnect;
import de.robotik.nao.communicator.core.sections.SectionSpeech;
import de.robotik.nao.communicator.core.sections.SectionStatus;
import de.robotik.nao.communicator.core.sections.SectionWifi;
import de.robotik.nao.communicator.core.widgets.RemoteDevice;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;

public class MainActivity extends FragmentActivity {

	private static List<Section> sections = new ArrayList<Section>();
	
	private static RemoteDevice connectedDevice = null;
	
	private SectionsPagerAdapter mSectionsPagerAdapter;
	private ViewPager mViewPager;

	/**
	 * Called if activity created
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_main);		
		
		// add layouts
		createPageFragmentLayouts();
		
		// Create the adapter that will return a fragment for each of the three
		// primary sections of the app.
		mSectionsPagerAdapter = new SectionsPagerAdapter(
				getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);

	}
	
	
	
	/**
	 * @param sectionName
	 * @return {@link Section} with parameter {@code sectionName} as title.
	 */
	public static Section getSection(String sectionName){
		for( Section section : sections ){
			if( section.getTitle().equals(sectionName) ){
				return section;
			}
		}
		return null;
	}
	
	
	
	/**
	 * Adds layouts for fragment pages
	 */
	private void createPageFragmentLayouts(){
		if( sections.size() == 0 ){
			
			// Add all new sections here
			sections.add( new SectionConnect("Connect") );
			sections.add( new SectionWifi("Hotspot") );
			sections.add( new SectionStatus("NAO") );
			sections.add( new SectionSpeech("Speech") );
			
		}
	}

	/**
	 * @return the sections
	 */
	public static List<Section> getSections() {
		return sections;
	}



	/**
	 * @return the connectedDevice
	 */
	public static RemoteDevice getConnectedDevice() {
		return connectedDevice;
	}



	/**
	 * @param connectedDevice the connectedDevice to set
	 */
	public static void setConnectedDevice(RemoteDevice connectedDevice) {
		MainActivity.connectedDevice = connectedDevice;
	}

}
