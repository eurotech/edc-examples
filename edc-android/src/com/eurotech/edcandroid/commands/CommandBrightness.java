package com.eurotech.edcandroid.commands;

import android.content.Context;
import android.content.Intent;

public class CommandBrightness implements Command {

	String name = "Brightness";
	String topic = "/command/brightness";
	
	public CommandBrightness() {
		
	}
	
	public void doBrightness(Context context, Float brightness) {
		
		Intent intent = new Intent(context, BrightnessActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

		intent.putExtra(BrightnessActivity.BRIGHTNESS_HEADER, brightness); 
		context.startActivity(intent);
	}
	
	@Override
	public void execute(Context context, String text) {
		Float brightness = Float.parseFloat(text);
		doBrightness(context, brightness);
	}

	@Override
	public void test(Context context) {
		doBrightness(context, 0.1F);
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
