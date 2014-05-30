package com.eurotech.edcdroid.settings;

import com.eurotech.edcdroid.Demo;

import android.app.ActionBar;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.widget.Switch;

public abstract class EdcPreferenceFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener {

	protected SwitchPreferenceEnabler mEnabler;
	protected int resourceId; 
	protected int resourceString;

	public abstract void setPreferenceValues();
	public abstract void onActivityCreatedInit();

	public void onActivityCreated(Bundle savedInstanceState) {

		super.onActivityCreated(savedInstanceState);

		setPreferenceValues();

		PreferenceManager.getDefaultSharedPreferences(getActivity()).registerOnSharedPreferenceChangeListener(this);

		addPreferencesFromResource(resourceId);

		Activity activity = getActivity();
		ActionBar actionbar = activity.getActionBar();
		Switch actionBarSwitch = new Switch(activity);

		actionbar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM, ActionBar.DISPLAY_SHOW_CUSTOM);
		actionbar.setCustomView(actionBarSwitch, new ActionBar.LayoutParams(ActionBar.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.WRAP_CONTENT, Gravity.CENTER_VERTICAL | Gravity.RIGHT));

		if (getActivity() != null) actionbar.setTitle(getString(resourceString));

		mEnabler = new SwitchPreferenceEnabler(getActivity(), actionBarSwitch, getString(resourceString));

		onActivityCreatedInit();
		updateSettings();
	}

	public void onResume() {
		super.onResume();
		mEnabler.resume();
		updateSettings();
	}

	public void onPause() {
		super.onPause();
		mEnabler.pause();
	}

	protected void updateSettings() {
		boolean available = mEnabler.isSwitchOn();

		int count = getPreferenceScreen().getPreferenceCount();
		for (int i = 0; i < count; ++i) {
			Preference pref = getPreferenceScreen().getPreference(i);
			pref.setEnabled(available);
		}
	}

	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		
		if (key.equals("pref_connectivity_connection_username")     ||
				key.equals("pref_connectivity_connection_password") ||
				key.equals("pref_connectivity_connection_broker")   ||
				key.equals("pref_connectivity_connection_account")  ||
				key.equals("pref_connectivity_connection_assetID")) {
			Demo.somethingChanged = true;
		}

		if (getActivity() != null && key.equals(getString(resourceString)))
			updateSettings();
	}
}