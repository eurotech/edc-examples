package com.eurotech.edcdroid.commands;

import android.app.Notification;
import android.content.Context;

import com.eurotech.edcdroid.Demo;
import com.eurotech.edcdroid.R;
import com.eurotech.edcdroid.gateway.EdcConnectionService;

public class CommandNotification implements Command {

	private static final String NOTIFICATION_TITLE           = "EDC notification";


	String name = "Notification";
	String topic = "/command/notification";

	public CommandNotification() {
	}

	public void doNotificate(Context context, String text) {
		EdcConnectionService.EdcNotification(context, R.drawable.bluelogo, NOTIFICATION_TITLE, text, Notification.FLAG_NO_CLEAR, Demo.class);
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
