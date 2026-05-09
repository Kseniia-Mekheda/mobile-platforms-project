package com.example.taskforge.receivers;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.example.taskforge.R;
import com.example.taskforge.ui.MainActivity;

public class SubscriptionReceiver extends BroadcastReceiver {

    public static final String EXTRA_SUB_TITLE = "EXTRA_SUB_TITLE";
    public static final String EXTRA_SUB_AMOUNT = "EXTRA_SUB_AMOUNT";
    // ⚡️ НОВІ КОНСТАНТИ
    public static final String EXTRA_SUB_ID = "EXTRA_SUB_ID";
    public static final String EXTRA_SUB_INTERVAL = "EXTRA_SUB_INTERVAL";

    private static final String CHANNEL_ID = "SubscriptionReminders";

    @Override
    public void onReceive(Context context, Intent intent) {
        String title = intent.getStringExtra(EXTRA_SUB_TITLE);
        double amount = intent.getDoubleExtra(EXTRA_SUB_AMOUNT, 0.0);
        long subId = intent.getLongExtra(EXTRA_SUB_ID, -1);
        long interval = intent.getLongExtra(EXTRA_SUB_INTERVAL, 0);

        if (title == null) title = "Невідома підписка";

        Toast.makeText(context, "🔔 СПРАЦЮВАЛО НАГАДУВАННЯ: " + title, Toast.LENGTH_LONG).show();

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Нагадування про підписки",
                    NotificationManager.IMPORTANCE_HIGH
            );
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }

        Intent mainIntent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                mainIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Час оплатити підписку!")
                .setContentText(title + " (" + amount + " UAH)")
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        int notificationId = (int) System.currentTimeMillis();
        if (notificationManager != null) {
            notificationManager.notify(notificationId, builder.build());
        }

        // ⚡️ ПЕРЕЗАПУСК БУДИЛЬНИКА (Естафета)
        if (subId != -1 && interval > 0) {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null) {
                Intent nextIntent = new Intent(context, SubscriptionReceiver.class);
                nextIntent.setAction("TASKFORGE_ALARM_" + subId);
                nextIntent.putExtra(EXTRA_SUB_TITLE, title);
                nextIntent.putExtra(EXTRA_SUB_AMOUNT, amount);
                nextIntent.putExtra(EXTRA_SUB_ID, subId);
                nextIntent.putExtra(EXTRA_SUB_INTERVAL, interval);

                PendingIntent nextPendingIntent = PendingIntent.getBroadcast(
                        context,
                        (int) subId,
                        nextIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                );

                long nextTriggerTime = System.currentTimeMillis() + interval;

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nextTriggerTime, nextPendingIntent);
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, nextTriggerTime, nextPendingIntent);
                } else {
                    alarmManager.set(AlarmManager.RTC_WAKEUP, nextTriggerTime, nextPendingIntent);
                }
            }
        }
    }
}