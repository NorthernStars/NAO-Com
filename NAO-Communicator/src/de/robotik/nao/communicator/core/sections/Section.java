package de.robotik.nao.communicator.core.sections;

import java.util.HashMap;
import java.util.Map;

import de.robotik.nao.communicator.MainActivity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public abstract class Section extends Fragment {
	
	private static final int WRONG_VALUE_MAX_COUNT = 2;
	
	protected String title = "PAGE TITLE";
	protected View rootView =  null;
	private Map<View, Integer> wrongValueCounter = new HashMap<View, Integer>();
	
	/**
	 * Default constructor
	 */
	public Section(){
		super();
	}
	
	/**
	 * Constructor
	 * @param aTitle Title of section
	 */
	public Section(String aTitle){
		title = aTitle;
	}

	/**
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}
	
	/**
	 * Called if view created
	 */
	abstract public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState);
	
	/**
	 * Finds {@link View} with rescource id.
	 * @param id	{@link Integer} rescource id.
	 * @return		{@link View} if found, {@code null} otherwise.
	 */
	protected View findViewById(int id){
		if( rootView != null ){
			return rootView.findViewById(id);
		}
		return null;
	}
	
	/**
	 * Update counter {@link Map} for filtering wrong values for some times
	 * @param vView		{@link View} of the value
	 * @return			{@code true} if counter is incremented,
	 * 					{@code false} if counter is full and counter will be reseted.
	 */
	protected boolean incrementWrongValueCounter(View vView){
		// check if map contains view
		if( wrongValueCounter.containsKey(vView) ){
			
			// check if to update counter
			if( wrongValueCounter.get(vView) < WRONG_VALUE_MAX_COUNT ){
				wrongValueCounter.put( vView, wrongValueCounter.get(vView)+1 );
				return true;
			}
			
			
		}
		
		wrongValueCounter.put(vView, 0);		
		return false;
	}
	
	/**
	 * Resets the counter value of {@link Map} for filtering wrong values.
	 * @param vView		{@link View} of value to reset.
	 */
	protected void resetWrongValueCounter(View vView){
		wrongValueCounter.put(vView, 0);
	}
	
	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
		super.setUserVisibleHint(isVisibleToUser);		
		MainActivity.getInstance().selectMenueItem(this);
	}

}
