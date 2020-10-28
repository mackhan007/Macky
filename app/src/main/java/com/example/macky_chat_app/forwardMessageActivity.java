package com.example.macky_chat_app;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class forwardMessageActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private RecyclerView friends_list_rv;
    private DatabaseReference friends_database,users_database;
    private FirebaseAuth mAuth;
    private String current_usser_id;
    private EditText search_et;
    private ImageView search_img;
    private String message,type;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forward_message);
        message = getIntent().getStringExtra("message");
        type = getIntent().getStringExtra("type");
        mAuth = FirebaseAuth.getInstance();
        current_usser_id = mAuth.getCurrentUser().getUid();
        toolbar = findViewById(R.id.forward_friend_appbar);
        friends_list_rv = findViewById(R.id.forward_friend_list_rv);
        search_et = findViewById(R.id.forward_friend_search_et);
        search_img = findViewById(R.id.forward_friend_search_img);
        friends_database = FirebaseDatabase.getInstance().getReference().child("Friends").child(current_usser_id);
        friends_database.keepSynced(true);
        users_database = FirebaseDatabase.getInstance().getReference().child("Users");
        users_database.keepSynced(true);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Friends");
        friends_list_rv.setHasFixedSize(true);
        friends_list_rv.setLayoutManager(new LinearLayoutManager(this));
        search_et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(count!=0){
                    String name = s.toString();
                    search_user(name);
                }else {
                    onStart();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        search_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = search_et.getText().toString();
                search_user(name);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseRecyclerOptions<friends> options = new FirebaseRecyclerOptions.Builder<friends>().setQuery(friends_database,friends.class).build();
        FirebaseRecyclerAdapter adapter = new FirebaseRecyclerAdapter<friends, friendsFragment.friendsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final friendsFragment.friendsViewHolder friendsViewHolder, final int position, @NonNull friends friends) {
                String date = friends.getDate();
                final String list_user_id = getRef(position).getKey();
                users_database.child(list_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        final String user_name = dataSnapshot.child("name").getValue().toString();
                        String status = dataSnapshot.child("status").getValue().toString();
                        final String thumb_image = dataSnapshot.child("thumb_image").getValue().toString();
                        if(dataSnapshot.hasChild("online")){
                            String user_online_status = dataSnapshot.child("online").getValue().toString();
                            friendsViewHolder.setUserStatus(user_online_status);
                        }
                        friendsViewHolder.setName(user_name);
                        friendsViewHolder.setThumbImage(thumb_image);
                        final String user_id = getRef(position).getKey();
                        friendsViewHolder.view.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if(!type.equals("none")) {
                                    sendMessage(user_id);
                                    Toast.makeText(forwardMessageActivity.this,"Message Forwarded",Toast.LENGTH_LONG).show();
                                }
                                Intent forwared_message = new Intent(forwardMessageActivity.this,chatActivity.class);
                                forwared_message.putExtra("user_id",user_id);
                                forwared_message.putExtra("user_name",user_name);
                                forwared_message.putExtra("user_thumb_image",thumb_image);
                                startActivity(forwared_message);
                                finish();
                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });
            }

            @Override
            public friendsFragment.friendsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.users_single_layout, parent, false);

                return new friendsFragment.friendsViewHolder(view);
            }
        };
        adapter.startListening();
        friends_list_rv.setAdapter(adapter);
    }

    private void sendMessage(String chat_user_id) {
        String current_user_ref = "messages/"+current_usser_id+"/"+chat_user_id;
        String chat_user_ref = "messages/"+chat_user_id+"/"+current_usser_id;
        final String currentTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
        final String currentDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
        DatabaseReference user_message_push = FirebaseDatabase.getInstance().getReference().child("messages").child(current_usser_id).child(chat_user_id).push();
        String push_id = user_message_push.getKey();
        Map messageMap = new HashMap();
        messageMap.put("message",message);
        messageMap.put("type",type);
        messageMap.put("time",currentTime);
        messageMap.put("date",currentDate);
        messageMap.put("from",current_usser_id);
        messageMap.put("timestamp", ServerValue.TIMESTAMP);
        messageMap.put("push_id",push_id);
        Map messageuserMap = new HashMap();
        messageuserMap.put(current_user_ref+"/"+push_id,messageMap);
        messageuserMap.put(chat_user_ref+"/"+push_id,messageMap);
        FirebaseDatabase.getInstance().getReference().updateChildren(messageuserMap, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                if(databaseError != null){
                    Log.d("CHAT_LOG", databaseError.getMessage().toString());
                }
            }
        });
        Map notification_map = new HashMap();
        notification_map.put("from",current_usser_id);
        notification_map.put("type","text");
        FirebaseDatabase.getInstance().getReference().child("notifications").child(chat_user_id).child(push_id).updateChildren(notification_map, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                if(databaseError != null){
                    Log.d("CHAT_LOG", databaseError.getMessage().toString());
                }
            }
        });
    }

    private void search_user(String name) {
        Query search_queary = friends_database.orderByChild("name").startAt(name).endAt(name + "\uf8ff");
        FirebaseRecyclerOptions<friends> options = new FirebaseRecyclerOptions.Builder<friends>().setQuery(search_queary,friends.class).build();
        FirebaseRecyclerAdapter adapter = new FirebaseRecyclerAdapter<friends, friendsFragment.friendsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final friendsFragment.friendsViewHolder friendsViewHolder, final int position, @NonNull friends friends) {
                String date = friends.getDate();
                final String list_user_id = getRef(position).getKey();
                users_database.child(list_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        final String user_name = dataSnapshot.child("name").getValue().toString();
                        String status = dataSnapshot.child("status").getValue().toString();
                        final String thumb_image = dataSnapshot.child("thumb_image").getValue().toString();
                        if(dataSnapshot.hasChild("online")){
                            String user_online_status = dataSnapshot.child("online").getValue().toString();
                            friendsViewHolder.setUserStatus(user_online_status);
                        }
                        friendsViewHolder.setName(user_name);
                        friendsViewHolder.setThumbImage(thumb_image);
                        final String user_id = getRef(position).getKey();
                        friendsViewHolder.view.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                sendMessage(user_id);
                                Intent forwared_message = new Intent(forwardMessageActivity.this,chatActivity.class);
                                forwared_message.putExtra("user_id",user_id);
                                forwared_message.putExtra("user_name",user_name);
                                forwared_message.putExtra("user_thumb_image",thumb_image);
                                Toast.makeText(forwardMessageActivity.this,"Message Forwarded",Toast.LENGTH_LONG).show();
                                startActivity(forwared_message);
                                finish();
                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });
            }

            @Override
            public friendsFragment.friendsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.users_single_layout, parent, false);

                return new friendsFragment.friendsViewHolder(view);
            }
        };
        adapter.startListening();
        friends_list_rv.setAdapter(adapter);
    }
}