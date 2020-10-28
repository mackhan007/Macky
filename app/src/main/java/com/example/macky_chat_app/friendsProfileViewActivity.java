package com.example.macky_chat_app;

import androidx.activity.OnBackPressedDispatcherOwner;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class friendsProfileViewActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private CircleImageView profile_view_dp_img;
    private TextView profile_view_name_tv,profile_view_status_tv,profile_view_total_friends_tv;
    private Button message_btn,unfriend_btn;
    private DatabaseReference user_database,current_user_databse,friend_database;
    private FirebaseUser current_user;
    private String name, status, image, thumb_image;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final String user_id = getIntent().getStringExtra("user_id");
        setContentView(R.layout.activity_friends_profile_view);
        toolbar = findViewById(R.id.profile_view_appbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        profile_view_dp_img = findViewById(R.id.profile_view_dp_img);
        profile_view_name_tv = findViewById(R.id.profile_view_name_tv);
        profile_view_status_tv = findViewById(R.id.profile_view_status_tv);
        profile_view_total_friends_tv = findViewById(R.id.profile_view_total_friends_tv);
        unfriend_btn = findViewById(R.id.unfriend_btn);
        message_btn = findViewById(R.id.message_btn);
        user_database = FirebaseDatabase.getInstance().getReference().child("Users").child(user_id);
        friend_database = FirebaseDatabase.getInstance().getReference().child("Friends");
        current_user = FirebaseAuth.getInstance().getCurrentUser();
        current_user_databse = FirebaseDatabase.getInstance().getReference().child("Users").child(current_user.getUid());

        user_database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                name = dataSnapshot.child("name").getValue().toString();
                status = dataSnapshot.child("status").getValue().toString();
                image = dataSnapshot.child("image").getValue().toString();
                thumb_image = dataSnapshot.child("thumb_image").getValue().toString();
                profile_view_name_tv.setText(name);
                profile_view_status_tv.setText(status);
                if (!image.equals("default")) {
                    Picasso.get().load(image).placeholder(R.drawable.dp).into(profile_view_dp_img);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
        message_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent chat_intent = new Intent(friendsProfileViewActivity.this,chatActivity.class);
                chat_intent.putExtra("user_id",user_id);
                chat_intent.putExtra("user_name",name);
                chat_intent.putExtra("user_thumb_image",thumb_image);
                startActivity(chat_intent);
            }
        });
        unfriend_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                unfriend_btn.setEnabled(false);
                friend_database.child(current_user.getUid()).child(user_id)
                        .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            friend_database.child(user_id).child(current_user.getUid())
                                    .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        startActivity(new Intent(friendsProfileViewActivity.this,MainActivity.class));
                                        finish();
                                        Toast.makeText(friendsProfileViewActivity.this,"Unfriended",Toast.LENGTH_SHORT).show();
                                    }else {
                                        Toast.makeText(friendsProfileViewActivity.this,"unable to unfriend,please try again later",Toast.LENGTH_SHORT).show();
                                    }
                                    unfriend_btn.setEnabled(true);
                                }
                            });
                        }
                    }
                });
            }
        });
    }
}
