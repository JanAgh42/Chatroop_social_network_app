package com.example.chatroop;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentActivity extends AppCompatActivity {

    private EditText comment;
    private RecyclerView all_comments;

    private DatabaseReference user_reference, post_reference, second_post_reference;
    private ValueEventListener listener1, listener2;

    private String post_id;
    private String current_user_id;
    private String current_username;
    private String current_image;

    private Boolean like = false, dislike = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);

        FirebaseAuth firebase_auth = FirebaseAuth.getInstance();
        current_user_id = firebase_auth.getCurrentUser().getUid();

        androidx.appcompat.widget.Toolbar comment_toolbar = findViewById(R.id.comments_toolbar);
        setSupportActionBar(comment_toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Komentáre");

        comment = (EditText)findViewById(R.id.comments_input);
        ImageButton post_comment = (ImageButton) findViewById(R.id.comments_button);

        post_id = getIntent().getExtras().get("PostId").toString();
        user_reference = FirebaseDatabase.getInstance().getReference().child("Users");
        post_reference = FirebaseDatabase.getInstance().getReference().child("Posts").child(post_id);
        second_post_reference = post_reference.child("Comments");

        all_comments = (RecyclerView)findViewById(R.id.comments_list);
        all_comments.setHasFixedSize(true);
        LinearLayoutManager layout_manager  = new LinearLayoutManager(this);
        layout_manager .setReverseLayout(true);
        layout_manager.setStackFromEnd(true);
        all_comments.setLayoutManager(layout_manager);

        user_reference.child(current_user_id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    if(dataSnapshot.hasChild("Fullname")){
                        current_username = dataSnapshot.child("Fullname").getValue().toString();
                    }
                    if(dataSnapshot.hasChild("ProfileImage")) {
                        current_image = dataSnapshot.child("ProfileImage").getValue().toString();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Comments, commentsHolder> firebase_adapter = new FirebaseRecyclerAdapter<Comments, commentsHolder>
                (Comments.class, R.layout.every_comment, commentsHolder.class, second_post_reference) {
            @Override
            protected void populateViewHolder(final commentsHolder viewHolder, final Comments model, int position) {

                final String comment_id = getRef(position).getKey();

                viewHolder.setUid(model.getUid());
                viewHolder.setFullname(model.getFullname());
                viewHolder.setComment(model.getComment());
                viewHolder.setTime(model.getTime());
                viewHolder.setProfileImage(model.getProfileImage(), getApplicationContext());
                viewHolder.showLikes(comment_id, post_id);
                viewHolder.showDislikes(comment_id, post_id);

                viewHolder.comment.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        sendToProfile(viewHolder.comment_user_id);
                    }
                });
                viewHolder.comment_options.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        PopupMenu popup = new PopupMenu(CommentActivity.this, v);
                        popup.inflate(R.menu.comment_menu);
                        Menu popup_menu = popup.getMenu();
                        if(!current_user_id.equals(viewHolder.comment_user_id)){
                            popup_menu.findItem(R.id.comment_settings_del).setEnabled(false);
                            popup_menu.findItem(R.id.comment_settings_edit).setEnabled(false);
                            popup_menu.findItem(R.id.comment_settings_del).setVisible(false);
                            popup_menu.findItem(R.id.comment_settings_edit).setVisible(false);
                        }
                        else{
                            popup_menu.findItem(R.id.comment_settings_report).setEnabled(false);
                            popup_menu.findItem(R.id.comment_settings_report).setVisible(false);
                        }
                        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                commentSettingsSelector(item, comment_id);
                                return false;
                            }
                        });
                        popup.show();
                    }
                });
                viewHolder.comment_like.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        like = true;
                        listener1 = new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if(like.equals(true)){
                                    if(dataSnapshot.child(comment_id).child("CommentLikes").hasChild(current_user_id)){
                                        second_post_reference.child(comment_id).child("CommentLikes").child(current_user_id).removeValue();
                                        like = false;
                                    }
                                    else{
                                        second_post_reference.child(comment_id).child("CommentLikes").child(current_user_id).setValue(true);
                                        second_post_reference.child(comment_id).child("CommentDislikes").child(current_user_id).removeValue();
                                        like = false;
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        };
                        post_reference.child("Comments").addValueEventListener(listener1);
                    }
                });
                viewHolder.comment_dislike.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dislike = true;
                        listener2 = new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if(dislike.equals(true)){
                                    if(dataSnapshot.child("Comments").child(comment_id).child("CommentDislikes").hasChild(current_user_id)){
                                        second_post_reference.child(comment_id).child("CommentDislikes").child(current_user_id).removeValue();
                                        dislike = false;
                                    }
                                    else{
                                        second_post_reference.child(comment_id).child("CommentDislikes").child(current_user_id).setValue(true);
                                        second_post_reference.child(comment_id).child("CommentLikes").child(current_user_id).removeValue();
                                        dislike = false;
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        };
                        post_reference.addValueEventListener(listener2);
                    }
                });
            }
        };
        firebase_adapter.startListening();
        all_comments.setAdapter(firebase_adapter);
    }

    private void commentSettingsSelector(MenuItem item, final String comment_id) {
        switch (item.getItemId()){
            case R.id.comment_settings_del:
                AlertDialog.Builder dialog_builder = new AlertDialog.Builder(this);
                dialog_builder.setTitle("Zmazať komentár")
                        .setMessage("Ste si istý/á, že chcete zmazať tento komentár?")
                        .setPositiveButton("Zmazať", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                deleteUserComment(comment_id);
                            }
                        }).setNegativeButton("Späť", null);
                AlertDialog alert = dialog_builder.create();
                alert.show();
                break;
            case R.id.comment_settings_edit:
                Toast.makeText(this, "Upraviť", Toast.LENGTH_LONG).show();
                break;
            case R.id.comment_settings_report:
                Intent to_report = new Intent(CommentActivity.this, ReportActivity.class);
                to_report.putExtra("PostId", comment_id);
                startActivity(to_report);
                break;
        }
    }

    private void deleteUserComment(String comment_id) {
        DatabaseReference comment_del_reference = FirebaseDatabase.getInstance().getReference().child("Posts").child(post_id).child("Comments").child(comment_id);
        comment_del_reference.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(CommentActivity.this, "Komentár bol úspešne odstránený.", Toast.LENGTH_SHORT).show();
                }
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

    public void sendComment(View view){
        String comment = this.comment.getText().toString();

        if(TextUtils.isEmpty(comment)){
            Toast.makeText(this, "Prosím zadajte Váš komentár.", Toast.LENGTH_SHORT).show();
        }
        else{
            postUserComment(comment);
            this.comment.setText("");
        }
    }

    private void postUserComment(String comment) {
        Calendar get_date = Calendar.getInstance();
        SimpleDateFormat current_date = new SimpleDateFormat("dd MMMM yyyy  HH:mm:ss");
        String curr_date = current_date.format(get_date.getTime());

        HashMap comment_map = new HashMap();
        comment_map.put("Uid", current_user_id);
        comment_map.put("Time", curr_date);
        comment_map.put("Comment", comment);
        comment_map.put("Fullname", current_username);
        comment_map.put("ProfileImage", current_image);

        second_post_reference.child(current_user_id + curr_date).updateChildren(comment_map).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if(task.isSuccessful()){
                    Toast.makeText(CommentActivity.this, "Komentár úspešne zdieľaný.", Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(CommentActivity.this, "Chyba: Skúste prosím neskôr.", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public static class commentsHolder extends RecyclerView.ViewHolder{

        View main_view;
        LinearLayout comment;
        String comment_user_id, current_user_id;
        static DatabaseReference action_reference;
        static ValueEventListener listener3, listener4;
        ImageButton comment_like, comment_dislike, comment_options;
        TextView like_amount, dislike_amount;
        int like_counter, dislike_counter;

        public commentsHolder(View itemView){
            super(itemView);
            main_view = itemView;
            comment = (LinearLayout)main_view.findViewById(R.id.comments_comment);
            comment_like = (ImageButton)main_view.findViewById(R.id.comments_like);
            comment_dislike = (ImageButton)main_view.findViewById(R.id.comments_dislike);
            comment_options = (ImageButton)main_view.findViewById(R.id.comments_options);
            like_amount = (TextView)main_view.findViewById(R.id.comments_like_amount);
            dislike_amount = (TextView)main_view.findViewById(R.id.comments_dislike_amount);

            current_user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
            action_reference = FirebaseDatabase.getInstance().getReference().child("Posts");
        }

        public void setUid(String uid) {
            comment_user_id = uid;
        }

        void showLikes(final String comment_id, String post_id){
            listener3 = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.hasChild(current_user_id)){
                        like_counter = (int) dataSnapshot.getChildrenCount();
                        comment_like.setImageResource(R.drawable.like_red_satisfied_smaller_15dp);
                        like_amount.setText((Integer.toString(like_counter)));
                    }
                    else{
                        like_counter = (int) dataSnapshot.getChildrenCount();
                        comment_like.setImageResource(R.drawable.like_black_satisfied_smaller_15dp);
                        like_amount.setText((Integer.toString(like_counter)));
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            };
            action_reference.child(post_id).child("Comments").child(comment_id).child("CommentLikes").addValueEventListener(listener3);
        }

        void showDislikes(final String comment_id, String post_id){
            listener4 = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.hasChild(current_user_id)){
                        dislike_counter = (int) dataSnapshot.getChildrenCount();
                        comment_dislike.setImageResource(R.drawable.dislike_red_dissatisfied_smaller_15dp);
                        dislike_amount.setText((Integer.toString(dislike_counter)));
                    }
                    else{
                        dislike_counter = (int) dataSnapshot.getChildrenCount();
                        comment_dislike.setImageResource(R.drawable.dislike_black_dissatisfied_smaller_15dp);
                        dislike_amount.setText((Integer.toString(dislike_counter)));
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            };
            action_reference.child(post_id).child("Comments").child(comment_id).child("CommentDislikes").addValueEventListener(listener4);
        }

        public void setFullname(String fullname) {
            TextView username = main_view.findViewById(R.id.comments_fullname);
            username.setText(fullname);
        }

        void setProfileImage(String profileImage, Context context) {
            CircleImageView profile_image = main_view.findViewById(R.id.comments_picture);
            Picasso.with(context).load(profileImage).fit().centerInside().placeholder(R.drawable.avatar).into(profile_image);
        }

        void setComment(String comment) {
            TextView comment_text = main_view.findViewById(R.id.comments_content);
            comment_text.setText(comment);
        }

        void setTime(String time) {
            TextView time_and_date = main_view.findViewById(R.id.comments_date);
            time_and_date.setText(time);
        }
    }

    private void sendToProfile(String user_id){
        Intent to_profile = new Intent(CommentActivity.this, ProfileActivity.class);
        to_profile.putExtra("UserId", user_id);
        startActivity(to_profile);
    }

    private void sendToMain(){
        Intent to_main = new Intent(CommentActivity.this, MainActivity.class);
        to_main.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(to_main);
        finish();
    }

    private void detachListeners() {
        if (listener1 != null) {
            post_reference.child("Comments").removeEventListener(listener1);
        }
        if (listener2 != null) {
            post_reference.removeEventListener(listener2);
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
