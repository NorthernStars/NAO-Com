package de.robotik.nao.communicator;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;

import de.robotik.nao.communicator.R;
import de.robotik.nao.communicator.core.MainFragment;
import de.robotik.nao.communicator.core.NAOComInstaller;
import de.robotik.nao.communicator.core.NavigationDrawerFragment;
import de.robotik.nao.communicator.core.revisions.ServerRevision;
import de.robotik.nao.communicator.core.revisions.ServerRevisionChecker;
import de.robotik.nao.communicator.core.widgets.RemoteDevice;
import de.robotik.nao.communicator.network.NetworkDataRecievedListenerNotifier;
import de.robotik.nao.communicator.network.data.NAOCommands;
import de.robotik.nao.communicator.network.data.response.DataResponsePackage;
import de.robotik.nao.communicator.network.interfaces.NetworkDataRecievedListener;
import de.robotik.nao.communicator.network.interfaces.NetworkDataSender;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager.OnActivityResultListener;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends FragmentActivity implements
	NetworkDataRecievedListener,
	NetworkDataSender{
	
	/**
	 * Static strings
	 */
	public static final String INSTALLER_INTENT_EXTRA_WORKSTATION = "installer.intent.extra.device";
	public static final String INSTALLER_INTENT_EXTRA_HOST = "installer.intent.extra.host";
	public static final String INSTALLER_INTENT_EXTRA_REVISION = "installer.intent.extra.revision";
	public static final String INSTALLER_INTENT_EXTRA_UPDATE = "installer.intent.extra.update";	
	
	/**
	 * Layout
	 */	
	private DrawerLayout mDrawerLayout;
	
	private ActionBarDrawerToggle mDrawerToggle;
	private NavigationDrawerFragment mNavigationDrawerFragment;
	private MainFragment mMainFragment;
	
	private String mTitle;
	
	/**
	 * Preferences and online backup
	 */
	private static MainActivity INSTANCE;
	private static final String SHARED_PREFERENCES = "naocom_preferences";
	private static ServerRevision onlineRevision;
	
	/**
	 * Listener lists
	 */
	private List<NetworkDataRecievedListener> dataRecievedListener = new ArrayList<NetworkDataRecievedListener>();
	private List<OnActivityResultListener> activityResultListener = new ArrayList<OnActivityResultListener>();
	
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		
		INSTANCE = this;
		
		setContentView(R.layout.activity_main);
		updateTitle( getString(R.string.title_offline) );
		
		// add layouts
		//createPageFragmentLayouts();

		// set navigation drawer fragment and drawer shadow
		FragmentManager vFragmentManager = getSupportFragmentManager();
		mNavigationDrawerFragment = (NavigationDrawerFragment) vFragmentManager
				.findFragmentById(R.id.navigation_drawer);
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerLayout.setDrawerShadow( R.drawable.drawer_shadow, GravityCompat.START );
        
        // set main fragment
        mMainFragment = new MainFragment();
        vFragmentManager.beginTransaction()
        	.replace(R.id.container, mMainFragment)
        	.commit();
        
        // ActionBarDrawerToggle ties together the the proper interactions
        // between the sliding drawer and the action bar app icon
        mDrawerToggle = new ActionBarDrawerToggle(
                this,					/* host Activity */
                mDrawerLayout,			/* DrawerLayout object */
                R.drawable.ic_drawer,	/* nav drawer image to replace 'Up' caret */
                R.string.menu_open,		/* "open drawer" description for accessibility */
                R.string.menu_close		/* "close drawer" description for accessibility */
                ) {
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
            }

            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        
        // enable ActionBar app icon to behave as action to toggle nav drawer
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
        
        // start fetching data for server update
        (new Thread(new ServerRevisionChecker())).start();

	}
	
	/**
	 * @return	{@link NavigationDrawerFragment}
	 */
	public NavigationDrawerFragment getNavigationDrawer(){
		return mNavigationDrawerFragment;
	}
	
	public DrawerLayout getDrawerLayout(){
		return mDrawerLayout;
	}
	
	/**
	 * @return	{@link MainFragment}
	 */
	public MainFragment getMainFragment(){
		return mMainFragment;
	}
	
	/**
	 * @return	{@link Integer} of online available server revision.
	 */
	public ServerRevision getOnlineRevision(){
		return onlineRevision;
	}
	
	/**
	 * Sets revision of online available server.
	 * @param aRevision	{@link ServerRevision} revision.
	 */
	public void setOnlineRevision(ServerRevision aRevision){
		if( aRevision == null ){
			aRevision = new ServerRevision();
		}
		onlineRevision = aRevision;
	}
	
	/**
	 * @return	Applications {@link SharedPreferences}.
	 */
	public static SharedPreferences getPreferences(){
		return getInstance().getSharedPreferences(SHARED_PREFERENCES, Context.MODE_PRIVATE);
	}
	
	/**
	 * Update action bar title
	 * @param aTitle	{@link String} for new title
	 */
	public void updateTitle(String aTitle){
		mTitle = aTitle;
		new Handler(Looper.getMainLooper()).post(new Runnable() {			
			@Override
			public void run() {
				setTitle( mTitle );
			}
		});
	}
	
	/**
	 * Function called if a section header is clicked.
	 * Expands / Collapses the following {@link LinearLayout}, if existing.
	 * @param vView		Section header {@link View}
	 */
	public void containerClicked(View vView){
		
		ViewGroup vParent = (ViewGroup) vView.getParent();
		if( vParent != null ){
			
			// get next view
			int vPosition = vParent.indexOfChild(vView)+1;
			if( vParent.getChildCount() > vPosition ){				
				View vViewContainer = vParent.getChildAt(vPosition);
				if( vViewContainer != null
						&& vViewContainer instanceof LinearLayout
						&& vView instanceof TextView){
					
					// cast text view
					TextView vTextView = (TextView) vView;					
					if( vViewContainer.getVisibility() == View.VISIBLE ){
						
						// set visible
						vViewContainer.setVisibility( View.GONE );
						vTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0,
								R.drawable.ic_action_expand, 0);
						
					} else {
						
						// set invisible
						vViewContainer.setVisibility( View.VISIBLE );
						vTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0,
								R.drawable.ic_action_collapse, 0);
						
					}
					
				}				
			}
		}
		
	}
	
	/**
	 * Addes an external {@link OnActivityResultListener}.
	 * @param listener	{@link OnActivityResultListener} to add.
	 */
	public void addOnActivityResultListener(OnActivityResultListener listener){		
		// add listener
		if( !activityResultListener.contains(listener) ){
			activityResultListener.add(listener);
		}
	}
	
	/**
	 * Removes an external {@link OnActivityResultListener}.
	 * @param listener	{@link OnActivityResultListener} to remove.
	 */
	public void removeOnActivityResultListener(OnActivityResultListener listener){
		activityResultListener.remove(listener);
	}
	
	/**
	 * Starts the installer to update or install the latest server revision on remote device.
	 * @param aDevice		{@link RemoteDevice} to install server.
	 * @param aUpdate		{@link Boolean} update flag. {@code true} if to update existing server, {@code false} otherwise.
	 */
	public void startInstaller(RemoteDevice aDevice, boolean aUpdate){
		
		ServerRevision vRevision = getOnlineRevision();
		
		if( vRevision.getRevision() >= 0 ){		
			// create intent
			Intent vIntent = new Intent(this, NAOComInstaller.class);
			Gson vGson = new Gson();
			String jsonRevision = vGson.toJson(vRevision);
			
			vIntent.putExtra( INSTALLER_INTENT_EXTRA_WORKSTATION, aDevice.getWorkstationName() );
			vIntent.putExtra( INSTALLER_INTENT_EXTRA_HOST, aDevice.getHostAdresses().get(0) );
			vIntent.putExtra( INSTALLER_INTENT_EXTRA_REVISION, jsonRevision );
			vIntent.putExtra( INSTALLER_INTENT_EXTRA_UPDATE, aUpdate );
			
			// disconnect
			if( aDevice != null ){
				aDevice.disconnect();
			}
			
			// start installer activity
			startActivity(vIntent);
		}
		
	}
	
	/**
	 * NEW THINGS
	 */
	
	/**
	 * Shows toast.
	 * @param aMessage		{@link Integer} id of message to show.
	 * @param aDurations	Duration either {@code LENGTH_SHORT} or {@code LENGTH_LONG}.
	 */
	public void makeToast(int aMessage, int aDurations){
		Toast.makeText(getApplicationContext(), getString(aMessage), aDurations).show();
	}
	
	@Override
	protected void onActivityResult(int requestCode,int resultCode,Intent data) {
		// TODO: reconnect remote nao
		
		// call listener
		for( OnActivityResultListener listener : activityResultListener ){
			listener.onActivityResult(requestCode, resultCode, data);
		}
		
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	@Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if( mDrawerToggle.onOptionsItemSelected(item) ){
			return true;
		}
		
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void addNetworkDataRecievedListener(NetworkDataRecievedListener listener){
		synchronized(dataRecievedListener) {
			dataRecievedListener.add(listener);
		}
	}
	
	@Override
	public void removeNetworkDataRecievedListener(NetworkDataRecievedListener listener){
		synchronized(dataRecievedListener) {
			if( listener == null ){
				dataRecievedListener.clear();
			}
			else{
				dataRecievedListener.remove(listener);
			}
		}
	}

	
	@Override
	public void notifyDataRecievedListeners(DataResponsePackage data){
		synchronized (dataRecievedListener) {
			for( NetworkDataRecievedListener listener : dataRecievedListener ){
				Runnable r = new NetworkDataRecievedListenerNotifier(listener, data);
				new Thread(r).start();
			}
		}
	}
	
	@Override
	public void onNetworkDataRecieved(DataResponsePackage data) {
		if( data.request.command == NAOCommands.SYS_DISCONNECT && data.requestSuccessfull ){
			updateTitle( "[offline] NAO Communicator" );
		} else {
			updateTitle( "[" + data.batteryLevel + "%] " + data.naoName );
		}
		notifyDataRecievedListeners(data);
	}
	
	
	
	/**
	 * @return Current {@link MainActivity} instance.
	 */
	public static MainActivity getInstance() {
		return INSTANCE;
	}

}
