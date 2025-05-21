package ch.petrce.triolingo.notifications;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;

public class AlarmScheduler {
    public static void scheduleDailyNotification(Context context) {
        // Intent to fire our BroadcastReceiver (chatgpt)
        Intent intent = new Intent(context, NotificationReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(
                context, 0, intent, PendingIntent.FLAG_IMMUTABLE
        );

        Calendar cal = Calendar.getInstance(); // new calender
        cal.set(Calendar.HOUR_OF_DAY, 12); // set hours
        cal.set(Calendar.MINUTE, 0); // set minutes
        cal.set(Calendar.SECOND, 0); // set seconeds
        // cal.setTimeInMillis(System.currentTimeMillis() + 10_000);
        long trigger = cal.getTimeInMillis();

        if (trigger < System.currentTimeMillis()) { // schedule for next day if time has passed.
            cal.add(Calendar.DAY_OF_YEAR, 1);
            trigger = cal.getTimeInMillis();
        }

        // Get AlarmManager and set an exact alarm that works in Doze mode (chatgpt)
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (am != null) {
            am.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    trigger,
                    pi
            );
        }
    }
}
