package com.eurotech.edcdroid.commands;

import java.util.Locale;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.widget.Toast;

public class SpeakActivity extends Activity implements OnInitListener {

	private enum LANGUAGES {
		US, ITA, ITALIANO, ITALY, ITALIAN, FR, FRENCH, FRANCE, FRANCIA, ENG, UK, GER, GERMAN, GERMANY, GERMANIA, TEDESCO, JAP, JAPAN, JAPANESE
	};

	static final String SPEAK_HEADER = "speak";
	private static final int DELAYED_MESSAGE = 1;

	String textToSpeak = "";
	String language = "";

	private TextToSpeech myTTS;
	private int MY_DATA_CHECK_CODE = 0;

	private Handler handler;

	@SuppressLint("HandlerLeak")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				if(msg.what == DELAYED_MESSAGE) {
					SpeakActivity.this.finish();
				}
				super.handleMessage(msg);
			}
		};

		Intent textIntent = this.getIntent();
		textToSpeak = textIntent.getStringExtra(SPEAK_HEADER);
		if (textToSpeak.contains("(")) {
			String[] splitString = textToSpeak.split("\\(");
			language = splitString[1].split("\\)")[0].toUpperCase(Locale.getDefault());
			textToSpeak = splitString[0];
		} else
			language = "US";

		Intent checkTTSIntent = new Intent();
		checkTTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
		startActivityForResult(checkTTSIntent, MY_DATA_CHECK_CODE);

		Message message = handler.obtainMessage(DELAYED_MESSAGE);
		handler.sendMessageDelayed(message, 1000);
	}

	protected void onDestroy() {
		super.onDestroy();
		myTTS.shutdown();
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == MY_DATA_CHECK_CODE) {
			if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
				myTTS = new TextToSpeech(this, this);
			}
			else {
				Intent installTTSIntent = new Intent();
				installTTSIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
				startActivity(installTTSIntent);
			}
		}
	}

	public void onInit(int initStatus) {
		if (initStatus == TextToSpeech.SUCCESS) {
			if(myTTS.isLanguageAvailable(getLocale(language)) == 1)
				myTTS.setLanguage(getLocale(language));
			else {
				Intent installIntent = new Intent(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
			    installIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			    installIntent.setPackage("com.google.android.tts");
			    try {
			        startActivity(installIntent);
			    } catch (ActivityNotFoundException ex) {
			    	Toast.makeText(this, "TTSpeech installation failed.", Toast.LENGTH_LONG).show();
			    }
			}
			myTTS.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, null);
		}
		else if (initStatus == TextToSpeech.ERROR) {
			Toast.makeText(this, "Sorry! Text To Speech failed.", Toast.LENGTH_LONG).show();
		}
	}

	public Locale getLocale(String language) {

		LANGUAGES sel = LANGUAGES.valueOf(language);
		switch (sel) {
		case US:
			return Locale.US;
		case ITA:
		case ITALIANO:
		case ITALY:
		case ITALIAN:
			return Locale.ITALY;
		case FR:
		case FRENCH:
		case FRANCE:
		case FRANCIA:
			return Locale.FRANCE;
		case UK:
		case ENG:
			return Locale.UK;
		case GER:
		case GERMANIA:
		case GERMANY:
		case TEDESCO:
		case GERMAN:
			return Locale.GERMANY;
		case JAP:
		case JAPAN:
		case JAPANESE:
			return Locale.JAPAN;
		default:
			return Locale.US;
		}
	}
}
