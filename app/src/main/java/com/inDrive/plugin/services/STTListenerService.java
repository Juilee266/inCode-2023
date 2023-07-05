package com.inDrive.plugin.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.Tag;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.inDrive.plugin.common.TextToSpeechProvider;
import com.inDrive.plugin.voice.MainActivity;
import com.inDrive.plugin.voice.R;

import java.util.Locale;

public class STTListenerService extends Service {
    private static final String CHANNEL_ID = "STTServiceChannel";
    private static final String TAG = "STTListenerService";

    private SpeechRecognizer speechRecognizer;
    private Intent recognizerIntent;
    private TextToSpeechProvider textToSpeechProvider;

    private BroadcastReceiver commandReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra("message");
            if (message == null) return;
            if (message.equals("START_LISTENING")) startListening();

            else if (message.equals("STOP_LISTENING")) stopListening();
        }
    };

    //Integer counter = 0;
    @Override
    public void onCreate() {
        super.onCreate();

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(getApplicationContext());
        recognizerIntent = getRecognizerIntent();
        speechRecognizer.setRecognitionListener(new SpeechRecognitionListener());
        startListening();

        LocalBroadcastManager.getInstance(this).registerReceiver(commandReceiver,
                new IntentFilter("STT_COMMANDS"));

        textToSpeechProvider = TextToSpeechProvider.getInstance(this);

        Log.i(TAG, "Initialized speech to text listener service.");
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
        Log.i(TAG, "Destroying speech to text listener service.");
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void startListening() {
        speechRecognizer.startListening(recognizerIntent);
    }

    public void stopListening() {
        speechRecognizer.cancel();
        speechRecognizer.stopListening();
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

    private Intent getRecognizerIntent() {
        Intent speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        return speechRecognizerIntent;
    }

    private Intent getDataIntent(String text) {
        Intent intent = new Intent("DATA_FROM_STT");
        intent.putExtra("message", text);
        return intent;
    }

    private class SpeechRecognitionListener implements RecognitionListener {
        private static final String TAG = "SpeechRecognitionListener";

        @Override
        public void onReadyForSpeech(Bundle params) {

        }

        @Override
        public void onBeginningOfSpeech() {

        }

        @Override
        public void onRmsChanged(float rmsdB) {

        }

        @Override
        public void onBufferReceived(byte[] buffer) {

        }

        @Override
        public void onEndOfSpeech() {

        }

        @Override
        public void onError(int error) {
            Log.d(TAG, String.valueOf(error));
            if (error != SpeechRecognizer.ERROR_RECOGNIZER_BUSY)
                startListening();
        }

        @Override
        public void onResults(Bundle results) {
            String text = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION).get(0);
            Log.d(TAG, text);

            textToSpeechProvider.speak(text);
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(getDataIntent(text));
            // Do not start listening from here.
            // Start listening again after TTS engine is done with speaking.
            //startListening();
        }

        @Override
        public void onPartialResults(Bundle partialResults) {

        }

        @Override
        public void onEvent(int eventType, Bundle params) {

        }
    }
}