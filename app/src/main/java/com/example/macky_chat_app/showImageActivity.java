package com.example.macky_chat_app;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.widget.Toast;

import java.io.File;

public class showImageActivity extends AppCompatActivity {

    private String message_id,type;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_image);

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        Uri uri = Uri.parse(getIntent().getStringExtra("file_uri"));
        message_id = getIntent().getStringExtra("message_id");
        type = getIntent().getStringExtra("type");
        if(type.equals("file")) {
            openFile(message_id,uri);
        }else if(type.equals("image")){
            openImage(uri);
        }
    }

    private void openImage(Uri uri) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "image/jpeg");
            finish();
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "No application found which can open the file", Toast.LENGTH_SHORT).show();
        }
    }

    private void openFile(String message_id,Uri uri) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            if (message_id.contains(".doc") || message_id.contains(".docx")) {
                // Word document
                intent.setDataAndType(uri, "application/msword");
                finish();
            } else if (message_id.contains(".pdf")) {
                // PDF file
                intent.setDataAndType(uri, "application/pdf");
                finish();
            } else if (message_id.contains(".ppt") || message_id.contains(".pptx")) {
                // Powerpoint file
                intent.setDataAndType(uri, "application/vnd.ms-powerpoint");
                finish();
            } else if (message_id.contains(".xls") || message_id.contains(".xlsx")) {
                // Excel file
                intent.setDataAndType(uri, "application/vnd.ms-excel");
                finish();
            } else if (message_id.contains(".zip")) {
                // ZIP file
                intent.setDataAndType(uri, "application/zip");
                finish();
            } else if (message_id.contains(".rar")){
                // RAR file
                intent.setDataAndType(uri, "application/x-rar-compressed");
                finish();
            } else if (message_id.contains(".rtf")) {
                // RTF file
                intent.setDataAndType(uri, "application/rtf");
                finish();
            } else if (message_id.contains(".wav") || message_id.contains(".mp3")) {
                // WAV audio file
                intent.setDataAndType(uri, "audio/x-wav");
                finish();
            } else if (message_id.contains(".gif")) {
                // GIF file
                intent.setDataAndType(uri, "image/gif");
                finish();
            } else if (message_id.contains(".jpg") || message_id.contains(".jpeg") || message_id.contains(".png")) {
                // JPG file
                intent.setDataAndType(uri, "image/jpeg");
                finish();
            } else if (message_id.contains(".txt")) {
                // Text file
                intent.setDataAndType(uri, "text/plain");
                finish();
            } else if (message_id.contains(".3gp") || message_id.contains(".mpg") ||
                    message_id.contains(".mpeg") || message_id.contains(".mpe") || message_id.contains(".mp4") || message_id.contains(".avi")) {
                // Video files
                intent.setDataAndType(uri, "video/*");
                finish();
            } else {
                intent.setDataAndType(uri, "*/*");
                finish();
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "No application found which can open the file", Toast.LENGTH_SHORT).show();
        }
    }
}