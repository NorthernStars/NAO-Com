package de.robotik.nao.communicator.core;

import java.util.Locale;




import de.robotik.nao.communicator.MainActivity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class SectionsPagerAdapter extends FragmentPagerAdapter {

	public SectionsPagerAdapter(FragmentManager fm) {
		super(fm);
	}

	/**
	 * Gets page using integer position
	 * @param position
	 * @return New framgent of section
	 */
	@Override
	public Fragment getItem(int position) {			
		if( position < getCount() ){
			return MainActivity.getInstance().getSections().get(position);
		}		
		return null;
	}
	

	/**
	 * @return Total number of sections
	 */
	@Override
	public int getCount() {
		return MainActivity.getInstance().getSections().size();
	}

	/**
	 * @return Title of section
	 */
	@Override
	public CharSequence getPageTitle(int position) {
		Locale locale = Locale.getDefault();
		if( position < getCount() ){
			return MainActivity.getInstance().getSections().get(position).getTitle().toUpperCase(locale);
		}
		return null;
	}
	
}
