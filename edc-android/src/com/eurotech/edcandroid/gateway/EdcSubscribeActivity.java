package com.eurotech.edcandroid.gateway;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.eurotech.edcandroid.EDCAndroid;
import com.eurotech.edcandroid.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;


public class EdcSubscribeActivity extends Activity implements OnClickListener {

	private final String All         = "Select/Deselect All";
	private final String None        = "No topics subscribed.";
	private final String NotReceived = "Topic list not received.";
	private ListView listView;
	private ArrayList<String>     topics;
	private ArrayAdapter<String> adapter;
	private Map<String,Boolean> cbStatus;
	private TopicListReceiver topicListReceiver;
	private Intent topicListIntent;
	private Intent unregisterIntent;
	private Intent registerIntent;
	private EdcTopicReceiver topicReceiver;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		
		EDCAndroid.localLog("BEGIN", EDCAndroid.ENABLE);
		
		topicReceiver = new EdcTopicReceiver();
		IntentFilter topicFilter = new IntentFilter(EdcConnectionIntents.TOPIC_LIST_REPLY);
		registerReceiver(topicReceiver, topicFilter);

		topicListReceiver = new TopicListReceiver();
		try { unregisterReceiver(topicListReceiver);
		} catch(IllegalArgumentException e) { }
		IntentFilter filter = new IntentFilter(EdcConnectionIntents.TOPIC_INTENT_LIST_TERMINATION);
		registerReceiver(topicListReceiver, filter);

		setContentView(R.layout.subscribe);
		
		topicListIntent = new Intent();
		topicListIntent.setAction(EdcConnectionIntents.TOPIC_LIST_INTENT);
		LocalSendBroadcast.sendBroadcast(getBaseContext(), topicListIntent);
		
		registerIntent = new Intent();
		registerIntent.setAction(EdcConnectionIntents.REGISTER_MESSAGE_RECEIVER_INTENT);
		
		unregisterIntent = new Intent();
		unregisterIntent.setAction(EdcConnectionIntents.UNREGISTER_MESSAGE_RECEIVER_INTENT);
		
		topics = new ArrayList<String>();
		if (!EdcConnectionService.broadcastEnabled) {
			Intent intent = new Intent();
			intent.setAction(EdcConnectionIntents.TOPIC_INTENT_LIST_TERMINATION);
			LocalSendBroadcast.sendBroadcast(getBaseContext(), intent);
		}
		cbStatus = new HashMap<String,Boolean>();
		setContentView(R.layout.subscribe);
		TextView tv = new TextView(getApplicationContext());
		tv.setTextSize(20);
		tv.setText("Select topics to unsubscribe");
		listView = (ListView) findViewById(R.id.subscribelistview);
		listView.addHeaderView(tv, null, false);

