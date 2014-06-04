package com.eurotech.edcandroid.gateway;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.eurotech.edcandroid.EDCAndroid;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class EdcMessageReceiver extends BroadcastReceiver {

	public EdcMessageReceiver() {
		EDCAndroid.localLog("BEGIN", EDCAndroid.ENABLE);
		EDCAndroid.localLog("END", EDCAndroid.ENABLE);
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		EDCAndroid.localLog("BEGIN", EDCAndroid.ENABLE);
		Map<String,Object> payload = new HashMap<String,Object>();
		Bundle extras = intent.getExtras();
		Iterator<String> keys = extras.keySet().iterator();
		while (keys.hasNext()) {
			String key = keys.next();
			Object value = extras.get(key);
			payload.put(key, value);
		}
		EdcReceiveActivity.NewMessage(payload);
		EDCAndroid.localLog("END", EDCAndroid.ENABLE);
	}
}
