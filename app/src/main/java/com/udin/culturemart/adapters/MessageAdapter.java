package com.udin.culturemart.adapters;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.udin.culturemart.R;
import com.udin.culturemart.models.MessageModel;
import com.udin.culturemart.models.UserModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageHolder>{
    ArrayList<MessageModel> messageList;
    UserModel chatWith;

    public MessageAdapter(ArrayList<MessageModel> messageList, UserModel chatWith) {
        this.messageList = messageList;
        this.chatWith = chatWith;
    }

    @NonNull
    @Override
    public MessageAdapter.MessageHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.item_message, parent, false);

        return new MessageAdapter.MessageHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageAdapter.MessageHolder holder, int position) {
        String messageText = messageList.get(position).getMessage();
        String messageFrom = messageList.get(position).getFrom();
        Date messageDate = new Date(messageList.get(position).getDatetime().getSeconds() * 1000);

        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat time = new SimpleDateFormat("HH:mm", Locale.getDefault());
        String timeStr = time.format(messageDate);

        if (messageFrom.equals(chatWith.getId().replaceAll(" ", ""))) {
            holder.tvMessageDatetime.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
            holder.tvMessageText.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
            holder.messageFromYou.setVisibility(View.VISIBLE);
            holder.messageFromMe.setVisibility(View.GONE);
            holder.messageLayout.setBackgroundColor(holder.itemView.getResources().getColor(R.color.white));
        }

        holder.tvMessageDatetime.setText(timeStr);
        holder.tvMessageText.setText(messageText);
    }

    @Override
    public int getItemCount() {
        return messageList != null ? messageList.size() : 0;
    }

    public static class MessageHolder extends RecyclerView.ViewHolder {
        TextView tvMessageText, tvMessageDatetime;
        LinearLayout messageFromMe, messageFromYou, messageLayout;
        public MessageHolder(@NonNull View itemView) {
            super(itemView);

            tvMessageText = itemView.findViewById(R.id.message_text);
            messageFromMe = itemView.findViewById(R.id.message_from_me);
            messageFromYou = itemView.findViewById(R.id.message_from_you);
            tvMessageDatetime = itemView.findViewById(R.id.message_datetime);
            messageLayout = itemView.findViewById(R.id.message_layout);
        }
    }
}
