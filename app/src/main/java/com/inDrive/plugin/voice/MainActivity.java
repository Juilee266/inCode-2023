package com.inDrive.plugin.voice;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.inDrive.plugin.common.TextToSpeechProvider;
import com.inDrive.plugin.services.LocationService;
import com.inDrive.plugin.services.STTListenerService;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private BroadcastReceiver speechToTextReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            String message = intent.getStringExtra("message");
            TextView tv= findViewById(R.id.dataFromSTT);
            tv.setText("Data from STT service :"+message);
        }
    };
    private BroadcastReceiver locationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            String message = intent.getStringExtra("message");
            TextView tv= findViewById(R.id.dataFromLocation);
            tv.setText("Data from Location service :"+message);
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO: Ask for missing permissions

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "Activity Created.");
        LocalBroadcastManager.getInstance(this).registerReceiver(speechToTextReceiver,
                new IntentFilter("DATA_FROM_STT"));
        LocalBroadcastManager.getInstance(this).registerReceiver(locationReceiver,
                new IntentFilter("DATA_FROM_LOCATION"));
    }

    @Override
    protected  void onStart() {
        super.onStart();
        Intent sttServiceIntent = new Intent(this, STTListenerService.class);
        sttServiceIntent.putExtra("inputExtra", "STT Service");
        ContextCompat.startForegroundService(this, sttServiceIntent);

        Intent locServiceIntent = new Intent(this, LocationService.class);
        locServiceIntent.putExtra("inputExtra", "Location Service");
        ContextCompat.startForegroundService(this, locServiceIntent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "Activity Resumed.");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "Activity Paused.");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "Activity Stopped.");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(speechToTextReceiver);
        Log.d(TAG, "Activity Destroyed.");
    }
}
