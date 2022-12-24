package com.example.chatroop;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.github.chrisbanes.photoview.PhotoView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

public class UserPostsActivity extends AppCompatActivity {

    private RecyclerView recycler_view;
    private DatabaseReference post_reference, action_reference;
    private Query profile_user_posts;
    private ValueEventListener listener1, listener2, listener3;

    private String profile_user_id, current_user_id;
    private Boolean like = false, dislike = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_posts);

        profile_user_id = getIntent().getExtras().get("ProfileId").toString();
        current_user_id = getIntent().getExtras().get("UserId").toString();

        post_reference = FirebaseDatabase.getInstance().getReference().child("Posts");
        action_reference = FirebaseDatabase.getInstance().getReference().child("Likes and Dislikes");
        profile_user_posts = post_reference.orderByChild("Uid").equalTo(profile_user_id);

        Toolbar user_posts_toolbar = findViewById(R.id.user_posts_toolbar);
        setSupportActionBar(user_posts_toolbar);
        getSupportActionBar().setTitle("Príspevky");

        recycler_view = findViewById(R.id.user_posts_only);
        recycler_view.setHasFixedSize(true);
        LinearLayoutManager layout_manager  = new LinearLayoutManager(this);
        layout_manager .setReverseLayout(true);
        layout_manager.setStackFromEnd(true);
        recycler_view.setLayoutManager(layout_manager);

        showUserPost();
    }

    private void showUserPost() {
        FirebaseRecyclerAdapter<Posts, MainActivity.postsHolder> firebase_adapter = new FirebaseRecyclerAdapter<Posts, MainActivity.postsHolder>
                (Posts.class, R.layout.every_post, MainActivity.postsHolder.class, profile_user_posts) {
            @Override
            protected void populateViewHolder(final MainActivity.postsHolder viewHolder,final Posts model, int position) {

                final String post_id = getRef(position).getKey();

                viewHolder.setFullname(model.getFullname());
                viewHolder.setDate(model.getDate());
                viewHolder.setDescription(model.getDescription());
                //viewHolder.setProfileImage(getApplicationContext(), model.getProfileImage());
                viewHolder.setPostImage(getApplicationContext(), model.getPostImage());
                viewHolder.setUid(model.getUid(), getApplicationContext());
                viewHolder.setPostImageId(model.getPostImageId());
                viewHolder.setLikeStatus(post_id);
                viewHolder.setDislikeStatus(post_id);
                viewHolder.post_settings.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        PopupMenu popup = new PopupMenu(UserPostsActivity.this, v);
                        popup.inflate(R.menu.post_menu);
                        Menu popup_menu = popup.getMenu();
                        if(!current_user_id.equals(viewHolder.post_user_id)){
                            popup_menu.findItem(R.id.post_settings_del).setEnabled(false);
                            popup_menu.findItem(R.id.post_settings_edit).setEnabled(false);
                            popup_menu.findItem(R.id.post_settings_edit).setVisible(false);
                            popup_menu.findItem(R.id.post_settings_del).setVisible(false);
                        }
                        else{
                            popup_menu.findItem(R.id.post_settings_hide).setEnabled(false);
                            popup_menu.findItem(R.id.post_settings_report).setEnabled(false);
                            popup_menu.findItem(R.id.post_settings_hide).setVisible(false);
                            popup_menu.findItem(R.id.post_settings_report).setVisible(false);
                        }
                        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                postSettingsSelector(item, post_id, viewHolder.post_image_id);
                                return false;
                            }
                        });
                        popup.show();
                    }
                });
                viewHolder.prof_image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        sendToProfile(viewHolder.post_user_id);
                    }
                });
                viewHolder.image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String post_image_path = viewHolder.image.getTag().toString();
                        AlertDialog.Builder mBuilder = new AlertDialog.Builder(UserPostsActivity.this);
                        LayoutInflater inflater = getLayoutInflater();
                        View dialogLayout = inflater.inflate(R.layout.pinch_to_zoom_pics, null);
                        PhotoView photo_view = (PhotoView)dialogLayout.findViewById(R.id.pinch_image);
                        Picasso.with(getApplicationContext()).load(post_image_path).into(photo_view);
                        AlertDialog mDialog = mBuilder.create();
                        mDialog.setView(dialogLayout);
                        mDialog.show();
                    }
                });
                viewHolder.post_like.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        like = true;
                        listener1 = new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if(like.equals(true)){
                                    if(dataSnapshot.child(post_id).hasChild(current_user_id)){
                                        action_reference.child("Likes").child(post_id).child(current_user_id).removeValue();
                                        like = false;
                                    }
                                    else{
                                        action_reference.child("Likes").child(post_id).child(current_user_id).setValue(true);
                                        action_reference.child("Dislikes").child(post_id).child(current_user_id).removeValue();
                                        like = false;
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        };
                        action_reference.child("Likes").addValueEventListener(listener1);
                    }
                });
                viewHolder.post_dislike.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dislike = true;
                        listener2 = new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if(dislike.equals(true)){
                                    if(dataSnapshot.child(post_id).hasChild(current_user_id)){
                                        action_reference.child("Dislikes").child(post_id).child(current_user_id).removeValue();
                                        dislike = false;
                                    }
                                    else{
                                        action_reference.child("Dislikes").child(post_id).child(current_user_id).setValue(true);
                                        action_reference.child("Likes").child(post_id).child(current_user_id).removeValue();
                                        dislike = false;
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        };
                        action_reference.child("Dislikes").addValueEventListener(listener1);
                    }
                });
                viewHolder.post_comment.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent to_comments = new Intent(UserPostsActivity.this, CommentActivity.class);
                        to_comments.putExtra("PostId", post_id);
                        startActivity(to_comments);
                    }
                });
            }
        };
        firebase_adapter.startListening();
        recycler_view.setAdapter(firebase_adapter);
    }

    public void postSettingsSelector(MenuItem item, final String post_id, final String post_image_id){
        switch(item.getItemId()){
            case R.id.post_settings_del:
                AlertDialog.Builder dialog_builder = new AlertDialog.Builder(this);
                dialog_builder.setTitle("Zmazať príspevok")
                        .setMessage("Ste si istý/á, že chcete zmazať tento príspevok?")
                        .setPositiveButton("Zmazať", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                deleteUserPost(post_id, post_image_id);
                            }
                        }).setNegativeButton("Späť", null);
                AlertDialog alert = dialog_builder.create();
                alert.show();
                break;
            case R.id.post_settings_report:
                Intent to_report = new Intent(UserPostsActivity.this, ReportActivity.class);
                to_report.putExtra("PostId", post_id);
                startActivity(to_report);
                break;
            case R.id.post_settings_edit:
                Intent to_post_settings = new Intent(UserPostsActivity.this, PostSettingsActivity.class);
                to_post_settings.putExtra("PostId", post_id);
                startActivity(to_post_settings);
                break;
            case R.id.post_settings_hide:
                Toast.makeText(UserPostsActivity.this, "Skryť", Toast.LENGTH_LONG).show();
                break;
        }
    }

    private void deleteUserPost(final String post_id, final String post_image_id) {
        post_reference.child(post_id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    StorageReference image_del_reference = FirebaseStorage.getInstance().getReference().child("Post Images").child(post_image_id);
                    image_del_reference.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                listener3 = new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if(dataSnapshot.exists()){
                                            if(dataSnapshot.child("Dislikes").hasChild(post_id)){
                                                action_reference.child("Dislikes").child(post_id).removeValue();
                                            }
                                            if(dataSnapshot.child("Likes").hasChild(post_id)){
                                                action_reference.child("Likes").child(post_id).removeValue();
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                };
                                action_reference.addValueEventListener(listener3);
                            }
                        }
                    });
                }
            }
        });
    }

    private void sendToProfile(String user_id){
        Intent to_profile = new Intent(UserPostsActivity.this, ProfileActivity.class);
        to_profile.putExtra("UserId", user_id);
        startActivity(to_profile);
    }

    private void detachListeners() {
        if (listener1 != null) {
            action_reference.child("Likes").removeEventListener(listener1);
        }
        if (listener2 != null) {
            action_reference.child("Dislikes").removeEventListener(listener2);
        }
        if (listener3 != null) {
            action_reference.removeEventListener(listener3);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            detachListeners();
        }catch (Exception E){
            E.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            detachListeners();
        }catch (Exception E){
            E.printStackTrace();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            detachListeners();
        }catch (Exception E){
            E.printStackTrace();
        }
    }
}
