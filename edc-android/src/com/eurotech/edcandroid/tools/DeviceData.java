package com.eurotech.edcandroid.tools;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;

public class DeviceData {

	private final String defaultValue = "uninitialized";

	public String accountName, assetId, clientId, brokerUrl, userName, password, defaultReceive, defaultPublish, uid, modelName, modelId, partNumber;
	public Boolean connectivity, autopublish, receiveCommands, sendGps;
	public int sensorInterval;


	public DeviceData (Context ctx){

		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);

		connectivity = sp.getBoolean("Cloud connection", false);

		userName = sp.getString("pref_connectivity_connection_username", defaultValue);
		password = sp.getString("pref_connectivity_connection_password", defaultValue);


		brokerUrl = sp.getString("pref_connectivity_connection_broker", defaultValue);
		accountName = sp.getString("pref_connectivity_connection_account", defaultValue);
		assetId = sp.getString("pref_connectivity_connection_assetID", defaultValue);

		defaultReceive = sp.getString("pref_connectivity_connection_rec", defaultValue);
		defaultPublish = sp.getString("pref_connectivity_connection_pub", defaultValue);

		TelephonyManager tManager = (TelephonyManager)ctx.getSystemService(Context.TELEPHONY_SERVICE);
		uid = tManager.getDeviceId();

		modelName = android.os.Build.MODEL;
		modelId = android.os.Build.DEVICE;
		partNumber = android.os.Build.FINGERPRINT;

		autopublish = sp.getBoolean("Auto-publish", false);
		sensorInterval = (short) Integer.parseInt(sp.getString("pref_connectivity_autopublish_freq_time", "60"));

		receiveCommands = sp.getBoolean("Commands", false);
	}
}
