package com.eurotech.edcdroid.commands;

import android.content.Context;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;

public class CommandRing implements Command {

	String name = "Ring";
	String topic = "/command/ring";
	
	public CommandRing() {
	}
	
	public void doRing(Context context) {
		Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		Ringtone r = RingtoneManager.getRingtone(context, notification);
		r.play();
	}
	
	@Override
	public void execute(Context context, String msg) {
		doRing(context);
	}

	@Override
	public void test(Context context) {
		doRing(context);
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
