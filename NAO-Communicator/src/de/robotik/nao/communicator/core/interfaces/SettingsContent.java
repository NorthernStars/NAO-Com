package de.robotik.nao.communicator.core.interfaces;

import java.util.Map;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Interface to get settings content
 * @author hannes
 *
 */
public interface SettingsContent {

	
	/**
	 * Functions to generate view and add listener.
	 */
	public void generateView(ViewGroup root);
	
	/**
	 * Function to get settings view.
	 * @return	{@link View} of settings.
	 */
	public View getView();
	
	/**
	 * Function to update settings {@link Map}.
	 */
	public void updateSettings();
	
	/**
	 * Function to update content of {@link TextView}.
	 * @param txtText	{@link TextView} to update.
	 */
	public void updateText(TextView txtText);
	
	/**
	 * Function to get Settings.
	 * @return	{@link Map} of {@link String} settings.
	 */
	public Map<String, String> getSettings();
	
	/**
	 * @return JSON {@link String} of settings
	 */
	public String toJson();
	
}
