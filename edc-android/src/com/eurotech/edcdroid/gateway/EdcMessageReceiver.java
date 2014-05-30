package com.eurotech.edcdroid.gateway;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.eurotech.edcdroid.Demo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class EdcMessageReceiver extends BroadcastReceiver {

	public EdcMessageReceiver() {
		Demo.localLog("BEGIN", Demo.ENABLE);
		Demo.localLog("END", Demo.ENABLE);
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		Demo.localLog("BEGIN", Demo.ENABLE);
		Map<String,Object> payload = new HashMap<String,Object>();
		Bundle extras = intent.getExtras();
		Iterator<String> keys = extras.keySet().iterator();
		while (keys.hasNext()) {
			String key = keys.next();
			Object value = extras.get(key);
			payload.put(key, value);
		}
		EdcReceiveActivity.NewMessage(payload);
		Demo.localLog("END", Demo.ENABLE);
	}
}
