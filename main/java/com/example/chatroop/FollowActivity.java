package com.example.chatroop;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

public class FollowActivity extends AppCompatActivity {

    private RecyclerView show_follow;
    private DatabaseReference follow_reference;

    private String follow_category, follow_user_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_follow);

        follow_category = getIntent().getExtras().get("category").toString();
        follow_user_id = getIntent().getExtras().get("user").toString();

        follow_reference = FirebaseDatabase.getInstance().getReference().child("Users").child(follow_user_id);

        show_follow = (RecyclerView)findViewById(R.id.show_all_follow);

        show_follow.setHasFixedSize(true);
        show_follow.setLayoutManager(new LinearLayoutManager(this));

        Toolbar follow_toolbar = findViewById(R.id.follow_toolbar);
        setSupportActionBar(follow_toolbar);

        if(follow_category.equals("followers")){
            getSupportActionBar().setTitle("Sledovatelia");
            retrieveFollowers("Followers");
        }
        else if(follow_category.equals("following")){
            getSupportActionBar().setTitle("Sledujem");
            retrieveFollowers("Following");
        }
        else if(follow_category.equals("nová správa")){
            getSupportActionBar().setTitle("Odoslať novú správu");
            retrieveFollowers("Following");
        }
    }

    private void retrieveFollowers(String follow_type) {
        Query search_people = follow_reference.child(follow_type).orderByChild("status").equalTo("friends");

        final FirebaseRecyclerAdapter<Users, SearchActivity.userHolder> firebase_adapter = new FirebaseRecyclerAdapter<Users, SearchActivity.userHolder>
                (Users.class, R.layout.every_search, SearchActivity.userHolder.class, search_people) {
            @Override
            protected void populateViewHolder(SearchActivity.userHolder viewHolder, Users model, final int position) {

                viewHolder.setProfileImage(model.getProfileImage(), getApplicationContext());
                viewHolder.setFullname(model.getFullname());
                viewHolder.setUsername(model.getUsername());

                viewHolder.search_result.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String user_id = getRef(position).getKey();
                        if(follow_category.equals("nová správa")){
                            sendToUserChat(user_id);
                        }
                        else {
                            sendToProfile(user_id);
                        }
                    }
                });
            }
        };
        firebase_adapter.startListening();
        show_follow.setAdapter(firebase_adapter);
    }

    private void sendToUserChat(String user_id){
        Intent to_user_chat = new Intent(FollowActivity.this, UserChatActivity.class);
        to_user_chat.putExtra("UserChat", user_id);
        startActivity(to_user_chat);
    }

    private void sendToProfile(String user_id){
        Intent to_profile = new Intent(FollowActivity.this, ProfileActivity.class);
        to_profile.putExtra("UserId", user_id);
        startActivity(to_profile);
    }
}
