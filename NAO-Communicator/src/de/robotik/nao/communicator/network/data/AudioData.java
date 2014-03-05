package de.robotik.nao.communicator.network.data;

/**
 * Class storing information about audio data
 * @author Hannes Eilers
 *
 */
public class AudioData {
	
	public int overallVolume;
	public float audioVolume;
	public float speechVolume;
	public String speechVoice;
	public String speechLanguage;
	public String[] speechLanguagesList;
	public float speechPitchShift;
	public float speechDoubleVoice;
	public float speechDoubleVoiceLevel;
	public float speechDoubleVoiceTimeShift;

}
