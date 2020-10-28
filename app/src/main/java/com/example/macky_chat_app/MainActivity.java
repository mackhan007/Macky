package com.example.macky_chat_app;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import static android.Manifest.permission.ACCESS_NETWORK_STATE;
import static android.Manifest.permission.ACCESS_WIFI_STATE;
import static android.Manifest.permission.BLUETOOTH;
import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.INTERNET;
import static android.Manifest.permission.MODIFY_AUDIO_SETTINGS;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.READ_PHONE_STATE;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference current_user_database;
    private androidx.appcompat.widget.Toolbar toolbar;
    private ViewPager viewPager;
    private sectionPagerAdaptor sectionpageradaptor;
    private TabLayout tabLayout;
    boolean local_status = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActivityCompat.requestPermissions(this,new String[]{READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE, INTERNET,
                CAMERA,ACCESS_NETWORK_STATE,READ_PHONE_STATE,RECORD_AUDIO,MODIFY_AUDIO_SETTINGS,BLUETOOTH,ACCESS_WIFI_STATE}, PackageManager.PERMISSION_GRANTED);
        mAuth = FirebaseAuth.getInstance();
        toolbar = (androidx.appcompat.widget.Toolbar) findViewById(R.id.main_appBar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Macky");

        //tabs
        viewPager = findViewById(R.id.main_tabPager);
        sectionpageradaptor = new sectionPagerAdaptor(getSupportFragmentManager());

        viewPager.setAdapter(sectionpageradaptor);

        tabLayout = findViewById(R.id.main_tabs);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.getTabAt(1).select();

    }
    private void sendToStart() {
        Intent startPageIntent = new Intent(MainActivity.this,startActivity.class);
        startActivity(startPageIntent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()){
            case R.id.main_logout_out_mbtn:current_user_database = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());
                                            current_user_database.child("online").setValue(ServerValue.TIMESTAMP);
                                            FirebaseAuth.getInstance().signOut();
                                            sendToStart();
                                            break;
            case R.id.main_account_settings_mbtn:Intent settings_intent = new Intent(MainActivity.this,settingsActivity.class);
                                                local_status = true;
                                                startActivity(settings_intent);
                                                break;
            case R.id.main_all_users_mbtn:Intent user_intent = new Intent(MainActivity.this,usersActivity.class);
                                            local_status=true;
                                            startActivity(user_intent);
                                            break;
            default:Toast.makeText(getApplicationContext(),"wrong Button",Toast.LENGTH_SHORT).show();
                    break;
        }
        return true;
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser == null){
            sendToStart();
        }else {
            current_user_database = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());
            current_user_database.child("online").setValue("true");
            current_user_database.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.hasChild("calledBy") && dataSnapshot.hasChild("call_channel_id") && dataSnapshot.hasChild("type")){
                        final String calling_info = dataSnapshot.child("calledBy").getValue().toString();
                        final String calling_channel = dataSnapshot.child("call_channel_id").getValue().toString();
                        final String call_type = dataSnapshot.child("type").getValue().toString();
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            public void run() {
                                if (!calling_info.equals("none") && !calling_channel.equals("none")) {
                                    Intent call_intent = new Intent(MainActivity.this, audioCallReceivingActivity.class);
                                    call_intent.putExtra("chat_user_id", calling_info);
                                    call_intent.putExtra("call_channel_id",calling_channel);
                                    call_intent.putExtra("type",call_type);
                                    startActivity(call_intent);
                                }
                            }
                        }, 2000);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });
        }
    }
}
