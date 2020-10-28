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
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class settingsActivity extends AppCompatActivity {

    private static final int MAX_LENGTH = 20000;
    private CircleImageView dp_img;
    private TextView name_tv, status_tv;
    private Button change_status;
    private DatabaseReference user_database,current_user_database;
    private FirebaseUser current_user;
    private static final int GALLERY_PICK = 1;
    private StorageReference profile_image_storage;
    private ProgressDialog progressDialog;
    private Toolbar toolbar;
    private boolean local_status = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        dp_img = findViewById(R.id.settings_dp_img);
        name_tv = findViewById(R.id.settings_name_tv);
        status_tv = findViewById(R.id.settings_status_tv);
        change_status = findViewById(R.id.change_status_btn);
        toolbar = findViewById(R.id.settings_appbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        profile_image_storage = FirebaseStorage.getInstance().getReference();

        current_user = FirebaseAuth.getInstance().getCurrentUser();
        String current_uid = current_user.getUid();
        user_database = FirebaseDatabase.getInstance().getReference().child("Users").child(current_uid);
        current_user_database = FirebaseDatabase.getInstance().getReference().child("Users").child(current_uid);
        current_user_database.keepSynced(true);
        //offline capabilities
        user_database.keepSynced(true);
        user_database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final String name, status, image, thumb_image;
                name = dataSnapshot.child("name").getValue().toString();
                status = dataSnapshot.child("status").getValue().toString();
                image = dataSnapshot.child("image").getValue().toString();
                thumb_image = dataSnapshot.child("thumb_image").getValue().toString();
                name_tv.setText(name);
                status_tv.setText(status);
                if (!image.equals("default")) {
                    Picasso.get().load(image).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.dp).into(dp_img, new Callback() {
                        @Override
                        public void onSuccess() {
                        }
                        @Override
                        public void onError(Exception e) {
                            Picasso.get().load(image).placeholder(R.drawable.dp).into(dp_img);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
        change_status.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent change_status = new Intent(settingsActivity.this, statusActivity.class);
                String status = status_tv.getText().toString();
                String name = name_tv.getText().toString();
                change_status.putExtra("old_status", status);
                change_status.putExtra("name", name);
                local_status = true;
                startActivity(change_status);
            }
        });
        dp_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                  /*
                Intent set_image = new Intent();
                set_image.setType("image/*");
                set_image.setAction(Intent.ACTION_GET_CONTENT);

                startActivityForResult(Intent.createChooser(set_image, "select image"), GALLERY_PICK);
              */
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1,1)
                        .start(settingsActivity.this);


            }

        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_PICK && resultCode == RESULT_OK) {
            Uri imageUri = data.getData();
            CropImage.activity(imageUri)
                    .setAspectRatio(1, 1)
                    .start(settingsActivity.this);
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                progressDialog = new ProgressDialog(settingsActivity.this);
                progressDialog.setTitle("Uploading Profile Image");
                progressDialog.setMessage("please wait while we are uploading your profile image");
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();


                Uri resultUri = result.getUri();
                File thumb_filePath = new File(resultUri.getPath());
                String current_user_id = current_user.getUid();
                Bitmap thub_bitmap = new Compressor(this)
                        .setMaxWidth(200).setMaxHeight(200).setQuality(65)
                        .compressToBitmap(thumb_filePath);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                thub_bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                final byte[] thumb_byte = baos.toByteArray();
                StorageReference image_path = profile_image_storage.child("profile_images").child(current_user_id + ".jpg");
                final StorageReference thumb_path = profile_image_storage.child("profile_images").child("thumbs").child(current_user_id + ".jpg");

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
                                        Toast.makeText(settingsActivity.this, "unable to upload thumbnail", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                        } else {
                            progressDialog.hide();
                            Toast.makeText(settingsActivity.this, "unable to upload image", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    private void uploadThumbfile() {
        String current_user_id = current_user.getUid();
        StorageReference thumb_path = profile_image_storage.child("profile_images").child("thumbs").child(current_user_id + ".jpg");
        final String[] profileThumb_url = new String[1];
        thumb_path.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Uri downloadUrl = uri;
                profileThumb_url[0] = downloadUrl.toString();
                user_database.child("thumb_image").setValue(profileThumb_url[0]).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            uploadfile();
                        } else {
                            progressDialog.hide();
                            Toast.makeText(getBaseContext(), "Unable to uploaded thumbnail,Please try again later", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }

    private void uploadfile() {
        String current_user_id = current_user.getUid();
        StorageReference image_path = profile_image_storage.child("profile_images").child(current_user_id + ".jpg");
        final String[] profile_url = new String[1];
        image_path.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Uri downloadUrl = uri;
                profile_url[0] = downloadUrl.toString();
                user_database.child("image").setValue(profile_url[0]).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            progressDialog.dismiss();
                            Toast.makeText(getBaseContext(), "profile pic uploaded successfully", Toast.LENGTH_SHORT).show();
                        } else {
                            progressDialog.hide();
                            Toast.makeText(getBaseContext(), "Unable to uploaded profile pic,Please try again later", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }
}
