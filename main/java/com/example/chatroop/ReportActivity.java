package com.example.chatroop;

import android.app.ProgressDialog;
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
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class ReportActivity extends AppCompatActivity {

    private EditText report_reason;
    private DatabaseReference report_reference;
    private ProgressDialog loading_main;

    private String post_id, reason, report_desc, current_user_id, current_date;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        post_id = getIntent().getExtras().get("PostId").toString();

        loading_main = new ProgressDialog(this);
        FirebaseAuth firebase_auth = FirebaseAuth.getInstance();
        current_user_id = firebase_auth.getCurrentUser().getUid();
        report_reference = FirebaseDatabase.getInstance().getReference().child("Reports").child(current_user_id).child(post_id);

        Toolbar report_toolbar = findViewById(R.id.report_toolbar);

        setSupportActionBar(report_toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Nahlásiť príspevok");

        report_reason = findViewById(R.id.report_reason);
        Spinner report_spinner = findViewById(R.id.report_spinner);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.report_array,
                android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        report_spinner.setAdapter(adapter);

        report_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                reason = parent.getItemAtPosition(position).toString();
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

    public void submitReport(View view){
        report_desc = report_reason.getText().toString();

        if(reason.equals("Choose a reason") || TextUtils.isEmpty(reason)){
            Toast.makeText(this, "Prosím vyberte si dôvod nahlásenia z nižšie uvedených možností.", Toast.LENGTH_LONG).show();
        }
        else if(TextUtils.isEmpty(report_desc)){
            Toast.makeText(this, "Prosím stručne opíšte svoj problém s týmto príspevkom.", Toast.LENGTH_LONG).show();
        }
        else{
            loading_main.setTitle("Odosielanie nahlásenia");
            loading_main.setMessage("Prosím čakajte kým sa odosiela vaše nahlásenie...");
            loading_main.setCanceledOnTouchOutside(true);
            loading_main.show();
            sendReport();
        }
    }

    private void sendReport() {
        Calendar get_date = Calendar.getInstance();
        SimpleDateFormat current_date = new SimpleDateFormat("dd MMMM yyyy  HH:mm:ss");
        this.current_date = current_date.format(get_date.getTime());

        HashMap report_map = new HashMap();
        report_map.put("Reporting user", current_user_id);
        report_map.put("Reported post", post_id);
        report_map.put("Date and time", this.current_date);
        report_map.put("Reason", reason);
        report_map.put("Description", report_desc);

        report_reference.updateChildren(report_map).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if(task.isSuccessful()){
                    sendToMain();
                    Toast.makeText(ReportActivity.this, "Príspevok bol úspešne nahlásený.", Toast.LENGTH_LONG).show();
                    loading_main.dismiss();
                }
                else{
                    String error_massage = task.getException().getMessage();
                    Toast.makeText(ReportActivity.this, "Chyba: " + error_massage, Toast.LENGTH_LONG).show();
                    loading_main.dismiss();
                }
            }
        });
    }

    private void sendToMain(){
        Intent to_main = new Intent(ReportActivity.this, MainActivity.class);
        to_main.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(to_main);
        finish();
    }
}
