package com.eurotech.edcdroid.settings;

import com.eurotech.edcdroid.R;

public class CloudConnectionFragment extends EdcPreferenceFragment {

	public void setPreferenceValues() {
	
		this.resourceId = R.xml.preferences_connectivity_connection;
		this.resourceString = R.string.pref_connectivity_connection;
	}

	@Override
	public void onActivityCreatedInit() {
	}
}
