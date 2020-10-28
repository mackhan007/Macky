package com.example.macky_chat_app;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class friendsFragment extends Fragment {

    private RecyclerView friends_list_rv;
    private DatabaseReference friends_database,users_database;
    private FirebaseAuth mAuth;
    private String current_usser_id;
    private View mainView;
    public friendsFragment() {
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mainView = inflater.inflate(R.layout.fragment_friends, container, false);
        friends_list_rv = mainView.findViewById(R.id.friends_list_rv);
        mAuth = FirebaseAuth.getInstance();
        current_usser_id = mAuth.getCurrentUser().getUid();
        friends_database = FirebaseDatabase.getInstance().getReference().child("Friends").child(current_usser_id);
        friends_database.keepSynced(true);
        users_database = FirebaseDatabase.getInstance().getReference().child("Users");
        users_database.keepSynced(true);
        friends_list_rv.setHasFixedSize(true);
        friends_list_rv.setLayoutManager(new LinearLayoutManager(getContext()));

        return mainView;
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseRecyclerOptions<friends> options = new FirebaseRecyclerOptions.Builder<friends>().setQuery(friends_database,friends.class).build();
        FirebaseRecyclerAdapter adapter = new FirebaseRecyclerAdapter<friends, friendsFragment.friendsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final friendsViewHolder friendsViewHolder, final int position, @NonNull friends friends) {
                String date = friends.getDate();
                final String list_user_id = getRef(position).getKey();
                users_database.child(list_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String user_name = dataSnapshot.child("name").getValue().toString();
                        String status = dataSnapshot.child("status").getValue().toString();
                        String thumb_image = dataSnapshot.child("thumb_image").getValue().toString();
                        if(dataSnapshot.hasChild("online")){
                            String user_online_status = dataSnapshot.child("online").getValue().toString();
                            friendsViewHolder.setUserStatus(user_online_status);
                        }
                        friendsViewHolder.setName(user_name);
                        friendsViewHolder.setThumbImage(thumb_image);
                        final String user_id = getRef(position).getKey();
                        friendsViewHolder.view.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent viewprofile = new Intent(getContext(),friendsProfileViewActivity.class);
                                viewprofile.putExtra("user_id",user_id);
                                startActivity(viewprofile);
                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });
            }

            @Override
            public friendsFragment.friendsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.users_single_layout, parent, false);

                return new friendsFragment.friendsViewHolder(view);
            }
        };
        adapter.startListening();
        friends_list_rv.setAdapter(adapter);
    }
    public static class friendsViewHolder extends RecyclerView.ViewHolder{
        View view;
        public friendsViewHolder(@NonNull View itemView) {
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

        public void setUserStatus(String user_online_status) {
            ImageView user_online_status_img = view.findViewById(R.id.user_single_online_img);
            ImageView user_offline_status_img = view.findViewById(R.id.user_single_offline_img);
            TextView user_status_tv = view.findViewById(R.id.user_single_status);
            user_online_status_img.setVisibility(View.INVISIBLE);
            user_offline_status_img.setVisibility(View.VISIBLE);
            if(user_online_status.equals("true")){
                user_online_status_img.setVisibility(View.VISIBLE);
                user_offline_status_img.setVisibility(View.INVISIBLE);
                user_status_tv.setText("online");
            }else {
                user_online_status_img.setVisibility(View.INVISIBLE);
                user_offline_status_img.setVisibility(View.VISIBLE);
                user_status_tv.setText("offline");
            }
        }
    }
}
