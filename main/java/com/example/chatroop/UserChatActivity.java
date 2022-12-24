package com.example.chatroop;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class UserChatActivity extends AppCompatActivity {

    private DatabaseReference chat_user_reference, message_reference, current_mess_reference;
    private RecyclerView every_message;
    private EditText write_message;
    private CircleImageView user_profile_picture;
    private LinearLayoutManager manager;
    private Retrofit retrofit;

    private String current_user_id, chat_user_id, chat_user_fullname, current_message, chat_profile_image,
            current_date, current_user_fullname, current_profile_image, token, title;
    private static final int file_id = 111;

    private byte[] encryption_key = {23, 46, -90, 12, -30, 51, 89, 12, -113, -4, 89, -71, 78, 90, 33, -49};
    private Cipher cipher, decipher;
    private SecretKeySpec secret_key;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_chat);

        FirebaseAuth user_auth = FirebaseAuth.getInstance();
        current_user_id = user_auth.getCurrentUser().getUid();
        chat_user_id = getIntent().getExtras().get("UserChat").toString();
        write_message = findViewById(R.id.chat_input);
        every_message = findViewById(R.id.chat_messages);
        user_profile_picture = findViewById(R.id.chat_user_image);

        retrofit = new Retrofit.Builder()
                .baseUrl("https://chatroop-1c6f0.web.app/api/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        try {
            cipher = Cipher.getInstance("AES");
            decipher = Cipher.getInstance("AES");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        }
        secret_key = new SecretKeySpec(encryption_key, "AES");

        chat_user_reference = FirebaseDatabase.getInstance().getReference().child("Users");
        current_mess_reference = FirebaseDatabase.getInstance().getReference().child("Messages").child(current_user_id);
        message_reference = FirebaseDatabase.getInstance().getReference().child("Messages").child(chat_user_id);

        Toolbar user_chat_toolbar = findViewById(R.id.chat_toolbar);
        setSupportActionBar(user_chat_toolbar);

        manager = new LinearLayoutManager(this);
        manager.setStackFromEnd(true);
        every_message.setHasFixedSize(true);
        every_message.setLayoutManager(manager);

        chat_user_reference.child(chat_user_id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    if(dataSnapshot.hasChild("Fullname")){
                        chat_user_fullname = dataSnapshot.child("Fullname").getValue().toString();
                        getSupportActionBar().setTitle(chat_user_fullname);
                    }
                    else{
                        getSupportActionBar().setTitle("Neznámy používateľ");
                    }
                    if(dataSnapshot.hasChild("Token")){
                        token = dataSnapshot.child("Token").getValue().toString();
                    }
                    if(dataSnapshot.hasChild("ProfileImage")){
                        chat_profile_image = dataSnapshot.child("ProfileImage").getValue().toString();

                        Picasso.with(UserChatActivity.this).load(chat_profile_image).fit().centerInside().placeholder(R.drawable.avatar)
                                .into(user_profile_picture);
                    }
                    if(dataSnapshot.child("Blocked users").hasChild(current_user_id)){
                        write_message.setEnabled(false);
                        Toast.makeText(UserChatActivity.this, "Tento používateľ Vás zablokoval.", Toast.LENGTH_LONG).show();
                    }
                }
                else{
                    getSupportActionBar().setTitle("Neznámy používateľ");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        chat_user_reference.child(current_user_id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    if(dataSnapshot.hasChild("Fullname")){
                        current_user_fullname = dataSnapshot.child("Fullname").getValue().toString();
                        title = current_user_fullname;
                    }
                    if(dataSnapshot.hasChild("ProfileImage")){
                        current_profile_image = dataSnapshot.child("ProfileImage").getValue().toString();
                    }
                    if(dataSnapshot.child("Blocked users").hasChild(chat_user_id)){
                        write_message.setEnabled(false);
                        Toast.makeText(UserChatActivity.this, "Tento používateľ je Vami zablokovaný.", Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        showEveryMessage();
    }

    private String encodeMessage(String string){
        byte[] byte_of_string = string.getBytes();
        byte[] encrypted_byte = new byte[byte_of_string.length];

        try {
            cipher.init(Cipher.ENCRYPT_MODE, secret_key);
            encrypted_byte = cipher.doFinal(byte_of_string);
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }

        String encrypted_message = null;

        encrypted_message = new String(encrypted_byte, StandardCharsets.ISO_8859_1);
        return encrypted_message;
    }

    private String decodeMessage(String string) throws UnsupportedEncodingException {
        byte[] encrypted_byte = string.getBytes(StandardCharsets.ISO_8859_1);
        String decrypted_message = string;
        byte[] decrypted_byte;

        try {
            decipher.init(Cipher.DECRYPT_MODE, secret_key);
            decrypted_byte = decipher.doFinal(encrypted_byte);
            decrypted_message = new String(decrypted_byte);
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }
        return decrypted_message;
    }

    private void showEveryMessage() {
        Query search_messages = current_mess_reference.child(chat_user_id).orderByKey();

        final FirebaseRecyclerAdapter<Messages, messageHolder> message_adapter = new FirebaseRecyclerAdapter<Messages, messageHolder>
                (Messages.class, R.layout.every_message, messageHolder.class, search_messages) {
            @Override
            protected void populateViewHolder(messageHolder viewHolder, Messages model, final int position) {
                viewHolder.setUid(model.getUid());
                try {
                    viewHolder.setMessage(decodeMessage(model.getMessage()));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        };
        message_adapter.startListening();
        every_message.setAdapter(message_adapter);
    }

    public void switchToProfile(View view){
        Intent to_user_profile = new Intent(UserChatActivity.this, ProfileActivity.class);
        to_user_profile.putExtra("UserId", chat_user_id);
        startActivity(to_user_profile);
    }

    public static class messageHolder extends RecyclerView.ViewHolder{

        View main_view;
        private String current_user_id, message_user_id;
        private TextView my_message, his_message;

        public messageHolder(View itemView) {
            super(itemView);
            main_view = itemView;
            current_user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
            my_message = main_view.findViewById(R.id.chat_my_message);
            his_message = main_view.findViewById(R.id.chat_his_message);
        }

        public void setUid(String Uid){
            message_user_id = Uid;
        }

        public void setMessage(String Message){
            if(current_user_id.equals(message_user_id)){
                my_message.setText(Message);
                his_message.setVisibility(View.INVISIBLE);
                my_message.setVisibility(View.VISIBLE);
            }
            else{
                his_message.setText(Message);
                my_message.setVisibility(View.INVISIBLE);
                his_message.setVisibility(View.VISIBLE);
            }
        }
    }

    public void sendChatMessage(View view){
        current_message = write_message.getText().toString();

        if(TextUtils.isEmpty(current_message)){
            Toast.makeText(this, "Prosím zadajte správu.", Toast.LENGTH_SHORT).show();
        }
        else{
            write_message.setText("");
            Calendar get_date = Calendar.getInstance();
            SimpleDateFormat current_date = new SimpleDateFormat("dd MMMM yyyy  HH:mm:ss");
            this.current_date = current_date.format(get_date.getTime());

            HashMap current_user_map = new HashMap();

            current_user_map.put("Fullname", current_user_fullname);
            current_user_map.put("Profileimage", chat_profile_image);
            current_user_map.put("Type", "text");
            current_user_map.put("Message", encodeMessage(current_message));
            current_user_map.put("Uid", current_user_id);
            current_user_map.put("Date", this.current_date);

            current_mess_reference.child(chat_user_id).child(this.current_date).updateChildren(current_user_map)
                    .addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {
                            if(task.isSuccessful()){
                                receivedMessage();
                            }
                            else{
                                String error_massage = task.getException().getMessage();
                                Toast.makeText(UserChatActivity.this, "Error: " + error_massage, Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        }
    }

    public void receivedMessage(){
        HashMap chat_user_map = new HashMap();

        chat_user_map.put("Fullname", current_user_fullname);
        chat_user_map.put("Profileimage", current_profile_image);
        chat_user_map.put("Message", encodeMessage(current_message));
        chat_user_map.put("Type", "text");
        chat_user_map.put("Uid", current_user_id);
        chat_user_map.put("Date", this.current_date);

        message_reference.child(current_user_id).child(this.current_date).updateChildren(chat_user_map).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if(task.isSuccessful()){
                    if(current_message.length() <= 22){
                        updateMessageList(current_message);
                    }
                    else{
                        String edited_message = current_message.substring(0, 20) + "...";
                        updateMessageList(edited_message);
                    }
                }
                else{
                    String error_massage = task.getException().getMessage();
                    Toast.makeText(UserChatActivity.this, "Chyba: " + error_massage, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void updateMessageList(final String message) {
        HashMap chat_update_map = new HashMap();

        chat_update_map.put("Profileimage", chat_profile_image);
        chat_update_map.put("Message", encodeMessage(message));
        chat_update_map.put("Type", "text");
        chat_update_map.put("Uid", chat_user_id);
        chat_update_map.put("Date", this.current_date);
        chat_update_map.put("Username", chat_user_fullname);

        chat_user_reference.child(current_user_id).child("My messages").child(chat_user_id).updateChildren(chat_update_map).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if(task.isSuccessful()){
                    updateMessageList2(message);
                }
                else{
                    String error_massage = task.getException().getMessage();
                    Toast.makeText(UserChatActivity.this, "Chyba: " + error_massage, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void updateMessageList2(final String body) {
        HashMap chat_update_map = new HashMap();

        chat_update_map.put("Profileimage", current_profile_image);
        chat_update_map.put("Message", encodeMessage(body));
        chat_update_map.put("Type", "text");
        chat_update_map.put("Uid", current_user_id);
        chat_update_map.put("Date", this.current_date);
        chat_update_map.put("Username", current_user_fullname);

        chat_user_reference.child(chat_user_id).child("My messages").child(current_user_id).updateChildren(chat_update_map).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if(task.isSuccessful()){
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
                else{
                    String error_massage = task.getException().getMessage();
                    Toast.makeText(UserChatActivity.this, "Error: " + error_massage, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void sendToUserChat(String user_id){
        Intent to_user_chat = new Intent(UserChatActivity.this, UserChatActivity.class);
        to_user_chat.putExtra("UserChat", user_id);
        startActivity(to_user_chat);
    }
}
