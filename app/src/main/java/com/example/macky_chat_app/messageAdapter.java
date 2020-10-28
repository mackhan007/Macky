package com.example.macky_chat_app;

import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.shape.RoundedCornerTreatment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;


public class messageAdapter extends RecyclerView.Adapter<messageAdapter.messageViewHolder> {
    private DatabaseReference user_databse,message_databse;
    private String name,image,message_id,current_user_id,from_user,optionid = null,optionType = null,optionMessage = null,optionPushId = null;
    private List<messages> messageList;
    private Context context,cntx;
    private ActionBar actionBar;
    private View chatOptionBar,action_bar_view;
    public messageAdapter(List<messages> messageList, Context cntx, ActionBar actionBar, View chatoptionbar,View view){
        this.messageList = messageList;
        this.context = cntx;
        this.actionBar = actionBar;
        this.chatOptionBar = chatoptionbar;
        this.action_bar_view = view;
    }
    @Override
    public messageViewHolder onCreateViewHolder(ViewGroup parent,int viewtype){
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_single_layout,parent,false);
        this.cntx = parent.getContext();
        return new messageViewHolder(v);
    }
    public class messageViewHolder extends RecyclerView.ViewHolder{

        public ConstraintLayout chat_user_layout,current_user_layout;

        public TextView chat_user_messageText_tv,chat_user_time_tv,chat_user_name_tv,chat_user_messageBigText_tv,chat_user_message_file_name_tv;
        public CircleImageView chat_user_dp_img;
        public ImageView chat_user_message_image;
        public ConstraintLayout chat_user_file_layout,chat_user_currentlocation_box;

        public TextView current_user_messageText_tv,current_user_time_tv,current_user_name_tv,current_user_messageBigText_tv,current_user_message_file_name_tv;
        public CircleImageView current_user_dp_img;
        public ImageView current_user_message_image;
        public ConstraintLayout current_user_file_layout,current_user_currentlocation_box;
        public ImageView optionBarDelete,optionBarBack,optionBarDownload,optionBarReply,optionBarForward,optionBarCopy;

        public ConstraintLayout currentReplyBox,chatReplyBox,currentReplyCurrentLocationBox,chatReplyCurrentLocationBox,currentReplyFileBox,chatReplyFileBox;
        public TextView currentReplyName,currentReplyMessage,currentReplyFileName;
        public TextView chatReplyName,chatReplyMessage,chatReplyFileName;
        public ImageView currentReplyImg,chatReplyImg;
        public messageViewHolder(View view){
            super(view);
            chat_user_layout = view.findViewById(R.id.chat_user_layout);
            current_user_layout = view.findViewById(R.id.current_user_layout);
            optionBarDelete = chatOptionBar.findViewById(R.id.chat_bar_delete_img);
            optionBarBack = chatOptionBar.findViewById(R.id.chat_bar_back_img);
            optionBarDownload = chatOptionBar.findViewById(R.id.chat_bar_download_img);
            optionBarReply = chatOptionBar.findViewById(R.id.chat_bar_reply_img);
            optionBarForward = chatOptionBar.findViewById(R.id.chat_bar_forward_img);
            optionBarCopy = chatOptionBar.findViewById(R.id.chat_bar_copy_img);

            chat_user_messageText_tv = view.findViewById(R.id.chat_user_message_text_tv);
            chat_user_messageBigText_tv = view.findViewById(R.id.chat_user_message_text_big_tv);
            chat_user_dp_img = view.findViewById(R.id.chat_user_dp_img);
            chat_user_name_tv = view.findViewById(R.id.chat_user_message_name_tv);
            chat_user_message_image = view.findViewById(R.id.chat_user_single_chat_message_img);
            chat_user_time_tv = view.findViewById(R.id.chat_user_message_time_tv);
            chat_user_file_layout = view.findViewById(R.id.chat_user_message_file_layout);
            chat_user_message_file_name_tv = view.findViewById(R.id.chat_user_message_file_name_tv);
            chat_user_currentlocation_box = view.findViewById(R.id.chat_user_currentlocation_box);
            chatReplyBox = view.findViewById(R.id.chatReplyBox);
            chatReplyCurrentLocationBox = view.findViewById(R.id.chatReplyCurrentLocationBox);
            chatReplyFileBox = view.findViewById(R.id.chatReplyFileBox);
            chatReplyName = view.findViewById(R.id.chatReplyName);
            chatReplyMessage = view.findViewById(R.id.chatReplyMessage);
            chatReplyFileName = view.findViewById(R.id.chatReplyFileName);
            chatReplyImg = view.findViewById(R.id.chatReplyImg);

            current_user_messageText_tv = view.findViewById(R.id.current_user_message_text_tv);
            current_user_messageBigText_tv = view.findViewById(R.id.current_user_message_text_big_tv);
            current_user_dp_img = view.findViewById(R.id.current_user_dp_img);
            current_user_name_tv = view.findViewById(R.id.current_user_message_name_tv);
            current_user_message_image = view.findViewById(R.id.current_user_single_chat_message_img);
            current_user_time_tv = view.findViewById(R.id.current_user_message_time_tv);
            current_user_file_layout = view.findViewById(R.id.current_user_message_file_layout);
            current_user_message_file_name_tv = view.findViewById(R.id.current_user_message_file_name_tv);
            current_user_currentlocation_box = view.findViewById(R.id.current_user_currentlocation_box);
            currentReplyBox = view.findViewById(R.id.currentReplyBox);
            currentReplyCurrentLocationBox = view.findViewById(R.id.currentReplyCurrentLocationBox);
            currentReplyFileBox = view.findViewById(R.id.currentReplyFileBox);
            currentReplyName= view.findViewById(R.id.currentReplyName);
            currentReplyMessage = view.findViewById(R.id.currentReplyMessage);
            currentReplyFileName = view.findViewById(R.id.currentReplyFileName);
            currentReplyImg = view.findViewById(R.id.currentReplyImg);
        }
    }
    @Override
    public void onBindViewHolder(final messageViewHolder viewHolder, final int position){
        viewHolder.currentReplyBox.setVisibility(View.GONE);
        viewHolder.chatReplyBox.setVisibility(View.GONE);
        final messages c = messageList.get(position);
        final String current_push_id = c.current_push_id;
        current_user_id = c.current_user_id;
        from_user = c.from_user_id;
        final String message_type = c.getType();
        final String currentDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
        user_databse = FirebaseDatabase.getInstance().getReference().child("Users");
        user_databse.keepSynced(true);
        message_databse = FirebaseDatabase.getInstance().getReference().child("messages").child(current_user_id).child(from_user);
        user_databse.child(c.getFrom()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                name = dataSnapshot.child("name").getValue().toString();
                image = dataSnapshot.child("thumb_image").getValue().toString();
                if(current_user_id.equals(c.getFrom())){
                    viewHolder.current_user_layout.setVisibility(View.VISIBLE);
                    viewHolder.chat_user_layout.setVisibility(View.GONE);
                    viewHolder.current_user_name_tv.setText(name);
                    if (!image.equals("default")) {
                        Picasso.get().load(image).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.dp).into(viewHolder.current_user_dp_img, new Callback() {
                            @Override
                            public void onSuccess() {
                            }
                            @Override
                            public void onError(Exception e) {
                                Picasso.get().load(image).placeholder(R.drawable.dp).into(viewHolder.current_user_dp_img);
                            }
                        });
                    }
                    //set_data
                    if(currentDate.equals(c.getDate())) {
                        int time_hh = Integer.parseInt(c.getTime().substring(0,2));
                        if(time_hh > 12){
                            int hh = time_hh - 12;
                            viewHolder.current_user_time_tv.setText(hh+c.getTime().substring(2)+" PM");
                        }else {
                            viewHolder.current_user_time_tv.setText(c.getTime()+" AM");
                        }
                    }else {
                        int time_hh = Integer.parseInt(c.getTime().substring(0,2));
                        if(time_hh > 12){
                            int hh = time_hh - 12;
                            viewHolder.current_user_time_tv.setText(hh+c.getTime().substring(2)+" PM , "+c.getDate());
                        }else {
                            viewHolder.current_user_time_tv.setText(c.getTime()+" AM , "+c.getDate());
                        }

                    }
                    switch (message_type){
                        case "text" :if(c.getMessage().length() > 40){
                            viewHolder.current_user_messageBigText_tv.setText(c.getMessage());
                            viewHolder.current_user_messageBigText_tv.setVisibility(View.VISIBLE);
                            viewHolder.current_user_messageText_tv.setVisibility(View.GONE);
                            viewHolder.current_user_message_image.setVisibility(View.GONE);
                            viewHolder.current_user_file_layout.setVisibility(View.GONE);
                        }else {
                            viewHolder.current_user_messageText_tv.setText(c.getMessage());
                            viewHolder.current_user_messageText_tv.setVisibility(View.VISIBLE);
                            viewHolder.current_user_messageBigText_tv.setVisibility(View.GONE);
                            viewHolder.current_user_message_image.setVisibility(View.GONE);
                            viewHolder.current_user_file_layout.setVisibility(View.GONE);
                        }
                            break;
                        case "image" :File file = new File(context.getExternalFilesDir("Macky/Images/"),c.getPush_id() + ".jpg");
                            if(file.exists()){
                                Picasso.get().load(Uri.fromFile(file)).into(viewHolder.current_user_message_image);
                            }else {
                                Picasso.get().load(c.getMessage()).into(viewHolder.current_user_message_image);
                            }
                            viewHolder.current_user_message_image.setVisibility(View.VISIBLE);
                            viewHolder.current_user_messageBigText_tv.setVisibility(View.GONE);
                            viewHolder.current_user_messageText_tv.setVisibility(View.GONE);
                            viewHolder.current_user_file_layout.setVisibility(View.GONE);
                            break;
                        case "file" : viewHolder.current_user_file_layout.setVisibility(View.VISIBLE);
                            message_id = c.getPush_id();
                            String file_name = find_filename(message_id);
                            viewHolder.current_user_message_file_name_tv.setText(file_name);
                            viewHolder.current_user_file_layout.setVisibility(View.VISIBLE);
                            viewHolder.current_user_messageBigText_tv.setVisibility(View.GONE);
                            viewHolder.current_user_messageText_tv.setVisibility(View.GONE);
                            viewHolder.current_user_message_image.setVisibility(View.GONE);
                            break;
                    }

                    message_databse.child(current_push_id).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
                            if(dataSnapshot.hasChild("reply")) {
                                final String replyId = dataSnapshot.child("reply").getValue().toString();
                                if(!replyId.equals("none")) {
                                    viewHolder.currentReplyBox.setVisibility(View.VISIBLE);
                                    message_databse.child(replyId).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            String replyType = snapshot.child("type").getValue().toString();
                                            String replyFrom = snapshot.child("from").getValue().toString();
                                            if(replyFrom.equals(current_user_id)) {
                                                viewHolder.currentReplyName.setText("You");
                                            }else {
                                                user_databse.child(replyId).addValueEventListener(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot dsnapshot) {
                                                        viewHolder.currentReplyName.setText(dsnapshot.child("name").getValue().toString());
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                                    }
                                                });
                                            }
                                            switch (replyType) {
                                                case "text" : viewHolder.currentReplyMessage.setText(snapshot.child("message").getValue().toString());
                                                        viewHolder.currentReplyMessage.setVisibility(View.VISIBLE);
                                                        break;
                                                case "image" : Picasso.get().load(snapshot.child("message").getValue().toString()).into(viewHolder.currentReplyImg);
                                                        viewHolder.currentReplyImg.setVisibility(View.VISIBLE);
                                                        break;
                                                case "file" : viewHolder.currentReplyFileName.setText(find_filename(snapshot.child("push_id").getValue().toString()));
                                                        viewHolder.currentReplyFileBox.setVisibility(View.VISIBLE);
                                                        break;
                                                case "sticker" : Picasso.get().load(snapshot.child("message").getValue().toString()).into(viewHolder.currentReplyImg);
                                                        viewHolder.currentReplyImg.setVisibility(View.VISIBLE);
                                                        break;
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

                }else {
                    viewHolder.current_user_layout.setVisibility(View.GONE);
                    viewHolder.chat_user_layout.setVisibility(View.VISIBLE);
                    viewHolder.chat_user_name_tv.setText(name);
                    if (!image.equals("default")) {
                        Picasso.get().load(image).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.dp).into(viewHolder.chat_user_dp_img, new Callback() {
                            @Override
                            public void onSuccess() {
                            }
                            @Override
                            public void onError(Exception e) {
                                Picasso.get().load(image).placeholder(R.drawable.dp).into(viewHolder.chat_user_dp_img);
                            }
                        });
                    }
                }
                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(message_type.equals("image")){
                            openImage(Uri.parse(c.getMessage()));
                        }else if(message_type.equals("file")){
                            openFile(c.getMessage(),Uri.parse(c.getMessage()));
                        }
                    }
                });
                viewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        messages option = messageList.get(position);
                        optionid = option.current_push_id;
                        optionMessage = option.getMessage();
                        optionPushId = option.getPush_id();
                        optionType = option.getType();
                        actionBar.setCustomView(chatOptionBar);
                        actionBar.setDisplayHomeAsUpEnabled(false);
                        return true;
                    }
                });
                viewHolder.optionBarDelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        FirebaseDatabase.getInstance().getReference().child("messages").child(current_user_id).child(from_user).child(optionid).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    Toast.makeText(context,"Message deleted",Toast.LENGTH_SHORT).show();
                                    messageList.remove(position);
                                    notifyItemRemoved(position);
                                }else {
                                    Toast.makeText(context,"Unable to delete the message, please try again later",Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                        actionBar.setCustomView(action_bar_view);
                        actionBar.setDisplayHomeAsUpEnabled(true);
                        optionid = null;
                        optionMessage = null;
                        optionPushId = null;
                        optionType = null;
                    }
                });
                viewHolder.optionBarCopy.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(optionType.equals("text")) {
                            setClipboard(context, optionMessage);
                        }else {
                            Toast.makeText(context,"File cannot be copied,try forwarding it",Toast.LENGTH_SHORT).show();
                        }
                        actionBar.setCustomView(action_bar_view);
                        actionBar.setDisplayHomeAsUpEnabled(true);
                        optionid = null;
                        optionMessage = null;
                        optionPushId = null;
                        optionType = null;
                    }
                });
                viewHolder.optionBarForward.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent forward_message = new Intent();
                        forward_message.setClass(context,forwardMessageActivity.class);
                        forward_message.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        forward_message.putExtra("message",optionMessage);
                        forward_message.putExtra("type",optionType);
                        context.startActivity(forward_message);
                        actionBar.setCustomView(action_bar_view);
                        actionBar.setDisplayHomeAsUpEnabled(true);
                        optionid = null;
                        optionMessage = null;
                        optionPushId = null;
                        optionType = null;
                    }
                });
                viewHolder.optionBarReply.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent check = new Intent();
                        check.setClass(context,testActivity.class);
                        check.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(check);
                        actionBar.setCustomView(action_bar_view);
                        actionBar.setDisplayHomeAsUpEnabled(true);
                        optionid = null;
                        optionMessage = null;
                        optionPushId = null;
                        optionType = null;
                    }
                });
                viewHolder.optionBarDownload.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(optionType.equals("image")){
                            downloadImage(optionMessage,optionPushId);
                        }else if(optionType.equals("file")) {
                            downloadFile(optionMessage,optionPushId);
                        }else {
                            Toast.makeText(context,"can't download "+optionType,Toast.LENGTH_SHORT).show();
                        }
                        actionBar.setCustomView(action_bar_view);
                        actionBar.setDisplayHomeAsUpEnabled(true);
                        optionid = null;
                        optionMessage = null;
                        optionPushId = null;
                        optionType = null;
                    }
                });
                viewHolder.optionBarBack.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        actionBar.setCustomView(action_bar_view);
                        actionBar.setDisplayHomeAsUpEnabled(true);
                        optionid = null;
                        optionMessage = null;
                        optionPushId = null;
                        optionType = null;
                    }
                });
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
        if(!current_user_id.equals(from_user)){
            if(currentDate.equals(c.getDate())) {
                int time_hh = Integer.parseInt(c.getTime().substring(0,2));
                if(time_hh > 12){
                    int hh = time_hh - 12;
                    viewHolder.chat_user_time_tv.setText(hh+c.getTime().substring(2)+" PM");
                }else {
                    viewHolder.chat_user_time_tv.setText(c.getTime()+" AM");
                }
            }else {
                int time_hh = Integer.parseInt(c.getTime().substring(0,2));
                if(time_hh > 12){
                    int hh = time_hh - 12;
                    viewHolder.chat_user_time_tv.setText(hh+c.getTime().substring(2)+" PM , " + c.getDate());
                }else {
                    viewHolder.chat_user_time_tv.setText(c.getTime() + " AM , " + c.getDate());
                }
            }
            switch (message_type){
                case "text" :if(c.getMessage().length() > 40){
                    viewHolder.chat_user_messageBigText_tv.setText(c.getMessage());
                    viewHolder.chat_user_messageBigText_tv.setVisibility(View.VISIBLE);
                    viewHolder.chat_user_messageText_tv.setVisibility(View.GONE);
                    viewHolder.chat_user_message_image.setVisibility(View.GONE);
                    viewHolder.chat_user_file_layout.setVisibility(View.GONE);
                }else {
                    viewHolder.chat_user_messageText_tv.setText(c.getMessage());
                    viewHolder.chat_user_messageText_tv.setVisibility(View.VISIBLE);
                    viewHolder.chat_user_messageBigText_tv.setVisibility(View.GONE);
                    viewHolder.chat_user_message_image.setVisibility(View.GONE);
                    viewHolder.chat_user_file_layout.setVisibility(View.GONE);
                }
                    break;
                case "image" : File file = new File(context.getExternalFilesDir("Macky/Images/"),c.getPush_id() + ".jpg");
                    if(file.exists()){
                        Picasso.get().load(Uri.fromFile(file)).into(viewHolder.chat_user_message_image);
                    }else {
                        Picasso.get().load(c.getMessage()).into(viewHolder.chat_user_message_image);
                    }
                    viewHolder.chat_user_message_image.setVisibility(View.VISIBLE);
                    viewHolder.chat_user_messageBigText_tv.setVisibility(View.GONE);
                    viewHolder.chat_user_messageText_tv.setVisibility(View.GONE);
                    viewHolder.chat_user_file_layout.setVisibility(View.GONE);
                    break;
                case "file" : viewHolder.chat_user_file_layout.setVisibility(View.VISIBLE);
                    message_id = c.getPush_id();
                    String file_name = find_filename(message_id);
                    viewHolder.chat_user_message_file_name_tv.setText(file_name);
                    viewHolder.chat_user_messageBigText_tv.setVisibility(View.GONE);
                    viewHolder.chat_user_messageText_tv.setVisibility(View.GONE);
                    viewHolder.chat_user_message_image.setVisibility(View.GONE);
                    break;
            }

            message_databse.child(current_push_id).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.hasChild("reply")) {
                        final String replyId = dataSnapshot.child("reply").getValue().toString();
                        if(!replyId.equals("none")) {
                            viewHolder.chatReplyBox.setVisibility(View.VISIBLE);
                            message_databse.child(replyId).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    String replyType = snapshot.child("type").getValue().toString();
                                    String replyFrom = snapshot.child("from").getValue().toString();
                                    if(replyFrom.equals(current_user_id)) {
                                        viewHolder.chatReplyName.setText("You");
                                    }else {
                                        user_databse.child(replyId).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dsnapshot) {
//                                                String replyname = dsnapshot.child("name").getValue().toString();
//                                                if(!replyname.equals(null)){
//                                                    viewHolder.chatReplyName.setText(replyname);
//                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                            }
                                        });
                                    }
                                    switch (replyType) {
                                        case "text" : viewHolder.chatReplyMessage.setText(snapshot.child("message").getValue().toString());
                                            viewHolder.chatReplyMessage.setVisibility(View.VISIBLE);
                                            break;
                                        case "image" : Picasso.get().load(snapshot.child("message").getValue().toString()).into(viewHolder.chatReplyImg);
                                            viewHolder.chatReplyImg.setVisibility(View.VISIBLE);
                                            break;
                                        case "file" : viewHolder.chatReplyFileName.setText(find_filename(snapshot.child("push_id").getValue().toString()));
                                            viewHolder.chatReplyFileName.setVisibility(View.VISIBLE);
                                            break;
                                        case "sticker" : Picasso.get().load(snapshot.child("message").getValue().toString()).into(viewHolder.chatReplyImg);
                                            viewHolder.chatReplyImg.setVisibility(View.VISIBLE);
                                            break;
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
        }
    }

    private void setClipboard(Context context, String message) {
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData.newPlainText("Copied Text", message);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(context,"Text Copied",Toast.LENGTH_SHORT).show();
    }

    private String find_filename(String message_id) {
        String filename = message_id;
        if(message_id.indexOf("_") != -1){
            filename = message_id.substring(message_id.indexOf("_")+1);
        }
        return filename;
    }

    private void downloadImage(String message,String message_id) {
        File file = new File(context.getExternalFilesDir("Macky/Images/"),message_id + ".jpg");
        if(!file.exists()) {
            DownloadManager downloadmanager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
            Uri uri = Uri.parse(message);
            DownloadManager.Request request = new DownloadManager.Request(uri);
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setDestinationInExternalFilesDir(context, "Macky/Images/", message_id + ".jpg");
            downloadmanager.enqueue(request);
            Toast.makeText(context,"Image Downloaded",Toast.LENGTH_SHORT).show();
        }else {
            Toast.makeText(context,"Image Already there",Toast.LENGTH_SHORT).show();
            Intent show_file = new Intent();
            show_file.setClass(context,showImageActivity.class);
            show_file.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            Uri img_uri = Uri.fromFile(file);
            show_file.putExtra("file_uri",img_uri.toString());
            show_file.putExtra("message_id",message_id);
            show_file.putExtra("type","image");
            context.startActivity(show_file);
        }
    }

    private void downloadFile(String message,String message_id) {
        File file = new File(context.getExternalFilesDir("Macky/Files/"),message_id);
        if(!file.exists()) {
            DownloadManager downloadmanager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
            Uri uri = Uri.parse(message);
            DownloadManager.Request request = new DownloadManager.Request(uri);
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setDestinationInExternalFilesDir(context, "Macky/Files/", message_id);
            downloadmanager.enqueue(request);
            Toast.makeText(context,"File Downloaded",Toast.LENGTH_SHORT).show();
        }else {
            Toast.makeText(context, "File Already there", Toast.LENGTH_SHORT).show();
            Intent show_file = new Intent();
            show_file.setClass(context,showImageActivity.class);
            show_file.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            Uri file_uri = Uri.fromFile(file);
            show_file.putExtra("file_uri",file_uri.toString());
            show_file.putExtra("message_id",message_id);
            show_file.putExtra("type","file");
            context.startActivity(show_file);
        }
    }

    private void openImage(Uri uri) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "image/jpeg");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(context, "No application found which can open the file", Toast.LENGTH_SHORT).show();
        }
    }

    private void openFile(String message_id,Uri uri) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            if (message_id.contains(".doc") || message_id.contains(".docx")) {
                // Word document
                intent.setDataAndType(uri, "application/msword");
            } else if (message_id.contains(".pdf")) {
                // PDF file
                intent.setDataAndType(uri, "application/pdf");
            } else if (message_id.contains(".ppt") || message_id.contains(".pptx")) {
                // Powerpoint file
                intent.setDataAndType(uri, "application/vnd.ms-powerpoint");
            } else if (message_id.contains(".xls") || message_id.contains(".xlsx")) {
                // Excel file
                intent.setDataAndType(uri, "application/vnd.ms-excel");
            } else if (message_id.contains(".zip")) {
                // ZIP file
                intent.setDataAndType(uri, "application/zip");
            } else if (message_id.contains(".rar")){
                // RAR file
                intent.setDataAndType(uri, "application/x-rar-compressed");
            } else if (message_id.contains(".rtf")) {
                // RTF file
                intent.setDataAndType(uri, "application/rtf");
            } else if (message_id.contains(".wav") || message_id.contains(".mp3")) {
                // WAV audio file
                intent.setDataAndType(uri, "audio/x-wav");
            } else if (message_id.contains(".gif")) {
                // GIF file
                intent.setDataAndType(uri, "image/gif");
            } else if (message_id.contains(".jpg") || message_id.contains(".jpeg") || message_id.contains(".png")) {
                // JPG file
                intent.setDataAndType(uri, "image/jpeg");
            } else if (message_id.contains(".txt")) {
                // Text file
                intent.setDataAndType(uri, "text/plain");
            } else if (message_id.contains(".3gp") || message_id.contains(".mpg") ||
                    message_id.contains(".mpeg") || message_id.contains(".mpe") || message_id.contains(".mp4") || message_id.contains(".avi")) {
                // Video files
                intent.setDataAndType(uri, "video/*");
            } else {
                intent.setDataAndType(uri, "*/*");
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(context, "No application found which can open the file,please try downloading", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public int getItemCount(){
        return messageList.size();
    }
}
