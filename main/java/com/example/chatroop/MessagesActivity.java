package com.example.chatroop;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.squareup.picasso.Picasso;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessagesActivity extends AppCompatActivity {

    private DatabaseReference user_reference, messages_reference;
    private RecyclerView every_sent_message;

    private String current_user_id;

    private byte[] encryption_key = {23, 46, -90, 12, -30, 51, 89, 12, -113, -4, 89, -71, 78, 90, 33, -49};
    private Cipher decipher;
    private SecretKeySpec secret_key;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);

        FirebaseAuth user_auth = FirebaseAuth.getInstance();
        current_user_id = user_auth.getCurrentUser().getUid();
        every_sent_message = (RecyclerView)findViewById(R.id.message_all);

        try {
            decipher = Cipher.getInstance("AES");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        }
        secret_key = new SecretKeySpec(encryption_key, "AES");

        Toolbar messages_toolbar = (Toolbar) findViewById(R.id.message_toolbar);
        setSupportActionBar(messages_toolbar);
        getSupportActionBar().setTitle("Odoslané správy");

        every_sent_message.setHasFixedSize(true);
        every_sent_message.setLayoutManager(new LinearLayoutManager(this));

        user_reference = FirebaseDatabase.getInstance().getReference().child("Users");
        messages_reference = FirebaseDatabase.getInstance().getReference().child("Messages").child(current_user_id);

        showEverySentMessage();
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

    private void showEverySentMessage() {
        Query search_sent_messages = user_reference.child(current_user_id).child("My messages").orderByChild("Date");

        Query second = messages_reference.orderByChild("Date").limitToLast(1);

        final FirebaseRecyclerAdapter<SentMessages, sentMessageHolder> sent_message_adapter = new FirebaseRecyclerAdapter<SentMessages, sentMessageHolder>
                (SentMessages.class, R.layout.every_sent_mess, sentMessageHolder.class, search_sent_messages) {
            @Override
            protected void populateViewHolder(final sentMessageHolder viewHolder, SentMessages model, final int position) {
                viewHolder.setUid(model.getUid());
                try {
                    viewHolder.setMessage(decodeMessage(model.getMessage()));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                viewHolder.setUsername(model.getUsername());
                viewHolder.setProfileimage(model.getProfileimage(), getApplicationContext());
                viewHolder.main_linear.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent to_user_chat = new Intent(MessagesActivity.this, UserChatActivity.class);
                        to_user_chat.putExtra("UserChat", viewHolder.sent_user_id);
                        startActivity(to_user_chat);
                    }
                });
            }
        };
        sent_message_adapter.startListening();
        every_sent_message.setAdapter(sent_message_adapter);
    }

    public static class sentMessageHolder extends RecyclerView.ViewHolder{

        private TextView sent_message, sent_username;
        private CircleImageView sent_profile_image;
        private String sent_user_id;
        private LinearLayout main_linear;

        public sentMessageHolder(View itemView){
            super(itemView);
            sent_message = itemView.findViewById(R.id.sent_Message);
            sent_username = itemView.findViewById(R.id.sent_fullname);
            main_linear = itemView.findViewById(R.id.sent_to_chat);
            sent_profile_image = itemView.findViewById(R.id.sent_profile_image);
        }

        public void setUid(String Uid){
            sent_user_id = Uid;
        }

        public void setMessage(String Message) {
            if(Message.length() <= 22){
                sent_message.setText(Message);
            }
            else {
                String edited_message = Message.substring(0, 20) + "...";
                sent_message.setText(edited_message);
            }
        }

        void setProfileimage(String Profileimage, Context context){
            Picasso.with(context).load(Profileimage).fit().centerInside().placeholder(R.drawable.avatar)
                    .into(sent_profile_image);
        }

        public void setUsername(String Fullname){
            sent_username.setText(Fullname);
        }
    }

    public void sendNewMessage(View view){
        sendToFollow("nová správa");
    }

    private void sendToFollow(String category) {
        Intent to_follow = new Intent(MessagesActivity.this, FollowActivity.class);
        to_follow.putExtra("category", category);
        to_follow.putExtra("user", current_user_id);
        startActivity(to_follow);
    }
}
