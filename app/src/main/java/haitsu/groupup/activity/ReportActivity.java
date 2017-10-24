package haitsu.groupup.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import haitsu.groupup.R;
import haitsu.groupup.other.DBConnections;

public class ReportActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener, View.OnClickListener {

    private String activityTitles[];
    private String selectedReason;
    private String selectedMember;
    private String groupID;

    private Toolbar toolbar;
    private EditText comments;
    private Button mSubmitButton;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;


    private DBConnections dbConnections = new DBConnections();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        // Initialize Firebase Auth
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();

        comments = (EditText) findViewById(R.id.further_comments);
        comments.setTextColor(Color.BLACK);
        mSubmitButton = (Button) findViewById(R.id.submit_button);

        activityTitles = getResources().getStringArray(R.array.nav_item_activity_titles);
        mSubmitButton.setOnClickListener(this);
        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        Spinner spinner3 = (Spinner) findViewById(R.id.spinner3);
        spinner.setOnItemSelectedListener(this);
        spinner3.setOnItemSelectedListener(this);
        getSupportActionBar().setTitle(activityTitles[6]);
        //Setups the back button seen in the toolbar.
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            //Handles the back button click.
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Spinner spinner = (Spinner) parent;
        if (spinner.getId() == R.id.spinner) {
            selectedReason = (String) parent.getItemAtPosition(position);
        } else if (spinner.getId() == R.id.spinner3) {
            selectedMember = (String) parent.getItemAtPosition(position);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.submit_button:
                if (!selectedReason.equals("Abusive Member")) {
                    selectedMember = "None";
                    dbConnections.submitReport(groupID, selectedReason, selectedMember, mFirebaseUser.getUid(), comments.getText().toString());
                } else {
                    dbConnections.submitReport(groupID, selectedReason, selectedMember, mFirebaseUser.getUid(), comments.getText().toString());
                }
                Toast.makeText(this, "Report submitted", Toast.LENGTH_LONG).show();
                break;
        }
    }
}
