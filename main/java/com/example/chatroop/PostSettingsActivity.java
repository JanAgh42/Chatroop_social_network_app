package com.example.chatroop;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class PostSettingsActivity extends AppCompatActivity {

    private DatabaseReference post_reference;
    private TextView post_settings_username, post_settings_date;
    private EditText post_settings_desc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_settings);

        String post_id = getIntent().getExtras().get("PostId").toString();
        post_reference = FirebaseDatabase.getInstance().getReference().child("Posts").child(post_id);

        post_settings_username = findViewById(R.id.post_settings_username);
        post_settings_date = findViewById(R.id.post_settings_date);
        post_settings_desc = findViewById(R.id.desc_input);

        post_reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    if(dataSnapshot.hasChild("Fullname")){
                        String username = dataSnapshot.child("Fullname").getValue().toString();
                        post_settings_username.setText(username);
                    }
                    if(dataSnapshot.hasChild("Date")){
                        String date = dataSnapshot.child("Date").getValue().toString();
                        post_settings_date.setText(date);
                    }
                    if(dataSnapshot.hasChild("Description")){
                        String description = dataSnapshot.child("Description").getValue().toString();
                        post_settings_desc.setText(description);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        Toolbar post_settings_toolbar = (Toolbar) findViewById(R.id.post_settings_toolbar);
        setSupportActionBar(post_settings_toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Upraviť príspevok");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == android.R.id.home){
            sendToMain();
        }
        return super.onOptionsItemSelected(item);
    }

    public void editPostDesc(View view){
        String new_post_desc = post_settings_desc.getText().toString();
        if(TextUtils.isEmpty(new_post_desc)){
            Toast.makeText(this, "Prosím zadajte popis príspevku.", Toast.LENGTH_SHORT).show();
        }
        else {
            post_reference.child("Description").setValue(new_post_desc).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()) {
                        sendToMain();
                        Toast.makeText(PostSettingsActivity.this, "Príspevok bol úspešne upravený.", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        Toast.makeText(PostSettingsActivity.this, "Chyba: Skúste prosím neskôr.", Toast.LENGTH_SHORT).show();

                    }
                }
            });
        }
    }

    private void sendToMain(){
        Intent to_main = new Intent(PostSettingsActivity.this, MainActivity.class);
        to_main.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(to_main);
        finish();
    }
}
