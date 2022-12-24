package com.example.chatroop;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class RegisterActivity extends AppCompatActivity {

    private EditText email_add, password, conf_password;
    private FirebaseAuth firebase_auth;
    private ProgressDialog loading_settings;

    private String current_user_id;
    private boolean verification;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        firebase_auth = FirebaseAuth.getInstance();
        loading_settings = new ProgressDialog(this);

        email_add = findViewById(R.id.reg_email_add);
        password = findViewById(R.id.reg_pass);
        conf_password = findViewById(R.id.reg_conf_pass);
    }

    /*@Override
    protected void onStart() {
        super.onStart();
        //FirebaseUser current_user = firebase_auth.getCurrentUser();
        if(firebase_auth.getCurrentUser() != null){
            emailVerification();
        }
    }*/

    public void createNewAccount(View view){
        getAllInfo();
    }

    private void getAllInfo() {
        String email = email_add.getText().toString();
        String password = this.password.getText().toString();
        String confirm = conf_password.getText().toString();

        if(TextUtils.isEmpty(email)){
            Toast.makeText(this, "Prosím zadajte svoju emailovú adresu.", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(password)){
            Toast.makeText(this, "Prosím zadajte svoje heslo.", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(confirm)){
            Toast.makeText(this, "Prosím zopakujte svoje heslo.", Toast.LENGTH_SHORT).show();
        }
        else if(!password.equals(confirm)){
            Toast.makeText(this, "Heslá sa nezhodujú.", Toast.LENGTH_SHORT).show();
        }
        else{
            loading_settings.setTitle("Vytvára sa účet");
            loading_settings.setMessage("Prosím čakajte kým sa vytvorí Váš účet...");
            loading_settings.show();
            loading_settings.setCanceledOnTouchOutside(true);

            firebase_auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                sendVerification();
                            }
                            else{
                                String error_message = task.getException().getMessage();
                                Toast.makeText(RegisterActivity.this, "Chyba: " + error_message, Toast.LENGTH_LONG).show();
                                loading_settings.dismiss();
                            }
                        }
                    });
        }
    }

    private void sendVerification(){
        FirebaseUser current_user = firebase_auth.getCurrentUser();

        if(current_user != null){
            verification = current_user.isEmailVerified();

            if(!verification){
                current_user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(RegisterActivity.this, "Bol Vám doručený email s odkazom na potvrdenie Vašej emailovej adresy.", Toast.LENGTH_LONG).show();
                            firebase_auth.signOut();
                            sendToLogin();
                            loading_settings.dismiss();
                        }
                        else{
                            String error_message = task.getException().getMessage();
                            Toast.makeText(RegisterActivity.this, "Chyba: " + error_message, Toast.LENGTH_LONG).show();
                            loading_settings.dismiss();
                        }
                    }
                });
            }
            else{
                sendToSetup();
                loading_settings.dismiss();
            }
        }
        else{
            loading_settings.dismiss();
        }
    }

    private void emailVerification(){
        FirebaseUser current_user = firebase_auth.getCurrentUser();
        verification = current_user.isEmailVerified();

        if(verification){
            current_user_id = firebase_auth.getCurrentUser().getUid();
            DatabaseReference user_reference = FirebaseDatabase.getInstance().getReference().child("Users");
            user_reference.addListenerForSingleValueEvent(new ValueEventListener() {
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
                    else{
                        sendToMain();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
        else{
            Toast.makeText(RegisterActivity.this, "Prosím potvrďte svoju emailovú adresu.", Toast.LENGTH_LONG).show();
            firebase_auth.signOut();
        }
    }

    public void sendToLogIn(View view) {
        sendToLogin();
    }

    private void sendToLogin(){
        Intent to_login = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(to_login);
    }

    private void sendToSetup(){
        Intent to_setup = new Intent(RegisterActivity.this, AccSetupActivity.class);
        to_setup.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(to_setup);
        finish();
    }

    private void sendToMain(){
        Intent to_main = new Intent(RegisterActivity.this, MainActivity.class);
        to_main.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(to_main);
        finish();
    }
}
