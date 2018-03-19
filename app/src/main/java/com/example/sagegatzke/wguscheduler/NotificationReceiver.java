package com.example.sagegatzke.wguscheduler;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import java.util.Date;

public class NotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String message = intent.getStringExtra("message");
        String title = intent.getStringExtra("title");
        int alarmId = intent.getIntExtra("alarmId", 0);
        int id = intent.getIntExtra("id", 0);
        String type = intent.getStringExtra("type");
        removeFlag(id, type, context);
        Intent notIntent = new Intent(context, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(context, alarmId, notIntent, PendingIntent.FLAG_CANCEL_CURRENT);
//        NotificationManagerCompat manager = NotificationManagerCompat.from(context);
//        NotificationCompat.BigTextStyle style = new NotificationCompat.BigTextStyle();
//        style.bigText(message);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context.getApplicationContext(), "scheduler_1");
//                        .setContentIntent(contentIntent)
//                        .setSmallIcon(R.drawable.ic_launcher_foreground)
//                        .setContentTitle(title)
//                        .setContentText(message)
//                        .setStyle(style)
//                        .setWhen(0)
//                        .setAutoCancel(true);
        NotificationCompat.BigTextStyle bigText = new NotificationCompat.BigTextStyle();
        bigText.bigText(message);
        bigText.setBigContentTitle(title);
        bigText.setSummaryText("summary");

        mBuilder.setContentIntent(contentIntent);
        mBuilder.setSmallIcon(R.drawable.ic_launcher_foreground);
        mBuilder.setContentTitle(title);
        mBuilder.setContentText(message);
        mBuilder.setPriority(Notification.PRIORITY_MAX);
        mBuilder.setStyle(bigText);
        //mBuilder.setDefaults(Notification.DEFAULT_SOUND);




//        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "home")
//                .setContentIntent(contentIntent)
//                .setSmallIcon(R.drawable.ic_launcher_foreground)
//                .setContentTitle(title)
//                .setContentText(message)
//                .setSound(S)
//                .setStyle(style)
//                .setWhen(0)
//                .setAutoCancel(true);

//        Notification notification = builder.build();
//        manager.notify(alarmId, notification);

        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("scheduler_1",
                    "Alarms",
                    NotificationManager.IMPORTANCE_DEFAULT);
            Uri uri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            AudioAttributes att = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build();
            channel.setSound(uri,att);

            mNotificationManager.createNotificationChannel(channel);
        }

        mNotificationManager.notify(alarmId, mBuilder.build());
    }

    private void removeFlag(int id, String type, Context context) {
        String name = type + id;
        SharedPreferences sharedPref = context.getSharedPreferences("NotificationPref", Context.MODE_PRIVATE);
        boolean notificationsOn = sharedPref.getBoolean(name, false);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(name, false);
        editor.commit();
    }
}

