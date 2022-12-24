package com.example.chatroop;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class Notification {

    public static void displayNotification(Context context, String title, String message){

        Intent intent = new Intent(context, LoginActivity.class);
        PendingIntent back_to_app = PendingIntent.getActivity(context, 1111, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        NotificationCompat.Builder notification = new NotificationCompat.Builder(context, MainActivity.current_user_id)
                .setSmallIcon(R.drawable.apk_logo)
                .setContentTitle(title)
                .setContentText(message)
                .setContentIntent(back_to_app)
                .setAutoCancel(true)
                .setLights(0,500,500)
                .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        NotificationManagerCompat manager = NotificationManagerCompat.from(context);
        manager.notify(1, notification.build());
    }
}
