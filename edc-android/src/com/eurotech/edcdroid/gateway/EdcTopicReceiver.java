package com.eurotech.edcdroid.gateway;

import java.util.StringTokenizer;

import com.eurotech.edcdroid.Demo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;


public class EdcTopicReceiver extends BroadcastReceiver {

	public EdcTopicReceiver() {
		Demo.localLog("BEGIN", Demo.ENABLE);
		Demo.localLog("END", Demo.ENABLE);
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		
		Demo.localLog("BEGIN", Demo.ENABLE);

		Bundle extra = intent.getExtras();
		String topicList = extra.getString(EdcConnectionIntents.SUBSCRIPTION_LIST);
		Demo.semanticTopicList.clear();
		StringTokenizer topicTokenizer = new StringTokenizer(topicList, ",");
		while (topicTokenizer.hasMoreTokens()) {
			String topic = topicTokenizer.nextToken();
			Demo.semanticTopicList.add(topic);
		}
		intent = new Intent();
		intent.setAction(EdcConnectionIntents.TOPIC_INTENT_LIST_TERMINATION);
		LocalSendBroadcast.sendBroadcast(context, intent);
		Demo.localLog("END", Demo.ENABLE);
	}
}
