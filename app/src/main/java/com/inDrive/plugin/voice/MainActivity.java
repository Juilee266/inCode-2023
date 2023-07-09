package com.inDrive.plugin.voice;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.inDrive.plugin.common.callbacks.ActionListenerCallback;
import com.inDrive.plugin.common.SpeechToTextProvider;
import com.inDrive.plugin.common.TextToSpeechProvider;
import com.inDrive.plugin.common.callbacks.OnInitListenerCallback;
import com.inDrive.plugin.model.Passenger;
import com.inDrive.plugin.ui.chat.MessageListAdapter;
import com.inDrive.plugin.ui.chat.model.Message;
import com.inDrive.plugin.ui.chat.model.Sender;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    private SpeechToTextProvider speechToTextProvider;
    private TextToSpeechProvider textToSpeechProvider;

    private RecyclerView recyclerView;
    private MessageListAdapter messageListAdapter;

    private boolean hasConvEnded = false;
    private boolean hasChatbotInitialized = false;
    private boolean hasTtsInitialized = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "Activity Created.");

        checkMissingPermissions();

        LocalBroadcastManager.getInstance(this).registerReceiver(updateFromChatbot,
                new IntentFilter("TTS_REQ_FROM_CHATBOT"));
        LocalBroadcastManager.getInstance(this).registerReceiver(finishConversation,
                new IntentFilter("REACHED_DEST"));
        recyclerView = (RecyclerView) findViewById(R.id.recycler_chat);
        messageListAdapter = new MessageListAdapter(this, new ArrayList<>());
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(messageListAdapter);

        Passenger p = new Passenger("JJ", "123456");
        chatbot = new Chatbot(this, p);
        chatbot.registerOnInitListenerCallback(new ChatbotInitializationListener());

        speechToTextProvider = SpeechToTextProvider.getInstance(this);
        textToSpeechProvider = TextToSpeechProvider.getInstance(this);
        textToSpeechProvider.setSpeechToTextProvider(speechToTextProvider);
        speechToTextProvider.setTextToSpeechProvider(textToSpeechProvider);

        speechToTextProvider.registerActionListenerCallback(new SpeechToTextActionListener());

        TextToSpeechActionListener ttsActionListener = new TextToSpeechActionListener();
        textToSpeechProvider.registerActionListenerCallback(ttsActionListener);
        textToSpeechProvider.registerOnInitListenerCallback(ttsActionListener);
    }

    private BroadcastReceiver updateFromChatbot = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            String message = intent.getStringExtra("message");
            addMessageToRecyclerView(new Message(message, Sender.SYSTEM));
            textToSpeechProvider.speak(message);
        }
    };

    private BroadcastReceiver finishConversation = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            String message = intent.getStringExtra("message");
            addMessageToRecyclerView(new Message(message, Sender.SYSTEM));
            textToSpeechProvider.speak(message);
            hasConvEnded = true;
        }
    };

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

    private void showGreeting() {
        runOnUiThread(() -> {
            String text = "Welcome to InDrive. This is a demo of the ride hailing plugin for the blind and visually impaired. Please speak after the beep.";
            addMessageToRecyclerView(new Message(text, Sender.SYSTEM));
            textToSpeechProvider.speak(text);

            new Handler().postDelayed(() ->speechToTextProvider.startListening(), 500);
        });
    }

    private void addMessageToRecyclerView(Message message) {
        if (message == null) return;

        messageListAdapter.addMessage(message);
        recyclerView.scrollToPosition(messageListAdapter.getItemCount() - 1);

    }

    private class TextToSpeechActionListener implements ActionListenerCallback, OnInitListenerCallback {

        @Override
        public void onInitialized() {
            hasTtsInitialized = true;

            if (!hasChatbotInitialized) return;

            showGreeting();
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
        public void onActionStarted() {

        }

        @Override
        public void onActionCompleted(Map<String, Object> resultMap) {
            try {

                String inferredText = (String) resultMap.get(SpeechToTextProvider.STT_INFERRED_TEXT);
                addMessageToRecyclerView(new Message(inferredText, Sender.USER));
                String ans;
                if(!hasConvEnded) {
                    ans = chatbot.getResponse(inferredText);
                }
                else {
                    if(inferredText.contains("5")) {
                        ans = "Thanks! Enjoy your day!";
                        hasConvEnded = false;
                    }
                    else {
                        ans = "Please give us a rating from 1 to 5.";
                    }
                }
                addMessageToRecyclerView(new Message(ans, Sender.SYSTEM));

                textToSpeechProvider.speak(ans);
                runOnUiThread(() -> new Handler().postDelayed(() ->speechToTextProvider.startListening(), 500));


            }
            catch (Exception e) {
                Log.e("Error", e.getLocalizedMessage());
            }

        }

        @Override
        public void onActionFailed() {
            runOnUiThread(() -> new Handler().postDelayed(() ->speechToTextProvider.startListening(), 500));
        }
    }

    private class ChatbotInitializationListener implements OnInitListenerCallback {

        @Override
        public void onInitialized() {
            hasChatbotInitialized = true;

            if (!hasTtsInitialized) return;

            showGreeting();
        }
    }
}
