package ru.henridellal.emerald;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;

public class PersistentNotification {
	@TargetApi(11)
	public static void setNotification(Context context) {
		Notification noti = new Notification.Builder(context)
				.setContentTitle(context.getResources().getString(R.string.app_name))
				.setContentText(" ")
				.setSmallIcon(R.mipmap.icon)
				//	.setLargeIcon(new Bitmap(Bitmap.ARGB_8888))
				.build();
		NotificationManager notiManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
		notiManager.notify(0, noti);
	}
}
