package com.example.chatroop;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class AccSetupActivity extends AppCompatActivity {

    private EditText username, full_name;
    private Spinner country_spinner;
    private Button acc_setup_button;
    private CircleImageView profile_pic;
    private DatabaseReference user_reference;
    private ProgressDialog loading_main;
    private StorageReference profile_image;
    private LinearLayout help_message;

    private String current_user_id, country, photo_download_url;
    private static final int gallery_id = 1, permission_id = 111;
    private int country_pos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_acc_setup);

        FirebaseAuth firebase_auth = FirebaseAuth.getInstance();
        current_user_id = firebase_auth.getCurrentUser().getUid();
        user_reference = FirebaseDatabase.getInstance().getReference().child("Users").child(current_user_id);
        profile_image = FirebaseStorage.getInstance().getReference().child("Profile Images");

        loading_main = new ProgressDialog(this);
        username = findViewById(R.id.set_username);
        full_name = findViewById(R.id.set_realname);
        country_spinner = findViewById(R.id.set_country);
        profile_pic = findViewById(R.id.set_picture);
        acc_setup_button = findViewById(R.id.set_button);
        help_message = findViewById(R.id.set_anim_one);

        help_message.setVisibility(View.VISIBLE);
        username.setEnabled(false);
        username.setVisibility(View.INVISIBLE);
        full_name.setEnabled(false);
        full_name.setVisibility(View.INVISIBLE);
        country_spinner.setEnabled(false);
        country_spinner.setVisibility(View.INVISIBLE);
        acc_setup_button.setEnabled(false);
        acc_setup_button.setVisibility(View.INVISIBLE);

        user_reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    if(dataSnapshot.hasChild("ProfileImage")) {
                        String image = dataSnapshot.child("ProfileImage").getValue().toString();
                        Picasso.with(AccSetupActivity.this).load(image).placeholder(R.drawable.avatar).fit().centerInside().into(profile_pic);
                        help_message.setVisibility(View.INVISIBLE);
                        username.setEnabled(true);
                        username.setVisibility(View.VISIBLE);
                        full_name.setEnabled(true);
                        full_name.setVisibility(View.VISIBLE);
                        country_spinner.setEnabled(true);
                        country_spinner.setVisibility(View.VISIBLE);
                        acc_setup_button.setEnabled(true);
                        acc_setup_button.setVisibility(View.VISIBLE);
                    }
                    else{
                        Toast.makeText(AccSetupActivity.this, "Nezabudnite si vybrať profilový obrázok!", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getApplicationContext(), R.array.countries_array,
                R.layout.accsetup_spinner_layout);
        adapter.setDropDownViewResource(R.layout.accsetup_spinner_dropdown);
        country_spinner.setAdapter(adapter);

        country_spinner.setSelection(0, true);
        View v = country_spinner.getSelectedView();

        country_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                country = parent.getItemAtPosition(position).toString();
                country_pos = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    public void saveInformation(View view){
        String username = this.username.getText().toString();
        String fullname = full_name.getText().toString();

        if(TextUtils.isEmpty(username)){
            Toast.makeText(this, "Prosím zadajte svoje používateľské meno", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(fullname)){
            Toast.makeText(this, "Prosím zadajte svoje celé meno", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(country) || country.equals("Choose a country")){
            Toast.makeText(this, "Prosím vyberte si svoju krajinu", Toast.LENGTH_SHORT).show();
        }
        else{
            loading_main.setTitle("Ukladanie nastavení");
            loading_main.setMessage("Prosím čakajte kým sa Vaše nastavenia ukladajú...");
            loading_main.show();
            loading_main.setCanceledOnTouchOutside(true);

            HashMap user_map = new HashMap();
            user_map.put("Username", username);
            user_map.put("Fullname", fullname);
            user_map.put("Country", country);
            user_map.put("CountryPosition", country_pos);
            user_map.put("Status", "");
            user_map.put("Gender", "");
            user_map.put("Birth", "");
            user_map.put("Relationship", "");

            user_reference.updateChildren(user_map).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if(task.isSuccessful()){
                        sendToMain();
                        Toast.makeText(AccSetupActivity.this, "Účet bol úspešne založený", Toast.LENGTH_LONG).show();
                        loading_main.dismiss();
                    }
                    else{
                        String error_massage = task.getException().getMessage();
                        Toast.makeText(AccSetupActivity.this, "Chyba: " + error_massage, Toast.LENGTH_LONG).show();
                        loading_main.dismiss();
                    }
                }
            });
        }
    }

    public void setProfilePicture(View view){
        if(ContextCompat.checkSelfPermission(AccSetupActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_GRANTED) {
            Intent to_gallery = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            to_gallery.addCategory(Intent.CATEGORY_OPENABLE);
            to_gallery.setType("image/*");
            startActivityForResult(to_gallery, gallery_id);
        }
        else{
            grantPermission();
        }
    }

    private void grantPermission(){
        if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)){
            new AlertDialog.Builder(this)
                    .setTitle("Vyžaduje sa povolenie")
                    .setMessage("Aplikácia musí mať prístup k úložisku zariadenia aby vedela pracovať spoľahlivo.")
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(AccSetupActivity.this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, permission_id);
                        }
                    }).create().show();
        }
        else{
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, permission_id);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == permission_id ){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Intent to_gallery = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                to_gallery.addCategory(Intent.CATEGORY_OPENABLE);
                to_gallery.setType("image/*");
                startActivityForResult(to_gallery, gallery_id);
            }
            else{
                Toast.makeText(AccSetupActivity.this, "Povolenie zamietnuté", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == gallery_id && resultCode == RESULT_OK && data != null){
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
                final StorageReference image_path = FirebaseStorage.getInstance().getReference().child("Profile Images").child(current_user_id + ".jpg");

                final UploadTask upload_task = image_path.putFile(crop_uri);

                upload_task.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        String message = e.toString();
                        Toast.makeText(AccSetupActivity.this, "Chyba: " + message, Toast.LENGTH_LONG).show();
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Toast.makeText(AccSetupActivity.this, "Obrázok bol nahratý úspešne.", Toast.LENGTH_SHORT).show();

                        final Task<Uri> urlTask = taskSnapshot.getStorage().getDownloadUrl();
                        urlTask.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                Uri downloadUrl = urlTask.getResult();
                                photo_download_url = String.valueOf(downloadUrl);

                                user_reference.child("ProfileImage").setValue(photo_download_url)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Intent to_setup = new Intent(AccSetupActivity.this, AccSetupActivity.class);
                                                    startActivity(to_setup);
                                                    loading_main.dismiss();
                                                } else {
                                                    String image_error = task.getException().getMessage();
                                                    Toast.makeText(AccSetupActivity.this, "Chyba: " + image_error, Toast.LENGTH_LONG).show();
                                                    loading_main.dismiss();
                                                }
                                            }
                                        });
                                }
                        });
                    }
                });
            }
            else{
                Toast.makeText(AccSetupActivity.this, "Chyba: Obrázok nemôže byť orezaný", Toast.LENGTH_LONG).show();
                loading_main.dismiss();
            }
        }
    }

    private void sendToMain(){
        Intent to_main = new Intent(AccSetupActivity.this, MainActivity.class);
        to_main.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(to_main);
        finish();
    }

}
