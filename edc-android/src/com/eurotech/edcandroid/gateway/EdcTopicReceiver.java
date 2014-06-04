package com.eurotech.edcandroid.gateway;

import java.util.StringTokenizer;

import com.eurotech.edcandroid.EDCAndroid;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;


public class EdcTopicReceiver extends BroadcastReceiver {

	public EdcTopicReceiver() {
		EDCAndroid.localLog("BEGIN", EDCAndroid.ENABLE);
		EDCAndroid.localLog("END", EDCAndroid.ENABLE);
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		
		EDCAndroid.localLog("BEGIN", EDCAndroid.ENABLE);

		Bundle extra = intent.getExtras();
		String topicList = extra.getString(EdcConnectionIntents.SUBSCRIPTION_LIST);
		EDCAndroid.semanticTopicList.clear();
		StringTokenizer topicTokenizer = new StringTokenizer(topicList, ",");
		while (topicTokenizer.hasMoreTokens()) {
			String topic = topicTokenizer.nextToken();
			EDCAndroid.semanticTopicList.add(topic);
		}
		intent = new Intent();
		intent.setAction(EdcConnectionIntents.TOPIC_INTENT_LIST_TERMINATION);
		LocalSendBroadcast.sendBroadcast(context, intent);
		EDCAndroid.localLog("END", EDCAndroid.ENABLE);
	}
}
