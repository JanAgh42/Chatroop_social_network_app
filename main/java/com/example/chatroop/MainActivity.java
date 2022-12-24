package com.example.chatroop;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.github.chrisbanes.photoview.PhotoView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recycler_view;
    private ActionBarDrawerToggle toolbar_toggle;
    private FirebaseAuth firebase_auth;
    private DatabaseReference user_reference, post_reference, action_reference;
    private CircleImageView profile_image, color_button;
    private TextView profile_user_name;
    private RelativeLayout nav_layout;
    private ValueEventListener listener1, listener2, listener3, listener7, listener8;

    private String token_id;
    static String current_user_id;
    private Boolean like = false, dislike = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firebase_auth = FirebaseAuth.getInstance();
        current_user_id = firebase_auth.getCurrentUser().getUid();
        user_reference = FirebaseDatabase.getInstance().getReference().child("Users");
        post_reference = FirebaseDatabase.getInstance().getReference().child("Posts");
        action_reference = FirebaseDatabase.getInstance().getReference().child("Likes and Dislikes");

        Toolbar home_toolbar = findViewById(R.id.home_toolbar);
        setSupportActionBar(home_toolbar);
        getSupportActionBar().setTitle("Hlavná stránka");

        DrawerLayout drawer_layout = findViewById(R.id.drawer_layout);
        NavigationView navigation_view = findViewById(R.id.nav_view);

        toolbar_toggle = new ActionBarDrawerToggle(MainActivity.this, drawer_layout,
                R.string.drawer_open, R.string.drawer_close);
        drawer_layout.addDrawerListener(toolbar_toggle);
        toolbar_toggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        recycler_view = findViewById(R.id.user_posts);
        recycler_view.setHasFixedSize(true);
        LinearLayoutManager layout_manager  = new LinearLayoutManager(this);
        layout_manager .setReverseLayout(true);
        layout_manager.setStackFromEnd(true);
        recycler_view.setLayoutManager(layout_manager);

        View nav_view = navigation_view.inflateHeaderView(R.layout.nav_header);

        profile_image = nav_view.findViewById(R.id.nav_profile_img);
        color_button = nav_view.findViewById(R.id.nav_color);
        nav_layout = nav_view.findViewById(R.id.nav_main);
        profile_user_name = nav_view.findViewById(R.id.nav_name);

        user_reference.child(current_user_id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    if(dataSnapshot.hasChild("Fullname")) {
                        String username = dataSnapshot.child("Fullname").getValue().toString();
                        profile_user_name.setText(username);
                    }
                    if(dataSnapshot.hasChild("ProfileImage")) {
                        String image = dataSnapshot.child("ProfileImage").getValue().toString();

                        Picasso.with(MainActivity.this).load(image).fit().centerInside().placeholder(R.drawable.avatar)
                                .into(profile_image);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        navigation_view.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                userMenuSelector(item);
                return false;
            }
        });

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel(current_user_id, "channel_name", NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("channel_desc");
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
            @Override
            public void onComplete(@NonNull Task<InstanceIdResult> task) {
                if(task.isSuccessful()){
                    token_id = task.getResult().getToken();
                    user_reference.child(current_user_id).child("Token").setValue(token_id).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Log.i("TOKEN", "Saved successfully");
                            }
                            else{
                                String error = task.getException().getMessage();
                                Log.i("TOKEN", "Error: " + error);
                            }
                        }
                    });
                }
                else{
                    String error = task.getException().getMessage();
                    Toast.makeText(MainActivity.this, error, Toast.LENGTH_LONG).show();
                }
            }
        });

        FirebaseMessaging.getInstance().subscribeToTopic("news").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Log.i("TOPIC NEWS", "Subscribed successfully");
                }
                else{
                    String error = task.getException().getMessage();
                    Log.i("TOPIC NEWS", "Error: " + error);
                }
            }
        });

        setNewColor();
        showEveryPost();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(firebase_auth.getCurrentUser() == null){
            sendToLogin();
        }
        else{
            checkUserExistence();
        }
    }

    private void checkUserExistence() {
        listener1 = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!dataSnapshot.hasChild(current_user_id)){
                    sendToSetup();
                }
                else if(!dataSnapshot.child(current_user_id).hasChild("Fullname")){
                    sendToSetup();
                }
                else if(!dataSnapshot.child(current_user_id).hasChild("ProfileImage")){
                    sendToSetup();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        user_reference.addValueEventListener(listener1);
    }

    private void showEveryPost() {
        Query sort_posts = post_reference.orderByChild("PostNumber");

        FirebaseRecyclerAdapter<Posts, postsHolder> firebase_adapter = new FirebaseRecyclerAdapter<Posts, postsHolder>
                (Posts.class, R.layout.every_post, postsHolder.class, sort_posts) {
            @Override
            protected void populateViewHolder(final postsHolder viewHolder, final Posts model, int position) {

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
                        PopupMenu popup = new PopupMenu(MainActivity.this, v);
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
                        AlertDialog.Builder mBuilder = new AlertDialog.Builder(MainActivity.this);
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
                        listener2 = new ValueEventListener() {
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
                        action_reference.child("Likes").addValueEventListener(listener2);
                    }
                });
                viewHolder.post_dislike.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dislike = true;
                        listener3 = new ValueEventListener() {
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
                        action_reference.child("Dislikes").addValueEventListener(listener3);
                    }
                });
                viewHolder.post_comment.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent to_comments = new Intent(MainActivity.this, CommentActivity.class);
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
                Intent to_report = new Intent(MainActivity.this, ReportActivity.class);
                to_report.putExtra("PostId", post_id);
                startActivity(to_report);
                break;
            case R.id.post_settings_edit:
                Intent to_post_settings = new Intent(MainActivity.this, PostSettingsActivity.class);
                to_post_settings.putExtra("PostId", post_id);
                startActivity(to_post_settings);
                break;
            case R.id.post_settings_hide:
                Toast.makeText(MainActivity.this, "Skryť", Toast.LENGTH_LONG).show();
                break;
        }
    }

    public static class postsHolder extends RecyclerView.ViewHolder{

        View main_view;
        ImageButton post_settings, post_like, post_dislike, post_comment;
        CircleImageView prof_image;
        static String post_user_id, post_image_id, current_user_id;
        int like_counter, dislike_counter;
        static DatabaseReference action_reference, post_user_reference;
        static ValueEventListener listener4, listener5, listener6;
        TextView number_of_likes, number_of_dislikes;
        ImageView image;

        public postsHolder(View itemView) {
            super(itemView);
            main_view = itemView;
            post_settings = main_view.findViewById(R.id.epost_settings);
            post_like = main_view.findViewById(R.id.post_like);
            post_dislike = main_view.findViewById(R.id.post_dislike);
            post_comment = main_view.findViewById(R.id.post_comment);
            number_of_likes = main_view.findViewById(R.id.num_of_likes);
            number_of_dislikes = main_view.findViewById(R.id.num_of_dislikes);
            prof_image = main_view.findViewById(R.id.epost_profile_image);

            action_reference = FirebaseDatabase.getInstance().getReference().child("Likes and Dislikes");
            post_user_reference = FirebaseDatabase.getInstance().getReference().child("Users");
            current_user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

        void setLikeStatus(final String post_id){
            listener4 = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.child(post_id).hasChild(current_user_id)){
                        like_counter = (int) dataSnapshot.child(post_id).getChildrenCount();
                        post_like.setImageResource(R.drawable.ic_sentiment_satisfied_used_24dp);
                        number_of_likes.setText((Integer.toString(like_counter) + (" páči sa mi to")));
                    }
                    else{
                        like_counter = (int) dataSnapshot.child(post_id).getChildrenCount();
                        post_like.setImageResource(R.drawable.ic_sentiment_satisfied_black_24dp);
                        number_of_likes.setText((Integer.toString(like_counter) + (" páči sa mi to")));
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            };
            action_reference.child("Likes").addValueEventListener(listener4);
        }

        void setDislikeStatus(final String post_id){
            listener5 = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.child(post_id).hasChild(current_user_id)){
                        dislike_counter = (int) dataSnapshot.child(post_id).getChildrenCount();
                        post_dislike.setImageResource(R.drawable.ic_sentiment_dissatisfied_used_24dp);
                        number_of_dislikes.setText((Integer.toString(dislike_counter) + (" nepáči sa mi to")));
                    }
                    else{
                        dislike_counter = (int) dataSnapshot.child(post_id).getChildrenCount();
                        post_dislike.setImageResource(R.drawable.ic_sentiment_dissatisfied_black_24dp);
                        number_of_dislikes.setText((Integer.toString(dislike_counter) + (" nepáči sa mi to")));
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            };
            action_reference.child("Dislikes").addValueEventListener(listener5);
        }

        void setUid(String uid, final Context context){
            post_user_id = uid;
            listener6 = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.hasChild("ProfileImage")){
                        String image = dataSnapshot.child("ProfileImage").getValue().toString();

                        Picasso.with(context).load(image).fit().centerInside().into(prof_image);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            };
            post_user_reference.child(post_user_id).addValueEventListener(listener6);
        }

        void setPostImageId(String post_image_id){
            this.post_image_id = post_image_id;
        }

        public void setFullname(String fullname){
            TextView username = main_view.findViewById(R.id.epost_username);
            username.setText(fullname);
        }

        /*public void setProfileImage(Context context, String profile_image){

        }*/

        public void setDate(String date){
            TextView post_date = main_view.findViewById(R.id.epost_date);
            post_date.setText(date);
        }

        void setDescription(String description){
            TextView post_desc = main_view.findViewById(R.id.epost_desc);
            post_desc.setText(description);
        }

        void setPostImage(Context context, String post_image){
            image = main_view.findViewById(R.id.epost_post_image);
            Picasso.with(context).load(post_image).resizeDimen(R.dimen.post_image_height, R.dimen.post_image_height).centerInside().onlyScaleDown().into(image);
            image.setTag(post_image);
        }
    }

    private void sendToSetup() {
        Intent to_setup = new Intent(MainActivity.this, AccSetupActivity.class);
        to_setup.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(to_setup);
        finish();
    }

    private void sendToLogin() {
        Intent to_login = new Intent(MainActivity.this, LoginActivity.class);
        to_login.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(to_login);
        finish();
    }

    private void sendToPost(){
        Intent to_post = new Intent(MainActivity.this, PostActivity.class);
        startActivity(to_post);
    }

    private void sendToMessages(){
        Intent to_messages = new Intent(MainActivity.this, MessagesActivity.class);
        startActivity(to_messages);
    }

    private void sendToProfile(String user_id){
        Intent to_profile = new Intent(MainActivity.this, ProfileActivity.class);
        to_profile.putExtra("UserId", user_id);
        startActivity(to_profile);
    }

    private void sendToSearch(){
        Intent to_search = new Intent(MainActivity.this, SearchActivity.class);
        startActivity(to_search);
    }

    private void sendToSettings(){
        Intent to_settings = new Intent(MainActivity.this, SettingsActivity.class);
        startActivity(to_settings);
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
                                    listener7 = new ValueEventListener() {
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
                                    action_reference.addValueEventListener(listener7);
                                }
                            }
                        });
                }
            }
        });
    }

    public void changeColor(View view){
        PopupMenu popup = new PopupMenu(MainActivity.this, view);
        popup.inflate(R.menu.color_menu);

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.nav_color_blue:
                        changeColorTo("blue");
                        break;
                    case R.id.nav_color_cyan:
                        changeColorTo("cyan");
                        break;
                    case R.id.nav_color_green:
                        changeColorTo("green");
                        break;
                    case R.id.nav_color_pink:
                        changeColorTo("pink");
                        break;
                    case R.id.nav_color_red:
                        changeColorTo("red");
                        break;
                    case R.id.nav_color_yellow:
                        changeColorTo("yellow");
                        break;
                }
                return false;
            }
        });
        popup.show();
    }

    private void changeColorTo(String color){
        user_reference.child(current_user_id).child("Navigation color").setValue(color).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Toast.makeText(MainActivity.this, "Farba bola úspešne zmenená.", Toast.LENGTH_SHORT).show();
                setNewColor();
            }
        });
    }

    private void setNewColor(){
        listener8 = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild("Navigation color")){
                    String color = dataSnapshot.child("Navigation color").getValue().toString();
                    if(color.equals("blue")){
                        nav_layout.setBackgroundColor(Color.BLUE);
                    }
                    if(color.equals("cyan")){
                        nav_layout.setBackgroundColor(Color.CYAN);
                    }
                    if(color.equals("green")){
                        nav_layout.setBackgroundColor(Color.GREEN);
                    }
                    if(color.equals("pink")){
                        nav_layout.setBackgroundColor(Color.MAGENTA);
                    }
                    if(color.equals("red")){
                        nav_layout.setBackgroundColor(Color.RED);
                    }
                    if(color.equals("yellow")){
                        nav_layout.setBackgroundColor(Color.YELLOW);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        user_reference.child(current_user_id).addValueEventListener(listener8);
    }

    public void addNewPost(View view){
        sendToPost();
    }

    private void userMenuSelector(MenuItem item) {
        switch(item.getItemId()){
            case R.id.nav_profile:
                sendToProfile(current_user_id);
                break;
            case R.id.nav_search:
                sendToSearch();
                break;
            case R.id.nav_settings:
                sendToSettings();
                break;
            case R.id.nav_post:
                sendToPost();
                break;
            case R.id.nav_log_out:
                AlertDialog.Builder dialog_builder = new AlertDialog.Builder(this);
                dialog_builder.setTitle("Odhlásiť sa")
                        .setMessage("Ste si istý/á, že sa chcete odhlásiť?")
                        .setPositiveButton("Odhlásiť sa", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                firebase_auth.signOut();
                                sendToLogin();
                            }
                        }).setNegativeButton("Späť", null);
                AlertDialog alert = dialog_builder.create();
                alert.show();
                break;
            case R.id.nav_messages:
                sendToMessages();
                break;
            case R.id.nav_home:
                Toast.makeText(this, "Domov", Toast.LENGTH_LONG).show();
                break;
        }
    }

    private void sendToMain(){
        Intent to_main = new Intent(MainActivity.this, MainActivity.class);
        to_main.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(to_main);
        finish();
    }

    private void detachListeners(){
        if(listener1 != null){
            user_reference.removeEventListener(listener1);
        }
        if(listener2 != null){
            action_reference.child("Likes").removeEventListener(listener2);
        }
        if(listener3 != null){
            action_reference.child("Dislikes").removeEventListener(listener3);
        }
        if(postsHolder.listener4 != null){
            postsHolder.action_reference.child("Likes").removeEventListener(postsHolder.listener4);
        }
        if(postsHolder.listener5 != null){
            postsHolder.action_reference.child("Dislikes").removeEventListener(postsHolder.listener5);
        }
        if(postsHolder.listener6 != null){
            postsHolder.post_user_reference.child(postsHolder.post_user_id).removeEventListener(postsHolder.listener6);
        }
        if(listener7 != null){
            action_reference.removeEventListener(listener7);
        }
        if(listener8 != null){
            user_reference.child(current_user_id).removeEventListener(listener8);
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
    protected void onDestroy() {
        super.onDestroy();
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

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(toolbar_toggle.onOptionsItemSelected(item)){
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
