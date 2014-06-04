package com.eurotech.edcandroid.gateway;

import android.content.Context;
import android.content.Intent;

public class LocalSendBroadcast {

	public static void sendBroadcast(Context context, Intent intent) {
		EdcConnectionService.broadcastEnabled = false;
		try {
			if (EdcConnectionService.edcClient.isConnected() && EdcConnectionService.networkIsConnected(context)) {
				EdcConnectionService.broadcastEnabled = true;
				context.sendBroadcast(intent);
			}
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
	}
}
