package com.inDrive.plugin.voice;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.inDrive.plugin.common.ActionListenerCallback;
import com.inDrive.plugin.common.SpeechToTextProvider;
import com.inDrive.plugin.common.TextToSpeechProvider;
import com.inDrive.plugin.entities.Passenger;
import com.inDrive.plugin.navigation.NavigationProvider;
import com.inDrive.plugin.navigation.graphhopper.response.direction.DirectionResponse;
import com.inDrive.plugin.ui.chat.MessageListAdapter;
import com.inDrive.plugin.ui.chat.model.Message;
import com.inDrive.plugin.ui.chat.model.Sender;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "Activity Created.");

        checkMissingPermissions();

        recyclerView = (RecyclerView) findViewById(R.id.recycler_chat);
        messageListAdapter = new MessageListAdapter(this, new ArrayList<>());
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(messageListAdapter);

        Passenger p = new Passenger("JJ", "123456");
        try {
            chatbot = new Chatbot(this, p);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

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

    private void addMessageToRecyclerView(Message message) {
        if (message == null) return;

        messageListAdapter.addMessage(message);
        recyclerView.scrollToPosition(messageListAdapter.getItemCount() - 1);

    }

    private class TextToSpeechActionListener implements ActionListenerCallback {

        @Override
        public void onInitialized() {
            Thread thread = new Thread(() -> {
                try {
                    while (!chatbot.isInitialized()) Thread.sleep(100);
                } catch (Exception e) {}
            });
            thread.start();

            try {
                thread.join();
            } catch (Exception e) {}

            String text = "The system is initialized. Start talking after the beep. Please ensure your device is closer to your mouth for best experience.";
            addMessageToRecyclerView(new Message(text, Sender.SYSTEM));
            textToSpeechProvider.speak(text);

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
                String inferredText = (String) resultMap.get(SpeechToTextProvider.STT_INFERRED_TEXT);
                addMessageToRecyclerView(new Message(inferredText, Sender.USER));

                String ans = chatbot.getResponse(inferredText);
                addMessageToRecyclerView(new Message(ans, Sender.SYSTEM));

                textToSpeechProvider.speak(ans);
                new Handler().postDelayed(() ->speechToTextProvider.startListening(), 500);

            }
            catch (Exception e) {
                Log.e("Error", e.getLocalizedMessage());
            }

        }

        @Override
        public void onActionFailed() {
            /*String text = "Did you say something?";
            addMessageToRecyclerView(new Message(text, Sender.SYSTEM));

            textToSpeechProvider.speak(text);*/
            new Handler().postDelayed(() ->speechToTextProvider.startListening(), 500);
        }
    }
}
