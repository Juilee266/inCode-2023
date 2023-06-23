package com.inDrive.plugin.voice;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.inDrive.plugin.common.servicebinding.ConcreteServiceConnection;
import com.inDrive.plugin.utils.tts.TextToSpeechService;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private ConcreteServiceConnection<TextToSpeechService> textToSpeechServiceConnection = new ConcreteServiceConnection<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "Activity Created.");
    }

    @Override
    protected  void onStart() {
        super.onStart();
        Intent intent = new Intent(this, TextToSpeechService.class);
        startService(intent);
        bindService(intent, textToSpeechServiceConnection, BIND_AUTO_CREATE);
        Log.d(TAG, "Activity Started.");

        // TODO: Remove once finalized. :P
        new Handler().postDelayed(
                new Runnable() {
                    @Override
                    public void run() {
                        if (textToSpeechServiceConnection.isBound()) {
                            TextToSpeechService service = textToSpeechServiceConnection.getService();
                            service.speak("Hello Juilee! Text to speech is working!");
                            service.speak("It may need some adjustments as we move ahead " +
                                            "with the implementation.");
                            service.speak("Let me know if you find any problem with it.");
                            service.speak("                                             ");
                            service.speak("And yes, merge it to master if everything is okay.");

                        }
                    }
                }, 2000);
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
        unbindService(textToSpeechServiceConnection);
        Log.d(TAG, "Activity Destroyed.");
    }
}
