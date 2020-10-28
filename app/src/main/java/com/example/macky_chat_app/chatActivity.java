package com.example.macky_chat_app;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class chatActivity extends AppCompatActivity {

    private String chat_user_id,chat_user_name,chat_user_thumb_image,current_user_id;
    private Toolbar toolbar;
    private CircleImageView chat_dp_img;
    private ImageView chat_add_img,chat_send_img,chat_bar_online_img,chat_bar_offline_img;
    private TextView chat_bar_name_tv,chat_bar_status_tv;
    private EditText chat_send_text_et;
    private DatabaseReference root_database;
    private FirebaseAuth mAuth;
    private RecyclerView chat_message_rv;
    private SwipeRefreshLayout message_swiperefreshlayout;
    private final List<messages> messagesList = new ArrayList<>();
    private LinearLayoutManager linearLayout;
    private messageAdapter messageAdapter;
    private static final int TOTAL_ITEMS_TO_LOAD = 12;
    private int currentposition = 0;
    private static final int GALLERY_PICK = 1,CAMERA_PICK = 2,FILE_PICK = 3;
    private String last_key = "";
    private String prev_key = "";
    private StorageReference image_storage;
    private LinearLayout text_typing_layout;
    private ConstraintLayout constraintLayout;
    private ImageView cancle_img,camera_img,gallery_img,file_img,chat_bar_audio_call_img;
    private int position = 0,last_position;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        chat_user_id = getIntent().getStringExtra("user_id");
        chat_user_name = getIntent().getStringExtra("user_name");
        chat_user_thumb_image = getIntent().getStringExtra("user_thumb_image");
        root_database = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        current_user_id = mAuth.getCurrentUser().getUid();
        toolbar = findViewById(R.id.chat_appbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view = inflater.inflate(R.layout.chat_appbar,null );
        View chatoptionbar = inflater.inflate(R.layout.chat_option_bar,null );
        actionBar.setCustomView(action_bar_view);
        chat_bar_name_tv = findViewById(R.id.chat_name_tv);
        chat_bar_status_tv = findViewById(R.id.chat_status_tv);
        chat_bar_audio_call_img = findViewById(R.id.chat_bar_audio_call_img);
        chat_dp_img = findViewById(R.id.chat_dp_img);
        chat_add_img = findViewById(R.id.chat_add_img);
        chat_send_img = findViewById(R.id.chat_send_img);
        chat_bar_online_img = findViewById(R.id.chat_bar_online_img);
        chat_bar_offline_img = findViewById(R.id.chat_bar_offline_img);
        chat_send_text_et = findViewById(R.id.chat_send_text_et);
        messageAdapter = new messageAdapter(messagesList,getApplicationContext(),actionBar,chatoptionbar,action_bar_view);
        chat_message_rv = findViewById(R.id.chat_message_rv);
        message_swiperefreshlayout = findViewById(R.id.swipe_message_layout);
        text_typing_layout = findViewById(R.id.text_typing_layout);
        constraintLayout = findViewById(R.id.constrain_layout);
        cancle_img = findViewById(R.id.chat_cancle_img);
        file_img = findViewById(R.id.chat_file_img);
        gallery_img = findViewById(R.id.chat_gallery_img);
        camera_img = findViewById(R.id.chat_camera_img);
        linearLayout = new LinearLayoutManager(this);
        linearLayout.setStackFromEnd(true);
        chat_message_rv.setHasFixedSize(true);
        chat_message_rv.setLayoutManager(linearLayout);
        chat_message_rv.setAdapter(messageAdapter);
        loadmessages();
        last_position = messageAdapter.getItemCount();
        chat_bar_online_img.setVisibility(View.INVISIBLE);
        chat_bar_offline_img.setVisibility(View.VISIBLE);
        chat_bar_name_tv.setText(chat_user_name);
        ImageView test = findViewById(R.id.chat_dp_img);

        chat_bar_audio_call_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog alertDialog = new AlertDialog.Builder(chatActivity.this).setIcon(R.mipmap.answer_call_img)
                        .setTitle("Calling Mode").setMessage("Select the calling mode").setPositiveButton("Audio Call", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                root_database.child("Users").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.child(current_user_id).hasChild("calling") && dataSnapshot.child(chat_user_id).hasChild("calling")) {
                                            String current_user_calling_status,chat_user_calling_status;
                                            current_user_calling_status = dataSnapshot.child(current_user_id).child("calling").getValue().toString();
                                            chat_user_calling_status = dataSnapshot.child(chat_user_id).child("calling").getValue().toString();
                                            if(current_user_calling_status.equals("none") && chat_user_calling_status.equals("none")){
                                                DatabaseReference user_calling_push = FirebaseDatabase.getInstance().getReference().child("Calling").child(current_user_id).push();
                                                String push_id = user_calling_push.getKey();
                                                final String current_user_ref = "Users/" + current_user_id;
                                                final String chat_user_ref = "Users/" + chat_user_id;
                                                Map calling_update = new HashMap();
                                                calling_update.put(current_user_ref + "/calling", chat_user_id);
                                                calling_update.put(chat_user_ref + "/calling", current_user_id);
                                                calling_update.put(chat_user_ref + "/calledBy", current_user_id);
                                                calling_update.put(current_user_ref + "/type", "audio");
                                                calling_update.put(chat_user_ref + "/type", "audio");
                                                calling_update.put(current_user_ref + "/call_channel_id",push_id);
                                                calling_update.put(chat_user_ref + "/call_channel_id",push_id);
                                                FirebaseDatabase.getInstance().getReference().updateChildren(calling_update, new DatabaseReference.CompletionListener() {
                                                    @Override
                                                    public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                                        if (databaseError == null) {
                                                            Toast.makeText(chatActivity.this, "Calling", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                });

                                                final String current_user_calling = "Calling/" + current_user_id + "/" + push_id;
                                                final String chat_user_calling = "Calling/" + chat_user_id + "/" + push_id;
                                                Map calling_details = new HashMap();
                                                calling_details.put(current_user_calling + "/from", current_user_id);
                                                calling_details.put(current_user_calling + "/timestamp_started", ServerValue.TIMESTAMP);
                                                calling_details.put(current_user_calling + "/type", "audio");
                                                calling_details.put(chat_user_calling + "/from", current_user_id);
                                                calling_details.put(chat_user_calling + "/timestamp_started", ServerValue.TIMESTAMP);
                                                calling_details.put(chat_user_calling + "/type", "audio");
                                                FirebaseDatabase.getInstance().getReference().updateChildren(calling_details, new DatabaseReference.CompletionListener() {
                                                    @Override
                                                    public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                                        if (databaseError == null) {
                                                            Toast.makeText(chatActivity.this, "Details logged", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                });

                                                Intent call_intent = new Intent(chatActivity.this,audioCallCallingActivity.class);
                                                call_intent.putExtra("chat_user_id",chat_user_id);
                                                call_intent.putExtra("call_channel_id",push_id);
                                                Toast.makeText(chatActivity.this,"Audio Calling",Toast.LENGTH_SHORT).show();
                                                startActivity(call_intent);
                                            }else {
                                                Toast.makeText(chatActivity.this,chat_user_name+"is on another call,please try again later",Toast.LENGTH_SHORT).show();
                                            }
                                        }else {
                                            DatabaseReference user_calling_push = FirebaseDatabase.getInstance().getReference().child("Calling").child(current_user_id).push();
                                            String push_id = user_calling_push.getKey();
                                            final String current_user_ref = "Users/" + current_user_id;
                                            final String chat_user_ref = "Users/" + chat_user_id;
                                            Map calling_update = new HashMap();
                                            calling_update.put(current_user_ref + "/calling", chat_user_id);
                                            calling_update.put(chat_user_ref + "/calling", current_user_id);
                                            calling_update.put(chat_user_ref + "/calledBy", current_user_id);
                                            calling_update.put(current_user_ref + "/type", "audio");
                                            calling_update.put(chat_user_ref + "/type", "audio");
                                            calling_update.put(current_user_ref + "/call_channel_id",push_id);
                                            calling_update.put(chat_user_ref + "/call_channel_id",push_id);
                                            FirebaseDatabase.getInstance().getReference().updateChildren(calling_update, new DatabaseReference.CompletionListener() {
                                                @Override
                                                public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                                    if (databaseError == null) {
                                                        Toast.makeText(chatActivity.this, "Calling", Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });

                                            final String current_user_calling = "Calling/" + current_user_id + "/" + push_id;
                                            final String chat_user_calling = "Calling/" + chat_user_id + "/" + push_id;
                                            Map calling_details = new HashMap();
                                            calling_details.put(current_user_calling + "/from", current_user_id);
                                            calling_details.put(current_user_calling + "/timestamp_started", ServerValue.TIMESTAMP);
                                            calling_details.put(current_user_calling + "/type", "audio");
                                            calling_details.put(chat_user_calling + "/from", current_user_id);
                                            calling_details.put(chat_user_calling + "/timestamp_started", ServerValue.TIMESTAMP);
                                            calling_details.put(chat_user_calling + "/type", "audio");
                                            FirebaseDatabase.getInstance().getReference().updateChildren(calling_details, new DatabaseReference.CompletionListener() {
                                                @Override
                                                public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                                    if (databaseError == null) {
                                                        Toast.makeText(chatActivity.this, "Details logged", Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });

                                            Intent call_intent = new Intent(chatActivity.this,audioCallCallingActivity.class);
                                            call_intent.putExtra("chat_user_id",chat_user_id);
                                            call_intent.putExtra("call_channel_id",push_id);
                                            Toast.makeText(chatActivity.this,"Audio Calling",Toast.LENGTH_SHORT).show();
                                            startActivity(call_intent);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                    }
                                });
                            }
                        }).setNegativeButton("Video Call", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                root_database.child("Users").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.child(current_user_id).hasChild("calling") && dataSnapshot.child(chat_user_id).hasChild("calling")) {
                                            String current_user_calling_status,chat_user_calling_status;
                                            current_user_calling_status = dataSnapshot.child(current_user_id).child("calling").getValue().toString();
                                            chat_user_calling_status = dataSnapshot.child(chat_user_id).child("calling").getValue().toString();
                                            if(current_user_calling_status.equals("none") && chat_user_calling_status.equals("none")){
                                                DatabaseReference user_calling_push = FirebaseDatabase.getInstance().getReference().child("Calling").child(current_user_id).push();
                                                String push_id = user_calling_push.getKey();
                                                final String current_user_ref = "Users/" + current_user_id;
                                                final String chat_user_ref = "Users/" + chat_user_id;
                                                Map calling_update = new HashMap();
                                                calling_update.put(current_user_ref + "/calling", chat_user_id);
                                                calling_update.put(chat_user_ref + "/calling", current_user_id);
                                                calling_update.put(chat_user_ref + "/calledBy", current_user_id);
                                                calling_update.put(current_user_ref + "/type", "video");
                                                calling_update.put(chat_user_ref + "/type", "video");
                                                calling_update.put(current_user_ref + "/call_channel_id",push_id);
                                                calling_update.put(chat_user_ref + "/call_channel_id",push_id);
                                                FirebaseDatabase.getInstance().getReference().updateChildren(calling_update, new DatabaseReference.CompletionListener() {
                                                    @Override
                                                    public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                                        if (databaseError == null) {
                                                            Toast.makeText(chatActivity.this, "Calling", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                });

                                                final String current_user_calling = "Calling/" + current_user_id + "/" + push_id;
                                                final String chat_user_calling = "Calling/" + chat_user_id + "/" + push_id;
                                                Map calling_details = new HashMap();
                                                calling_details.put(current_user_calling + "/from", current_user_id);
                                                calling_details.put(current_user_calling + "/timestamp_started", ServerValue.TIMESTAMP);
                                                calling_details.put(current_user_calling + "/type", "video");
                                                calling_details.put(chat_user_calling + "/from", current_user_id);
                                                calling_details.put(chat_user_calling + "/timestamp_started", ServerValue.TIMESTAMP);
                                                calling_details.put(chat_user_calling + "/type", "video");
                                                FirebaseDatabase.getInstance().getReference().updateChildren(calling_details, new DatabaseReference.CompletionListener() {
                                                    @Override
                                                    public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                                        if (databaseError == null) {
                                                            Toast.makeText(chatActivity.this, "Details logged", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                });

                                                Intent call_intent = new Intent(chatActivity.this,videoCallActivity.class);
                                                call_intent.putExtra("chat_user_id",chat_user_id);
                                                call_intent.putExtra("call_channel_id",push_id);
                                                Toast.makeText(chatActivity.this,"Video Calling",Toast.LENGTH_SHORT).show();
                                                startActivity(call_intent);
                                            }else {
                                                Toast.makeText(chatActivity.this,chat_user_name+"is on another call,please try again later",Toast.LENGTH_SHORT).show();
                                            }
                                        }else {
                                            DatabaseReference user_calling_push = FirebaseDatabase.getInstance().getReference().child("Calling").child(current_user_id).push();
                                            String push_id = user_calling_push.getKey();
                                            final String current_user_ref = "Users/" + current_user_id;
                                            final String chat_user_ref = "Users/" + chat_user_id;
                                            Map calling_update = new HashMap();
                                            calling_update.put(current_user_ref + "/calling", chat_user_id);
                                            calling_update.put(chat_user_ref + "/calling", current_user_id);
                                            calling_update.put(chat_user_ref + "/calledBy", current_user_id);
                                            calling_update.put(current_user_ref + "/type", "video");
                                            calling_update.put(chat_user_ref + "/type", "video");
                                            calling_update.put(current_user_ref + "/call_channel_id",push_id);
                                            calling_update.put(chat_user_ref + "/call_channel_id",push_id);
                                            FirebaseDatabase.getInstance().getReference().updateChildren(calling_update, new DatabaseReference.CompletionListener() {
                                                @Override
                                                public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                                    if (databaseError == null) {
                                                        Toast.makeText(chatActivity.this, "Calling", Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });

                                            final String current_user_calling = "Calling/" + current_user_id + "/" + push_id;
                                            final String chat_user_calling = "Calling/" + chat_user_id + "/" + push_id;
                                            Map calling_details = new HashMap();
                                            calling_details.put(current_user_calling + "/from", current_user_id);
                                            calling_details.put(current_user_calling + "/timestamp_started", ServerValue.TIMESTAMP);
                                            calling_details.put(current_user_calling + "/type", "video");
                                            calling_details.put(chat_user_calling + "/from", current_user_id);
                                            calling_details.put(chat_user_calling + "/timestamp_started", ServerValue.TIMESTAMP);
                                            calling_details.put(chat_user_calling + "/type", "video");
                                            FirebaseDatabase.getInstance().getReference().updateChildren(calling_details, new DatabaseReference.CompletionListener() {
                                                @Override
                                                public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                                    if (databaseError == null) {
                                                        Toast.makeText(chatActivity.this, "Details logged", Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });

                                            Intent call_intent = new Intent(chatActivity.this,videoCallActivity.class);
                                            call_intent.putExtra("chat_user_id",chat_user_id);
                                            call_intent.putExtra("call_channel_id",push_id);
                                            Toast.makeText(chatActivity.this,"Video Calling",Toast.LENGTH_SHORT).show();
                                            startActivity(call_intent);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                    }
                                });
                            }
                        }).show();
            }
        });

        chat_send_text_et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(count!=0){
                    root_database.child("Chat").child(chat_user_id).child(current_user_id).child("typing").setValue(true);
                }else if(count == 0){
                    root_database.child("Chat").child(chat_user_id).child(current_user_id).child("typing").setValue(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse(chat_user_thumb_image);
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(uri, "image/jpeg");
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(chatActivity.this, "No application found which can open the file", Toast.LENGTH_SHORT).show();
                }
            }
        });

        action_bar_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent viewprofile = new Intent(chatActivity.this,friendsProfileViewActivity.class);
                viewprofile.putExtra("user_id",chat_user_id);
                startActivity(viewprofile);
            }
        });

        image_storage = FirebaseStorage.getInstance().getReference();
        root_database.child("Chat").child(current_user_id).child(chat_user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild("typing")){
                    Boolean typing = (Boolean) dataSnapshot.child("typing").getValue();
                    if(typing){
                        chat_bar_status_tv.setText("Typing");
                    }else {
                        root_database.child("Users").child(chat_user_id).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                final String online_status = dataSnapshot.child("online").getValue().toString();
                                if(online_status.equals("true")){
                                    chat_bar_status_tv.setText("Online");
                                    chat_bar_online_img.setVisibility(View.VISIBLE);
                                    chat_bar_offline_img.setVisibility(View.INVISIBLE);
                                }else {
                                    Handler handler = new Handler();
                                    handler.postDelayed(new Runnable() {
                                        public void run() {
                                            getTimeAgo getTimeAgo = new getTimeAgo();
                                            long last_time = Long.parseLong(online_status);
                                            String last_seen = getTimeAgo.getTimeAgo(last_time,getApplicationContext());
                                            chat_bar_status_tv.setText("Last Seen : "+last_seen);
                                            chat_bar_online_img.setVisibility(View.INVISIBLE);
                                            chat_bar_offline_img.setVisibility(View.VISIBLE);
                                        }
                                    }, 1000);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
        root_database.child("Users").child(chat_user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final String online_status = dataSnapshot.child("online").getValue().toString();
                if(online_status.equals("true")){
                    chat_bar_status_tv.setText("Online");
                    chat_bar_online_img.setVisibility(View.VISIBLE);
                    chat_bar_offline_img.setVisibility(View.INVISIBLE);
                }else {
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        public void run() {
                            getTimeAgo getTimeAgo = new getTimeAgo();
                            long last_time = Long.parseLong(online_status);
                            String last_seen = getTimeAgo.getTimeAgo(last_time,getApplicationContext());
                            chat_bar_status_tv.setText("Last Seen : "+last_seen);
                            chat_bar_online_img.setVisibility(View.INVISIBLE);
                            chat_bar_offline_img.setVisibility(View.VISIBLE);
                        }
                    }, 1000);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
        if (!chat_user_thumb_image.equals("default")) {
            Picasso.get().load(chat_user_thumb_image).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.dp).into(chat_dp_img, new Callback() {
                @Override
                public void onSuccess() {
                }
                @Override
                public void onError(Exception e) {
                    Picasso.get().load(chat_user_thumb_image).placeholder(R.drawable.dp).into(chat_dp_img);
                }
            });
        }
        root_database.child("Chat").child(current_user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!dataSnapshot.hasChild(chat_user_id)){
                    Map chatAddMap = new HashMap();
                    chatAddMap.put("timestamp", ServerValue.TIMESTAMP);
                    Map chatUserMap = new HashMap();
                    chatUserMap.put("Chat/"+current_user_id+"/"+chat_user_id,chatAddMap);
                    chatUserMap.put("Chat/"+chat_user_id+"/"+current_user_id,chatAddMap);
                    root_database.updateChildren(chatUserMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                            if(databaseError != null){
                                Log.d("CHAT_LOG",databaseError.getMessage().toString());
                            }
                        }
                    });
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
        chat_send_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
                chat_send_text_et.setText("");
            }
        });

        chat_add_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                constraintLayout.setVisibility(View.VISIBLE);
                chat_add_img.setVisibility(View.GONE);
                chat_send_text_et.setVisibility(View.GONE);
                chat_send_img.setVisibility(View.GONE);
            }
        });
        cancle_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                constraintLayout.setVisibility(View.GONE);
                chat_add_img.setVisibility(View.VISIBLE);
                chat_send_text_et.setVisibility(View.VISIBLE);
                chat_send_img.setVisibility(View.VISIBLE);
            }
        });
        gallery_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendImagebygallery();
            }
        });
        camera_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendImagebycamera();
            }
        });
        file_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendFile();
            }
        });

        message_swiperefreshlayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(messageAdapter.getItemCount() != last_position) {
                    position++;
                    currentposition = 0;
                    linearLayout.setStackFromEnd(false);
                    loadmoremessages();
                    last_position = messageAdapter.getItemCount();
                    linearLayout.setStackFromEnd(true);
                    linearLayout.smoothScrollToPosition(chat_message_rv,null,0);
                }else{
                    message_swiperefreshlayout.setEnabled(false);
                }
            }
        });
    }

    private void sendFile() {
        Intent file_intent = new Intent(Intent.ACTION_GET_CONTENT);
        file_intent.setType("*/*");
        file_intent.addCategory(Intent.CATEGORY_OPENABLE);
        try {
            startActivityForResult(Intent.createChooser(file_intent,"Select a file to upload"),FILE_PICK);
        }catch (android.content.ActivityNotFoundException e){
            Toast.makeText(chatActivity.this,"Please install File Manager",Toast.LENGTH_SHORT).show();
        }
    }

    private void sendImagebycamera() {
        Intent camera_intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(camera_intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(camera_intent, CAMERA_PICK);
        }else {
            Toast.makeText(chatActivity.this,"No camera found",Toast.LENGTH_SHORT).show();
        }
    }

    private void sendImagebygallery() {
        Intent gallery_intent = new Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        try {
            startActivityForResult(gallery_intent,GALLERY_PICK);
        }catch (android.content.ActivityNotFoundException e){
            Toast.makeText(chatActivity.this,"Please install Gallery",Toast.LENGTH_SHORT).show();
        }
        /*
        gallery_intent.setType("image/*");
        gallery_intent.setAction(Intent.ACTION_GET_CONTENT);
        try {
        startActivityForResult(Intent.createChooser(gallery_intent,"SELECT IMAGE"),GALLERY_PICK);
        }catch (android.content.ActivityNotFoundException e){
            Toast.makeText(chatActivity.this,"Please install File Manager",Toast.LENGTH_SHORT).show();
        }
        */
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == GALLERY_PICK && resultCode == RESULT_OK){
            Uri imageuri = data.getData();
            DatabaseReference user_message_push = root_database.child("messages").child(current_user_id).child(chat_user_id).push();
            final String push_id = user_message_push.getKey();
            StorageReference filepath = image_storage.child("message_images").child(push_id+".jpg");
            filepath.putFile(imageuri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if(task.isSuccessful()){
                        uploadImage(push_id);
                    }
                }
            });
        }else if(requestCode == FILE_PICK && resultCode == RESULT_OK){
            Uri fileuri = data.getData();
            String uristring = fileuri.toString();
            File myfile = new File(uristring);
            String displayName = null;
            if(uristring.startsWith("content://")){
                Cursor cursor = null;
                try {
                    cursor = chatActivity.this.getContentResolver().query(fileuri,null,null,null,null);
                    if(cursor != null && cursor.moveToFirst()){
                        displayName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                    }
                }finally {
                    cursor.close();
                }
            }else if(uristring.startsWith("file://")){
                displayName = myfile.getName();
            }
            DatabaseReference user_message_push = root_database.child("messages").child(current_user_id).child(chat_user_id).push();
            final String push_id = user_message_push.getKey();
            StorageReference filepath = image_storage.child("message_files").child(push_id+"_"+displayName);
            final String finalDisplayName = displayName;
            filepath.putFile(fileuri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if(task.isSuccessful()){
                        uploadFile(push_id, finalDisplayName);
                    }
                }
            });
        }else if(requestCode == CAMERA_PICK && resultCode == RESULT_OK){
            Bitmap pic = (Bitmap) data.getExtras().get("data");
            Uri imageuri = getImageUri(getApplicationContext(),pic);
            DatabaseReference user_message_push = root_database.child("messages").child(current_user_id).child(chat_user_id).push();
            final String push_id = user_message_push.getKey();
            StorageReference filepath = FirebaseStorage.getInstance().getReference().child("message_images").child(push_id+".jpg");
            filepath.putFile(imageuri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if(task.isSuccessful()){
                        Toast.makeText(chatActivity.this,"Image Captured",Toast.LENGTH_SHORT).show();
                        uploadImage(push_id);
                    }
                }
            });
        }

        constraintLayout.setVisibility(View.GONE);
        chat_add_img.setVisibility(View.VISIBLE);
        chat_send_text_et.setVisibility(View.VISIBLE);
        chat_send_img.setVisibility(View.VISIBLE);
    }

    private Uri getImageUri(Context applicationContext, Bitmap pic) {
        /*
        ByteArrayOutputStream byteArrayOutputStream= new ByteArrayOutputStream();
        pic.compress(Bitmap.CompressFormat.JPEG,100,byteArrayOutputStream);
         */
        Bitmap OutImage = Bitmap.createScaledBitmap(pic, 1000, 1000,true);
        String path = MediaStore.Images.Media.insertImage(applicationContext.getContentResolver(), OutImage, "Title", null);
        return Uri.parse(path);
    }

    private void uploadFile(final String push_id, final String displayName) {
        final String current_user_ref = "messages/"+current_user_id+"/"+chat_user_id;
        final String chat_user_ref = "messages/"+chat_user_id+"/"+current_user_id;
        final String currentTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
        final String currentDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
        final String[] file_url = new String[1];
        StorageReference filepath = image_storage.child("message_files").child(push_id+"_"+displayName);
        filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Uri download_url = uri;
                file_url[0] = download_url.toString();
                Map messageMap = new HashMap();
                messageMap.put("message",file_url[0]);
                messageMap.put("type","file");
                messageMap.put("time",currentTime);
                messageMap.put("date",currentDate);
                messageMap.put("from",current_user_id);
                messageMap.put("timestamp",ServerValue.TIMESTAMP);
                messageMap.put("push_id",push_id+"_"+displayName);
                Map messageuserMap = new HashMap();
                messageuserMap.put(current_user_ref+"/"+push_id,messageMap);
                messageuserMap.put(chat_user_ref+"/"+push_id,messageMap);
                root_database.updateChildren(messageuserMap, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                        if(databaseError != null){
                            Log.d("CHAT_LOG", databaseError.getMessage().toString());
                        }
                    }
                });
                Map notification_map = new HashMap();
                notification_map.put("from",current_user_id);
                notification_map.put("type","file");
                root_database.child("notifications").child(chat_user_id).child(push_id).updateChildren(notification_map, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                        if(databaseError != null){
                            Log.d("CHAT_LOG", databaseError.getMessage().toString());
                        }
                    }
                });
            }
        });
    }

    private void uploadImage(final String push_id) {
        final String current_user_ref = "messages/"+current_user_id+"/"+chat_user_id;
        final String chat_user_ref = "messages/"+chat_user_id+"/"+current_user_id;
        final String currentTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
        final String currentDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
        final String[] image_url = new String[1];
        StorageReference filepath = image_storage.child("message_images").child(push_id+".jpg");
        filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Uri download_url = uri;
                image_url[0] = download_url.toString();
                Map messageMap = new HashMap();
                messageMap.put("message",image_url[0]);
                messageMap.put("type","image");
                messageMap.put("time",currentTime);
                messageMap.put("date",currentDate);
                messageMap.put("from",current_user_id);
                messageMap.put("timestamp",ServerValue.TIMESTAMP);
                messageMap.put("push_id",push_id);
                Map messageuserMap = new HashMap();
                messageuserMap.put(current_user_ref+"/"+push_id,messageMap);
                messageuserMap.put(chat_user_ref+"/"+push_id,messageMap);
                root_database.updateChildren(messageuserMap, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                        if(databaseError != null){
                            Log.d("CHAT_LOG", databaseError.getMessage().toString());
                        }
                    }
                });
                Map notification_map = new HashMap();
                notification_map.put("from",current_user_id);
                notification_map.put("type","image");
                root_database.child("notifications").child(chat_user_id).child(push_id).updateChildren(notification_map, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                        if(databaseError != null){
                            Log.d("CHAT_LOG", databaseError.getMessage().toString());
                        }
                    }
                });
            }
        });
    }

    private void loadmoremessages() {
        DatabaseReference message_ref = root_database.child("messages").child(current_user_id).child(chat_user_id);
        Query messagequery = message_ref.orderByKey().endAt(last_key).limitToLast(TOTAL_ITEMS_TO_LOAD);
        messagequery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                String current_push_id = dataSnapshot.getKey();
                messages messages = dataSnapshot.getValue(messages.class).withId(current_push_id,current_user_id,chat_user_id);
                String message_key = dataSnapshot.getKey();
                if(!prev_key.equals(message_key)){
                    messagesList.add(currentposition++,messages);
                }else {
                    prev_key = last_key;
                }
                if(currentposition == 1){
                    last_key = dataSnapshot.getKey();
                }
                messageAdapter.notifyDataSetChanged();
                message_swiperefreshlayout.setRefreshing(false);
                if((messageAdapter.getItemCount()-(position*TOTAL_ITEMS_TO_LOAD)) >= TOTAL_ITEMS_TO_LOAD - 1) {
                    chat_message_rv.scrollToPosition(last_position);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void loadmessages() {
        DatabaseReference message_ref = root_database.child("messages").child(current_user_id).child(chat_user_id);
        message_ref.keepSynced(true);
        Query messagequery = message_ref;
        messagequery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                String current_push_id = dataSnapshot.getKey();
                messages messages = dataSnapshot.getValue(messages.class).withId(current_push_id,current_user_id,chat_user_id);
                currentposition++;
                if(currentposition == 1){
                    last_key = dataSnapshot.getKey();
                    prev_key = last_key;
                }
                messagesList.add(messages);
                messageAdapter.notifyDataSetChanged();
                chat_message_rv.scrollToPosition(messagesList.size() - 1);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void sendMessage() {
        String message = chat_send_text_et.getText().toString();
        if(!TextUtils.isEmpty(message)){
            String current_user_ref = "messages/"+current_user_id+"/"+chat_user_id;
            String chat_user_ref = "messages/"+chat_user_id+"/"+current_user_id;
            final String currentTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
            final String currentDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
            DatabaseReference user_message_push = root_database.child("messages").child(current_user_id).child(chat_user_id).push();
            String push_id = user_message_push.getKey();
            Map messageMap = new HashMap();
            messageMap.put("message",message);
            messageMap.put("type","text");
            messageMap.put("time",currentTime);
            messageMap.put("date",currentDate);
            messageMap.put("from",current_user_id);
            messageMap.put("timestamp",ServerValue.TIMESTAMP);
            messageMap.put("push_id",push_id);
            Map messageuserMap = new HashMap();
            messageuserMap.put(current_user_ref+"/"+push_id,messageMap);
            messageuserMap.put(chat_user_ref+"/"+push_id,messageMap);
            root_database.updateChildren(messageuserMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                    if(databaseError != null){
                        Log.d("CHAT_LOG", databaseError.getMessage().toString());
                    }
                }
            });
            Map notification_map = new HashMap();
            notification_map.put("from",current_user_id);
            notification_map.put("type","text");
            root_database.child("notifications").child(chat_user_id).child(push_id).updateChildren(notification_map, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                    if(databaseError != null){
                        Log.d("CHAT_LOG", databaseError.getMessage().toString());
                    }
                }
            });
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        DatabaseReference user_chatupadte;
        user_chatupadte = root_database.child("Chat").child(current_user_id).child(chat_user_id);
        user_chatupadte.child("timestamp").setValue(ServerValue.TIMESTAMP).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(!task.isSuccessful()){
                    Log.d("CHAT_LOG", task.getException().toString());
                }
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        DatabaseReference user_chatupadte;
        user_chatupadte = root_database.child("Chat").child(current_user_id).child(chat_user_id);
        user_chatupadte.child("timestamp").setValue(ServerValue.TIMESTAMP).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(!task.isSuccessful()){
                    Log.d("CHAT_LOG", task.getException().toString());
                }
            }
        });
        root_database.child("Chat").child(chat_user_id).child(current_user_id).child("typing").setValue(false);
    }
}
