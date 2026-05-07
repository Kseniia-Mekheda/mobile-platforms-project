package com.example.taskforge.domain.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.example.taskforge.data.entities.Subscription;
import com.example.taskforge.receivers.SubscriptionReceiver;

public class AlarmScheduler {

    /**
     * Планує нагадування про підписку через AlarmManager.
     */
    public static void scheduleSubscriptionReminder(Context context, Subscription subscription) {
        if (!subscription.reminder_enabled) {
            return;
        }

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        Intent intent = new Intent(context, SubscriptionReceiver.class);
        intent.putExtra(SubscriptionReceiver.EXTRA_SUB_TITLE, subscription.title);
        intent.putExtra(SubscriptionReceiver.EXTRA_SUB_AMOUNT, subscription.amount);

        // Використовуємо id підписки як requestCode для скасування/оновлення
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                (int) subscription.id,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Якщо маємо інтервал повторення - налаштовуємо повторюваний будильник
        if (subscription.repeat_interval > 0) {
            alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    subscription.start_date_ms,
                    subscription.repeat_interval,
                    pendingIntent
            );
        } else {
            // Одноразовий
            alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    subscription.start_date_ms,
                    pendingIntent
            );
        }
    }

    /**
     * Скасовує заплановане нагадування.
     */
    public static void cancelSubscriptionReminder(Context context, long subscriptionId) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        Intent intent = new Intent(context, SubscriptionReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                (int) subscriptionId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        alarmManager.cancel(pendingIntent);
    }
}
