package com.eurotech.edcdroid.gateway;

import java.util.Arrays;
import java.util.Map;
//import java.util.Date;
import java.util.Iterator;
import java.util.Map.Entry;

import com.eurotech.edcdroid.Demo;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.view.Gravity;
import android.widget.TextView;

public class EdcReceiveActivity extends Activity {

	int VerticalSpacing = 5;
	int HorizontalSpacing = 10;
	int SavedButtonState[] = null;
	private static TextView textMessage;
	private static int ID = 0;
	private static String separator;
	private static final String space = "\u00A0";

	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		
		Demo.localLog("BEGIN", Demo.ENABLE);

		textMessage = new TextView(this);
		textMessage.setMovementMethod(new ScrollingMovementMethod());
		textMessage.setSingleLine(false);
		textMessage.setGravity(Gravity.TOP);
		textMessage.setTextSize(15);
		textMessage.setTypeface(Typeface.MONOSPACE);
		setContentView(textMessage);
		String header = "| Waiting for EDC messages |";
		char[] bytes = new char[(header).length()];
		Arrays.fill(bytes, '-');
		separator = new String(bytes);
		separator = "+" + separator.substring(1, separator.length() - 1) + "+";
		header = "<br><b>" + header + "</b><br>";
		textMessage.append(Html.fromHtml(separator));
		textMessage.append(Html.fromHtml(header.replaceAll(" ", space)));
		textMessage.append(Html.fromHtml(separator + "<br>"));
		
		Demo.localLog("END", Demo.ENABLE);
	}

	protected void onDestroy() {
		super.onDestroy();
		Demo.localLog("BEGIN", Demo.ENABLE);
		Demo.localLog("END", Demo.ENABLE);
	}

	public static void NewMessage(Map<String,Object> payload) {
		Demo.localLog("BEGIN", Demo.ENABLE);
		String msg;
		separator = lineFiller(textMessage, "-");
		separator = "+" + separator.substring(1, separator.length() - 1) + "+";
		ID++;
		if (ID == 1)
			textMessage.append(Html.fromHtml(separator));
		msg = "<br> <b>ID:</b> " + ID;
		Iterator<Entry<String, Object>> metrics = payload.entrySet().iterator();
		while (metrics.hasNext()) {
			Entry<String, Object> metric = metrics.next();
			String name = metric.getKey();
			Object value = metric.getValue();
			msg += "<br> <b>" + name + ":</b> " + value;
		}
		textMessage.append(Html.fromHtml(msg.replaceAll(" ", space)));
		textMessage.append("\n" + Html.fromHtml(separator));
		final int scrollAmount = textMessage.getLayout().getLineTop(textMessage.getLineCount()) - textMessage.getHeight();
		if(scrollAmount > 0)
			textMessage.scrollTo(0, scrollAmount);
		else
			textMessage.scrollTo(0,0);
		Demo.localLog("END", Demo.ENABLE);
	}

	private static String lineFiller (TextView tv, String fillChar) {
		String filler = fillChar;
		while (tv.getPaint().measureText(filler += fillChar) < tv.getMeasuredWidth());
		return filler.substring(0, filler.length() - 1);
	}
}