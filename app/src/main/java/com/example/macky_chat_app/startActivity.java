package com.example.macky_chat_app;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.INTERNET;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class startActivity extends AppCompatActivity {

    private Button newaccount_btn,login_btn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        ActivityCompat.requestPermissions(this,new String[]{READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE, INTERNET,CAMERA}, PackageManager.PERMISSION_GRANTED);
        newaccount_btn = findViewById(R.id.newaccount_btn);
        login_btn = findViewById(R.id.login_btn);

        newaccount_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent new_account = new Intent(startActivity.this,registerActivity.class);
                startActivity(new_account);
            }
        });
        login_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent login_account = new Intent(startActivity.this,loginActivity.class);
                startActivity(login_account);
            }
        });
    }
}
