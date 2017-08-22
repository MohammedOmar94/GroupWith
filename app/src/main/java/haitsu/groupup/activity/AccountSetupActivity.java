package haitsu.groupup.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import haitsu.groupup.R;
import haitsu.groupup.other.DBConnections;

public class AccountSetupActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener {

    private String selectedAge;

    private Button mfinishSetup;
    private Toolbar toolbar;

    DBConnections dbConnections = new DBConnections();

    FirebaseAuth mFirebaseAuth;
    FirebaseUser mFirebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_setup);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Initialize Firebase Auth
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();

        mfinishSetup = (Button) findViewById(R.id.finish_setup);
        mfinishSetup.setOnClickListener(this);

        Spinner spinner = (Spinner) findViewById(R.id.spinner2);
        spinner.setOnItemSelectedListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.finish_setup:
                dbConnections.createUserAccount(((EditText) findViewById(R.id.username)),mFirebaseUser.getEmail(), selectedAge);
                startActivity(new Intent(AccountSetupActivity.this, MainActivity.class));
                finish();
        }
    }

    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        // An item was selected. You can retrieve the selected item using
        Spinner spinner = (Spinner) parent;
        if (spinner.getId() == R.id.spinner2) {
            selectedAge = (String) parent.getItemAtPosition(pos);
            //do this
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}

