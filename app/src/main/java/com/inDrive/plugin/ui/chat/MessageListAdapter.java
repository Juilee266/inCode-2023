package com.inDrive.plugin.ui.chat;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.inDrive.plugin.ui.chat.model.Message;
import com.inDrive.plugin.ui.chat.model.Sender;
import com.inDrive.plugin.voice.R;

import java.util.List;

public class MessageListAdapter extends RecyclerView.Adapter {
    private Context context;

    private List<Message> messages;

    public MessageListAdapter(Context context, List<Message> messages) {
        this.context = context;
        this.messages = messages;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;

        if (viewType == Sender.USER.getValue()) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.user_message, parent, false);
            return new UserMessageHolder(view);
        } else if (viewType == Sender.SYSTEM.getValue()) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.system_message, parent, false);
            return new SystemMessageHolder(view);
        }

        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = this.messages.get(position);

        int viewType = holder.getItemViewType();

        if (viewType == Sender.USER.getValue())
            ((UserMessageHolder)holder).bind(message);
        else if (viewType == Sender.SYSTEM.getValue())
            ((SystemMessageHolder)holder).bind(message);
    }

    @Override
    public int getItemCount() {
        return this.messages.size();
    }

    @Override
    public int getItemViewType(int position) {
        Message message = this.messages.get(position);
        return message.getSender().getValue();
    }

    public void addMessage(Message message) {
        if (message == null || message.getText() == null || message.getText().length() == 0) return;

        messages.add(message);
        new Thread(() -> notifyDataSetChanged());
    }

    private class UserMessageHolder extends RecyclerView.ViewHolder {
        private TextView messageText;

        public UserMessageHolder(View itemView) {
            super(itemView);
            messageText = (TextView) itemView.findViewById(R.id.text_user_message);
        }

        public void bind(Message message) {
            messageText.setText(message.getText());
        }
    }

    private class SystemMessageHolder extends RecyclerView.ViewHolder {
        private TextView messageText;

        public SystemMessageHolder(View itemView) {
            super(itemView);
            messageText = (TextView) itemView.findViewById(R.id.text_system_message);
        }

        public void bind(Message message) {
            messageText.setText(message.getText());
        }
    }
}
