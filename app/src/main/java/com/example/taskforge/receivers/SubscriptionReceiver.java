package com.example.taskforge.receivers;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.example.taskforge.ui.MainActivity;

public class SubscriptionReceiver extends BroadcastReceiver {

    public static final String EXTRA_SUB_TITLE = "EXTRA_SUB_TITLE";
    public static final String EXTRA_SUB_AMOUNT = "EXTRA_SUB_AMOUNT";
    private static final String CHANNEL_ID = "SubscriptionReminders";

    @Override
    public void onReceive(Context context, Intent intent) {
        String title = intent.getStringExtra(EXTRA_SUB_TITLE);
        double amount = intent.getDoubleExtra(EXTRA_SUB_AMOUNT, 0.0);

        if (title == null) title = "Невідома підписка";

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Нагадування про підписки",
                    NotificationManager.IMPORTANCE_HIGH
            );
            notificationManager.createNotificationChannel(channel);
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
                .setContentTitle("Нагадування про оплату!")
                .setContentText("Час оплатити підписку: " + title + " (" + amount + ")")
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        // Генеруємо унікальний ID для кожної підписки
        int notificationId = (int) System.currentTimeMillis();
        notificationManager.notify(notificationId, builder.build());
    }
}
