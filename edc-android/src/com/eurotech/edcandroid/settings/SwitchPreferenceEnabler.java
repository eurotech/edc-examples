package com.eurotech.edcandroid.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;

public class SwitchPreferenceEnabler implements OnCheckedChangeListener {

	protected final Context mContext;
	private Switch mSwitch;
	private String key;
	
	public SwitchPreferenceEnabler(Context context, Switch swtch, String key) {
		mContext = context;
		this.key = key;
		setSwitch(swtch);
	}

	public void setSwitch(Switch swtch) {
		if (mSwitch == swtch)
			return;
		if (mSwitch != null)
			mSwitch.setOnCheckedChangeListener(null);
		mSwitch = swtch;
		mSwitch.setOnCheckedChangeListener(this);
		mSwitch.setChecked(isSwitchOn());
	}

	public void onCheckedChanged(CompoundButton view, boolean isChecked) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
		Editor editor = prefs.edit();
		editor.putBoolean(key, isChecked);
		editor.commit();
	}

	public boolean isSwitchOn() {
		SharedPreferences prefs;
		prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
		return prefs.getBoolean(key, true);
	}

	public void resume() {
		mSwitch.setOnCheckedChangeListener(this);
		mSwitch.setChecked(isSwitchOn());
	}

	public void pause() {
		mSwitch.setOnCheckedChangeListener(null);
	}
}