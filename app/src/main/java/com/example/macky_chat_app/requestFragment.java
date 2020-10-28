package com.example.macky_chat_app;

import android.content.Intent;
import android.graphics.Typeface;
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
public class requestFragment extends Fragment {

    private RecyclerView friend_requests_list_rv;
    private DatabaseReference friends_request_database, users_database;
    private FirebaseAuth mAuth;
    private String current_usser_id;
    private View mainview;
    private boolean request_status = false;
    public requestFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mainview = inflater.inflate(R.layout.fragment_request, container, false);
        friend_requests_list_rv = mainview.findViewById(R.id.friend_requests_list_rv);
        mAuth = FirebaseAuth.getInstance();
        current_usser_id = mAuth.getCurrentUser().getUid();
        friends_request_database = FirebaseDatabase.getInstance().getReference().child("Friend_request").child(current_usser_id);
        friends_request_database.keepSynced(true);
        users_database = FirebaseDatabase.getInstance().getReference().child("Users");
        users_database.keepSynced(true);
        friend_requests_list_rv.setHasFixedSize(true);
        friend_requests_list_rv.setLayoutManager(new LinearLayoutManager(getContext()));

        return mainview;
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseRecyclerOptions<friendRequest> options = new FirebaseRecyclerOptions.Builder<friendRequest>().setQuery(friends_request_database, friendRequest.class).build();
        FirebaseRecyclerAdapter adapter = new FirebaseRecyclerAdapter<friendRequest, requestViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final requestViewHolder requestViewHolder, int position, @NonNull friendRequest friendRequest) {
                final String request_type = friendRequest.getRequest_type();
                    final String list_user_id = getRef(position).getKey();
                    users_database.child(list_user_id).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            String user_name = dataSnapshot.child("name").getValue().toString();
                            String status = dataSnapshot.child("status").getValue().toString();
                            String thumb_image = dataSnapshot.child("thumb_image").getValue().toString();
                            requestViewHolder.setName(user_name);
                            requestViewHolder.setThumbImage(thumb_image);
                            requestViewHolder.setStatus(request_type);
                            requestViewHolder.view.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent viewprofile = new Intent(getContext(), profileActivity.class);
                                    viewprofile.putExtra("from_user_id", list_user_id);
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
            public requestViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.users_single_layout, parent, false);

                return new requestViewHolder(view);
            }
        };
        adapter.startListening();
        friend_requests_list_rv.setAdapter(adapter);
    }
}
class  requestViewHolder extends RecyclerView.ViewHolder {
    View view;

    public requestViewHolder(@NonNull View itemView) {
        super(itemView);
        view = itemView;
    }
    public void setName(String name) {
        TextView user_name_tv = view.findViewById(R.id.user_single_name);
        user_name_tv.setText(name);
    }
    public void setThumbImage(String thumb_image) {
        if (!thumb_image.equals("default")) {
            CircleImageView user_dp_img = view.findViewById(R.id.user_single_dp);
            Picasso.get().load(thumb_image).placeholder(R.drawable.dp).into(user_dp_img);
        }
    }

    public void setStatus(String status) {
        TextView user_status_tv = view.findViewById(R.id.user_single_status);
        user_status_tv.setText("Friend request "+status);
    }
}
