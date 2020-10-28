package com.example.macky_chat_app;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class chatFragment extends Fragment {

    private RecyclerView chat_list_rv;
    private DatabaseReference chats_database, users_database, message_database,seen_database;
    private FirebaseAuth mAuth;
    private String current_usser_id,del_id;
    private View mainview,onclick_actionbar_chatusers;
    private ImageView new_chat_img,back_chatusers_img,delete_chatusers_img;
    private ActionBar actionBar;
    private int del_position;

    public chatFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mainview = inflater.inflate(R.layout.fragment_chat, container, false);
        chat_list_rv = mainview.findViewById(R.id.chat_list_rv);
        new_chat_img = mainview.findViewById(R.id.new_chat_img);
        mAuth = FirebaseAuth.getInstance();
        current_usser_id = mAuth.getCurrentUser().getUid();
        chats_database = FirebaseDatabase.getInstance().getReference().child("Chat").child(current_usser_id);
        chats_database.keepSynced(true);
        users_database = FirebaseDatabase.getInstance().getReference().child("Users");
        users_database.keepSynced(true);
        message_database = FirebaseDatabase.getInstance().getReference().child("messages").child(current_usser_id);
        message_database.keepSynced(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        chat_list_rv.setHasFixedSize(true);
        chat_list_rv.setLayoutManager(linearLayoutManager);
        onclick_actionbar_chatusers = inflater.inflate(R.layout.onclick_actionbar_chatusers,null);
        actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        back_chatusers_img = onclick_actionbar_chatusers.findViewById(R.id.back_chatusers_img);
        delete_chatusers_img = onclick_actionbar_chatusers.findViewById(R.id.delete_chatusers_img);
        new_chat_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent newchat = new Intent(getContext(),forwardMessageActivity.class);
                newchat.putExtra("message","none");
                newchat.putExtra("type","none");
                startActivity(newchat);
            }
        });

        return mainview;
    }

    @Override
    public void onStart() {
        super.onStart();
        Query cosversationQuery = chats_database.orderByChild("timestamp");
        FirebaseRecyclerOptions<chats> options = new FirebaseRecyclerOptions.Builder<chats>().setQuery(cosversationQuery, chats.class).build();
        final FirebaseRecyclerAdapter adapter = new FirebaseRecyclerAdapter<chats, chatViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final chatViewHolder chatViewHolder, final int position, @NonNull chats chats) {
                long seen = chats.getTimestamp();
                final String list_user_id = getRef(position).getKey();
                seen_database = chats_database.child(list_user_id);
                seen_database.keepSynced(true);
                final Query lastmessageQuery = message_database.child(list_user_id).limitToLast(1);
                lastmessageQuery.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable final String s) {
                        String data = dataSnapshot.child("message").getValue().toString();
                        String type = dataSnapshot.child("type").getValue().toString();
                        final long timestamp = (long) dataSnapshot.child("timestamp").getValue();
                        final String from = dataSnapshot.child("from").getValue().toString();
                        chatViewHolder.setMessage(data,type);
                        if(type.equals("file")){
                            String message_id = dataSnapshot.child("push_id").getValue().toString();
                            chatViewHolder.setMessage(message_id,type);
                        }
                        seen_database.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if(!from.equals(current_usser_id)) {
                                    chatViewHolder.setStatus((long) dataSnapshot.child("timestamp").getValue(), timestamp);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                            }
                        });
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
                users_database.child(list_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        final String user_name = dataSnapshot.child("name").getValue().toString();
                        String status = dataSnapshot.child("status").getValue().toString();
                        final String thumb_image = dataSnapshot.child("thumb_image").getValue().toString();
                        String user_online_status = dataSnapshot.child("online").getValue().toString();
                        chatViewHolder.setName(user_name);
                        chatViewHolder.setThumbImage(thumb_image);
                        final String user_id = getRef(position).getKey();
                        chatViewHolder.view.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent viewprofile = new Intent(getContext(), chatActivity.class);
                                viewprofile.putExtra("user_id", user_id);
                                viewprofile.putExtra("user_name", user_name);
                                viewprofile.putExtra("user_thumb_image", thumb_image);
                                startActivity(viewprofile);
                            }
                        });
                        chatViewHolder.view.setOnLongClickListener(new View.OnLongClickListener() {
                            @Override
                            public boolean onLongClick(View v) {
                                chatViewHolder.view.setEnabled(false);
                                actionBar.setDisplayShowCustomEnabled(true);
                                actionBar.setCustomView(onclick_actionbar_chatusers);
                                del_id = getRef(position).getKey();
                                del_position = position;
                                return false;
                            }
                        });
                        back_chatusers_img.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                chatViewHolder.view.setEnabled(true);
                                actionBar.setDisplayShowCustomEnabled(true);
                                actionBar.setCustomView(R.layout.app_bar);
                                actionBar.setTitle("Macky");
                                del_id = null;
                            }
                        });
                        delete_chatusers_img.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                chatViewHolder.view.setEnabled(true);
                                actionBar.setDisplayShowCustomEnabled(true);
                                actionBar.setCustomView(R.layout.app_bar);
                                actionBar.setTitle("Macky");
                                delete_chatuser(del_id,del_position);
                            }
                        });
                    }

                    private void delete_chatuser(final String del_id, final int del_position) {
                        message_database.child(del_id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()) {
                                    chats_database.child(del_id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                Toast.makeText(getContext(),"Messages are deleted,but chat list isn't",Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    });
                                }
                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });
            }

            @Override
            public chatViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.users_single_layout, parent, false);

                return new chatViewHolder(view);
            }
        };
        adapter.startListening();
        chat_list_rv.setAdapter(adapter);
    }
}


class chatViewHolder extends RecyclerView.ViewHolder{
    View view;
    public chatViewHolder(@NonNull View itemView) {
        super(itemView);
        view = itemView;
    }

    public void setMessage(String data,String type) {
        TextView user_status_tv = view.findViewById(R.id.user_single_status);
        if(data != null && type.equals("text")) {
            user_status_tv.setText(data);
        }else if(data!= null && type.equals("image")){
            user_status_tv.setText("Image");
        }else if(data!= null && type.equals("file")){
            String file_name = find_filename(data);
            user_status_tv.setText(file_name);
        }
    }

    private String find_filename(String message_id) {
        String filename = message_id;
        if(message_id.indexOf("_") != -1){
            filename = message_id.substring(message_id.indexOf("_")+1);
        }
        return filename;
    }

    public void setName(String name){
        TextView user_name_tv = view.findViewById(R.id.user_single_name) ;
        user_name_tv.setText(name);
    }

    public void setStatus(long seen_timestamp, long timestamp) {
        TextView user_status_tv = view.findViewById(R.id.user_single_status);
        if((timestamp-seen_timestamp) >= 0){
            user_status_tv.setTypeface(Typeface.DEFAULT_BOLD);
        }else {
            user_status_tv.setTypeface(Typeface.DEFAULT);
        }
    }

    public void setThumbImage(String thumb_image) {
        if (!thumb_image.equals("default")){
            CircleImageView user_dp_img = view.findViewById(R.id.user_single_dp);
            Picasso.get().load(thumb_image).placeholder(R.drawable.dp).into(user_dp_img);
        }
    }
}
