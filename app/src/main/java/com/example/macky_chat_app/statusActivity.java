package com.example.macky_chat_app;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class statusActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TextInputLayout status_til,name_til;
    private Button save_changes_btn;
    private DatabaseReference status_database,current_user_database;
    private FirebaseUser current_user;
    private ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);
        toolbar = findViewById(R.id.status_appbar);
        status_til = findViewById(R.id.status_input_til);
        name_til = findViewById(R.id.name_input_til);
        save_changes_btn = findViewById(R.id.save_changes_btn);
        progressDialog = new ProgressDialog(statusActivity.this);
        String old_status = getIntent().getStringExtra("old_status");
        String old_name = getIntent().getStringExtra("name");
        name_til.getEditText().setText(old_name);
        status_til.getEditText().setText(old_status);

        current_user = FirebaseAuth.getInstance().getCurrentUser();
        String current_uid = current_user.getUid();
        status_database = FirebaseDatabase.getInstance().getReference().child("Users").child(current_uid);
        current_user_database = FirebaseDatabase.getInstance().getReference().child("Users").child(current_uid);
        current_user_database.keepSynced(true);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Account Status");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        save_changes_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog.setTitle("Saving Your changes");
                progressDialog.setMessage("please wait while we save changes");
                progressDialog.show();

                String status = status_til.getEditText().getText().toString();
                String name = name_til.getEditText().getText().toString();
                Map userMap = new HashMap<>();
                userMap.put("name", name);
                userMap.put("status", status);

                status_database.updateChildren(userMap, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                        if (databaseError == null) {
                            progressDialog.dismiss();
                            finish();
                        } else {
                            progressDialog.hide();
                            Toast.makeText(getApplicationContext(), "unable to change details,Please try again later.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }
        });
    }
}
