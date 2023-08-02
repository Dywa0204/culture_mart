package com.udin.culturemart.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.udin.culturemart.R;
import com.udin.culturemart.adapters.ChatAdapter;
import com.udin.culturemart.models.UserModel;

public class ChatActivity extends AppCompatActivity {

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    ChatAdapter chatAdapter;
    RecyclerView chatContainer;
    RecyclerView.LayoutManager layoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Gson gson = new Gson();
        String userJson = getIntent().getStringExtra("user");

        UserModel user = gson.fromJson(userJson, UserModel.class);

        chatAdapter = new ChatAdapter(user.getChatList());
        chatContainer = findViewById(R.id.chat_container);
        layoutManager = new LinearLayoutManager(getBaseContext(), LinearLayoutManager.VERTICAL, false);
        chatContainer.setLayoutManager(layoutManager);
        chatContainer.setAdapter(chatAdapter);

        Log.d("WKWKWK", userJson);
    }
}