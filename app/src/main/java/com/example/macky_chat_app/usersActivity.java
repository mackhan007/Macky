package com.example.macky_chat_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.squareup.picasso.Picasso;

import java.text.StringCharacterIterator;

import de.hdodenhof.circleimageview.CircleImageView;

public class usersActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private RecyclerView user_list_rv;
    private DatabaseReference user_database,current_user_database;
    private FirebaseAuth mAuth;
    private EditText search_et;
    private ImageView search_img;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);
        mAuth = FirebaseAuth.getInstance();
        toolbar = findViewById(R.id.users_appbar);
        user_list_rv = findViewById(R.id.users_list_rv);
        search_et = findViewById(R.id.users_search_et);
        search_img = findViewById(R.id.users_search_img);
        user_database = FirebaseDatabase.getInstance().getReference().child("Users");
        current_user_database = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());
        current_user_database.keepSynced(true);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("All Users");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        user_list_rv.setHasFixedSize(true);
        user_list_rv.setLayoutManager(new LinearLayoutManager(this));
        search_et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(count!=0){
                    String name = s.toString();
                    search_user(name);
                }else if(count == 0){
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

    private void search_user(String name) {
        Query search_queary = user_database.orderByChild("name").startAt(name).endAt(name + "\uf8ff");
        FirebaseRecyclerOptions<users> options = new FirebaseRecyclerOptions.Builder<users>().setQuery(search_queary,users.class).build();
        FirebaseRecyclerAdapter adapter = new FirebaseRecyclerAdapter<users, usersViewHolder>(options) {
            @Override
            public usersViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.users_single_layout, parent, false);

                return new usersViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull usersViewHolder usersViewHolder, int position,@NonNull users users) {
                usersViewHolder.setName(users.getName());
                usersViewHolder.setStatus(users.getStatus());
                usersViewHolder.setThumbImage(users.getThumb_image());
                final String user_id = getRef(position).getKey();
                usersViewHolder.view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent profile_view = new Intent(usersActivity.this,profileActivity.class);
                        profile_view.putExtra("from_user_id",user_id);
                        startActivity(profile_view);
                    }
                });
            }
        };
        adapter.startListening();
        user_list_rv.setAdapter(adapter);

    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseRecyclerOptions<users> options = new FirebaseRecyclerOptions.Builder<users>().setQuery(user_database,users.class).build();
        FirebaseRecyclerAdapter adapter = new FirebaseRecyclerAdapter<users, usersViewHolder>(options) {
            @Override
            public usersViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.users_single_layout, parent, false);

                return new usersViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull usersViewHolder usersViewHolder, int position,@NonNull users users) {
                usersViewHolder.setName(users.getName());
                usersViewHolder.setStatus(users.getStatus());
                usersViewHolder.setThumbImage(users.getThumb_image());
                final String user_id = getRef(position).getKey();
                usersViewHolder.view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent profile_view = new Intent(usersActivity.this,profileActivity.class);
                        profile_view.putExtra("from_user_id",user_id);
                        startActivity(profile_view);
                    }
                });
            }
        };
        adapter.startListening();
        user_list_rv.setAdapter(adapter);
    }

    public static class usersViewHolder extends RecyclerView.ViewHolder{
        View view;
        public usersViewHolder(@NonNull View itemView) {
            super(itemView);
            view = itemView;
        }
        public void setName(String name){
            TextView user_name_tv = view.findViewById(R.id.user_single_name) ;
            user_name_tv.setText(name);
        }

        public void setStatus(String status) {
            TextView user_status_tv = view.findViewById(R.id.user_single_status);
            user_status_tv.setText(status);
        }

        public void setThumbImage(String thumb_image) {
            if (!thumb_image.equals("default")){
                CircleImageView user_dp_img = view.findViewById(R.id.user_single_dp);
                Picasso.get().load(thumb_image).placeholder(R.drawable.dp).into(user_dp_img);
            }
        }
    }
}
