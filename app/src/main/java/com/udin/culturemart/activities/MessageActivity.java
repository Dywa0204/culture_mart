package com.udin.culturemart.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.gson.Gson;
import com.udin.culturemart.R;
import com.udin.culturemart.adapters.MessageAdapter;
import com.udin.culturemart.models.MessageModel;
import com.udin.culturemart.models.UserModel;

import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.Objects;

public class MessageActivity extends AppCompatActivity {

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    String chatId;

    UserModel chatWith;

    TextView tvMessageLoading, tvMessageFullname;
    TextInputEditText messageField;
    ImageButton messageBackButton, messageSend;

    MessageAdapter messageAdapter;
    RecyclerView messageContainer;
    RecyclerView.LayoutManager layoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        tvMessageLoading = findViewById(R.id.message_loading);
        tvMessageFullname = findViewById(R.id.message_fullname);
        messageBackButton = findViewById(R.id.message_back_button);
        messageSend = findViewById(R.id.message_send);
        messageField = findViewById(R.id.message_field);

        chatId = getIntent().getStringExtra("chat_id");

        Gson gson = new Gson();
        String chatWithJson = getIntent().getStringExtra("chat_with");
        chatWith = gson.fromJson(chatWithJson, UserModel.class);

        tvMessageFullname.setText(chatWith.getFullname());

        db.collection("chats")
                .document(chatId)
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                        ArrayList<MessageModel> messageList = new ArrayList<>();
                        ArrayList<Object> messageListTemp = (ArrayList<Object>) value.getData().get("messages");
                        assert messageListTemp != null;
                        for (Object message: messageListTemp) {
                            Map<String, Object> messageMap = (Map<String, Object>) message;
                            MessageModel mm = new MessageModel(
                                    (Timestamp) messageMap.get("datetime"),
                                    (String) messageMap.get("from"),
                                    (String) messageMap.get("message")
                            );

                            messageList.add(mm);
                        }

                        tvMessageLoading.setVisibility(View.GONE);

                        messageAdapter = new MessageAdapter(messageList, chatWith);
                        messageContainer = findViewById(R.id.message_container);
                        layoutManager = new LinearLayoutManager(getBaseContext(), LinearLayoutManager.VERTICAL, false);
                        messageContainer.setLayoutManager(layoutManager);
                        messageContainer.setAdapter(messageAdapter);
                    }
                });

        messageBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        messageSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
            }
        });
    }

    private void sendMessage() {
        Date date = new Date();
        Timestamp datetime = new Timestamp(date);

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        String from = sharedPref.getString("user_id", "");

        String text = Objects.requireNonNull(messageField.getText()).toString();

        MessageModel message = new MessageModel(datetime, from, text);

        db.collection("chats")
                .document(chatId)
                .update("messages", FieldValue.arrayUnion(message))
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        messageField.setText("");
                        messageField.clearFocus();

                        int chatSize = messageAdapter.getItemCount();
                        messageContainer.scrollToPosition(chatSize - 1);
                    }
                });
    }
}