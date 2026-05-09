package com.example.taskforge.domain.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.example.taskforge.data.entities.Subscription;
import com.example.taskforge.receivers.SubscriptionReceiver;

public class AlarmScheduler {

    public static void scheduleSubscriptionReminder(Context context, Subscription subscription) {
        if (!subscription.reminder_enabled) {
            return;
        }

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        Intent intent = new Intent(context, SubscriptionReceiver.class);
        intent.setAction("TASKFORGE_ALARM_" + subscription.id);

        intent.putExtra(SubscriptionReceiver.EXTRA_SUB_TITLE, subscription.title);
        intent.putExtra(SubscriptionReceiver.EXTRA_SUB_AMOUNT, subscription.amount);
        intent.putExtra(SubscriptionReceiver.EXTRA_SUB_ID, subscription.id);
        intent.putExtra(SubscriptionReceiver.EXTRA_SUB_INTERVAL, subscription.repeat_interval);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                (int) subscription.id,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        long triggerTime = System.currentTimeMillis() + subscription.repeat_interval;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
        }
    }

    public static void cancelSubscriptionReminder(Context context, long subscriptionId) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        Intent intent = new Intent(context, SubscriptionReceiver.class);

        // ⚡️ При скасуванні також вказуємо цей унікальний екшн
        intent.setAction("TASKFORGE_ALARM_" + subscriptionId);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                (int) subscriptionId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        alarmManager.cancel(pendingIntent);
    }
}