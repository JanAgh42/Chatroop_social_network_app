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

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    private EditText email_field, password_field;
    private FirebaseAuth firebase_auth;
    private ProgressDialog loading_main;
    private GoogleSignInClient mGoogleSignInClient;
    private ValueEventListener listener1;
    private DatabaseReference user_reference;

    private static final int RC_SIGN_IN = 124;
    private String current_user_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        firebase_auth = FirebaseAuth.getInstance();

        loading_main = new ProgressDialog(this);
        email_field = findViewById(R.id.log_email_add);
        password_field = findViewById(R.id.log_pass);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    public void googleLogIn(View view){
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN && resultCode == RESULT_OK  && data != null) {
            loading_main.setTitle("Prebieha prihlasovanie");
            loading_main.setMessage("Prosím čakajte kým Vás systém neprihlási...");
            loading_main.setCanceledOnTouchOutside(true);
            loading_main.show();

            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);

            try{
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            }
            catch (ApiException E){
                Toast.makeText(LoginActivity.this, "Chyba: Skúste sa prihlásiť znova neskôr.", Toast.LENGTH_LONG).show();
                loading_main.dismiss();
            }
        }
        else{
            Toast.makeText(LoginActivity.this, "Chyba: Účet s touto emailovou adresou už existuje.", Toast.LENGTH_LONG).show();
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        firebase_auth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            sendToMain();
                            Toast.makeText(LoginActivity.this, "Prihlásenie bolo úspešné.", Toast.LENGTH_LONG).show();
                            loading_main.dismiss();
                        }
                        else {
                            String error_message = task.getException().getMessage();
                            sendToLogin();
                            Toast.makeText(LoginActivity.this, "Chyba: " + error_message, Toast.LENGTH_LONG).show();
                            loading_main.dismiss();
                        }
                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(firebase_auth.getCurrentUser() != null){
            emailVerification();
        }
    }

    public void userLogIn(View view){
        String email = email_field.getText().toString();
        String password = password_field.getText().toString();

        if(TextUtils.isEmpty(email)){
            Toast.makeText(this, "Prosím zadajte svoju emailovú adresu.", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(password)){
            Toast.makeText(this, "Prosím zadajte svoje prihlasovacie heslo.", Toast.LENGTH_SHORT).show();
        }
        else{
            loading_main.setTitle("Prebieha prihlasovanie");
            loading_main.setMessage("Prosím čakajte kým Vás systém neprihlási...");
            loading_main.setCanceledOnTouchOutside(true);
            loading_main.show();

            firebase_auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                emailVerification();
                            }
                            else{
                                String error_message = task.getException().getMessage();
                                Toast.makeText(LoginActivity.this, "Chyba: " + error_message, Toast.LENGTH_LONG).show();
                                loading_main.dismiss();
                            }
                        }
                    });
        }
    }

    private void emailVerification(){
        FirebaseUser current_user = firebase_auth.getCurrentUser();
        boolean verification = current_user.isEmailVerified();

        if(verification){
            current_user_id = firebase_auth.getCurrentUser().getUid();
            user_reference = FirebaseDatabase.getInstance().getReference().child("Users");
            listener1 = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(!dataSnapshot.hasChild(current_user_id)){
                        sendToSetup();
                        loading_main.dismiss();
                    }
                    else if(!dataSnapshot.child(current_user_id).hasChild("Fullname")){
                        sendToSetup();
                        loading_main.dismiss();
                    }
                    else if(!dataSnapshot.child(current_user_id).hasChild("ProfileImage")){
                        sendToSetup();
                        loading_main.dismiss();
                    }
                    else{
                        sendToMain();
                        loading_main.dismiss();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            };
            user_reference.addValueEventListener(listener1);
        }
        else{
            Toast.makeText(LoginActivity.this, "Prosím potvrďte svoju emailovú adresu.", Toast.LENGTH_LONG).show();
            firebase_auth.signOut();
            loading_main.dismiss();
        }
    }

    public void resetPassword(View view){
        Intent to_reset = new Intent(LoginActivity.this, PasswordActivity.class);
        startActivity(to_reset);
    }

    private void sendToMain(){
        Intent to_main = new Intent(LoginActivity.this, MainActivity.class);
        to_main.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(to_main);
        finish();
    }

    private void sendToSetup() {
        Intent to_setup = new Intent(LoginActivity.this, AccSetupActivity.class);
        to_setup.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(to_setup);
        finish();
    }

    private void sendToLogin() {
        Intent to_login = new Intent(LoginActivity.this, LoginActivity.class);
        to_login.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(to_login);
        finish();
    }

    public void sendToRegister(View view) {
        Intent to_register = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(to_register);
    }

    private void detachListeners() {
        if (listener1 != null) {
            user_reference.removeEventListener(listener1);
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
