package com.example.macky_chat_app;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class registerActivity extends AppCompatActivity {

    private TextInputLayout name_til,email_til,password_til;
    private Button createaccount_btn;
    private FirebaseAuth mAuth;
    private Toolbar toolbar;
    private ProgressDialog progressDialog;
    private DatabaseReference database;
    private FirebaseUser current_user;
    private CircleImageView register_dp_img;
    private boolean dp_click = false;
    private Uri resultUri;
    private byte[] thumb_byte;
    private String current_user_id;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        toolbar = findViewById(R.id.register_appbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Register");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        progressDialog = new ProgressDialog(this);

        mAuth = FirebaseAuth.getInstance();

        name_til = findViewById(R.id.registerName_til);
        email_til = findViewById(R.id.loginEmail_til);
        password_til = findViewById(R.id.loginPassword_til);
        createaccount_btn = findViewById(R.id.login_btn);
        register_dp_img = findViewById(R.id.register_dp_img);

        register_dp_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1,1)
                        .start(registerActivity.this);
            }
        });

        createaccount_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name,email,password;
                name = name_til.getEditText().getText().toString();
                email = email_til.getEditText().getText().toString();
                password = password_til.getEditText().getText().toString();

                if(!TextUtils.isEmpty(name) || !TextUtils.isEmpty(email) || !TextUtils.isEmpty(password)){
                    progressDialog.setTitle("Registering User");
                    progressDialog.setMessage("Logging you in....");
                    progressDialog.setCanceledOnTouchOutside(false);
                    progressDialog.show();
                    register_user(name,email,password);
                }

            }
        });
    }

    private void register_user(final String name, String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            current_user = FirebaseAuth.getInstance().getCurrentUser();
                            String devicesToken = FirebaseInstanceId.getInstance().getToken();
                            current_user_id = current_user.getUid();
                            database = FirebaseDatabase.getInstance().getReference().child("Users").child(current_user_id);
                            String default_dp = "https://firebasestorage.googleapis.com/v0/b/chat-app-3bbfc.appspot.com/o/profile_images%2Fdp.jpg?alt=media&token=33f49eb4-9940-45d8-9a02-884572c10dcc";

                            HashMap<String, String> userMap = new HashMap<>();
                            userMap.put("name",name);
                            userMap.put("status","Hi there.I'm using Macky.");
                            userMap.put("image",default_dp);
                            userMap.put("thumb_image",default_dp);
                            userMap.put("device_token",devicesToken);

                            database.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        if(dp_click){
                                            setimage();Intent mainpage = new Intent(registerActivity.this, MainActivity.class);
                                            mainpage.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                            startActivity(mainpage);
                                            finish();
                                        }else {
                                            progressDialog.dismiss();
                                        }
                                    }
                                }
                            });
                        } else {
                            progressDialog.hide();
                            Toast.makeText(registerActivity.this,"Unable to create an account,please try after some time.",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                resultUri = result.getUri();
                File thumb_filePath = new File(resultUri.getPath());
                Bitmap thub_bitmap = new Compressor(this)
                        .setMaxWidth(200).setMaxHeight(200).setQuality(65)
                        .compressToBitmap(thumb_filePath);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                thub_bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                thumb_byte = baos.toByteArray();
                dp_click = true;
                Picasso.get().load(resultUri).into(register_dp_img);
            }
        }
    }

    private void setimage(){
        StorageReference image_path = FirebaseStorage.getInstance().getReference().child("profile_images").child(current_user_id + ".jpg");
        final StorageReference thumb_path = FirebaseStorage.getInstance().getReference().child("profile_images").child("thumbs").child(current_user_id + ".jpg");
        image_path.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful()) {
                    UploadTask uploadTask = thumb_path.putBytes(thumb_byte);
                    uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if (task.isSuccessful()) {
                                uploadThumbfile();
                            } else {
                                progressDialog.hide();
                                Toast.makeText(registerActivity.this, "unable to upload thumbnail", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                } else {
                    progressDialog.hide();
                    Toast.makeText(registerActivity.this, "unable to upload image", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    private void uploadThumbfile() {
        StorageReference thumb_path = FirebaseStorage.getInstance().getReference().child("profile_images").child("thumbs").child(current_user_id + ".jpg");
        final String[] profileThumb_url = new String[1];
        thumb_path.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Uri downloadUrl = uri;
                profileThumb_url[0] = downloadUrl.toString();
                FirebaseDatabase.getInstance().getReference().child("Users").child(current_user_id).child("thumb_image").setValue(profileThumb_url[0]).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            uploadfile();
                        } else {
                            progressDialog.hide();
                            Toast.makeText(registerActivity.this, "Unable to uploaded thumbnail,Please try again later", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }
    private void uploadfile() {
        StorageReference image_path = FirebaseStorage.getInstance().getReference().child("profile_images").child(current_user_id + ".jpg");
        final String[] profile_url = new String[1];
        image_path.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Uri downloadUrl = uri;
                profile_url[0] = downloadUrl.toString();
                FirebaseDatabase.getInstance().getReference().child("Users").child(current_user_id).child("image").setValue(profile_url[0]).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            progressDialog.dismiss();
                            Toast.makeText(registerActivity.this, "profile pic uploaded successfully", Toast.LENGTH_SHORT).show();
                            Intent mainpage = new Intent(registerActivity.this, MainActivity.class);
                            mainpage.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(mainpage);
                            finish();
                        } else {
                            progressDialog.hide();
                            Toast.makeText(registerActivity.this, "Unable to uploaded profile pic,Please try again later", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }

}
