package com.inDrive.plugin.common;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;

import com.inDrive.plugin.common.callbacks.ActionListenerCallback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class SpeechToTextProvider {
    public static final String STT_INFERRED_TEXT = "STT_INFERRED_TEXT";

    private static final String TAG = "SpeechToTextProvider";

    private static SpeechToTextProvider instace = null;

    private SpeechRecognizer speechRecognizer;
    private Intent recognizerIntent;

    private List<ActionListenerCallback> actionListenerCallbacks;

    private TextToSpeechProvider textToSpeechProvider;

    private AtomicBoolean isOkayToListen;

    private SpeechToTextProvider(Context context) {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context);

        recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

        speechRecognizer.setRecognitionListener(new SpeechRecognitionListener());

        actionListenerCallbacks = new ArrayList<>();

        isOkayToListen = new AtomicBoolean(true);

        Log.i(TAG, "Successfully initialized speech to text provider.");
    }

    public static SpeechToTextProvider getInstance(Context context) {
        if (instace == null) {
            synchronized (SpeechToTextProvider.class) {
                if (instace == null)
                    instace = new SpeechToTextProvider(context);
            }
        }

        return instace;
    }

    public void setTextToSpeechProvider(TextToSpeechProvider textToSpeechProvider) {
        this.textToSpeechProvider = textToSpeechProvider;
        this.textToSpeechProvider.registerActionListenerCallback(new TextToSpeechActionCallback());
    }

    public void registerActionListenerCallback(ActionListenerCallback callback) {
        actionListenerCallbacks.add(callback);
    }

    public void startListening() {
        try {
            while (!isOkayToListen.get()) Thread.sleep(100);
        } catch (Exception e) {
            Log.e(TAG, "Error while waiting to get ready for listening.", e);
        }

        for (ActionListenerCallback callback : actionListenerCallbacks)
            callback.onActionStarted();

        speechRecognizer.startListening(recognizerIntent);
    }

    public void stopListening() {
        speechRecognizer.stopListening();
    }

    private class TextToSpeechActionCallback implements ActionListenerCallback {
        @Override
        public void onActionStarted() {
            isOkayToListen.set(false);
        }

        @Override
        public void onActionCompleted(Map<String, Object> resultMap) {
            isOkayToListen.set(true);
        }

        @Override
        public void onActionFailed() {
            isOkayToListen.set(true);
        }
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
            Log.e(
                    TAG,
                    String.format("Error while listening to voice input from user. Error Code: %d", error)
            );

            for (ActionListenerCallback callback : actionListenerCallbacks)
                callback.onActionFailed();
        }

        @Override
        public void onResults(Bundle results) {
            String inferredText = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION).get(0);
            Log.d(TAG, String.format("Received voice input from user: %s", inferredText));

            Map<String, Object> map = new HashMap<>();
            map.put(SpeechToTextProvider.STT_INFERRED_TEXT, inferredText);
            for (ActionListenerCallback callback : actionListenerCallbacks)
                callback.onActionCompleted(map);
        }

        @Override
        public void onPartialResults(Bundle partialResults) {

        }

        @Override
        public void onEvent(int eventType, Bundle params) {

        }
    }

}
