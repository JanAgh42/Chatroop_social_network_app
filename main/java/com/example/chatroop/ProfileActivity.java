package com.example.chatroop;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.github.chrisbanes.photoview.PhotoView;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.IOException;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ProfileActivity extends AppCompatActivity {

    private DatabaseReference profile_reference, user_reference, post_reference;
    private CircleImageView profile_picture;
    private ImageView background_picture;
    private TextView username, nickname, profile_posts, profile_following, profile_followers, profile_status, dateof_birth, profile_country;
    private ProgressDialog loading_main;
    private ImageButton add_button, remove_button, accept_button, decline_button;
    private ValueEventListener listener1, listener2;
    private Retrofit retrofit;

    private String current_user_id, profile_user_id, following_state, follower_state, user_name, fullname,
            profileimage, type, backgroundimage, token, title, body;
    private static final int profile_id = 123, storage_id = 111, background_id = 222;
    private boolean i_blocked_him = false, he_blocked_me = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        FirebaseAuth user_auth = FirebaseAuth.getInstance();
        current_user_id = user_auth.getCurrentUser().getUid();

        profile_user_id = getIntent().getExtras().get("UserId").toString();

        profile_reference = FirebaseDatabase.getInstance().getReference().child("Users").child(profile_user_id);
        user_reference = FirebaseDatabase.getInstance().getReference().child("Users").child(current_user_id);
        post_reference = FirebaseDatabase.getInstance().getReference().child("Posts");

        Query following = profile_reference.child("Following").orderByChild("status").equalTo("friends");
        Query follower = profile_reference.child("Followers").orderByChild("status").equalTo("friends");
        Query post = post_reference.orderByChild("Uid").equalTo(profile_user_id);

        profile_picture = (CircleImageView)findViewById(R.id.profile_picture);
        background_picture = findViewById(R.id.profile_background);
        username = (TextView)findViewById(R.id.profile_realname);
        nickname = (TextView)findViewById(R.id.profile_nickname);
        profile_status = (TextView)findViewById(R.id.profile_status);
        dateof_birth = (TextView)findViewById(R.id.profile_dateof);
        profile_country = (TextView)findViewById(R.id.profile_country);
        profile_posts = (TextView)findViewById(R.id.profile_posts);
        add_button = (ImageButton)findViewById(R.id.profile_add_button);
        remove_button = (ImageButton)findViewById(R.id.profile_remove_button);
        accept_button = (ImageButton)findViewById(R.id.profile_accept_button);
        decline_button = (ImageButton)findViewById(R.id.profile_cancel_button);
        profile_followers = (TextView)findViewById(R.id.profile_followers);
        profile_following = (TextView)findViewById(R.id.profile_following);
        profile_posts = (TextView)findViewById(R.id.profile_posts);

        loading_main = new ProgressDialog(this);

        Toolbar profile_toolbar = (Toolbar) findViewById(R.id.profile_toolbar);
        setSupportActionBar(profile_toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        if(!current_user_id.equals(profile_user_id)){
            retrofit = new Retrofit.Builder()
                    .baseUrl("https://chatroop-1c6f0.web.app/api/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        set3();
        body = "Žiadosť o sledovanie";

        profile_reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    if(dataSnapshot.hasChild("Username")){
                        user_name = dataSnapshot.child("Username").getValue().toString();
                        nickname.setText(user_name);
                    }
                    if(dataSnapshot.hasChild("Fullname")){
                        fullname = dataSnapshot.child("Fullname").getValue().toString();
                        username.setText(fullname);

                        if(current_user_id.equals(profile_user_id)){
                            getSupportActionBar().setTitle("Váš profil");
                        }
                        else{
                            getSupportActionBar().setTitle(fullname);
                            if(dataSnapshot.child("Blocked users").hasChild(current_user_id)){
                                he_blocked_me = true;
                            }
                            else{
                                he_blocked_me = false;
                            }
                            if(dataSnapshot.hasChild("Token")){
                                token = dataSnapshot.child("Token").getValue().toString();
                            }
                        }
                    }
                    if(dataSnapshot.hasChild("Status")){
                        String status = dataSnapshot.child("Status").getValue().toString();
                        profile_status.setText(status);
                    }
                    if(dataSnapshot.hasChild("Birth")){
                        String dateof = dataSnapshot.child("Birth").getValue().toString();
                        dateof_birth.setText(dateof);
                    }
                    if(dataSnapshot.hasChild("Country")){
                        String country = dataSnapshot.child("Country").getValue().toString();
                        profile_country.setText(country);
                    }
                    if(dataSnapshot.hasChild("ProfileImage")){
                        profileimage = dataSnapshot.child("ProfileImage").getValue().toString();

                        Picasso.with(ProfileActivity.this).load(profileimage).fit().centerInside().placeholder(R.drawable.avatar)
                                .into(profile_picture);
                    }
                    if(dataSnapshot.hasChild("BackgroundImage")) {
                        backgroundimage = dataSnapshot.child("BackgroundImage").getValue().toString();

                        Picasso.with(ProfileActivity.this).load(backgroundimage).fit().centerCrop().into(background_picture);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        follower.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    int follower_count = (int) dataSnapshot.getChildrenCount();
                    profile_followers.setText(Integer.toString(follower_count));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        following.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    int following_count = (int) dataSnapshot.getChildrenCount();
                    profile_following.setText(Integer.toString(following_count));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        post.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    int post_count = (int) dataSnapshot.getChildrenCount();
                    profile_posts.setText(Integer.toString(post_count));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        listener1 = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    if (dataSnapshot.child("Following").exists()) {
                        if (dataSnapshot.child("Following").hasChild(profile_user_id)) {
                            following_state = dataSnapshot.child("Following").child(profile_user_id).child("status").getValue().toString();
                            setFollowers();
                        } else {
                            following_state = "not friends";
                            setFollowers();
                        }
                    } else {
                        following_state = "not friends";
                        setFollowers();
                    }
                    if(!current_user_id.equals(profile_user_id)){
                        if(dataSnapshot.child("Blocked users").hasChild(profile_user_id)){
                            i_blocked_him = true;
                        }
                        else{
                            i_blocked_him = false;
                        }
                        if(dataSnapshot.hasChild("Fullname")){
                            title = dataSnapshot.child("Fullname").getValue().toString();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        user_reference.addValueEventListener(listener1);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            sendToMain();
        }
        return super.onOptionsItemSelected(item);
    }

    private void setFollowers(){
        listener2 = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    if(dataSnapshot.hasChild(profile_user_id)){
                        follower_state = dataSnapshot.child(profile_user_id).child("status").getValue().toString();
                        setProfile();
                    }
                    else{
                        follower_state = "not friends";
                        setProfile();
                    }
                }
                else{
                    follower_state = "not friends";
                    setProfile();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        user_reference.child("Followers").addValueEventListener(listener2);
    }

    public void showFollowers(View view){
        sendToFollow("followers");
    }

    public void showFollowing(View view){
        sendToFollow("following");
    }

    public void showAllUserPosts(View view){
        Intent to_user_posts = new Intent(ProfileActivity.this, UserPostsActivity.class);
        to_user_posts.putExtra("ProfileId", profile_user_id);
        to_user_posts.putExtra("UserId", current_user_id);
        startActivity(to_user_posts);
    }

    public void editProfilePhoto(View view){
        if(current_user_id.equals(profile_user_id)) {
            AlertDialog.Builder dialog_builder = new AlertDialog.Builder(this);
            dialog_builder.setTitle("Zmeniť profilový obrázok")
                    .setMessage("Ste si istý/á, že chcete zmenit profilový obrázok?")
                    .setPositiveButton("Zmeniť", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            type = "profile";
                            editPhoto();
                        }
                    }).setNegativeButton("Späť", null);
            AlertDialog alert = dialog_builder.create();
            alert.show();
        }
        else{
            AlertDialog.Builder mBuilder = new AlertDialog.Builder(ProfileActivity.this);
            LayoutInflater inflater = getLayoutInflater();
            View dialogLayout = inflater.inflate(R.layout.pinch_to_zoom_pics, null);
            PhotoView photo_view = (PhotoView)dialogLayout.findViewById(R.id.pinch_image);
            Picasso.with(getApplicationContext()).load(profileimage).into(photo_view);
            AlertDialog mDialog = mBuilder.create();
            mDialog.setView(dialogLayout);
            mDialog.show();
        }
    }

    private void setProfile(){
        if((current_user_id.equals(profile_user_id))){
            set3();
        }
        else{
            if(following_state.equals("not friends") && follower_state.equals("not friends")){
                if(i_blocked_him || he_blocked_me){
                    set3();
                }
                else {
                    set1();
                }
            }
            else if(following_state.equals("pending")){
                set2();
            }
            else if(follower_state.equals("received")){
                if(he_blocked_me || i_blocked_him){
                    set3();
                }
                else {
                    add_button.setEnabled(false);
                    add_button.setVisibility(View.INVISIBLE);
                    remove_button.setEnabled(false);
                    remove_button.setVisibility(View.INVISIBLE);
                    accept_button.setEnabled(true);
                    accept_button.setVisibility(View.VISIBLE);
                    decline_button.setEnabled(true);
                    decline_button.setVisibility(View.VISIBLE);
                }
            }
            else if(following_state.equals("friends")){
                set2();
            }
            else if(follower_state.equals("friends")){
                if(i_blocked_him || he_blocked_me) {
                    set3();
                }
                else {
                    set1();
                }
            }
        }
    }

    private void set1(){
        accept_button.setEnabled(false);
        accept_button.setVisibility(View.INVISIBLE);
        decline_button.setEnabled(false);
        decline_button.setVisibility(View.INVISIBLE);
        remove_button.setEnabled(false);
        remove_button.setVisibility(View.INVISIBLE);
        add_button.setEnabled(true);
        add_button.setVisibility(View.VISIBLE);
    }

    private void set2(){
        accept_button.setEnabled(false);
        accept_button.setVisibility(View.INVISIBLE);
        decline_button.setEnabled(false);
        decline_button.setVisibility(View.INVISIBLE);
        add_button.setEnabled(false);
        add_button.setVisibility(View.INVISIBLE);
        remove_button.setEnabled(true);
        remove_button.setVisibility(View.VISIBLE);
    }

    private void set3(){
        add_button.setEnabled(false);
        add_button.setVisibility(View.INVISIBLE);
        remove_button.setEnabled(false);
        remove_button.setVisibility(View.INVISIBLE);
        accept_button.setEnabled(false);
        accept_button.setVisibility(View.INVISIBLE);
        decline_button.setEnabled(false);
        decline_button.setVisibility(View.INVISIBLE);
    }

    public void followUser(View view){
        updateSetDatabase("Following", "Followers", "pending", "received",
                "Požiadavka na sledovanie bola úspešne odoslaná.");

        Api api = retrofit.create(Api.class);
        Call<ResponseBody> call = api.sendNotification(token, title, body);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    Log.i("MESSAGE: ", response.body().string());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });
    }

    public void unfollowUser(View view){
        if(following_state.equals("pending")){
            updateRemoveDatabase("Following", "Followers", "Požiadavka na sledovanie bola úspešne zrušená.");
        }
        else if(following_state.equals("friends")){
            AlertDialog.Builder dialog_builder = new AlertDialog.Builder(this);
            dialog_builder.setTitle("Prestať sledovať používateľa")
                    .setMessage("Ste si istí, že chcete prestať sledovať tohto používateľa?")
                    .setPositiveButton("Zrušiť sledovanie", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            updateRemoveDatabase("Following", "Followers", "úspešne ste prestali sledovať tohto používateľa.");
                        }
                    }).setNegativeButton("Späť", null);
            AlertDialog alert = dialog_builder.create();
            alert.show();
        }
    }

    public void acceptRequest(View view){
        updateSetDatabase("Followers", "Following", "friends", "friends",
                "Úspešne ste potvrdili požiadavku o sledovanie.");
    }

    public void declineRequest(View view){
        updateRemoveDatabase("Followers", "Following", "Úspešne ste zamietli požiadavku o sledovanie.");
    }

    private void updateRemoveDatabase(String follow1, final String follow2, final String message){
        user_reference.child(follow1).child(profile_user_id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    profile_reference.child(follow2).child(current_user_id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Toast.makeText(ProfileActivity.this, message, Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        });
    }

    private void updateSetDatabase(String follow1, final String follow2, String value1, final String value2, final String message){

        HashMap follow_map = new HashMap();
        follow_map.put("ProfileImage", profileimage);
        follow_map.put("Fullname", fullname);
        follow_map.put("Username", user_name);
        follow_map.put("status", value1);

        user_reference.child(follow1).child(profile_user_id).updateChildren(follow_map).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    profile_reference.child(follow2).child(current_user_id).child("status").setValue(value2).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(ProfileActivity.this, message, Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }
            }
        });
    }

    public void setBackgroundPhoto(View view){
        if (current_user_id.equals(profile_user_id)) {
            type = "background";
            editPhoto();
        }
    }

    private void editPhoto() {
        if(ContextCompat.checkSelfPermission(ProfileActivity.this, android.Manifest.permission.READ_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_GRANTED) {
            Intent to_gallery = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            to_gallery.addCategory(Intent.CATEGORY_OPENABLE);
            to_gallery.setType("image/*");
            if(type.equals("background")){
                startActivityForResult(to_gallery, background_id);
            }
            else if(type.equals("profile")){
                startActivityForResult(to_gallery, profile_id);
            }

        }
        else{
            grantStoragePermission();
        }
    }

    private void grantStoragePermission(){
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                new AlertDialog.Builder(this)
                        .setTitle("Vyžaduje sa povolenie")
                        .setMessage("Aplikácia musí mať prístup k úložisku zariadenia na to, aby vedela spracovať vašu požiadavku.")
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(ProfileActivity.this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, storage_id);
                            }
                        }).create().show();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, storage_id);
            }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == storage_id ){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                editPhoto();
            }
            else{
                Toast.makeText(ProfileActivity.this, "Povolenie zamietnuté", Toast.LENGTH_LONG).show();
            }
        }
    }

    public void profileSettings(View view){
        PopupMenu popup = new PopupMenu(ProfileActivity.this, view);
        popup.inflate(R.menu.profile_menu);
        Menu popup_menu = popup.getMenu();

        if(!current_user_id.equals(profile_user_id)){
            popup_menu.findItem(R.id.profile_settings_add).setEnabled(false);
            popup_menu.findItem(R.id.profile_settings_set).setEnabled(false);
            popup_menu.findItem(R.id.profile_settings_add).setVisible(false);
            popup_menu.findItem(R.id.profile_settings_set).setVisible(false);

            if(i_blocked_him && he_blocked_me){
                popup_menu.findItem(R.id.profile_settings_block).setVisible(false);
                popup_menu.findItem(R.id.profile_settings_block).setEnabled(false);
                popup_menu.findItem(R.id.profile_settings_mess).setEnabled(false);
                popup_menu.findItem(R.id.profile_settings_mess).setVisible(false);
                popup_menu.findItem(R.id.profile_settings_unblock).setEnabled(true);
                popup_menu.findItem(R.id.profile_settings_unblock).setVisible(true);
            }
            else if(i_blocked_him){
                popup_menu.findItem(R.id.profile_settings_mess).setEnabled(false);
                popup_menu.findItem(R.id.profile_settings_mess).setVisible(false);
                popup_menu.findItem(R.id.profile_settings_block).setVisible(false);
                popup_menu.findItem(R.id.profile_settings_block).setEnabled(false);
                popup_menu.findItem(R.id.profile_settings_unblock).setEnabled(true);
                popup_menu.findItem(R.id.profile_settings_unblock).setVisible(true);
            }
            else if(he_blocked_me){
                popup_menu.findItem(R.id.profile_settings_unblock).setEnabled(false);
                popup_menu.findItem(R.id.profile_settings_unblock).setVisible(false);
                popup_menu.findItem(R.id.profile_settings_mess).setEnabled(false);
                popup_menu.findItem(R.id.profile_settings_mess).setVisible(false);
                popup_menu.findItem(R.id.profile_settings_block).setVisible(true);
                popup_menu.findItem(R.id.profile_settings_block).setEnabled(true);
            }
            else{
                popup_menu.findItem(R.id.profile_settings_unblock).setEnabled(false);
                popup_menu.findItem(R.id.profile_settings_unblock).setVisible(false);
                popup_menu.findItem(R.id.profile_settings_mess).setEnabled(true);
                popup_menu.findItem(R.id.profile_settings_mess).setVisible(true);
                popup_menu.findItem(R.id.profile_settings_block).setVisible(true);
                popup_menu.findItem(R.id.profile_settings_block).setEnabled(true);
            }
            if(!following_state.equals("friends")){
                popup_menu.findItem(R.id.profile_settings_mess).setEnabled(false);
                popup_menu.findItem(R.id.profile_settings_mess).setVisible(false);
            }
        }
        else{
            popup_menu.findItem(R.id.profile_settings_mess).setEnabled(false);
            popup_menu.findItem(R.id.profile_settings_block).setEnabled(false);
            popup_menu.findItem(R.id.profile_settings_rep).setEnabled(false);
            popup_menu.findItem(R.id.profile_settings_mess).setVisible(false);
            popup_menu.findItem(R.id.profile_settings_block).setVisible(false);
            popup_menu.findItem(R.id.profile_settings_rep).setVisible(false);
            popup_menu.findItem(R.id.profile_settings_unblock).setEnabled(false);
            popup_menu.findItem(R.id.profile_settings_unblock).setVisible(false);
        }

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                profileSettingsSelector(item);
                return false;
            }
        });
        popup.show();
    }

    private void profileSettingsSelector(MenuItem item) {
        switch (item.getItemId()){
            case R.id.profile_settings_add:
                sendToPost();
                break;
            case R.id.profile_settings_set:
                sendToSettings();
                break;
            case R.id.profile_settings_mess:
                sendToUserChat(profile_user_id);
                break;
            case R.id.profile_settings_block:
                AlertDialog.Builder block_builder = new AlertDialog.Builder(this);
                block_builder.setTitle("Zablokovať užívateľa")
                        .setMessage("Ste si istý/á, že chcete zablokovať daného používateľa?")
                        .setPositiveButton("Zablokovať", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                user_reference.child("Blocked users").child(profile_user_id).setValue(true).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        Toast.makeText(ProfileActivity.this, "Úspešne ste zablokovali daného používateľa.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }).setNegativeButton("Späť", null);
                AlertDialog alert = block_builder.create();
                alert.show();
                break;
            case R.id.profile_settings_unblock:
                AlertDialog.Builder unblock_builder = new AlertDialog.Builder(this);
                unblock_builder.setTitle("Odblokovať užívateľa")
                        .setMessage("Ste si istý/á, že chcete odblokovať daného používateľa?")
                        .setPositiveButton("Odblokovať", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                user_reference.child("Blocked users").child(profile_user_id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        Toast.makeText(ProfileActivity.this, "Úspešne ste odblokovali daného používateľa.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }).setNegativeButton("Späť", null);
                AlertDialog alert2 = unblock_builder.create();
                alert2.show();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == profile_id && resultCode == RESULT_OK && data != null){
            Uri image_uri = data.getData();
            CropImage.activity(image_uri).setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1).start(this);
        }
        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult crop_result = CropImage.getActivityResult(data);
            if(resultCode == RESULT_OK){
                loading_main.setTitle("Obrázok sa ukladá");
                loading_main.setMessage("Prosím čakajte kým sa Váš profilový obrázok ukladá...");
                loading_main.show();
                loading_main.setCanceledOnTouchOutside(true);

                Uri crop_uri = crop_result.getUri();

                savePicture(crop_uri, "Profile Images", "ProfileImage");
            }
            else{
                Toast.makeText(ProfileActivity.this, "Error: Image cannot be cropped", Toast.LENGTH_LONG).show();
                loading_main.dismiss();
            }
        }
        if(requestCode == background_id && resultCode == RESULT_OK && data != null){
            loading_main.setTitle("Obrázok sa ukladá");
            loading_main.setMessage("Prosím čakajte kým sa Váš profilový obrázok ukladá...");
            loading_main.show();
            loading_main.setCanceledOnTouchOutside(true);

            Uri image_uri = data.getData();

            savePicture(image_uri, "Background Images", "BackgroundImage");
        }
    }

    private void savePicture(Uri photo, String type, final String header){

        final StorageReference image_path = FirebaseStorage.getInstance().getReference().child(type).child(current_user_id + ".jpg");

        UploadTask upload_task = image_path.putFile(photo);
        Task<Uri> result_task = upload_task.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if(!task.isSuccessful()){
                    throw task.getException();
                }
                return image_path.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if(task.isSuccessful()){
                    final String image_url = task.getResult().toString();
                    profile_reference.child(header).setValue(image_url)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        sendToProfile(profile_user_id);
                                        loading_main.dismiss();
                                    }
                                    else{
                                        String image_error = task.getException().getMessage();
                                        Toast.makeText(ProfileActivity.this, "Chyba: " + image_error, Toast.LENGTH_LONG).show();
                                        loading_main.dismiss();
                                    }
                                }
                            });

                }
            }
        });
    }

    private void sendToMain(){
        Intent to_main = new Intent(ProfileActivity.this, MainActivity.class);
        to_main.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(to_main);
        finish();
    }

    private void sendToSettings(){
        Intent to_settings = new Intent(ProfileActivity.this, SettingsActivity.class);
        startActivity(to_settings);
    }

    private void sendToUserChat(String user_id) {
        Intent to_user_chat = new Intent(ProfileActivity.this, UserChatActivity.class);
        to_user_chat.putExtra("UserChat", user_id);
        startActivity(to_user_chat);
    }

    private void sendToPost(){
        Intent to_post = new Intent(ProfileActivity.this, PostActivity.class);
        startActivity(to_post);
    }

    private void sendToProfile(String user_id){
        Intent to_profile = new Intent(ProfileActivity.this, ProfileActivity.class);
        to_profile.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        to_profile.putExtra("UserId", user_id);
        startActivity(to_profile);
        finish();
    }

    private void sendToFollow(String category){
        Intent to_follow = new Intent(ProfileActivity.this, FollowActivity.class);
        to_follow.putExtra("category", category);
        to_follow.putExtra("user", profile_user_id);
        startActivity(to_follow);
    }

    private void detachListeners() {
        if (listener1 != null) {
            user_reference.removeEventListener(listener1);
        }
        if (listener2 != null) {
            user_reference.child("Followers").removeEventListener(listener2);
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
