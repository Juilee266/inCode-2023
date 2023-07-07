package com.inDrive.plugin.voice;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.inDrive.plugin.common.ActionListenerCallback;
import com.inDrive.plugin.common.SpeechToTextProvider;
import com.inDrive.plugin.common.TextToSpeechProvider;
import com.inDrive.plugin.entities.Passenger;
import com.inDrive.plugin.navigation.NavigationProvider;
import com.inDrive.plugin.navigation.graphhopper.GraphhopperClient;
import com.inDrive.plugin.navigation.graphhopper.response.direction.DirectionResponse;
import com.inDrive.plugin.services.LocationService;
import com.inDrive.plugin.services.STTListenerService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "InDriveMainActivity";

    private static final String[] requiredPermissions = new String[] {
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION,
            Manifest.permission.RECORD_AUDIO
    };
    private static final int PERMISSION_REQUEST_CODE = 200;
    private Chatbot chatbot;

    private volatile SpeechToTextProvider speechToTextProvider;
    private volatile TextToSpeechProvider textToSpeechProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "Activity Created.");

        Passenger p = new Passenger("JJ", "123456");
        try {
            chatbot = new Chatbot(this, p);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        checkMissingPermissions();

        speechToTextProvider = SpeechToTextProvider.getInstance(this);
        textToSpeechProvider = TextToSpeechProvider.getInstance(this);
        textToSpeechProvider.setSpeechToTextProvider(speechToTextProvider);
        speechToTextProvider.setTextToSpeechProvider(textToSpeechProvider);
        speechToTextProvider.registerActionListenerCallback(new SpeechToTextActionListener());
        textToSpeechProvider.registerActionListenerCallback(new TextToSpeechActionListener());

        NavigationProvider navigationProvider = new NavigationProvider(this);
        Optional<DirectionResponse> response = navigationProvider.getDirections("Kasba Ganpati", "Shivajinagar Railway Station");
        Log.d(TAG, response.get().toString());
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
        Log.d(TAG, "Activity Destroyed.");
    }

    private void checkMissingPermissions() {
        List<String> missingPermissions = new ArrayList<>();
        for (String permission : requiredPermissions) {
            if (checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED)
                continue;
            missingPermissions.add(permission);
        }

        if (missingPermissions.isEmpty())
            return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            requestPermissions(missingPermissions.toArray(new String[0]), PERMISSION_REQUEST_CODE);
        else
            ActivityCompat.requestPermissions(this, missingPermissions.toArray(new String[0]), PERMISSION_REQUEST_CODE);
    }

    private class TextToSpeechActionListener implements ActionListenerCallback {

        @Override
        public void onInitialized() {
            new Thread(() -> textToSpeechProvider.speak("Hello there Abhishek! Please don't startle my poor code. Just say - Book a cab from London to Manchester.")).start();
//            I hope this will keep working as expected. " +
//                    "Please refer MainActivity to check how to use callbacks. And yes, do not start any listening or speaking " +
//                    "until TTS is fully initialized. Otherwise we will run into sync issues. Now say something nice after the beep. " +
//                    "PS - No need to call speak method of text to speech provider in a thread. " +
//                    "Abhishek just did it to test concurrent executions. You may speak now.")).start();
            // Adding delay to make sure TTS starts speaking before STT starts listening
            new Handler().postDelayed(() ->speechToTextProvider.startListening(), 500);
        }

        @Override
        public void onActionStarted() {

        }

        @Override
        public void onActionCompleted(Map<String, Object> resultMap) {

        }

        @Override
        public void onActionFailed() {

        }
    }

    private class SpeechToTextActionListener implements ActionListenerCallback {

        @Override
        public void onInitialized() {

        }

        @Override
        public void onActionStarted() {

        }

        @Override
        public void onActionCompleted(Map<String, Object> resultMap) {
            try {
                String ans = chatbot.getResponse((String) resultMap.get(SpeechToTextProvider.STT_INFERRED_TEXT));
                new Thread(() -> textToSpeechProvider.speak(ans)).start();
                new Handler().postDelayed(() ->speechToTextProvider.startListening(), 500);

            }
            catch (Exception e) {
                Log.e("Error", e.getLocalizedMessage());
            }

        }

        @Override
        public void onActionFailed() {
            new Thread(() -> textToSpeechProvider.speak("Did you say something?")).start();
            new Handler().postDelayed(() ->speechToTextProvider.startListening(), 500);
        }
    }
}
