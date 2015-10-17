package de.robotik.nao.communicator.core;

import java.util.ArrayList;
import java.util.List;
import de.robotik.nao.communicator.MainActivity;
import de.robotik.nao.communicator.R;
import de.robotik.nao.communicator.core.sections.Section;
import de.robotik.nao.communicator.core.sections.SectionConnect;
import de.robotik.nao.communicator.core.sections.SectionFunctions;
import de.robotik.nao.communicator.core.sections.SectionHotspot;
import de.robotik.nao.communicator.core.sections.SectionLed;
import de.robotik.nao.communicator.core.sections.SectionProgramming;
import de.robotik.nao.communicator.core.sections.SectionSpeech;
import de.robotik.nao.communicator.core.sections.SectionStatus;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class MainFragment extends Fragment implements
	OnPageChangeListener{
	
	private ViewPager pager;
	
	private SectionsPagerAdapter mSectionsPagerAdapter;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// get root view
		pager = (ViewPager) inflater.
				inflate(R.layout.fragment_main, container, false);
		
		// set pager adapter
		mSectionsPagerAdapter = new SectionsPagerAdapter(MainActivity.getInstance().getSupportFragmentManager());
		pager.setAdapter(mSectionsPagerAdapter);
		pager.setOnPageChangeListener(this);
		
		return pager;
	}
	
	/**
	 * Select a section page.
	 * @param aPosition	{@link Integer} position of section.
	 */
	public void selectSection(int aPosition){
		if( aPosition < mSectionsPagerAdapter.getCount() ){
			pager.setCurrentItem(aPosition);
		}
	}
	
	@Override
	public void onPageScrollStateChanged(int arg0) {}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {}

	@Override
	public void onPageSelected(int position) {
		MainActivity.getInstance().getNavigationDrawer().setItemChecked(position, true);
	}
	
	/**
	 * {@link FragmentStatePagerAdapter} to handle page sections.
	 * @author Hannes Eilers	 *
	 */
	private class SectionsPagerAdapter extends FragmentStatePagerAdapter{

		private SparseArray<Section> mSections = new SparseArray<Section>();
		private List<Class<?>> mSectionClasses = new ArrayList<Class<?>>();
		
		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
			
			/**
			 * Add section classes
			 */
			mSectionClasses.add(SectionConnect.class);
			mSectionClasses.add(SectionHotspot.class);
			mSectionClasses.add(SectionStatus.class);
			mSectionClasses.add(SectionSpeech.class);
			mSectionClasses.add(SectionFunctions.class);
			mSectionClasses.add(SectionLed.class);
			mSectionClasses.add(SectionProgramming.class);
		}
		
		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			super.destroyItem(container, position, object);
			mSections.delete(position);
		}
		
		@Override
		public Fragment getItem(int position) {			
			if( mSections.get(position) == null ){				
				try{
					
					// create fragment
					mSections.append(position, (Section) mSectionClasses.get(position).newInstance());
					
				} catch( IllegalAccessException e ){
					e.printStackTrace();
				} catch (java.lang.InstantiationException e) {
					e.printStackTrace();
				}				
			}
			
			return mSections.get(position);
		}
		
		@Override
		public int getCount() {
			return mSectionClasses.size();
		}
		
	}

}
