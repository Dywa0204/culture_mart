package com.udin.culturemart;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.udin.culturemart.activities.HomeActivity;
import com.udin.culturemart.activities.LoginActivity;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    boolean isTokenExist = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkToken();
    }

    private void checkToken() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        String token = sharedPref.getString("user_id", "");

        if (!token.isEmpty()) isTokenExist = true;

        ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
        service.schedule(this::stopSplashScreen, 3, TimeUnit.SECONDS);
    }

    private void stopSplashScreen() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        if (isTokenExist) intent = new Intent(MainActivity.this, HomeActivity.class);

        startActivity(intent);
        finish();
    }
}