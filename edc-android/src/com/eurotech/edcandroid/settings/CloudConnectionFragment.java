package com.eurotech.edcandroid.settings;

import com.eurotech.edcandroid.R;

import android.preference.Preference;

public class CloudConnectionFragment extends EdcPreferenceFragment {

	public void setPreferenceValues() {
		this.resourceId = R.xml.preferences_connectivity_connection;
		this.resourceString = R.string.pref_connectivity_connection;
	}

	@Override
	public void onActivityCreatedInit() {
		
		if (this.getActivity() == null) return;
		
		Preference pref; String value;
		
		pref = (Preference) findPreference("pref_connectivity_connection_username");
		value = pref.getSharedPreferences().getString("pref_connectivity_connection_username", "void");
		if (!value.equals("void")) {
			pref.setSummary(value);
		}
		pref = (Preference) findPreference("pref_connectivity_connection_password");
		value = pref.getSharedPreferences().getString("pref_connectivity_connection_password", "void");
		if (!value.equals("void")) {
			value = new String(new char[value.length()]).replace('\0', '*');
			pref.setSummary(value);
		}
		pref = (Preference) findPreference("pref_connectivity_connection_broker");
		value = pref.getSharedPreferences().getString("pref_connectivity_connection_broker", "void");
		if (!value.equals("void")) {
			pref.setSummary(value);
		}
		pref = (Preference) findPreference("pref_connectivity_connection_account");
		value = pref.getSharedPreferences().getString("pref_connectivity_connection_account", "void");
		if (!value.equals("void")) {
			pref.setSummary(value);
		}
		pref = (Preference) findPreference("pref_connectivity_connection_assetID");
		value = pref.getSharedPreferences().getString("pref_connectivity_connection_assetID", "void");
		if (!value.equals("void")) {
			pref.setSummary(value);
		}
	}
}