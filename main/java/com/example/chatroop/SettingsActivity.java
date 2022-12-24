package com.example.chatroop;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class SettingsActivity extends AppCompatActivity {

    private EditText fullname, username, date, status;
    private Spinner country_spinner, gender_spinner, relationship_spinner;
    private DatabaseReference user_reference;

    private String country, gender, relationship, current_user_id, current_fullname, current_username;
    private int country_pos, gender_pos = 0, relationship_pos = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar settings_toolbar = findViewById(R.id.settings_toolbar);
        setSupportActionBar(settings_toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Nastavenia");

        FirebaseAuth firebase_auth = FirebaseAuth.getInstance();
        current_user_id = firebase_auth.getCurrentUser().getUid();

        fullname = findViewById(R.id.settings_username);
        username = findViewById(R.id.settings_nickname);
        date = findViewById(R.id.settings_birthdate);
        status = findViewById(R.id.settings_status);

        country_spinner = findViewById(R.id.settings_country);
        gender_spinner = findViewById(R.id.settings_gender);
        relationship_spinner = findViewById(R.id.settings_relationship);

        user_reference = FirebaseDatabase.getInstance().getReference().child("Users");

        user_reference.child(current_user_id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    if(dataSnapshot.hasChild("Fullname")){
                        current_fullname = dataSnapshot.child("Fullname").getValue().toString();
                        SettingsActivity.this.fullname.setText(current_fullname);
                    }
                    if(dataSnapshot.hasChild("Username")){
                        current_username = dataSnapshot.child("Username").getValue().toString();
                        SettingsActivity.this.username.setText(current_username);
                    }
                    if(dataSnapshot.hasChild("Birth")){
                        String birth = dataSnapshot.child("Birth").getValue().toString();
                        date.setText(birth);
                    }
                    if(dataSnapshot.hasChild("Status")){
                        String status = dataSnapshot.child("Status").getValue().toString();
                        SettingsActivity.this.status.setText(status);
                    }
                    if(dataSnapshot.hasChild("CountryPosition")){
                        String country = dataSnapshot.child("CountryPosition").getValue().toString();
                        country_pos = Integer.parseInt(country);
                    }
                    if(dataSnapshot.hasChild("RelationshipPosition")){
                        String relation = dataSnapshot.child("RelationshipPosition").getValue().toString();
                        relationship_pos = Integer.parseInt(relation);
                    }
                    if(dataSnapshot.hasChild("GenderPosition")){
                        String gender = dataSnapshot.child("GenderPosition").getValue().toString();
                        gender_pos = Integer.parseInt(gender);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.countries_array,
                android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        country_spinner.setAdapter(adapter);
        country_spinner.post(new Runnable() {
            @Override
            public void run() {
                country_spinner.setSelection(country_pos);
            }
        });

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

        ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(this, R.array.gender_array,
                android.R.layout.simple_spinner_item);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        gender_spinner.setAdapter(adapter2);
        gender_spinner.post(new Runnable() {
            @Override
            public void run() {
                gender_spinner.setSelection(gender_pos);
            }
        });

        gender_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                gender = parent.getItemAtPosition(position).toString();
                gender_pos = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        ArrayAdapter<CharSequence> adapter3 = ArrayAdapter.createFromResource(this, R.array.relationship_array,
                android.R.layout.simple_spinner_item);
        adapter3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        relationship_spinner.setAdapter(adapter3);
        relationship_spinner.post(new Runnable() {
            @Override
            public void run() {
                relationship_spinner.setSelection(relationship_pos);
            }
        });

        relationship_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                relationship = parent.getItemAtPosition(position).toString();
                relationship_pos = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            sendToMain();
        }
        return super.onOptionsItemSelected(item);
    }

    public void saveInfoChanges(View view){
        final String fullname = this.fullname.getText().toString();
        final String username = this.username.getText().toString();
        final String date = this.date.getText().toString();
        final String status = this.status.getText().toString();

        if(TextUtils.isEmpty(fullname)){
            Toast.makeText(this, "Prosím zadajte svoje celé meno.", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(username)){
            Toast.makeText(this, "Prosím zadajte svoje používateľské meno.", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(date)){
            Toast.makeText(this, "Prosím zadajte svoj dátum narodenia.", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(country) || country.equals("Choose a country")){
            Toast.makeText(this, "Prosím vyberte si svoju krajinu.", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(gender) || gender.equals("Not set yet")){
            Toast.makeText(this, "Prosím vyberte si svoje pohlavie.", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(relationship) || relationship.equals("Not set yet")){
            Toast.makeText(this, "Prosím vyberte si Váš vzťah.", Toast.LENGTH_SHORT).show();
        }
        else {
            if(!fullname.equals(current_fullname) || !username.equals(current_username)) {
                AlertDialog.Builder dialog_builder = new AlertDialog.Builder(this);
                dialog_builder.setTitle("Uložiť zmeny")
                        .setMessage("Niektoré dôležité osobné informácie boli zmenené. Chcete uložiť zmeny?")
                        .setPositiveButton("Uložiť", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                HashMap user_map = new HashMap();
                                user_map.put("Username", username);
                                user_map.put("Fullname", fullname);
                                user_map.put("Country", country);
                                user_map.put("CountryPosition", country_pos);
                                user_map.put("Status", status);
                                user_map.put("Gender", gender);
                                user_map.put("GenderPosition", gender_pos);
                                user_map.put("Birth", date);
                                user_map.put("Relationship", relationship);
                                user_map.put("RelationshipPosition", relationship_pos);

                                user_reference.child(current_user_id).updateChildren(user_map).addOnCompleteListener(new OnCompleteListener() {
                                    @Override
                                    public void onComplete(@NonNull Task task) {
                                        if (task.isSuccessful()) {
                                            sendToMain();
                                            Toast.makeText(SettingsActivity.this, "Osobné údaje boli úspešne uložené.", Toast.LENGTH_LONG).show();
                                            //loading_main.dismiss();
                                        } else {
                                            String error_massage = task.getException().getMessage();
                                            Toast.makeText(SettingsActivity.this, "Chyba: " + error_massage, Toast.LENGTH_LONG).show();
                                            //loading_main.dismiss();
                                        }
                                    }
                                });
                            }
                        }).setNegativeButton("Späť", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SettingsActivity.this.fullname.setText(current_fullname);
                        SettingsActivity.this.username.setText(current_username);
                    }
                });
                AlertDialog alert = dialog_builder.create();
                alert.show();
            }
            else{
                HashMap user_map = new HashMap();
                user_map.put("Username", username);
                user_map.put("Fullname", fullname);
                user_map.put("Country", country);
                user_map.put("CountryPosition", country_pos);
                user_map.put("Status", status);
                user_map.put("Gender", gender);
                user_map.put("GenderPosition", gender_pos);
                user_map.put("Birth", date);
                user_map.put("Relationship", relationship);
                user_map.put("RelationshipPosition", relationship_pos);

                user_reference.child(current_user_id).updateChildren(user_map).addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            sendToMain();
                            Toast.makeText(SettingsActivity.this, "Osobné údaje boli úspešne uložené.", Toast.LENGTH_LONG).show();
                            //loading_main.dismiss();
                        } else {
                            String error_massage = task.getException().getMessage();
                            Toast.makeText(SettingsActivity.this, "Chyba: " + error_massage, Toast.LENGTH_LONG).show();
                            //loading_main.dismiss();
                        }
                    }
                });
            }
        }
    }

    private void sendToMain(){
        Intent to_main = new Intent(SettingsActivity.this, MainActivity.class);
        to_main.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(to_main);
        finish();
    }
}
