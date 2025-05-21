package ch.petrce.triolingo.notifications;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import ch.petrce.triolingo.MainActivity;
import ch.petrce.triolingo.R;

public class NotificationReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "study_reminder_channel"; // channel id

    @Override
    public void onReceive(Context context, Intent intent) {
        createNotificationChannel(context); // create channel

        Intent activityIntent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, activityIntent, PendingIntent.FLAG_IMMUTABLE);

        // Build notification with icon, text & priority
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.triolingo_icon_round)
                .setContentTitle("Triolingo")
                .setContentText("How about you study for a bit?")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);
        // get notificationservice from android
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(100, builder.build()); // send builder value
    }

    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel( // create Notificationchannel
                    CHANNEL_ID,
                    "Study Reminder",
                    NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("Daily reminder to study");
            channel.enableLights(true);
            channel.setLightColor(Color.BLUE);

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null)
                notificationManager.createNotificationChannel(channel);
        }
    }
}
