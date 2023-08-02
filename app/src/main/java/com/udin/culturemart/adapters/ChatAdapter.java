package com.udin.culturemart.adapters;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.udin.culturemart.R;
import com.udin.culturemart.activities.MessageActivity;
import com.udin.culturemart.models.ChatModel;
import com.udin.culturemart.models.UserModel;

import java.util.ArrayList;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatHolder> {
    ArrayList<ChatModel> chatList;

    public ChatAdapter(ArrayList<ChatModel> chatList) {
        this.chatList = chatList;
    }

    @NonNull
    @Override
    public ChatAdapter.ChatHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.item_chat, parent, false);

        return new ChatHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatAdapter.ChatHolder holder, int position) {
        String fullname = chatList.get(position).getWith().getFullname();
        String lastChat = chatList.get(position).getLastChat().getMessage();
        String chatId = chatList.get(position).getId();
        UserModel chatWith = chatList.get(position).getWith();

        holder.tvFullName.setText(fullname);
        holder.tvLastChat.setText(lastChat);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), MessageActivity.class);
                intent.putExtra("chat_id", chatId);

                Gson gson = new Gson();
                String userJson = gson.toJson(chatWith);
                intent.putExtra("chat_with", userJson);

                view.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return chatList != null ? chatList.size() : 0;
    }

    public static class ChatHolder extends RecyclerView.ViewHolder {

        TextView tvFullName, tvLastChat;

        public ChatHolder(@NonNull View itemView) {
            super(itemView);

            tvFullName = itemView.findViewById(R.id.chat_fullname);
            tvLastChat = itemView.findViewById(R.id.chat_last);
        }
    }
}
