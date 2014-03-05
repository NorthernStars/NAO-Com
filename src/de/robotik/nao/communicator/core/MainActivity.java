package de.robotik.nao.communicator.core;

import java.util.ArrayList;
import java.util.List;

import de.northernstars.naocom.R;
import de.robotik.nao.communicator.core.sections.Page;
import de.robotik.nao.communicator.core.sections.PageConnect;
import de.robotik.nao.communicator.core.sections.PageSpeech;
import de.robotik.nao.communicator.core.sections.PageStatus;
import de.robotik.nao.communicator.core.sections.PageWifi;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;

public class MainActivity extends FragmentActivity {

	private static List<Page> sections = new ArrayList<Page>();
	
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
	 * Adds layouts for fragment pages
	 */
	private void createPageFragmentLayouts(){
		if( sections.size() == 0 ){
			
			// Add all new sections here
			sections.add( new PageConnect("Connect") );
			sections.add( new PageWifi("Hotspot") );
			sections.add( new PageStatus("NAO") );
			sections.add( new PageSpeech("Speech") );
			
		}
	}

	/**
	 * @return the sections
	 */
	public static List<Page> getSections() {
		return sections;
	}

}
