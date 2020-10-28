package com.example.macky_chat_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class loginActivity extends AppCompatActivity {

    private TextInputLayout email_til,password_til;
    private Button login_btn;
    private FirebaseAuth mAuth;
    private Toolbar toolbar;
    private ProgressDialog progressDialog;
    private DatabaseReference user_database;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        toolbar = findViewById(R.id.register_appbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Login to your account");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        progressDialog = new ProgressDialog(this);

        mAuth = FirebaseAuth.getInstance();

        email_til = findViewById(R.id.loginEmail_til);
        password_til = findViewById(R.id.loginPassword_til);
        login_btn = findViewById(R.id.login_btn);
        user_database = FirebaseDatabase.getInstance().getReference().child("Users");

        login_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email,password;
                email = email_til.getEditText().getText().toString();
                password = password_til.getEditText().getText().toString();

                if(!TextUtils.isEmpty(email) || !TextUtils.isEmpty(password)) {
                    progressDialog.setTitle("Logging In");
                    progressDialog.setMessage("please wait while we are logging you in");
                    progressDialog.setCanceledOnTouchOutside(false);
                    progressDialog.show();
                    login_user(email, password);
                }
                
            }
        });
    }

    private void login_user(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            progressDialog.dismiss();
                            String current_user_id = mAuth.getCurrentUser().getUid();
                            String devicesToken = FirebaseInstanceId.getInstance().getToken();
                            user_database.child(current_user_id).child("device_token").setValue(devicesToken).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        Intent mainintent = new Intent(loginActivity.this,MainActivity.class);
                                        mainintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(mainintent);
                                        finish();
                                    }else {
                                        Toast.makeText(loginActivity.this,"Unable To detect your device",Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        } else {
                            progressDialog.hide();
                            Toast.makeText(loginActivity.this,"Unable to log you in,please try after some time.",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
