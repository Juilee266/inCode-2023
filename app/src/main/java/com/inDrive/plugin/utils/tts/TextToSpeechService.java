package com.inDrive.plugin.utils.tts;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.support.annotation.Nullable;
import android.util.Log;

import com.inDrive.plugin.common.servicebinding.ServiceBinder;

import java.util.Locale;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TextToSpeechService extends Service implements TextToSpeech.OnInitListener {
    private static final String TAG = "TextToSpeechService";

    private TextToSpeech textToSpeech;
    private ServiceBinder<TextToSpeechService> binder;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    public void onCreate() {
        super.onCreate();
        textToSpeech = new TextToSpeech(getApplicationContext(), this);
        binder = new ServiceBinder<>(this);
        Log.i(TAG, "Successfully created text to speech instance.");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return this.binder;
    }

    @Override
    public void onInit(int status) {
        if (status != TextToSpeech.SUCCESS) {
            String message = String.format("Error while initializing text to speech. " +
                    "Init status: %d", status);
            Log.e(TAG, message);
            throw new RuntimeException(message);
        }

        int result = textToSpeech.setLanguage(Locale.US);
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            String message = "Unsupported language (en-US) used for text to speech.";
            Log.e(TAG, message);
            throw new RuntimeException(message);
        }

        Log.i(TAG, "Successfully initialized text to speech with locale en-US.");
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "Shutting down executors.");
        executorService.shutdown();

        if (textToSpeech != null) {
            Log.i(TAG, "Stopping text to speech instance.");
            textToSpeech.stop();
            textToSpeech.shutdown();
        }

        super.onDestroy();
        Log.i(TAG, "Successfully stopped text to speech service.");
    }

    public void speak(String text) {
        Log.d(TAG, String.format("Speaking: %s", text));
        try {
            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    textToSpeech.speak(text, TextToSpeech.QUEUE_ADD, null);
                }
            });
        } catch (Exception ex) {
            Log.e(TAG, String.format("Error while converting text (%s) to speech.", text), ex);
        }
    }

    // TODO: Call this while quitting the app when voice assistant is ready
    public void stop() {
        stopSelf();
    }
}
