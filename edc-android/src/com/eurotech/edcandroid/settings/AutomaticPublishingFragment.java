package com.eurotech.edcandroid.settings;

import com.eurotech.edcandroid.R;
import com.eurotech.edcandroid.tools.SensorSelection;

import android.content.Context;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.LocationManager;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;

public class AutomaticPublishingFragment extends EdcPreferenceFragment {

	private SensorManager mSensorManager;
	private LocationManager mLocManager;

	public void setPreferenceValues() {

		this.resourceId = R.xml.preferences_connectivity_autopublish;
		this.resourceString = R.string.pref_connectivity_autopublish;
	}

	@Override
	public void onActivityCreatedInit() {

		if (this.getActivity() == null) return;

		Preference pref; String value;
		pref = (Preference) findPreference("pref_connectivity_autopublish_freq_time");
		value = pref.getSharedPreferences().getString("pref_connectivity_autopublish_freq_time", "void");
		if (!value.equals("void")) {
			value += " sec.";
			pref.setSummary(value);
		}

		PreferenceCategory notificationsCategory = (PreferenceCategory) findPreference("pref_connectivity_autopublish_content");

		mSensorManager = (SensorManager) this.getActivity().getSystemService(Context.SENSOR_SERVICE);
		mLocManager =  (LocationManager) this.getActivity().getSystemService(Context.LOCATION_SERVICE);

		for (int i = 0; i < SensorSelection.SENSORS_TYPES.length; i++) {
			if (SensorSelection.SENSORS_TYPES.equals("Location")) {
				Criteria criteria = new Criteria();
				criteria.setAccuracy(Criteria.ACCURACY_FINE);
				criteria.setCostAllowed(false);
				String providerName = mLocManager.getBestProvider(criteria, true);
				if (providerName != null) {
					CheckBoxPreference togglePref = new CheckBoxPreference(this.getActivity());
					togglePref.setKey("sensor_"+SensorSelection.SENSORS_CODES[i]);
					togglePref.setTitle(SensorSelection.SENSORS_NAMES[i]);
					togglePref.setSummary("Metric: " + SensorSelection.SENSORS_NAMES[i].replaceAll("\\s+", "_") + "<_index>");
					notificationsCategory.addPreference(togglePref);
				}
			} else if (mSensorManager.getDefaultSensor(SensorSelection.SENSORS_TYPES[i]) != null){
				CheckBoxPreference togglePref = new CheckBoxPreference(this.getActivity());
				togglePref.setKey("sensor_"+SensorSelection.SENSORS_CODES[i]);
				togglePref.setTitle(SensorSelection.SENSORS_NAMES[i]);
				togglePref.setSummary("Metric: " + SensorSelection.SENSORS_NAMES[i].replaceAll("\\s+", "_") + "<_index>");
				notificationsCategory.addPreference(togglePref);
			}
		}
	}
}
