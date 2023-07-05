package com.inDrive.plugin.voice;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.inDrive.plugin.common.TextToSpeechProvider;
import com.inDrive.plugin.navigation.NavigationProvider;
import com.inDrive.plugin.navigation.graphhopper.GraphhopperClient;
import com.inDrive.plugin.navigation.graphhopper.response.direction.DirectionResponse;
import com.inDrive.plugin.services.LocationService;
import com.inDrive.plugin.services.STTListenerService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "InDriveMainActivity";

    private static final String[] requiredPermissions = new String[] {
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION,
            Manifest.permission.FOREGROUND_SERVICE,
            Manifest.permission.RECORD_AUDIO
    };
    private static final int PERMISSION_REQUEST_CODE = 200;
    private static final String TAG = "MainActivity";
    private static Chatbot chatbot;

    private BroadcastReceiver speechToTextReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            String message = intent.getStringExtra("message");
            TextView tv= findViewById(R.id.dataFromSTT);
            tv.setText("Data from STT service :"+message);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO: Ask for missing permissions

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "Activity Created.");

        checkMissingPermissions();

        LocalBroadcastManager.getInstance(this).registerReceiver(speechToTextReceiver,
                new IntentFilter("DATA_FROM_STT"));

        Intent sttListnerServiceIntent = new Intent(this, STTListenerService.class);
        sttListnerServiceIntent.putExtra("inputExtra", "STT Service");
        ContextCompat.startForegroundService(this, sttListnerServiceIntent);


        NavigationProvider navigationProvider = new NavigationProvider(this);
        Optional<DirectionResponse> response = navigationProvider.getDirections("Kasba Ganpati", "Shivajinagar Railway Station");
        Log.d(TAG, response.get().toString());
        LocalBroadcastManager.getInstance(this).registerReceiver(locationReceiver,
                new IntentFilter("DATA_FROM_LOCATION"));
        chatbot = new Chatbot(this);
    }

    @Override
    protected  void onStart() {
        super.onStart();
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

    private void checkMissingPermissions() {
        List<String> missingPermissions = new ArrayList<>();
        for (String permission : requiredPermissions) {
            if (checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED)
                continue;
            missingPermissions.add(permission);
        }

b        if (missingPermissions.isEmpty())
            return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            requestPermissions(missingPermissions.toArray(new String[0]), PERMISSION_REQUEST_CODE);
        else
            ActivityCompat.requestPermissions(this, missingPermissions.toArray(new String[0]), PERMISSION_REQUEST_CODE);
    }
}