		listView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> ad, View view, int position, long id) {
				ListView lv = EdcSubscribeActivity.this.listView;
				String key = (String) lv.getItemAtPosition(position);
				cbStatus.put(key, !cbStatus.get(key));
				if (key.equals(All)) {
					for (int i = 1; i < cbStatus.size(); i++) {
						String k = (String) lv.getItemAtPosition(i);
						cbStatus.put(k, cbStatus.get(All));
						lv.setItemChecked(i, cbStatus.get(All));
					}
				}
			}
		});
		Button subscribeButton = (Button) findViewById(R.id.subscribebutton);
		subscribeButton.setOnClickListener(this);
		Button unsubscribeButton = (Button) findViewById(R.id.unsubscribebutton);
		unsubscribeButton.setOnClickListener(this);
		EDCAndroid.localLog("END", EDCAndroid.ENABLE);
	}
	
	@Override
	public void onStart() {
		super.onStart();
		EDCAndroid.localLog("BEGIN", EDCAndroid.ENABLE);
		adapter = new ArrayAdapter<String>(getBaseContext(), android.R.layout.simple_list_item_multiple_choice, topics);
		EDCAndroid.localLog("END", EDCAndroid.ENABLE);
	}

	@Override
	public void onClick(View v) {
		EDCAndroid.localLog("BEGIN", EDCAndroid.ENABLE);
		final Context context = getBaseContext();
		switch (v.getId()) {
		case R.id.subscribebutton:
			LinearLayout layout = new LinearLayout(EdcSubscribeActivity.this);
			layout.setOrientation(LinearLayout.VERTICAL);
			final EditText inputAssetID = new EditText(EdcSubscribeActivity.this);
			layout.addView(inputAssetID);
			inputAssetID.setHint("<Comma-separater list of assets>");
			final EditText inputTopic = new EditText(EdcSubscribeActivity.this);
			layout.addView(inputTopic);
			inputTopic.setHint("Comma-separated list of topics");
			AlertDialog.Builder alert = new AlertDialog.Builder(EdcSubscribeActivity.this);
			alert.setView(layout);

			alert.setPositiveButton("Subscribe", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					String semTopics = inputTopic.getText().toString().trim().replaceAll("\\s","");
					String subAssets = inputAssetID.getText().toString().trim().replaceAll("\\s","");
					if (subAssets.isEmpty()) subAssets = EdcConnectionService.ASSET_ID;
					Intent intent = new Intent();
					intent.setAction(EdcConnectionIntents.SUBSCRIBE_INTENT);
					intent.putExtra(EdcConnectionIntents.SUBSCRIBED_ASSETS, subAssets);
					intent.putExtra(EdcConnectionIntents.SUBSCRIBED_TOPICS, semTopics);
					LocalSendBroadcast.sendBroadcast(context, intent);
					LocalSendBroadcast.sendBroadcast(context, topicListIntent);
					dialog.dismiss();
				}});
			alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					dialog.cancel();
				}});
			alert.show();
			break;
		case R.id.unsubscribebutton:
			Set<String> keys = cbStatus.keySet();
			if (keys.size() > 1) {
				String selectedItems = "";
				for (String key : keys) {
					if (cbStatus.get(key) && !key.equals(All))
						selectedItems += key + ",";
				}
				Intent intent = new Intent();
				intent.setAction(EdcConnectionIntents.TOPIC_UNSUBSCRIBE_INTENT);
				intent.putExtra(EdcConnectionIntents.UNSUBSCRIBED_TOPICS, selectedItems);
				LocalSendBroadcast.sendBroadcast(context, intent);
				LocalSendBroadcast.sendBroadcast(context, topicListIntent);
			}
			break;
		}
		EDCAndroid.localLog("END", EDCAndroid.ENABLE);
	}

	public void updateTopicView() {
		EDCAndroid.localLog("BEGIN", EDCAndroid.ENABLE);
		Context context = getBaseContext();
		topics.clear();
		cbStatus.clear();
		for (String topic : EDCAndroid.semanticTopicList) {
			topics.add(topic);
		}
		Collections.sort(topics);
		for (String topic : topics)
			cbStatus.put(topic, false);
		if (!EdcConnectionService.broadcastEnabled) {
			topics.add(NotReceived);
			cbStatus.put(NotReceived, false);
			listView.setChoiceMode(ListView.CHOICE_MODE_NONE);
		} else if (topics.isEmpty()) {
				topics.add(None);
				cbStatus.put(None, false);
				listView.setChoiceMode(ListView.CHOICE_MODE_NONE);
		} else {
			topics.add(All);
			cbStatus.put(All, false);
			listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		}
		if (adapter.isEnabled(0)) {
			adapter = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_multiple_choice, topics);
			listView.setAdapter(adapter);
		} else {
			adapter.notifyDataSetChanged();
		}
		for (Entry<String, Boolean> entry: cbStatus.entrySet())
			entry.setValue(false);
		for (int i = 0; i < listView.getCount(); i++)
			listView.setItemChecked(i, false);
		LocalSendBroadcast.sendBroadcast(context, unregisterIntent);
		LocalSendBroadcast.sendBroadcast(context, registerIntent);
		EDCAndroid.localLog("END", EDCAndroid.ENABLE);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		EDCAndroid.localLog("BEGIN", EDCAndroid.ENABLE);
		unregisterReceiver(topicListReceiver);
		unregisterReceiver(topicReceiver);
		EDCAndroid.localLog("END", EDCAndroid.ENABLE);
	}

	public class TopicListReceiver extends BroadcastReceiver {

		public TopicListReceiver() {
			EDCAndroid.localLog("BEGIN", EDCAndroid.ENABLE);
			EDCAndroid.localLog("END", EDCAndroid.ENABLE);
		}

		@Override
		public void onReceive(Context context, Intent intent) {
			EDCAndroid.localLog("BEGIN", EDCAndroid.ENABLE);
			updateTopicView();
			EDCAndroid.localLog("END", EDCAndroid.ENABLE);
		}
	}
}
