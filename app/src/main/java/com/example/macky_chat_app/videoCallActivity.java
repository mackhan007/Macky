package com.example.macky_chat_app;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.RtcEngine;
import io.agora.rtc.video.VideoCanvas;
import io.agora.rtc.video.VideoEncoderConfiguration;

public class videoCallActivity extends AppCompatActivity {

    private static final String TAG = testActivity.class.getSimpleName();

    private static final int PERMISSION_REQ_ID = 22;

    private static final String[] REQUESTED_PERMISSIONS = {
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private RtcEngine mRtcEngine;
    private boolean mCallEnd;
    private boolean mMuted;

    private FrameLayout mLocalContainer;
    private RelativeLayout mRemoteContainer;
    private SurfaceView mLocalView;
    private SurfaceView mRemoteView;

    private ImageView mMuteBtn;

    private CircleImageView video_chat_user_img;
    private RelativeLayout control_panel;
    private TextView video_connection_tv;

    private final IRtcEngineEventHandler mRtcEventHandler = new IRtcEngineEventHandler() {
        @Override
        public void onJoinChannelSuccess(String channel, final int uid, int elapsed) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.i("macky","Join channel success, uid: " + (uid & 0xFFFFFFFFL));
                }
            });
        }

        @Override
        public void onFirstRemoteVideoDecoded(final int uid, int width, int height, int elapsed) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.i("macky","First remote video decoded, uid: " + (uid & 0xFFFFFFFFL));
                    setupRemoteVideo(uid);
                }
            });
        }

        @Override
        public void onUserOffline(final int uid, int reason) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.i("macky","User offline, uid: " + (uid & 0xFFFFFFFFL));
                    onRemoteUserLeft();
                }
            });
        }
    };

    private void setupRemoteVideo(int uid) {
        int count = mRemoteContainer.getChildCount();
        View view = null;
        for (int i = 0; i < count; i++) {
            View v = mRemoteContainer.getChildAt(i);
            if (v.getTag() instanceof Integer && ((int) v.getTag()) == uid) {
                view = v;
            }
        }

        if (view != null) {
            return;
        }

        video_chat_user_img.setVisibility(View.GONE);
        video_connection_tv.setVisibility(View.GONE);
        video_call_chat_user_name_tv.setVisibility(View.GONE);
        mRemoteView = RtcEngine.CreateRendererView(getBaseContext());
        mRemoteContainer.addView(mRemoteView);
        mRtcEngine.setupRemoteVideo(new VideoCanvas(mRemoteView, VideoCanvas.RENDER_MODE_HIDDEN, uid));
        mRemoteView.setTag(uid);
    }

    private void onRemoteUserLeft() {
        removeRemoteVideo();
    }

    private void removeRemoteVideo() {
        if (mRemoteView != null) {
            mRemoteContainer.removeView(mRemoteView);
        }
        mRemoteView = null;
    }

    private String chat_user_id,call_channel_id,current_user_id,chat_user_name,chat_user_img,endCallStatus;
    private FirebaseAuth mAuth;
    private DatabaseReference root_database;
    private CircleImageView video_call_chat_user_img;
    private TextView video_call_chat_user_name_tv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_call);
        chat_user_id = getIntent().getStringExtra("chat_user_id");
        call_channel_id = getIntent().getStringExtra("call_channel_id");
        initUI();
        mAuth = FirebaseAuth.getInstance();
        current_user_id = mAuth.getCurrentUser().getUid();
        root_database = FirebaseDatabase.getInstance().getReference();
        root_database.child("Users").child(chat_user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                chat_user_name = dataSnapshot.child("name").getValue().toString();
                chat_user_img = dataSnapshot.child("image").getValue().toString();
                endCallStatus = dataSnapshot.child("calling").getValue().toString();
                video_call_chat_user_name_tv.setText(chat_user_name);
                if(!chat_user_img.equals("default")){
                    Picasso.get().load(chat_user_img).placeholder(R.drawable.dp).into(video_call_chat_user_img);
                }
                if(endCallStatus.equals("none")){
                    endcall();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        if (checkSelfPermission(REQUESTED_PERMISSIONS[0], PERMISSION_REQ_ID) &&
                checkSelfPermission(REQUESTED_PERMISSIONS[1], PERMISSION_REQ_ID) &&
                checkSelfPermission(REQUESTED_PERMISSIONS[2], PERMISSION_REQ_ID)) {
            initEngineAndJoinChannel();
        }

    }


    private void initUI() {
        mLocalContainer = findViewById(R.id.video_call_local_video_view_container);
        mRemoteContainer = findViewById(R.id.video_call_remote_video_view_container);

        control_panel = findViewById(R.id.video_call_control_panel);
        video_chat_user_img = findViewById(R.id.video_call_video_chat_user_img);
        video_connection_tv = findViewById(R.id.video_call_video_connection_tv);
        mMuteBtn = findViewById(R.id.video_call_btn_mute);
        video_call_chat_user_img = findViewById(R.id.video_call_video_chat_user_img);
        video_call_chat_user_name_tv = findViewById(R.id.video_call_name_tv);

    }

    private boolean checkSelfPermission(String permission, int requestCode) {
        if (ContextCompat.checkSelfPermission(this, permission) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, REQUESTED_PERMISSIONS, requestCode);
            return false;
        }

        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == PERMISSION_REQ_ID) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED ||
                    grantResults[1] != PackageManager.PERMISSION_GRANTED ||
                    grantResults[2] != PackageManager.PERMISSION_GRANTED) {
                showLongToast("Need permissions " + Manifest.permission.RECORD_AUDIO +
                        "/" + Manifest.permission.CAMERA + "/" + Manifest.permission.WRITE_EXTERNAL_STORAGE);
                finish();
                return;
            }
            initEngineAndJoinChannel();
        }
    }

    private void showLongToast(final String msg) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void initEngineAndJoinChannel() {
        initializeEngine();
        setupVideoConfig();
        setupLocalVideo();
        joinChannel();
    }

    private void initializeEngine() {
        try {
            mRtcEngine = RtcEngine.create(getBaseContext(), getString(R.string.agora_app_id), mRtcEventHandler);
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
            throw new RuntimeException("NEED TO check rtc sdk init fatal error\n" + Log.getStackTraceString(e));
        }
    }

    private void setupVideoConfig() {
        mRtcEngine.enableVideo();
        mRtcEngine.setVideoEncoderConfiguration(new VideoEncoderConfiguration(
                VideoEncoderConfiguration.VD_640x360,
                VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_15,
                VideoEncoderConfiguration.STANDARD_BITRATE,
                VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_FIXED_PORTRAIT));
    }

    private void setupLocalVideo() {
        mLocalView = RtcEngine.CreateRendererView(getBaseContext());
        mLocalView.setZOrderMediaOverlay(true);
        mLocalContainer.addView(mLocalView);
        mRtcEngine.setupLocalVideo(new VideoCanvas(mLocalView, VideoCanvas.RENDER_MODE_HIDDEN, 0));
    }

    private void joinChannel() {
        String token = getString(R.string.agora_access_token);
        if (TextUtils.isEmpty(token) || TextUtils.equals(token, "#YOUR ACCESS TOKEN#")) {
            token = null;
        }
        mRtcEngine.joinChannel(null, call_channel_id, "", 0);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!mCallEnd) {
            leaveChannel();
        }
        RtcEngine.destroy();
        endcall();
    }

    private void leaveChannel() {
        mRtcEngine.leaveChannel();
    }

    public void onLocalAudioMuteClicked(View view) {
        mMuted = !mMuted;
        mRtcEngine.muteLocalAudioStream(mMuted);
        int res = mMuted ? R.mipmap.mic_img : R.mipmap.mic_off_img;
        mMuteBtn.setImageResource(res);
    }

    public void onSwitchCameraClicked(View view) {
        mRtcEngine.switchCamera();
    }

    public void onCallClicked(View view) {
        endCall();
    }


    private void endCall() {
        removeLocalVideo();
        removeRemoteVideo();
        leaveChannel();
        endcall();
    }

    private void removeLocalVideo() {
        if (mLocalView != null) {
            mLocalContainer.removeView(mLocalView);
        }
        mLocalView = null;
    }


    @Override
    protected void onStart() {
        super.onStart();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    protected void onStop() {
        super.onStop();
        endcall();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private void endcall() {
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
                    Toast.makeText(videoCallActivity.this,"Call Ended",Toast.LENGTH_SHORT).show();
                    Intent mainpage = new Intent(videoCallActivity.this, MainActivity.class);
                    mainpage.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(mainpage);
                    finish();
                }
            }
        });

        final String current_user_calling = "Calling/" + current_user_id + "/" + call_channel_id + "/timestamp_ended";
        final String chat_user_calling = "Calling/" + chat_user_id + "/" + call_channel_id + "/timestamp_ended";
        Map calling_details = new HashMap();
        calling_details.put(current_user_calling, ServerValue.TIMESTAMP);
        calling_details.put(chat_user_calling, ServerValue.TIMESTAMP);
        FirebaseDatabase.getInstance().getReference().updateChildren(calling_details, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                if (databaseError == null) {
                    Toast.makeText(videoCallActivity.this, "Details logged", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

}