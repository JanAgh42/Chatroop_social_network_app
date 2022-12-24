package com.example.chatroop;

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
import com.google.firebase.auth.FirebaseAuth;

public class PasswordActivity extends AppCompatActivity {

    private EditText reset_password_email;
    private FirebaseAuth firebase_auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password);

        firebase_auth = FirebaseAuth.getInstance();

        reset_password_email = findViewById(R.id.reset_email);
    }

    public void sendResetEmail(View view){
        String email = reset_password_email.getText().toString();

        if(TextUtils.isEmpty(email)){
            Toast.makeText(this, "Prosím zadajte svoju emailovú adresu.", Toast.LENGTH_SHORT).show();
        }
        else{
            firebase_auth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        Toast.makeText(PasswordActivity.this, "Bol Vám doručený email s dodatočnými pokynmi.", Toast.LENGTH_LONG).show();
                        sendToLogin();
                    }
                    else{
                        String error = task.getException().getMessage();
                        Toast.makeText(PasswordActivity.this, "Chyba: " + error, Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }

    private void sendToLogin(){
        Intent to_login = new Intent(PasswordActivity.this, LoginActivity.class);
        to_login.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(to_login);
        finish();
    }

}
