package com.example.chatroop;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class PostActivity extends AppCompatActivity {

    private ImageButton select_image, send_post;
    private EditText post_desc;

    private static final int gallery_id = 1, permission_id = 111, document_id = 2;
    private Uri post_image_uri;
    private String post_description, curr_date, download_url, current_user_id, post_image_id;
    private DatabaseReference user_info, post_info;
    private ProgressDialog loading_main;
    private LinearLayout helper_text;

    private long count_posts;
    String profile_image, full_name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        FirebaseAuth firebase_auth = FirebaseAuth.getInstance();
        current_user_id = firebase_auth.getCurrentUser().getUid();

        select_image = findViewById(R.id.post_select_image);
        post_desc = findViewById(R.id.post_description);
        send_post = findViewById(R.id.send_post_button);
        helper_text = findViewById(R.id.post_helper_text);
        user_info = FirebaseDatabase.getInstance().getReference().child("Users");
        post_info = FirebaseDatabase.getInstance().getReference().child("Posts");
        loading_main = new ProgressDialog(this);


        Toolbar post_toolbar = findViewById(R.id.post_toolbar);
        setSupportActionBar(post_toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Nový príspevok");

        post_desc.setVisibility(View.INVISIBLE);
        send_post.setVisibility(View.INVISIBLE);
        post_desc.setEnabled(false);
        send_post.setEnabled(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            sendToMain();
        }
        return super.onOptionsItemSelected(item);
    }

    public void sendToGallery(View view){
        choosePicture();
    }

    private void choosePicture(){
        if(ContextCompat.checkSelfPermission(PostActivity.this, android.Manifest.permission.READ_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_GRANTED) {
            Intent to_gallery = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            to_gallery.addCategory(Intent.CATEGORY_OPENABLE);
            to_gallery.setType("image/*");
            startActivityForResult(to_gallery, gallery_id);
        }
        else{
            grantStoragePermission();
        }
    }

    private void grantStoragePermission(){
        if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)){
            new AlertDialog.Builder(this)
                    .setTitle("Vyžaduje sa povolenie")
                    .setMessage("Aplikácia musí mať prístup k úložisku zariadenia na to, aby vedela pracovať spoľahlivo.")
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(PostActivity.this, new String[] {android.Manifest.permission.READ_EXTERNAL_STORAGE}, permission_id);
                        }
                    }).create().show();
        }
        else{
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, permission_id);
        }
    }

    /*private void grantDocumentPermission(){
        if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.MANAGE_DOCUMENTS)){
            new AlertDialog.Builder(this)
                    .setTitle("Permission is required")
                    .setMessage("You have to allow document management to be able to use this app correctly")
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(PostActivity.this, new String[] {Manifest.permission.MANAGE_DOCUMENTS}, document_id);
                        }
                    }).create().show();
        }
        else{
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.MANAGE_DOCUMENTS}, document_id);
        }
    }*/

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == permission_id ){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                choosePicture();
            }
            else{
                Toast.makeText(PostActivity.this, "Povolenie zamietnuté", Toast.LENGTH_LONG).show();
            }
        }

        /*if(requestCode == document_id ){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                choosePicture();
            }
            else{
                Toast.makeText(PostActivity.this, "Document permission denied", Toast.LENGTH_LONG).show();
            }
        }*/
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == gallery_id && resultCode == PostActivity.RESULT_OK && data != null){
            post_image_uri = data.getData();
            select_image.setImageURI(post_image_uri);

            helper_text.setVisibility(View.INVISIBLE);
            helper_text.setEnabled(false);
            post_desc.setVisibility(View.VISIBLE);
            send_post.setVisibility(View.VISIBLE);
            post_desc.setEnabled(true);
            send_post.setEnabled(true);
        }
    }

    public void uploadPost(View view){
        post_description = post_desc.getText().toString();

        if(post_image_uri == null){
            Toast.makeText(this, "Prosím vyberte si obrázok.", Toast.LENGTH_SHORT).show();
        }
        else if(post_description.equals("")){
            Toast.makeText(this, "Prosím zadajte popis príspevku.", Toast.LENGTH_SHORT).show();
        }
        else {
            loading_main.setTitle("Zdieľanie príspevku");
            loading_main.setMessage("Prosím čakajte kým sa Váš príspevok zdieľa...");
            loading_main.show();
            loading_main.setCanceledOnTouchOutside(true);
            uploadToFirebase();
        }
    }

    private void uploadToFirebase() {
        user_info.child(current_user_id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    if((dataSnapshot.hasChild("Fullname")) && (dataSnapshot.hasChild("ProfileImage"))) {

                        full_name = dataSnapshot.child("Fullname").getValue().toString();
                        profile_image = dataSnapshot.child("ProfileImage").getValue().toString();

                        Calendar get_date = Calendar.getInstance();
                        SimpleDateFormat current_date = new SimpleDateFormat("dd MMMM yyyy  HH:mm:ss");
                        curr_date = current_date.format(get_date.getTime());

                        post_image_id = current_user_id + post_image_uri.getLastPathSegment() + "-" + curr_date + ".jpg";

                        final StorageReference image_path = FirebaseStorage.getInstance().getReference().child("Post Images").child(post_image_id);

                        UploadTask upload_task = image_path.putFile(post_image_uri);
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
                                    download_url = task.getResult().toString();
                                    savePostInfo();
                                }
                                else {
                                    String error_message = task.getException().getMessage();
                                    Toast.makeText(PostActivity.this, "Chyba: " + error_message, Toast.LENGTH_LONG).show();
                                    loading_main.dismiss();
                                }
                            }
                        });
                    }
                    else{
                        Toast.makeText(PostActivity.this, "Chyba: Nemáte nastavený profilový obrázok.", Toast.LENGTH_LONG).show();
                        loading_main.dismiss();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void savePostInfo() {
        post_info.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    count_posts = dataSnapshot.getChildrenCount();
                    uploadPostInfo();
                }
                else{
                    count_posts = 0;
                    uploadPostInfo();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void uploadPostInfo(){
        HashMap post_map = new HashMap();
        post_map.put("Uid", current_user_id);
        post_map.put("Date", curr_date);
        post_map.put("PostNumber", count_posts);
        post_map.put("Description", post_description);
        post_map.put("ProfileImage", profile_image);
        post_map.put("Fullname", full_name);
        post_map.put("PostImage", download_url);
        post_map.put("PostImageId", post_image_id);

        post_info.child(current_user_id + "-" + curr_date).updateChildren(post_map).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if (task.isSuccessful()) {
                    sendToMain();
                    Toast.makeText(PostActivity.this, "Príspevok úspešne zdieľaný.", Toast.LENGTH_SHORT).show();
                    loading_main.dismiss();
                } else {
                    Toast.makeText(PostActivity.this, "Chyba: Príspevok sa nedá zdieľať.", Toast.LENGTH_LONG).show();
                    loading_main.dismiss();
                }
            }
        });
    }

    private void sendToMain(){
        Intent to_main = new Intent(PostActivity.this, MainActivity.class);
        to_main.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(to_main);
        finish();
    }
}
