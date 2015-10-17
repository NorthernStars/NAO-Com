package de.robotik.nao.communicator.core;

import de.robotik.nao.communicator.MainActivity;
import de.robotik.nao.communicator.R;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Fragment used for managing interactions for and presentation of a navigation
 * drawer. See the <a href=
 * "https://developer.android.com/design/patterns/navigation-drawer.html#Interaction"
 * > design guidelines</a> for a complete explanation of the behaviors
 * implemented here.
 */
public class NavigationDrawerFragment extends Fragment implements
	OnClickListener{

	private LinearLayout divMenu;
	
	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		// get widgets
		View vRootView = inflater.inflate(R.layout.fragment_navigation_drawer, container, false);
		divMenu = (LinearLayout) vRootView.findViewById(R.id.divMenu);
		
		// set menu items
		for( String s :  getResources().getStringArray(R.array.menu_items) ){
			TextView vTextView = (TextView) inflater.inflate(R.layout.menu_list_item, divMenu, false);
			vTextView.setText(s);
			divMenu.addView(vTextView);
			vTextView.setOnClickListener(this);
		}	
		
		return vRootView;
	}
	
	/**
	 * Selects menu item
	 * @param aPosition	{@link Integer} position of item.
	 * @param aChecked	Set {@code true} to set item selected, {@code false} otherwise.
	 */
	public void setItemChecked(int aPosition, boolean aChecked){
		for( int i=0; i < divMenu.getChildCount(); i++ ){
			if( i == aPosition ){
				divMenu.getChildAt(i).setSelected(true);
			} else {
				divMenu.getChildAt(i).setSelected(false);
			}
		}
	}
	
	/**
	 * Closes navigation menu
	 */
	public void closeNavigation(){
		MainActivity.getInstance().getDrawerLayout().closeDrawers();
	}
	
	/**
	 * Shows navigation menu
	 */
	public void showNavigation(){
		MainActivity.getInstance().getDrawerLayout().openDrawer(Gravity.START);
	}

	@Override
	public void onClick(View v) {
		for( int i=0; i < divMenu.getChildCount(); i++ ){
			if( divMenu.getChildAt(i) == v ){
				setItemChecked(i, true);
				MainActivity.getInstance().getMainFragment().selectSection(i);
				closeNavigation();
			}
		}
	}
	
}
