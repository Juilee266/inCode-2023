package com.inDrive.plugin.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.inDrive.plugin.voice.MainActivity;
import com.inDrive.plugin.voice.R;

public class STTListenerService extends Service {
    public static final String CHANNEL_ID = "STTServiceChannel";
    Integer counter = 0;
    @Override
    public void onCreate() {
        super.onCreate();

        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //REST OF CODE HERE//
                counter = counter + 1;
                Intent intent1 = new Intent("DATA_FROM_STT");
                // You can also include some extra data.
                intent1.putExtra("message", counter.toString());
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent1);
            }

        }).start();
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String input = intent.getStringExtra("inputExtra");
        createNotificationChannel();
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("STT Listener Service")
                .setContentText(input)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentIntent(pendingIntent)
                .build();
        startForeground(1, notification);

        //do heavy work on a background thread
        //stopSelf();
        return START_NOT_STICKY;
    }
    @Override
    public void onDestroy() {
        System.out.println("Stopped service");
        super.onDestroy();
    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d("serv_create", "Created service");
        return null;
    }


    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }
}