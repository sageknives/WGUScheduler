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

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context.getApplicationContext(), "scheduler_1");
        NotificationCompat.BigTextStyle bigText = new NotificationCompat.BigTextStyle();
        bigText.bigText(message);
        bigText.setBigContentTitle(title);
        bigText.setSummaryText("summary");

        builder.setContentIntent(contentIntent);
        builder.setColor(context.getResources().getColor(R.color.colorPrimaryDark));
        builder.setSmallIcon(R.mipmap.ic_notify_foreground);
        builder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_notify));

        builder.setContentTitle(title);
        builder.setContentText(message);
        builder.setPriority(Notification.PRIORITY_MAX);
        builder.setStyle(bigText);

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("scheduler_1","Alarms", NotificationManager.IMPORTANCE_DEFAULT);
            Uri uri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            AudioAttributes att = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build();
            channel.setSound(uri,att);
            manager.createNotificationChannel(channel);
        }

        manager.notify(alarmId, builder.build());
    }

    private void removeFlag(int id, String type, Context context) {
        if(type.equals("course")) return;
        String name = type + id;
        SharedPreferences sharedPref = context.getSharedPreferences("NotificationPref", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(name, false);
        editor.commit();
    }
}

