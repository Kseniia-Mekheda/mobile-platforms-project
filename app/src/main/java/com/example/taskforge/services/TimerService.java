package com.example.taskforge.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.taskforge.ui.MainActivity; // Fallback activity to open

public class TimerService extends Service {

    public static final String ACTION_START = "ACTION_START";
    public static final String ACTION_STOP = "ACTION_STOP";
    public static final String EXTRA_TASK_ID = "EXTRA_TASK_ID";
    public static final String EXTRA_TASK_TITLE = "EXTRA_TASK_TITLE";
    public static final String ACTION_TIMER_TICK = "ACTION_TIMER_TICK";
    public static final String EXTRA_TIME_ELAPSED = "EXTRA_TIME_ELAPSED";

    private static final String CHANNEL_ID = "TimerServiceChannel";
    private static final int NOTIFICATION_ID = 1;

    private boolean isRunning = false;
    private long elapsedTimeMs = 0;
    private long taskId = -1;
    private String taskTitle = "Task";
    
    private Handler handler = new Handler();
    private Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            elapsedTimeMs += 1000;
            broadcastTime();
            updateNotification();
            handler.postDelayed(this, 1000);
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            if (ACTION_START.equals(action)) {
                taskId = intent.getLongExtra(EXTRA_TASK_ID, -1);
                taskTitle = intent.getStringExtra(EXTRA_TASK_TITLE);
                if (taskTitle == null) taskTitle = "Task";
                
                // You could load existing elapsed time from DB here.
                
                if (!isRunning) {
                    isRunning = true;
                    startForeground(NOTIFICATION_ID, buildNotification());
                    handler.postDelayed(timerRunnable, 1000);
                }
            } else if (ACTION_STOP.equals(action)) {
                stopTimer();
            }
        }
        return START_NOT_STICKY;
    }

    private void stopTimer() {
        isRunning = false;
        handler.removeCallbacks(timerRunnable);
        // Here you would optimally broadcast a STOP event to save elapsed time to the DB
        stopForeground(true);
        stopSelf();
    }

    private void broadcastTime() {
        Intent intent = new Intent(ACTION_TIMER_TICK);
        intent.putExtra(EXTRA_TIME_ELAPSED, elapsedTimeMs);
        intent.putExtra(EXTRA_TASK_ID, taskId);
        sendBroadcast(intent);
    }

    private Notification buildNotification() {
        // Додаємо Intent для повернення в додаток при кліку на сповіщення
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        long seconds = (elapsedTimeMs / 1000) % 60;
        long minutes = (elapsedTimeMs / (1000 * 60)) % 60;
        long hours = (elapsedTimeMs / (1000 * 60 * 60));
        String timeString = String.format("%02d:%02d:%02d", hours, minutes, seconds);

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Таймер: " + taskTitle)
                .setContentText("Час: " + timeString)
                .setSmallIcon(android.R.drawable.ic_menu_edit)
                .setContentIntent(pendingIntent)
                .setOnlyAlertOnce(true)
                .build();
    }

    private void updateNotification() {
        NotificationManager manager = getSystemService(NotificationManager.class);
        if (manager != null) {
            manager.notify(NOTIFICATION_ID, buildNotification());
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Task Timer Channel",
                    NotificationManager.IMPORTANCE_LOW
            );
            serviceChannel.setDescription("Сповіщення для активного таймера завдання");
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(serviceChannel);
            }
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
