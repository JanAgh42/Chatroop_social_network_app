package com.example.chatroop;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class SearchActivity extends AppCompatActivity {

    private EditText search_input;
    private RecyclerView search_users;
    private DatabaseReference user_reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        search_input = findViewById(R.id.search_input);
        search_users = findViewById(R.id.search_all_users);

        user_reference = FirebaseDatabase.getInstance().getReference().child("Users");

        search_users.setHasFixedSize(true);
        search_users.setLayoutManager(new LinearLayoutManager(this));

        Toolbar search_toolbar = (Toolbar) findViewById(R.id.search_toolbar);
        setSupportActionBar(search_toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Vyhľadať používateľov");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            sendToMain();
        }
        return super.onOptionsItemSelected(item);
    }

    public void searchUser(View view){
        String search_input = this.search_input.getText().toString();

        if(TextUtils.isEmpty(search_input)){
            Toast.makeText(this, "Vyhľadávanie je prázdne.", Toast.LENGTH_LONG).show();
        }
        else{
            this.search_input.setText("");
            retrieveUsers(search_input);
        }
    }

    private void retrieveUsers(String search_input) {
            Query search_people = user_reference.orderByChild("Fullname")
                    .startAt(search_input).endAt(search_input + "\uf8ff");

            final FirebaseRecyclerAdapter<Users, userHolder> firebase_adapter = new FirebaseRecyclerAdapter<Users, userHolder>
                    (Users.class, R.layout.every_search, userHolder.class, search_people) {
                @Override
                protected void populateViewHolder(userHolder viewHolder, Users model, final int position) {

                    viewHolder.setProfileImage(model.getProfileImage(), getApplicationContext());
                    viewHolder.setFullname(model.getFullname());
                    viewHolder.setUsername(model.getUsername());

                    viewHolder.search_result.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String user_id = getRef(position).getKey();
                            sendToProfile(user_id);
                        }
                    });
                }
            };
            search_users.setAdapter(firebase_adapter);
            search_users.removeAllViews();
            firebase_adapter.notifyDataSetChanged();
    }

    public static class userHolder extends RecyclerView.ViewHolder{

        View main_view;
        LinearLayout search_result;

        public userHolder(View itemView){
            super(itemView);
            main_view = itemView;
            search_result = (LinearLayout)main_view.findViewById(R.id.search_searchbar);
        }

        void setProfileImage(String profileImage, Context context) {
            CircleImageView profile_photo = (CircleImageView)main_view.findViewById(R.id.search_profile_image);
            Picasso.with(context).load(profileImage).fit().centerInside().into(profile_photo);
        }

        public void setFullname(String fullname) {
            TextView full_name = (TextView)main_view.findViewById(R.id.search_fullname);
            full_name.setText(fullname);
        }

        public void setUsername(String username){
            TextView nick_name = (TextView)main_view.findViewById(R.id.search_nickname);
            nick_name.setText(username);
        }
    }

    private void sendToMain(){
        Intent to_main = new Intent(SearchActivity.this, MainActivity.class);
        startActivity(to_main);
    }

    private void sendToProfile(String user_id){
        Intent to_profile = new Intent(SearchActivity.this, ProfileActivity.class);
        to_profile.putExtra("UserId", user_id);
        startActivity(to_profile);
    }
}
