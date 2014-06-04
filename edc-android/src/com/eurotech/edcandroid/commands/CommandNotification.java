package com.eurotech.edcandroid.commands;

import android.app.Notification;
import android.content.Context;

import com.eurotech.edcandroid.EDCAndroid;
import com.eurotech.edcandroid.R;
import com.eurotech.edcandroid.tools.EdcNotification;

public class CommandNotification implements Command {

	private static final String NOTIFICATION_TITLE           = "EDC notification";


	String name = "Notification";
	String topic = "/command/notification";

	public CommandNotification() {
	}

	public void doNotificate(Context context, String text) {
		new EdcNotification(context, R.drawable.bluelogo, NOTIFICATION_TITLE, text, Notification.FLAG_NO_CLEAR, EDCAndroid.class);
	}
	
	@Override
	public void execute(Context context, String text) {
		doNotificate(context, text);
	}

	@Override
	public void test(Context context) {
		doNotificate(context,"Demo notification");
	}

	@Override
	public String getTitle() {
		return name;
	}

	@Override
	public String getTopic() {
		return topic;
	}

	@Override
	public int getIcon() {
		return 0;
	}
}
