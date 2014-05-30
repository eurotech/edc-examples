package com.eurotech.edcdroid.commands;

import android.content.Context;

public interface Command {

	public void execute(Context context, String msg);
	public String getTitle();
	public String getTopic();
	public int getIcon();
	public void test(Context context);
}
