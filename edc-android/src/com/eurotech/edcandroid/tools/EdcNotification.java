package com.eurotech.edcandroid.tools;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

public class EdcNotification {

	public EdcNotification(Context context, int logo, String title, String message, int notificationFlags, Class<?> notificationClass) {
		
		boolean autoCancel = false;
		int defaults = 0;
		
		if ((notificationFlags & Notification.FLAG_NO_CLEAR) != 0) {
			autoCancel = false;
		}
		if ((notificationFlags & Notification.FLAG_AUTO_CANCEL) != 0) {
			autoCancel = true;
		}
		Intent notificationIntent = new Intent(context, notificationClass);
		PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		defaults |= Notification.DEFAULT_LIGHTS;
		defaults |= Notification.DEFAULT_VIBRATE;
		
		Notification notification = new Notification.Builder(context)
                                           .setContentIntent(contentIntent)
        		                           .setDeleteIntent(contentIntent)
                                           .setTicker(message)
                                           .setWhen(0)
                                           .setContentTitle(title)
                                           .setStyle(new Notification.BigTextStyle().bigText(message))
                                           .setContentText(message)
                                           .setDefaults(defaults)
                                           .setSmallIcon(logo)
                                           .setAutoCancel(autoCancel)
                                           .build();
		
		NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.notify(0, notification);
	}
}