package de.robotik.nao.communicator.core.sections;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public abstract class Page extends Fragment {
	
	protected String title = "PAGE TITLE";
	protected View rootView =  null;
	
	/**
	 * Default constructor
	 */
	public Page(){
		super();
	}
	
	/**
	 * Constructor
	 * @param aTitle Title of section
	 */
	public Page(String aTitle){
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
	
	protected View findViewById(int id){
		if( rootView != null ){
			return rootView.findViewById(id);
		}
		return null;
	}

}
