package com.inDrive.plugin.common;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import lombok.Getter;

public class TextToSpeechProvider implements TextToSpeech.OnInitListener {
    private static final String TAG = "TextToSpeechProvider";

    private static TextToSpeechProvider instance = null;

    private TextToSpeech textToSpeech;
    private Context context;
    private AtomicBoolean isOkayToSpeak;
    private ExecutorService executorService;
    private SpeechToTextProvider speechToTextProvider;
    private List<ActionListenerCallback> actionListenerCallbacks;

    private TextToSpeechProvider(Context context) {
        this.context = context;
        executorService = Executors.newSingleThreadExecutor();
        textToSpeech = new TextToSpeech(context, this);
        actionListenerCallbacks = new ArrayList<>();
        isOkayToSpeak = new AtomicBoolean(false);

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

    public void setSpeechToTextProvider(SpeechToTextProvider speechToTextProvider) {
        this.speechToTextProvider = speechToTextProvider;
        this.speechToTextProvider.registerActionListenerCallback(new SpeechToTextActionCallback());
    }

    public void registerActionListenerCallback(ActionListenerCallback callback) {
        actionListenerCallbacks.add(callback);
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

        textToSpeech.setOnUtteranceProgressListener(new UtteranceListenerCallback());

        isOkayToSpeak.set(true);
        Log.i(TAG, "Successfully initialized text to speech with locale en-US.");

        for (ActionListenerCallback callback : actionListenerCallbacks) {
            callback.onInitialized();
        }
    }

    public void speak(String text) {
        executorService.submit(() -> {
            try {
                while (!isOkayToSpeak.get()) Thread.sleep(100);

                for (ActionListenerCallback callback : actionListenerCallbacks)
                    callback.onActionStarted();

                textToSpeech.speak(text, TextToSpeech.QUEUE_ADD, null, "InCode TTS");
            } catch (Exception ex) {
                Log.e(TAG, String.format("Error while converting text (%s) to speech.", text), ex);
            }
        });
    }

    public void dispose() {
        executorService.shutdown();
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }

        Log.i(TAG, "Successfully disposed text to speech provider.");
    }

    private class SpeechToTextActionCallback implements ActionListenerCallback {

        @Override
        public void onInitialized() {

        }

        @Override
        public void onActionStarted() {
            isOkayToSpeak.set(false);
        }

        @Override
        public void onActionCompleted(Map<String, Object> resultMap) {
            isOkayToSpeak.set(true);
        }

        @Override
        public void onActionFailed() {
            isOkayToSpeak.set(true);
        }
    }

    private class UtteranceListenerCallback extends UtteranceProgressListener {

        @Override
        public void onStart(String utteranceId) {
        }

        @Override
        public void onDone(String utteranceId) {
            for (ActionListenerCallback callback : actionListenerCallbacks)
                callback.onActionCompleted(null);
        }

        @Override
        public void onError(String utteranceId) {
            for (ActionListenerCallback callback : actionListenerCallbacks)
                callback.onActionFailed();
        }
    }
}
