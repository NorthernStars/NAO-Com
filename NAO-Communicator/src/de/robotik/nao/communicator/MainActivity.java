package de.robotik.nao.communicator;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;

import de.robotik.nao.communicator.R;
import de.robotik.nao.communicator.core.NAOComInstaller;
import de.robotik.nao.communicator.core.RemoteNAO;
import de.robotik.nao.communicator.core.SectionsPagerAdapter;
import de.robotik.nao.communicator.core.revisions.ServerRevision;
import de.robotik.nao.communicator.core.revisions.ServerRevisionChecker;
import de.robotik.nao.communicator.core.sections.Section;
import de.robotik.nao.communicator.core.sections.SectionConnect;
import de.robotik.nao.communicator.core.sections.SectionFunctions;
import de.robotik.nao.communicator.core.sections.SectionLed;
import de.robotik.nao.communicator.core.sections.SectionProgramming;
import de.robotik.nao.communicator.core.sections.SectionSpeech;
import de.robotik.nao.communicator.core.sections.SectionStatus;
import de.robotik.nao.communicator.core.sections.SectionHotspot;
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
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class MainActivity extends FragmentActivity implements
	NetworkDataRecievedListener,
	NetworkDataSender,
	OnItemClickListener{
	
	public static final String INSTALLER_INTENT_EXTRA_WORKSTATION = "installer.intent.extra.device";
	public static final String INSTALLER_INTENT_EXTRA_HOST = "installer.intent.extra.host";
	public static final String INSTALLER_INTENT_EXTRA_REVISION = "installer.intent.extra.revision";
	public static final String INSTALLER_INTENT_EXTRA_UPDATE = "installer.intent.extra.update";
	
	private static MainActivity INSTANCE;
	private static final String SHARED_PREFERENCES = "naocom_preferences";
	private static final String INSTANCE_STATE_KESY_HOST_ADRESSES = "HOSTS";
	private static ServerRevision onlineRevision;
	
	private List<Section> mSections = new ArrayList<Section>();	
	private RemoteDevice mConnectedDevice = null;
	private List<NetworkDataRecievedListener> dataRecievedListener = new ArrayList<NetworkDataRecievedListener>();
	private List<OnActivityResultListener> activityResultListener = new ArrayList<OnActivityResultListener>();
	
	private String mTitle = "[offline] NAO Communicator";
	private SectionsPagerAdapter mSectionsPagerAdapter;
	private ViewPager mViewPager;
	private DrawerLayout mDrawerLayout;
	private ActionBarDrawerToggle mDrawerToggle;
	private LinearLayout mMenu;
	private ListView mMenueListView;
	private String[] mMenuItems;

	/**
	 * Called if activity created
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		INSTANCE = this;
		
		setContentView(R.layout.activity_main);	
		updateTitle(mTitle);
		
		// add layouts
		createPageFragmentLayouts();
		
		// Create the adapter that will return a fragment for each of
		// primary sections of the app.
		mSectionsPagerAdapter = new SectionsPagerAdapter(
				getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);
		
		// set up left slide menu
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerLayout.setDrawerShadow( R.drawable.drawer_shadow, GravityCompat.START );
		mMenu = (LinearLayout) findViewById(R.id.menu);
		mMenueListView = (ListView) findViewById(R.id.lstMenu);
		mMenuItems = getResources().getStringArray(R.array.menu_items);
		mMenueListView.setAdapter( new ArrayAdapter<String>(this,
				R.layout.menu_list_item, mMenuItems) );
		
		mMenueListView.setOnItemClickListener(this);
		mMenueListView.setItemChecked(0, true);
		
		// enable ActionBar app icon to behave as action to toggle nav drawer
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
        
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
                getActionBar().setTitle(mTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView) {
                getActionBar().setTitle(mTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        
        // start fetching data for server update
        (new Thread(new ServerRevisionChecker())).start();

	}	
	
	@Override
	protected void onActivityResult(int requestCode,int resultCode,Intent data) {
		// reconnect remote nao
		RemoteNAO vRemoteNao = RemoteNAO.getCurrentRemoteNao();
		if( vRemoteNao != null && !vRemoteNao.isConnected() ){
			vRemoteNao.reconnect();
		}
		
		// call listener
		for( OnActivityResultListener listener : activityResultListener ){
			listener.onActivityResult(requestCode, resultCode, data);
		}
		
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	/**
	 * Adds layouts for fragment pages
	 */
	private void createPageFragmentLayouts(){
		if( mSections.size() == 0 ){			
			// Add all new sections here
			mSections.add( new SectionConnect("Connect") );
			mSections.add( new SectionHotspot("Hotspot") );
			mSections.add( new SectionStatus("NAO Status") );
			mSections.add( new SectionSpeech("Speech") );
			mSections.add( new SectionFunctions("Functions") );
			mSections.add( new SectionLed("LEDs") );
			mSections.add( new SectionProgramming("Programming") );			
		}
	}

	/**
	 * @return the sections
	 */
	public List<Section> getSections() {
		return mSections;
	}

	/**
	 * @return the connectedDevice
	 */
	public RemoteDevice getConnectedDevice() {
		return mConnectedDevice;
	}

	/**
	 * @param aConnectedDevice the connectedDevice to set
	 */
	public void setConnectedDevice(RemoteDevice aConnectedDevice) {
		if( mConnectedDevice != null ){
			mConnectedDevice.getNao().removeNetworkDataRecievedListener(this);
		}
		mConnectedDevice = aConnectedDevice;
		
		if( mConnectedDevice != null ){
			mConnectedDevice.getNao().addNetworkDataRecievedListener(this);
		}
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
	 * @return Current {@link ViewPager}
	 */
	public ViewPager getViewPager() {
		return mViewPager;
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
	 * Selects item in menu drawer
	 * @param view
	 */
	public void selectMenueItem(Section section){
		for( int i=0; i < mSections.size(); i++ ){
			Section s = mSections.get(i);
			if( s == section ){
				mMenueListView.setItemChecked(i, true);
			}
		}
		
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
			vIntent.putExtra( INSTALLER_INTENT_EXTRA_HOST, aDevice.getNao().getHostAdresses().get(0) );
			vIntent.putExtra( INSTALLER_INTENT_EXTRA_REVISION, jsonRevision );
			vIntent.putExtra( INSTALLER_INTENT_EXTRA_UPDATE, aUpdate );
			
			// disconnect
			if( aDevice != null ){
				aDevice.getNao().disconnect();
			}
			
			// start installer activity
			startActivity(vIntent);
		}
		
	}
	
	/**
	 * @return Current {@link MainActivity} instance.
	 */
	public static MainActivity getInstance() {
		return INSTANCE;
	}
	
	/**
	 * @return	Applications {@link SharedPreferences}.
	 */
	public static SharedPreferences getPreferences(){
		return getInstance().getSharedPreferences(SHARED_PREFERENCES, Context.MODE_PRIVATE);
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

	@Override
	protected void onStop() {		
		RemoteNAO nao = RemoteNAO.getCurrentRemoteNao();
		if( nao != null ){
			nao.disconnect();
		}
		super.onStop();
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
	public void notifyDataRecievedListeners(DataResponsePackage data){
		synchronized (dataRecievedListener) {
			for( NetworkDataRecievedListener listener : dataRecievedListener ){
				Runnable r = new NetworkDataRecievedListenerNotifier(listener, data);
				new Thread(r).start();
			}
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if( mDrawerToggle.onOptionsItemSelected(item) ){
			// menu item clicked
			if( mDrawerLayout.isDrawerOpen(mMenu) ){
				mDrawerLayout.closeDrawer(mMenu);
			} else {
				mDrawerLayout.openDrawer(mMenu);
			}
		}
		
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		mViewPager.setCurrentItem(position);
		mMenueListView.setItemChecked(position, true);
		mDrawerLayout.closeDrawer(mMenu);
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {	
		// check if device connected
		RemoteDevice vDevice = getConnectedDevice();
		if( vDevice != null && vDevice.getNao().isConnected() ){
			
			// add connected hosts
			ArrayList<String> vHostAdresses = new ArrayList<String>( vDevice.getNao().getHostAdresses() );
			outState.putStringArrayList( INSTANCE_STATE_KESY_HOST_ADRESSES, vHostAdresses );
			
		}
		super.onSaveInstanceState(outState);
	}
	
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {		
		// check if device was connected
		if( savedInstanceState.containsKey(INSTANCE_STATE_KESY_HOST_ADRESSES) ){
			
			ArrayList<String> vHostAdresses = savedInstanceState.getStringArrayList(INSTANCE_STATE_KESY_HOST_ADRESSES);
			if( vHostAdresses.size() > 0 ){
				
				// create new remote device and add host adresses
				RemoteDevice vDevice = new RemoteDevice(this, vHostAdresses.get(0));
				for( String vHost : vHostAdresses ){
					vDevice.addAdress(vHost);
				}
				
				// add remote device
				SectionConnect.getInstance().clearDevicesList();
				SectionConnect.getInstance().addRemoteDevice(vDevice);
				
				// connect device
				vDevice.connect();
				
			}
		}
		super.onRestoreInstanceState(savedInstanceState);
	}

}
