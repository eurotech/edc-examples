package com.eurotech.edcandroid.commands;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.WindowManager;

public class BrightnessActivity extends Activity {

	static final String BRIGHTNESS_HEADER = "brightness";
	private static final int DELAYED_MESSAGE = 1;

	private Handler handler;

	@SuppressLint("HandlerLeak")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				if (msg.what == DELAYED_MESSAGE) {
					BrightnessActivity.this.finish();
				}
				super.handleMessage(msg);
			}
		};
		Intent brightnessIntent = this.getIntent();
		float brightness = brightnessIntent.getFloatExtra(BRIGHTNESS_HEADER, 0);
		WindowManager.LayoutParams lp = getWindow().getAttributes();
		lp.screenBrightness = brightness / 100;
		getWindow().setAttributes(lp);

		Message message = handler.obtainMessage(DELAYED_MESSAGE);
		handler.sendMessageDelayed(message, 1000);
	}
}