package de.robotik.nao.communicator.network.data;

/**
 * Class storing information about audio data
 * @author Hannes Eilers
 *
 */
public class AudioData {
	
	public int masterVolume;
	public float playerVolume;
	public float speechVolume;
	public String speechVoice;
	public String speechLanguage;
	public String[] speechLanguagesList;
	public String[] speechVoicesList;
	public float speechPitchShift;
	public float speechDoubleVoice;
	public float speechDoubleVoiceLevel;
	public float speechDoubleVoiceTimeShift;

	public String toString(){
		String ret = "\tmasterVolume: " + masterVolume
				+ "\n\tplayerVolume: " + playerVolume
				+ "\n\tspeechVolume: " + speechVolume
				+ "\n\tspeechVoice: " + speechVoice
				+ "\n\tspeechLanguage: " + speechLanguage
				+ "\n\tspeechLanguages: ";
		
		for( String lang : speechLanguagesList ){
			ret += "\n\t\t" + lang;
		}
		
		ret += "\n\tspeechVoices: ";
		for( String voice : speechVoicesList ){
			ret += "\n\t\t" + voice;
		}
		
		ret += "\n\tspeechPitchShift: " + speechPitchShift
				+ "\n\tspeechDoubleVoice: " + speechDoubleVoice
				+ "\n\tspeechDoubleVoiceLevel: " + speechDoubleVoiceLevel
				+ "\n\tspeechDoubleVoiceTimeShift: " + speechDoubleVoiceTimeShift;
		
		return ret;
	}
	
}
