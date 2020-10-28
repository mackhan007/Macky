package com.example.macky_chat_app;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class audioCallReceivingActivity extends AppCompatActivity {

    private TextView audio_receiving_name_tv,audio_receiving_connection_type_tv,audio_receiving_mode_tv;
    private CircleImageView audio_receiving_user_img;
    private ImageView audio_receiving_pickup_call_img,audio_receiving_end_call_img;
    private String chat_user_id,chat_user_name,chat_user_img,call_channel_id,current_user_id,endCallStatus,type;
    private DatabaseReference root_database;
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_call_receiving);
        chat_user_id = getIntent().getStringExtra("chat_user_id");
        call_channel_id = getIntent().getStringExtra("call_channel_id");
        type = getIntent().getStringExtra("type");
        mAuth = FirebaseAuth.getInstance();
        current_user_id = mAuth.getCurrentUser().getUid();
        audio_receiving_name_tv = findViewById(R.id.audio_receiving_name_tv);
        audio_receiving_connection_type_tv = findViewById(R.id.audio_receiving_connection_type_tv);
        audio_receiving_user_img = findViewById(R.id.audio_receiving_user_img);
        audio_receiving_pickup_call_img = findViewById(R.id.audio_receiving_pickup_call_img);
        audio_receiving_end_call_img = findViewById(R.id.audio_receiving_end_call_img);
        audio_receiving_mode_tv = findViewById(R.id.audio_receiving_mode_tv);
        audio_receiving_mode_tv.setText("Calling Mode : "+type);
        root_database = FirebaseDatabase.getInstance().getReference();
        root_database.child("Users").child(chat_user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                chat_user_name = dataSnapshot.child("name").getValue().toString();
                chat_user_img = dataSnapshot.child("image").getValue().toString();
                endCallStatus = dataSnapshot.child("calling").getValue().toString();
                audio_receiving_name_tv.setText(chat_user_name);
                if(!chat_user_img.equals("default")){
                    Picasso.get().load(chat_user_img).placeholder(R.drawable.dp).into(audio_receiving_user_img);
                }
                if(endCallStatus.equals("none")) {
                    Intent mainpage = new Intent(audioCallReceivingActivity.this, MainActivity.class);
                    mainpage.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(mainpage);
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
        audio_receiving_pickup_call_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(type.equals("audio")) {
                    Intent call_picked = new Intent(audioCallReceivingActivity.this, audioCallCallingActivity.class);
                    call_picked.putExtra("chat_user_id", chat_user_id);
                    call_picked.putExtra("call_channel_id", call_channel_id);
                    Toast.makeText(audioCallReceivingActivity.this, "Call Started", Toast.LENGTH_SHORT).show();
                    startActivity(call_picked);
                    finish();
                }else if(type.equals("video")){
                    Intent call_picked = new Intent(audioCallReceivingActivity.this, videoCallActivity.class);
                    call_picked.putExtra("chat_user_id", chat_user_id);
                    call_picked.putExtra("call_channel_id", call_channel_id);
                    Toast.makeText(audioCallReceivingActivity.this, "Call Started", Toast.LENGTH_SHORT).show();
                    startActivity(call_picked);
                    finish();
                }else {
                    Toast.makeText(audioCallReceivingActivity.this,"Unable to detect the calling mode",Toast.LENGTH_SHORT).show();
                }
            }
        });
        audio_receiving_end_call_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String current_user_ref = "Users/" + current_user_id;
                final String chat_user_ref = "Users/" + chat_user_id;
                Map calling_update = new HashMap();
                calling_update.put(current_user_ref + "/calling", "none");
                calling_update.put(chat_user_ref + "/calling", "none");
                calling_update.put(current_user_ref + "/calledBy", "none");
                calling_update.put(chat_user_ref + "/calledBy", "none");
                calling_update.put(current_user_ref + "/type", "none");
                calling_update.put(chat_user_ref + "/type", "none");
                calling_update.put(current_user_ref + "/call_channel_id","none");
                calling_update.put(chat_user_ref + "/call_channel_id","none");
                FirebaseDatabase.getInstance().getReference().updateChildren(calling_update, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                        if(databaseError == null){
                            Toast.makeText(audioCallReceivingActivity.this,"Call Ended",Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }
                });
                Intent mainpage = new Intent(audioCallReceivingActivity.this, MainActivity.class);
                mainpage.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(mainpage);
                finish();
            }
        });
    }
}