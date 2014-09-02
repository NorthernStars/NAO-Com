package de.robotik.nao.communicator.core.sections;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import de.northernstars.naocom.R;
import de.robotik.nao.communicator.core.MainActivity;
import de.robotik.nao.communicator.core.RemoteNAO;
import de.robotik.nao.communicator.network.data.NAOCommands;
import de.robotik.nao.communicator.network.data.response.DataResponsePackage;
import de.robotik.nao.communicator.network.interfaces.NetworkDataRecievedListener;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.LinearLayout;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

public class SectionSpeech extends Section implements
	NetworkDataRecievedListener,
	OnRefreshListener,
	OnClickListener,
	OnItemClickListener,
	OnItemSelectedListener,
	OnFocusChangeListener,
	OnSeekBarChangeListener,
	TextWatcher{
	
	private static final int HISTORY_LENGTH = 10;
	private static final String SAVED_TEXT_FILE = "savedTexts";
	
	private SwipeRefreshLayout swipeSpeech;
	private EditText txtSpeechInputText;
	private Spinner lstSavedText;
	private ImageButton btnSpeechRemoveSavedText;
	private CheckBox chkSpeechAutomatic;
	private Button btnSayText;
	private Button btnSaveText;
	private TextView lblSpeechRate;
	private SeekBar skbSpeechRate;
	private TextView lblSpeechModulation;
	private SeekBar skbSpeechModulation;
	private TextView lblSpeechVolume;
	private SeekBar skbSpeechVolume;
	private LinearLayout lstSpeechHistory;
	
	private List<String> mHistory = new ArrayList<String>();
	
	private List<String> mSavedText = new ArrayList<String>();
	private ArrayAdapter<String> mSavedTextAdapter;
	
	private DataResponsePackage lastResponsePackage;
	
	public SectionSpeech() {}
	
	public SectionSpeech(String title){
		super(title);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		// connect ui widgets
		rootView = inflater.inflate(R.layout.page_speech, container, false);
		
		// get widgets
		swipeSpeech = (SwipeRefreshLayout) findViewById(R.id.swipeSpeech);
		txtSpeechInputText = (EditText) findViewById(R.id.txtSpeechInputText);
		lstSavedText = (Spinner) findViewById(R.id.lstSavedText);
		btnSpeechRemoveSavedText = (ImageButton) findViewById(R.id.btnSpeechRemoveSavedText);
		chkSpeechAutomatic = (CheckBox) findViewById(R.id.chkSpeechAutomatic);
		btnSayText = (Button) findViewById(R.id.btnSayText);
		btnSaveText = (Button) findViewById(R.id.btnSaveText);
		lblSpeechRate = (TextView) findViewById(R.id.lblSpeechRate);
		skbSpeechRate = (SeekBar) findViewById(R.id.skbSpeechRate);
		lblSpeechModulation = (TextView) findViewById(R.id.lblSpeechModulation);
		skbSpeechModulation = (SeekBar) findViewById(R.id.skbSpeechModulation);
		lblSpeechVolume = (TextView) findViewById(R.id.lblSpeechVolume);
		skbSpeechVolume = (SeekBar) findViewById(R.id.skbSpeechVolume);
		lstSpeechHistory = (LinearLayout) findViewById(R.id.lstSpeechHistory);
		
		// set seekbar progresses
		lblSpeechModulation.setText( Integer.toString(skbSpeechModulation.getProgress()) + "%" );
		lblSpeechRate.setText( Integer.toString(skbSpeechRate.getProgress()) + "%" );
		
		// set listener
		txtSpeechInputText.setOnFocusChangeListener(this);
		txtSpeechInputText.addTextChangedListener(this);
		
		skbSpeechModulation.setOnSeekBarChangeListener(this);
		skbSpeechRate.setOnSeekBarChangeListener(this);
		skbSpeechVolume.setOnSeekBarChangeListener(this);
		
		// onclick listener
		btnSayText.setOnClickListener(this);
		btnSaveText.setOnClickListener(this);
		btnSpeechRemoveSavedText.setOnClickListener(this);
		
		// saved text adpater
		mSavedTextAdapter = new ArrayAdapter<String>(getActivity(),
				R.layout.menu_list_item,
				mSavedText);
		lstSavedText.setAdapter(mSavedTextAdapter);
		lstSavedText.setOnItemSelectedListener(this);
		loadSavedTextsFromFile();
		
		// set swipe layout
		swipeSpeech.setOnRefreshListener(this);
		swipeSpeech.setColorSchemeResources(
				R.color.darkerblue,
				R.color.darkblue,
				R.color.blue,
				R.color.lighterblue);
		
		// Netwoprk data recieved listener
		MainActivity.getInstance().addNetworkDataRecievedListener(this);
		
		return rootView;
	}
	
	/**
	 * Load saved texts from file
	 */
	private void loadSavedTextsFromFile(){		
		try {
			
			mSavedText.clear();
			BufferedReader in = new BufferedReader(
					new InputStreamReader( getActivity().openFileInput(SAVED_TEXT_FILE) ) );
			mSavedText.add("");
			
			String line;
			while( (line = in.readLine()) != null ){
				mSavedText.add(line);
			}
			
			mSavedTextAdapter.notifyDataSetChanged();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Remove entry from saved texts.
	 * @param aPosition	{@link Integer} position of text in list.
	 */
	private void removeFromSavedText(int aPosition){
		if( aPosition < mSavedText.size() && aPosition > 0 ){
			mSavedText.remove(aPosition);
			saveText();
			mSavedTextAdapter.notifyDataSetChanged();
		}
	}
	
	private void addSavedText( String aText){
		if( !isInSavedText(aText) ){
			// add text
			mSavedText.add(aText);
			saveText();
			mSavedTextAdapter.notifyDataSetChanged();
		}
	}
	
	/**
	 * Save text to file.
	 * @param aText	{@link String} text.
	 */
	private void saveText(){		
		try {

			// save to file
			FileOutputStream out = getActivity().openFileOutput(
					SAVED_TEXT_FILE, Context.MODE_PRIVATE);			
			for( String s : mSavedText ){
				if( s.length() > 0 ){
					s += "\n";
					out.write( s.getBytes() );
				}
			}
			out.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}
	
	/**
	 * Checks if a text is already in saved texts.
	 * @param aText	{@link String} text.
	 * @return		{@code true} if {@code aText} is in saved texts, {@code false} otherwise.
	 */
	private boolean isInSavedText(String aText){
		for( String s : mSavedText ){
			if( s.equals(aText) ){
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Adds a new text to history.
	 * @param aText	{@link String} text.
	 */
	private void addToHistory(String aText){
		aText = aText.trim();
		
		if( !isInHistory(aText) && aText.length() > 0 ){
			
			TextView txt = new TextView(getActivity());
			txt.setText( aText );
			mHistory.add( aText );
			lstSpeechHistory.addView( txt );
			
			while( mHistory.size() > HISTORY_LENGTH ){
				lstSpeechHistory.removeViewAt(0);
				mHistory.remove(0);
			}
		}
	}
	
	/**
	 * Checks if a text is already in history.
	 * @param aText	{@link String} text.
	 * @return		{@code true} if {@code aText} is in history, {@code false} otherwise.
	 */
	private boolean isInHistory(String aText){
		for( String s : mHistory ){
			if( s.equals(aText) ){
				return true;
			}
		}
		
		return false;
	}

	@Override
	public void onRefresh() {
		if( !RemoteNAO.sendCommand(NAOCommands.SYS_GET_INFO) ){
			swipeSpeech.setRefreshing(false);
		}
	}

	@Override
	public void onClick(View v) {
		if( v == btnSayText ){
			
			// say text and add to history
			String vText = txtSpeechInputText.getText().toString().trim();
			int speed = skbSpeechRate.getProgress();
			int shape = skbSpeechModulation.getProgress();
			
			if( RemoteNAO.sendCommand( NAOCommands.SAY,
					new String[]{
						vText,
						Integer.toString(speed),
						Integer.toString(shape)} ) ){			
				addToHistory(vText);
			}
			
		} else if( v == btnSaveText ){
			
			// save text
			String vText = txtSpeechInputText.getText().toString().trim();
			addSavedText( vText );
			
		} else if( v == btnSpeechRemoveSavedText ){
			
			// remove saved text item
			int position = lstSavedText.getSelectedItemPosition();
			removeFromSavedText(position);
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		txtSpeechInputText.setText( mHistory.get(position) );
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position,
			long id) {
		txtSpeechInputText.setText( mSavedText.get(position) );
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {}

	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		if( v == txtSpeechInputText && hasFocus ){
			txtSpeechInputText.setText("");
		}
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {}

	@Override
	public void afterTextChanged(Editable s) {
		if( chkSpeechAutomatic.isChecked() && s.length() > 0 ){
			char lastChar = s.charAt( s.length()-1 );
			
			// check if end of sentence > click say button
			if( lastChar == '.' || lastChar == '!' || lastChar == '?'
					|| ( lastChar == ' '
						&& s.length() > 1
						&& s.charAt(s.length()-2) == ' ' )){
				onClick(btnSayText);
			}
			
		}
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		progress = ( (int)Math.round(progress/10.0) ) * 10;
		seekBar.setProgress(progress);
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		int progress = seekBar.getProgress();
		
		if( seekBar == skbSpeechModulation ){
			lblSpeechModulation.setText( Integer.toString(progress) + "%" );			
		} else if( seekBar == skbSpeechRate ){
			lblSpeechRate.setText( Integer.toString(progress) + "%" );
		} else if( seekBar == skbSpeechVolume ){			
			lblSpeechVolume.setText( Integer.toString(progress) + "%" );
			RemoteNAO.sendCommand(
					NAOCommands.SET_SPEECH_VOLUME,
					new String[]{ Float.toString( (float)(progress)/100.0f ) } );
		}
	}
	
	@Override
	public void onNetworkDataRecieved(DataResponsePackage data) {
		if( data.audioData != null ){
			
			lastResponsePackage = data;
			
			getActivity().runOnUiThread( new Runnable(){
				@Override
				public void run() {
					int vVolume = (int)(lastResponsePackage.audioData.speechVolume * 100.0f);
					lblSpeechVolume.setText( Integer.toString(vVolume) + "%" );
					skbSpeechVolume.setProgress( vVolume );
				}
			});
			
		}
	}

}
