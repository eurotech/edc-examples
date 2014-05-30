package com.eurotech.edcdroid.commands;

import android.content.Context;
import android.os.Vibrator;

public class CommandVibrate implements Command {
	
	String name = "Vibrate";
	String topic = "/command/vibrate";

	public CommandVibrate() {
	}

	public void doVibrate(Context context, int len) {
		Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
		v.vibrate(len);
	}

	@Override
	public void execute(Context context, String value) {
		if (value.equals("short")) {
			doVibrate(context, 300);
		} else if (value.equals("long")) {			
			doVibrate(context, 600);
		} else if (value.equals("annoying")) {				
			doVibrate(context, 2000);
		}
	}

	@Override
	public void test(Context context){
		doVibrate(context, 2000);
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
