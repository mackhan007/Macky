package com.example.macky_chat_app;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import io.agora.rtc.Constants;
import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.RtcEngine;

public class audioCallCallingActivity extends AppCompatActivity {

    private String chat_user_id,call_channel_id,current_user_id,chat_user_name,chat_user_img,endCallStatus;
    private FirebaseAuth mAuth;
    private DatabaseReference root_database;
    private SensorManager sensorManager;
    private Sensor proximitySensor;
    private TextView audio_calling_name_tv,audio_calling_connection_type_tv;
    private CircleImageView audio_calling_user_img;
    private ImageView video_call_img;
    private static final String LOG_TAG = testActivity.class.getSimpleName();
    private static final int PERMISSION_REQ_ID_RECORD_AUDIO = 22;
    private RtcEngine mRtcEngine;
    private final IRtcEngineEventHandler mRtcEventHandler = new IRtcEngineEventHandler() {
        @Override
        public void onUserOffline(final int uid, final int reason) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    onRemoteUserLeft(uid, reason);
                }
            });
        }
        @Override
        public void onUserMuteAudio(final int uid, final boolean muted) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    onRemoteUserVoiceMuted(uid, muted);
                }
            });
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_call_calling);
        chat_user_id = getIntent().getStringExtra("chat_user_id");
        call_channel_id = getIntent().getStringExtra("call_channel_id");
        audio_calling_name_tv = findViewById(R.id.audio_calling_name_tv);
        audio_calling_connection_type_tv = findViewById(R.id.audio_calling_connection_type_tv);
        audio_calling_user_img = findViewById(R.id.audio_calling_user_img);
        video_call_img = findViewById(R.id.audio_calling_video_img);
        mAuth = FirebaseAuth.getInstance();
        current_user_id = mAuth.getCurrentUser().getUid();
        root_database = FirebaseDatabase.getInstance().getReference();
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        if(proximitySensor != null){
            sensorManager.registerListener(proximitySensorEventListener,proximitySensor,sensorManager.SENSOR_DELAY_NORMAL);
        }
        root_database.child("Users").child(chat_user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                chat_user_name = dataSnapshot.child("name").getValue().toString();
                chat_user_img = dataSnapshot.child("image").getValue().toString();
                endCallStatus = dataSnapshot.child("calling").getValue().toString();
                audio_calling_name_tv.setText(chat_user_name);
                if(!chat_user_img.equals("default")){
                    Picasso.get().load(chat_user_img).placeholder(R.drawable.dp).into(audio_calling_user_img);
                }
                if(endCallStatus.equals("none")){
                    endcall();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO, PERMISSION_REQ_ID_RECORD_AUDIO)) {
            initAgoraEngineAndJoinChannel();
        }

        video_call_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog alertDialog = new AlertDialog.Builder(audioCallCallingActivity.this).setIcon(R.mipmap.video_img)
                        .setTitle("Switch To Video Mode").setMessage("Are you sure?").setPositiveButton("yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent video_call_intent = new Intent(audioCallCallingActivity.this,videoCallActivity.class);
                                video_call_intent.putExtra("chat_user_id",chat_user_id);
                                video_call_intent.putExtra("call_channel_id",call_channel_id);
                                startActivity(video_call_intent);
                                finish();
                            }
                        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).show();
            }
        });

    }

    private void initAgoraEngineAndJoinChannel() {
        initializeAgoraEngine();
        joinChannel();
    }

    private SensorEventListener proximitySensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            WindowManager.LayoutParams params = audioCallCallingActivity.this.getWindow().getAttributes();
            if(event.sensor.getType() == Sensor.TYPE_PROXIMITY){
                if(event.values[0] == 0){
                    params.flags |= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
                    params.screenBrightness = 0;
                    getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                    getWindow().setAttributes(params);
                }else {
                    params.flags |= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
                    params.screenBrightness = -1f;
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                    getWindow().setAttributes(params);
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };

    public boolean checkSelfPermission(String permission, int requestCode) {
        Log.i(LOG_TAG, "checkSelfPermission " + permission + " " + requestCode);
        if (ContextCompat.checkSelfPermission(this,
                permission)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{permission},
                    requestCode);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        Log.i(LOG_TAG, "onRequestPermissionsResult " + grantResults[0] + " " + requestCode);

        switch (requestCode) {
            case PERMISSION_REQ_ID_RECORD_AUDIO: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initAgoraEngineAndJoinChannel();
                } else {
                    showLongToast("No permission for " + Manifest.permission.RECORD_AUDIO);
                    finish();
                }
                break;
            }
        }
    }

    public final void showLongToast(final String msg) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        leaveChannel();
        RtcEngine.destroy();
        mRtcEngine = null;
    }

    public void onLocalAudioMuteClicked(View view) {
        ImageView iv = (ImageView) view;
        if (iv.isSelected()) {
            iv.setSelected(false);
            iv.setImageResource(R.mipmap.mic_off_img);
        } else {
            iv.setSelected(true);
            iv.setImageResource(R.mipmap.mic_img);
        }
        mRtcEngine.muteLocalAudioStream(iv.isSelected());
    }

    public void onSwitchSpeakerphoneClicked(View view) {
        ImageView iv = (ImageView) view;
        if (iv.isSelected()) {
            iv.setSelected(false);
            iv.setImageResource(R.mipmap.speker_img);
        } else {
            iv.setSelected(true);
            iv.setImageResource(R.mipmap.speker_off_img);
        }
        mRtcEngine.setEnableSpeakerphone(view.isSelected());
    }

    public void onEncCallClicked(View view) {
        endcall();
    }

    private void initializeAgoraEngine() {
        try {
            mRtcEngine = RtcEngine.create(getBaseContext(), getString(R.string.agora_app_id), mRtcEventHandler);
            mRtcEngine.setChannelProfile(Constants.CHANNEL_PROFILE_COMMUNICATION);
        } catch (Exception e) {
            Log.e(LOG_TAG, Log.getStackTraceString(e));
            throw new RuntimeException("NEED TO check rtc sdk init fatal error\n" + Log.getStackTraceString(e));
        }
    }

    private void joinChannel() {
        String accessToken = getString(R.string.agora_access_token);
        if (TextUtils.equals(accessToken, "") || TextUtils.equals(accessToken, "#YOUR ACCESS TOKEN#")) {
            accessToken = null;
        }
        mRtcEngine.joinChannel(null, call_channel_id, "", 0);
    }

    private void leaveChannel() {
        mRtcEngine.leaveChannel();
    }

    private void onRemoteUserLeft(int uid, int reason) {
        showLongToast(String.format(Locale.US, "user %d left %d", (uid & 0xFFFFFFFFL), reason));
        endcall();
    }

    private void onRemoteUserVoiceMuted(int uid, boolean muted) {
        showLongToast(String.format(Locale.US, "user %d muted or unmuted %b", (uid & 0xFFFFFFFFL), muted));
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
                    showLongToast("Call Ended");
                    Intent mainpage = new Intent(audioCallCallingActivity.this, MainActivity.class);
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
                    Toast.makeText(audioCallCallingActivity.this, "Details logged", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

}