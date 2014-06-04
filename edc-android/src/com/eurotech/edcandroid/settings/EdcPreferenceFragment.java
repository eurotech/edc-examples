package com.eurotech.edcandroid.settings;

import com.eurotech.edcandroid.EDCAndroid;

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

		EDCAndroid.localLog("BEGIN");

		setPreferenceValues();

		PreferenceManager.getDefaultSharedPreferences(getActivity()).registerOnSharedPreferenceChangeListener(this);

		addPreferencesFromResource(resourceId);

		Activity activity = getActivity();
		ActionBar actionbar = activity.getActionBar();
		Switch actionBarSwitch = new Switch(activity);

		actionbar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM, ActionBar.DISPLAY_SHOW_CUSTOM);
		actionbar.setCustomView(actionBarSwitch, new ActionBar.LayoutParams(ActionBar.LayoutParams.WRAP_CONTENT,
				ActionBar.LayoutParams.WRAP_CONTENT,
				Gravity.CENTER_VERTICAL | Gravity.RIGHT));

		if (getActivity() != null) actionbar.setTitle(getString(resourceString));

		mEnabler = new SwitchPreferenceEnabler(getActivity(), actionBarSwitch, getString(resourceString));

		onActivityCreatedInit();
		updateSettings();

		EDCAndroid.localLog("END");
	}

	public void onResume() {
		super.onResume();
		EDCAndroid.localLog("BEGIN");
		mEnabler.resume();
		updateSettings();
		EDCAndroid.localLog("END");
	}

	public void onPause() {
		super.onPause();
		EDCAndroid.localLog("BEGIN");
		mEnabler.pause();
		EDCAndroid.localLog("END");
	}


	protected void updateSettings() {
		boolean available = mEnabler.isSwitchOn();

		EDCAndroid.localLog("BEGIN");

		int count = getPreferenceScreen().getPreferenceCount();
		for (int i = 0; i < count; ++i) {
			Preference pref = getPreferenceScreen().getPreference(i);
			pref.setEnabled(available);
		}

		EDCAndroid.localLog("END");
	}

	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

		EDCAndroid.localLog("BEGIN");

		Preference pref; String value;

		if (key.equals("pref_connectivity_connection_username")         ||
				key.equals("pref_connectivity_connection_password")     ||
				key.equals("pref_connectivity_connection_broker")       ||
				key.equals("pref_connectivity_connection_account")      ||
				key.equals("pref_connectivity_connection_assetID")) {
			EDCAndroid.somethingChanged = true;
			pref = (Preference) findPreference(key);
			value = sharedPreferences.getString(key, "void");
			if (pref != null) {
				if (key.equals("pref_connectivity_connection_password")) {
					value = new String(new char[value.length()]).replace('\0', '*');
				}
				pref.setSummary(value);
			}
		}
		if (key.equals("pref_connectivity_autopublish_freq_time")) {
			pref = (Preference) findPreference(key);
			if (pref != null) {
				value = sharedPreferences.getString(key, "void");
				if (!value.equals("void")) {
					value += " sec.";
					pref.setSummary(value);
				}
			}
		}

		EDCAndroid.localLog("END");
	}
}