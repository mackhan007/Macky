package com.example.macky_chat_app;

import android.content.res.AssetManager;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import java.util.List;

public class testActivity extends AppCompatActivity {

    private EditText chat_send_text_et2;
    private ConstraintLayout stickerPanel;
    private ImageView testcross,sticker_img;
    private ListView stickerTabList,stickerPanelList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);


        chat_send_text_et2 = findViewById(R.id.chat_send_text_et2);
        stickerPanel = findViewById(R.id.stickerPanel);
        testcross = findViewById(R.id.testcross);
        sticker_img = findViewById(R.id.sticker_img);
        stickerTabList = findViewById(R.id.stickerTabList);

        sticker_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stickerPanel.setVisibility(View.VISIBLE);
            }
        });

        testcross.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stickerPanel.setVisibility(View.GONE);
            }
        });
    }
}


