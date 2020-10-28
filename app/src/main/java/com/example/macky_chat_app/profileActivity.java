package com.example.macky_chat_app;

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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class profileActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private CircleImageView profile_dp_img;
    private TextView profile_name_tv,profile_status_tv,porfile_total_friends_tv;
    private Button send_request_btn,decline_request_btn,change_profile_btn;
    private DatabaseReference user_database,friend_request_database,friend_database,notification_database,current_user_databse;
    private FirebaseUser current_user;
    private String current_state = "not_friends";
    private String user_name,user_status;
    private boolean own_profile = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final String user_id = getIntent().getStringExtra("from_user_id");
        setContentView(R.layout.activity_profile);
        toolbar = findViewById(R.id.profile_appbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        profile_dp_img = findViewById(R.id.profile_dp_img);
        profile_name_tv = findViewById(R.id.profile_name_tv);
        profile_status_tv = findViewById(R.id.profile_status_tv);
        porfile_total_friends_tv = findViewById(R.id.profile_total_friends_tv);
        send_request_btn = findViewById(R.id.request_btn);
        decline_request_btn = findViewById(R.id.decline_request_btn);
        change_profile_btn = findViewById(R.id.change_profile_btn);
        user_database = FirebaseDatabase.getInstance().getReference().child("Users").child(user_id);
        friend_request_database = FirebaseDatabase.getInstance().getReference().child("Friend_request");
        friend_database = FirebaseDatabase.getInstance().getReference().child("Friends");
        notification_database = FirebaseDatabase.getInstance().getReference().child("notifications");
        current_user = FirebaseAuth.getInstance().getCurrentUser();
        current_user_databse = FirebaseDatabase.getInstance().getReference().child("Users").child(current_user.getUid());


        user_database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String name, status, image, thumb_image;
                name = dataSnapshot.child("name").getValue().toString();
                status = dataSnapshot.child("status").getValue().toString();
                image = dataSnapshot.child("image").getValue().toString();
                thumb_image = dataSnapshot.child("thumb_image").getValue().toString();
                profile_name_tv.setText(name);
                profile_status_tv.setText(status);
                if (!image.equals("default")) {
                    Picasso.get().load(image).placeholder(R.drawable.dp).into(profile_dp_img);
                }

                if(current_user.getUid().equals(user_id)){
                    own_profile = true;
                    decline_request_btn.setVisibility(View.GONE);
                    send_request_btn.setVisibility(View.GONE);
                    change_profile_btn.setVisibility(View.VISIBLE);
                    user_name = name;
                    user_status = status;
                }


                //check state
                friend_request_database.child(current_user.getUid()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.hasChild(user_id)){
                            String request_type = dataSnapshot.child(user_id).child("request_type").getValue().toString();
                            if(request_type.equals("sent")) {
                                current_state = "request_sent";
                                send_request_btn.setText("CANCEL FRIEND REQUEST");
                                decline_request_btn.setVisibility(View.INVISIBLE);
                            }else if(request_type.equals("received")){
                                current_state = "request_received";
                                send_request_btn.setText("ACCEPT FRIEND REQUEST");
                                decline_request_btn.setVisibility(View.VISIBLE);
                            }
                        }else {
                            friend_database.child(current_user.getUid()).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.hasChild(user_id)){
                                        current_state = "friends";
                                        send_request_btn.setText("UNFRIEND");
                                        decline_request_btn.setVisibility(View.INVISIBLE);
                                    }
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                }
                            });
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(profileActivity.this,"unable to connect to server",Toast.LENGTH_SHORT).show();
                    }
                });
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
        send_request_btn.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {

             //not friend

             if (current_state.equals("not_friends")) {
                 send_request_btn.setEnabled(false);
                 friend_request_database.child(current_user.getUid()).child(user_id).child("request_type")
                         .setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>() {
                     @Override
                     public void onComplete(@NonNull Task<Void> task) {
                         if (task.isSuccessful()) {
                             friend_request_database.child(user_id).child(current_user.getUid()).child("request_type")
                                     .setValue("received").addOnCompleteListener(new OnCompleteListener<Void>() {
                                 @Override
                                 public void onComplete(@NonNull Task<Void> task) {

                                     //Notification
                                     HashMap<String , String> notification = new HashMap<>();
                                     notification.put("from",current_user.getUid());
                                     notification.put("type","request");
                                     notification_database.child(user_id).push().setValue(notification).addOnCompleteListener(new OnCompleteListener<Void>() {
                                         @Override
                                         public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                current_state = "request_sent";
                                                send_request_btn.setText("CANCEL FRIEND REQUEST");
                                                Toast.makeText(profileActivity.this, "Friend request sent", Toast.LENGTH_SHORT).show();
                                            }
                                         }
                                     });
                                 }
                             });
                         } else {
                             Toast.makeText(profileActivity.this, "unable to send request", Toast.LENGTH_SHORT).show();
                         }
                         send_request_btn.setEnabled(true);
                     }
                 });
             }

             //cancel request

             if (current_state.equals("request_sent")) {
                 send_request_btn.setEnabled(false);
                 friend_request_database.child(current_user.getUid()).child(user_id)
                         .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                     @Override
                     public void onComplete(@NonNull Task<Void> task) {
                         if (task.isSuccessful()) {
                             friend_request_database.child(user_id).child(current_user.getUid())
                                     .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                 @Override
                                 public void onComplete(@NonNull Task<Void> task) {
                                     if (task.isSuccessful()) {
                                         //notification_database.child(user_id)
                                         current_state = "not_friends";
                                         send_request_btn.setText("SEND FRIEND REQUEST");
                                         Toast.makeText(profileActivity.this, "CANCELLED FRIEND REQUEST", Toast.LENGTH_SHORT).show();
                                     } else {
                                         Toast.makeText(profileActivity.this, "unable to cancel friend request,please try again later", Toast.LENGTH_SHORT).show();
                                     }
                                     send_request_btn.setEnabled(true);
                                 }
                             });
                         }
                     }
                 });
             }

             //accept friend request

             if(current_state.equals("request_received")){
                 send_request_btn.setEnabled(false);
                 final String current_date = DateFormat.getDateInstance().format(new Date());
                 friend_database.child(current_user.getUid()).child(user_id).child("date").setValue(current_date)
                         .addOnCompleteListener(new OnCompleteListener<Void>() {
                     @Override
                     public void onComplete(@NonNull Task<Void> task) {
                         if(task.isSuccessful()){
                             friend_database.child(user_id).child(current_user.getUid()).child("date").setValue(current_date)
                                     .addOnCompleteListener(new OnCompleteListener<Void>() {
                                         @Override
                                         public void onComplete(@NonNull Task<Void> task) {
                                             if(task.isSuccessful()){
                                                 friend_request_database.child(current_user.getUid()).child(user_id)
                                                         .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                     @Override
                                                     public void onComplete(@NonNull Task<Void> task) {
                                                         if (task.isSuccessful()) {
                                                             friend_request_database.child(user_id).child(current_user.getUid())
                                                                     .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                 @Override
                                                                 public void onComplete(@NonNull Task<Void> task) {
                                                                     if (task.isSuccessful()) {
                                                                         current_state = "friends";
                                                                         send_request_btn.setText("UNFRIEND");
                                                                         decline_request_btn.setVisibility(View.INVISIBLE);
                                                                         Toast.makeText(profileActivity.this,"Friend request accepted",Toast.LENGTH_SHORT).show();
                                                                     } else {
                                                                         Toast.makeText(profileActivity.this,"unable to accept friend request,please try again later",Toast.LENGTH_SHORT).show();
                                                                     }
                                                                     send_request_btn.setEnabled(true);
                                                                 }
                                                             });
                                                         }
                                                     }
                                                 });
                                             }else {
                                                 Toast.makeText(profileActivity.this,"unable to accept friend request,please try again later",Toast.LENGTH_SHORT).show();
                                             }
                                         }
                                     });
                         }
                     }
                 });
             }

             //unfriend state

             if(current_state.equals("friends")){
                 send_request_btn.setEnabled(false);
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
                                         current_state = "not_friends";
                                         send_request_btn.setText("SEND FRIEND REQUEST");
                                         Toast.makeText(profileActivity.this,"Unfriended",Toast.LENGTH_SHORT).show();
                                     }else {
                                         Toast.makeText(profileActivity.this,"unable to unfriend,please try again later",Toast.LENGTH_SHORT).show();
                                     }
                                     send_request_btn.setEnabled(true);
                                 }
                             });
                         }
                     }
                 });
             }
         }
     });
        decline_request_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                friend_request_database.child(current_user.getUid()).child(user_id)
                        .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            friend_request_database.child(user_id).child(current_user.getUid())
                                    .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        current_state = "not_friends";
                                        send_request_btn.setText("SEND FRIEND REQUEST");
                                        decline_request_btn.setVisibility(View.INVISIBLE);
                                        Toast.makeText(profileActivity.this, "DECLINED FRIEND REQUEST", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(profileActivity.this, "unable to decline friend request,please try again later", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                    }
                });
            }
        });
        change_profile_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent change_status = new Intent(profileActivity.this, statusActivity.class);
                change_status.putExtra("old_status", user_status);
                change_status.putExtra("name", user_name);
                startActivity(change_status);
            }
        });
    }
}
