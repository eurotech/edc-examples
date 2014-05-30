package com.eurotech.edcdroid.commands;

import android.content.Context;
import android.content.Intent;

public class CommandSpeak implements Command {

	String name = "Speak";
	String topic = "/command/speak";

	public CommandSpeak() {
	}

	public void doSpeak(Context context, String text) {
		Intent intent = new Intent(context, SpeakActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra(SpeakActivity.SPEAK_HEADER, text); 
		context.startActivity(intent);
	}
	
	@Override
	public void execute(Context context, String text) {
		doSpeak(context, text);
	}

	@Override
	public void test(Context context) {
		doSpeak(context, "I am a demo");
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
