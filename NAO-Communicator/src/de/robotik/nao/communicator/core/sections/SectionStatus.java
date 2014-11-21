package de.robotik.nao.communicator.core.sections;

import java.util.Map;

import de.robotik.nao.communicator.R;
import de.robotik.nao.communicator.MainActivity;
import de.robotik.nao.communicator.core.RemoteNAO;
import de.robotik.nao.communicator.network.data.NAOAutonomousLifeStates;
import de.robotik.nao.communicator.network.data.NAOCommands;
import de.robotik.nao.communicator.network.data.NAOJoints;
import de.robotik.nao.communicator.network.data.response.DataResponsePackage;
import de.robotik.nao.communicator.network.interfaces.NetworkDataRecievedListener;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.TextView;

public class SectionStatus extends Section implements
	NetworkDataRecievedListener,
	OnSeekBarChangeListener,
	OnClickListener,
	OnItemSelectedListener,
	OnRefreshListener{

	private DataResponsePackage currentResponseData;
	private ArrayAdapter<NAOAutonomousLifeStates> adapterAutonomousLifeStates;
	private boolean disableSending = false;
	private boolean created = true;	
	
	private SwipeRefreshLayout swipeStatus;
	
	private TextView txtStatusDeviceName;
	private ImageView imgStatusBattery;
	private Button btnStatusChangeNaoName;
	private Button btnStatusLeftHand;
	private Button btnStatusRightHand;
	private SeekBar skbSystemVolume;
	private SeekBar skbPlayerVolume;	
	private Spinner spAutonomousLife;
	private TextView lblStatusSystemVolume;
	private TextView lblStatusPlayerVolume;
	private TextView lblStatusAutonomousLife;
	
	private ImageView imgJointBody;
	private ImageView imgJointHead;
	private ImageView imgJointLArm;
	private ImageView imgJointRArm;
	private ImageView imgJointLLeg;
	private ImageView imgJointRLeg;
	private ImageView imgJointLHand;
	private ImageView imgJointRHand;
	
	// Currently unsused because of missing chain i naoqi
//	private ImageView imgJointLShoulder;
//	private ImageView imgJointRShoulder;
//	private ImageView imgJointLElbow;
//	private ImageView imgJointRElbow;
//	private ImageView imgJointLWrist;
//	private ImageView imgJointRWrist;
//	private ImageView imgJointLHip;
//	private ImageView imgJointRHip;
//	private ImageView imgJointLKnee;
//	private ImageView imgJointRKnee;
//	private ImageView imgJointlAnkle;
//	private ImageView imgJointRAnkle;
	
	
	/**
	 * Constructor
	 */
	public SectionStatus() {}
	
	/**
	 * Constructor
	 * @param title	{@link String} title.
	 */
	public SectionStatus(String title) {
		super(title);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		created = true;
		
		// connect ui widgets
		rootView = inflater.inflate(R.layout.page_status, container, false);
		
		// get widgets
		swipeStatus = (SwipeRefreshLayout) findViewById(R.id.swipeStatus);
		
		txtStatusDeviceName = (TextView) findViewById(R.id.txtStatusDevicename);
		imgStatusBattery = (ImageView) findViewById(R.id.imgStatusBattery);
		btnStatusChangeNaoName = (Button) findViewById(R.id.btnStatusChangeNaoName);
		btnStatusLeftHand = (Button) findViewById(R.id.btnStatusLeftHand);
		btnStatusRightHand = (Button) findViewById(R.id.btnStatusRightHand);
		skbSystemVolume = (SeekBar) findViewById(R.id.skbSystemVolume);
		skbPlayerVolume = (SeekBar) findViewById(R.id.skbStatusPlayerVolume);
		spAutonomousLife = (Spinner) findViewById(R.id.spAutonomousLife);
		lblStatusPlayerVolume = (TextView) findViewById(R.id.lblStatusPlayerVolume);
		lblStatusSystemVolume = (TextView) findViewById(R.id.lblStatusSystemVolume);
		lblStatusAutonomousLife = (TextView) findViewById(R.id.lblStatusAutonomousLife);
		
		(imgJointBody = (ImageView) findViewById(R.id.imgJointBody)).setOnClickListener(this);
		(imgJointHead = (ImageView) findViewById(R.id.imgJointHead)).setOnClickListener(this);
		(imgJointLArm = (ImageView) findViewById(R.id.imgJointLArm)).setOnClickListener(this);
		(imgJointRArm = (ImageView) findViewById(R.id.imgJointRArm)).setOnClickListener(this);
		(imgJointLLeg = (ImageView) findViewById(R.id.imgJointLLeg)).setOnClickListener(this);
		(imgJointRLeg = (ImageView) findViewById(R.id.imgJointRLeg)).setOnClickListener(this);
		(imgJointLHand = (ImageView) findViewById(R.id.imgJointLHand)).setOnClickListener(this);
		(imgJointRHand = (ImageView) findViewById(R.id.imgJointRHand)).setOnClickListener(this);
		
		// Currently unused beachause of missing chain in naoqi
//		(imgJointLShoulder = (ImageView) findViewById(R.id.imgJointLShoulder)).setOnClickListener(this);
//		(imgJointRShoulder = (ImageView) findViewById(R.id.imgJointRShoulder)).setOnClickListener(this);
//		(imgJointLElbow = (ImageView) findViewById(R.id.imgJointLElbow)).setOnClickListener(this);
//		(imgJointRElbow = (ImageView) findViewById(R.id.imgJointRElbow)).setOnClickListener(this);
//		(imgJointLWrist = (ImageView) findViewById(R.id.imgJointLWrist)).setOnClickListener(this);
//		(imgJointRWrist = (ImageView) findViewById(R.id.imgJointRWrist)).setOnClickListener(this);
//		(imgJointLHip = (ImageView) findViewById(R.id.imgJointLHip)).setOnClickListener(this);
//		(imgJointRHip = (ImageView) findViewById(R.id.imgJointRHip)).setOnClickListener(this);
//		(imgJointLKnee = (ImageView) findViewById(R.id.imgJointLKnee)).setOnClickListener(this);
//		(imgJointRKnee = (ImageView) findViewById(R.id.imgJointRKnee)).setOnClickListener(this);
//		(imgJointlAnkle = (ImageView) findViewById(R.id.imgJointlAnkle)).setOnClickListener(this);
//		(imgJointRAnkle = (ImageView) findViewById(R.id.imgJointRAnkle)).setOnClickListener(this);
		
		// set swipe layout
		swipeStatus.setOnRefreshListener(this);
		swipeStatus.setColorSchemeResources(
				R.color.darkerblue,
				R.color.darkblue,
				R.color.blue,
				R.color.lighterblue);
		
		// set spinner items
		adapterAutonomousLifeStates = new ArrayAdapter<NAOAutonomousLifeStates>(
				getActivity(),
				android.R.layout.simple_spinner_item,
				NAOAutonomousLifeStates.values());
		spAutonomousLife.setAdapter( adapterAutonomousLifeStates );
		
		// add listener
		MainActivity.getInstance().addNetworkDataRecievedListener(this);
		
		btnStatusChangeNaoName.setOnClickListener(this);
		btnStatusLeftHand.setOnClickListener(this);
		btnStatusRightHand.setOnClickListener(this);
		
		skbSystemVolume.setOnSeekBarChangeListener(this);
		skbPlayerVolume.setOnSeekBarChangeListener(this);
		spAutonomousLife.setOnItemSelectedListener(this);
		
		return rootView;
	}
	
	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
		super.setUserVisibleHint(isVisibleToUser);
		
		if( isVisibleToUser ){
			RemoteNAO.sendCommand(NAOCommands.SYS_SET_REQUIRED_DATA, new String[]{
					"stiffnessData",
					"masterVolume",
					"playerVolume",
					"lifeState",
			});
		}
	}
	
	/**
	 * Updates the stiffness images for the joints.
	 */
	private void updateJointImages(){
		Map<NAOJoints, Float> jointStiffness = currentResponseData.stiffnessData.getJointStiffness();
		for( NAOJoints joint : jointStiffness.keySet() ){
			
			float stiffness = jointStiffness.get(joint);
					
			switch( joint ){
			case Body:
				setJointImage(imgJointBody, stiffness);
				break;
			case Head:
				setJointImage(imgJointHead, stiffness);
				break;
			case LArm:
				setJointImage(imgJointLArm, stiffness);
				break;
			case LHand:
				setJointImage(imgJointLHand, stiffness);
				break;
			case LLeg:
				setJointImage(imgJointLLeg, stiffness);
				break;
			case RArm:
				setJointImage(imgJointRArm, stiffness);
				break;
			case RHand:
				setJointImage(imgJointRHand, stiffness);
				break;
			case RLeg:
				setJointImage(imgJointRLeg, stiffness);
				break;
				
			default:
				break;
			}
			
		}
	}
	
	/**
	 * Sets image of a joint.
	 * @param aImage		{@link ImageView} of the image.
	 * @param aStiffness	{@link Float} stiffness of the joint.
	 */
	private void setJointImage(ImageView aImage, float aStiffness){
		if( aStiffness == 0.0f ){
			aImage.setImageResource(R.drawable.stiffness_green);
		} else if( aStiffness == 1.0 ){
			aImage.setImageResource(R.drawable.stiffness_red);
		} else {
			aImage.setImageResource(R.drawable.stiffness_orange);
		}
	}
	
	/**
	 * Shows a dialog to change NAOs name.
	 */
	private void showChangeNaoNameDialog(){
		final EditText input = new EditText( MainActivity.getInstance() );
		input.setText( txtStatusDeviceName.getText() );
		
		AlertDialog.Builder dialog = new AlertDialog.Builder( MainActivity.getInstance())
			.setTitle(R.string.status_change_name)
			.setMessage(R.string.status_change_name_msg)
			.setView(input)
			.setPositiveButton(R.string.btnOK, new DialogInterface.OnClickListener() {				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					String name = input.getText().toString();
					RemoteNAO.sendCommand(NAOCommands.SET_NAO_NAME,
							new String[]{name});
				}
			})
			.setNegativeButton(R.string.btnCancel, new DialogInterface.OnClickListener() {				
				@Override
				public void onClick(DialogInterface dialog, int which) {}
			});
		dialog.show();
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		if( !disableSending ){
			int progress = seekBar.getProgress();
			if( seekBar == skbSystemVolume ){
				
				RemoteNAO.sendCommand(
					NAOCommands.SET_SYSTEM_VOLUME,
					new String[]{ Integer.toString(progress) });
				
			} else if( seekBar == skbPlayerVolume ){
				
				RemoteNAO.sendCommand(
						NAOCommands.SET_PLAYER_VOLUME,
						new String[]{ Float.toString(progress/100.0f) });
				
			}
		}
	}

	@Override
	public void onClick(View v) {
		if( !disableSending ){
		
			switch( v.getId() ){
			case R.id.btnStatusChangeNaoName:
				showChangeNaoNameDialog();
				break;
			}
			
			if( currentResponseData != null && currentResponseData.stiffnessData != null ){
				Map<NAOJoints, Float> jointStiffness = currentResponseData.stiffnessData.getJointStiffness();
					
				switch( v.getId() ){
				case R.id.imgJointBody:
					RemoteNAO.sendCommand(
						NAOCommands.SET_JOINT_STIFFNESS,
						new String[]{
								NAOJoints.Body.name(),
								(jointStiffness.get(NAOJoints.Body) < 1.0f ? "1.0" : "0.0") });
					break;
				case R.id.imgJointHead:
					RemoteNAO.sendCommand(
							NAOCommands.SET_JOINT_STIFFNESS,
							new String[]{
									NAOJoints.Head.name(),
									(jointStiffness.get(NAOJoints.Head) < 1.0f ? "1.0" : "0.0") });
					break;
				case R.id.imgJointLArm:
					RemoteNAO.sendCommand(
							NAOCommands.SET_JOINT_STIFFNESS,
							new String[]{
									NAOJoints.LArm.name(),
									(jointStiffness.get(NAOJoints.LArm) < 1.0f ? "1.0" : "0.0") });
					break;
				case R.id.imgJointLHand:
					RemoteNAO.sendCommand(
							NAOCommands.SET_JOINT_STIFFNESS,
							new String[]{
									NAOJoints.LHand.name(),
									(jointStiffness.get(NAOJoints.LHand) < 1.0f ? "1.0" : "0.0") });
					break;
				case R.id.imgJointLLeg:
					RemoteNAO.sendCommand(
							NAOCommands.SET_JOINT_STIFFNESS,
							new String[]{
									NAOJoints.LLeg.name(),
									(jointStiffness.get(NAOJoints.LLeg) < 1.0f ? "1.0" : "0.0") });
					break;
				case R.id.imgJointRArm:
					RemoteNAO.sendCommand(
							NAOCommands.SET_JOINT_STIFFNESS,
							new String[]{
									NAOJoints.RArm.name(),
									(jointStiffness.get(NAOJoints.RArm) < 1.0f ? "1.0" : "0.0") });
					break;
				case R.id.imgJointRHand:
					RemoteNAO.sendCommand(
							NAOCommands.SET_JOINT_STIFFNESS,
							new String[]{
									NAOJoints.RHand.name(),
									(jointStiffness.get(NAOJoints.RHand) < 1.0f ? "1.0" : "0.0") });
					break;
				case R.id.imgJointRLeg:
					RemoteNAO.sendCommand(
							NAOCommands.SET_JOINT_STIFFNESS,
							new String[]{
									NAOJoints.RLeg.name(),
									(jointStiffness.get(NAOJoints.RLeg) < 1.0f ? "1.0" : "0.0") });
					break;
				
				case R.id.btnStatusLeftHand:
					RemoteNAO.sendCommand(
						NAOCommands.OPEN_HAND,
						new String[]{
								NAOJoints.LHand.name(),
								(currentResponseData.stiffnessData.isLeftHandOpen() ? "False" : "True") });
					break;
				
				case R.id.btnStatusRightHand:
					RemoteNAO.sendCommand(
							NAOCommands.OPEN_HAND,
							new String[]{
									NAOJoints.RHand.name(),
									(currentResponseData.stiffnessData.isRightHandOpen() ? "False" : "True") });
					break;
				}
			}
			
		}
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position,
			long id) {
		adapterAction(parent, position);
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		adapterAction(parent, parent.getSelectedItemPosition());
	}
	
	/**
	 * Sends lifestate position.
	 * @param parent	Parent {@link AdapterView}
	 * @param position	{@link Integer} position of selected item.
	 */
	private void adapterAction(AdapterView<?> parent, int position){
		if( !disableSending && !created ){
			NAOAutonomousLifeStates state = (NAOAutonomousLifeStates) parent.getItemAtPosition(position);
			RemoteNAO.sendCommand(
					NAOCommands.SET_LIFE_STATE,
					new String[]{ state.name() });
		}
		
		created = false;
	}

	@Override
	public void onRefresh() {
		if( !RemoteNAO.sendCommand(NAOCommands.SYS_GET_INFO) ){
			swipeStatus.setRefreshing(false);
		}
	}
	
	@Override
	public synchronized void onNetworkDataRecieved(DataResponsePackage data) {
		currentResponseData = data;
		
		MainActivity.getInstance().runOnUiThread(new Runnable() {			
			@Override
			public void run() {
				
				disableSending = true;
				
				txtStatusDeviceName.setText( currentResponseData.naoName );
				lblStatusSystemVolume.setText( Integer.toString(currentResponseData.audioData.masterVolume) + "%" );
				lblStatusPlayerVolume.setText( Integer.toString((int)(currentResponseData.audioData.playerVolume * 100.0f)) + "%" );
				if( currentResponseData.lifeState != null ){
					lblStatusAutonomousLife.setText( currentResponseData.lifeState.name() );
				}
				
				// set battery level
				int battery = currentResponseData.batteryLevel;
				if( battery >= 100 ){
					imgStatusBattery.setImageResource(R.drawable.bat_level_5);
				} else if( battery >= 80 ){
					imgStatusBattery.setImageResource(R.drawable.bat_level_4);
				} else if( battery >= 60 ){
					imgStatusBattery.setImageResource(R.drawable.bat_level_3);
				} else if( battery >= 40 ){
					imgStatusBattery.setImageResource(R.drawable.bat_level_2);
				}  else if( battery >= 20 ){
					imgStatusBattery.setImageResource(R.drawable.bat_level_1);
				} else {
					imgStatusBattery.setImageResource(R.drawable.bat_level_0);
				}
				
				updateJointImages();
				
				if( currentResponseData.stiffnessData.isLeftHandOpen() ){
					btnStatusLeftHand.setText(R.string.joints_control_lhand_close);
				} else {
					btnStatusLeftHand.setText(R.string.joints_control_lhand_open);
				}
				
				if( currentResponseData.stiffnessData.isRightHandOpen() ){
					btnStatusRightHand.setText(R.string.joints_control_rhand_close);
				} else {
					btnStatusRightHand.setText(R.string.joints_control_rhand_open);
				}
				
				swipeStatus.setRefreshing(false);
				disableSending = false;
			}
		});		
	}

}
