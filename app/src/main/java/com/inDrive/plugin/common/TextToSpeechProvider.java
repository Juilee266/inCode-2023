package com.inDrive.plugin.common;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.inDrive.plugin.common.servicebinding.ConcreteServiceConnection;
import com.inDrive.plugin.common.servicebinding.ServiceBinder;
import com.inDrive.plugin.services.STTListenerService;

import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TextToSpeechProvider implements TextToSpeech.OnInitListener {
    private static final String TAG = "TextToSpeechProvider";

    private static TextToSpeechProvider instance = null;

    private TextToSpeech textToSpeech;
    private Context context;

    private TextToSpeechProvider(Context context) {
        this.context = context;
        textToSpeech = new TextToSpeech(context, this);

        Log.i(TAG, "Successfully initialized text to speech provider.");
    }

    public static TextToSpeechProvider getInstance(Context context) {
        if (instance == null) {
            synchronized (TextToSpeechProvider.class) {
                if (instance == null) {
                    instance = new TextToSpeechProvider(context);
                }
            }
        }

        return instance;
    }

    @Override
    public void onInit(int status) {
        if (status != TextToSpeech.SUCCESS) {
            String message = String.format("Error while initializing text to speech. " +
                    "Init status: %d", status);
            Log.e(TAG, message);
            throw new RuntimeException(message);
        }

        // TODO: Research and make it configurable
        int result = textToSpeech.setLanguage(Locale.US);
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            String message = "Unsupported language (en-US) used for text to speech.";
            Log.e(TAG, message);
            throw new RuntimeException(message);
        }

        textToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String utteranceId) {
            }

            @Override
            public void onDone(String utteranceId) {
                LocalBroadcastManager.getInstance(context).sendBroadcast(getSTTCommandIntent(false));
            }

            @Override
            public void onError(String utteranceId) {
                LocalBroadcastManager.getInstance(context).sendBroadcast(getSTTCommandIntent(false));
            }
        });

        Log.i(TAG, "Successfully initialized text to speech with locale en-US.");
    }

    public void speak(String text) {
        try {
            LocalBroadcastManager.getInstance(context).sendBroadcast(getSTTCommandIntent(true));
            textToSpeech.speak(text, TextToSpeech.QUEUE_ADD, null, "InCode TTS");
        } catch (Exception ex) {
            Log.e(TAG, String.format("Error while converting text (%s) to speech.", text), ex);
        }
    }

    public void dispose() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }

        Log.i(TAG, "Successfully disposed text to speech provider.");
    }

    private Intent getSTTCommandIntent(boolean stop) {
        Intent intent = new Intent("STT_COMMANDS");
        intent.putExtra("message", stop ? "STOP_LISTENING" : "START_LISTENING");
        return intent;
    }
}
