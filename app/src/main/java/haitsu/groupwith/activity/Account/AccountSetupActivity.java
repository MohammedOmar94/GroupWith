package haitsu.groupwith.activity.Account;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.amplitude.api.Amplitude;
import com.amplitude.api.Identify;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.joda.time.LocalDate;
import org.joda.time.Years;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;

import haitsu.groupwith.PermissionUtils;
import haitsu.groupwith.R;
import haitsu.groupwith.activity.MainActivity;
import haitsu.groupwith.fragment.DatePickerFragment;
import haitsu.groupwith.other.DBConnections;
import haitsu.groupwith.other.LocationManager;

public class AccountSetupActivity extends AppCompatActivity
        implements View.OnClickListener, AdapterView.OnItemSelectedListener,
        DialogInterface.OnDismissListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        PermissionUtils.PermissionResultCallback {

    //Age users selects from the dropdown list.
    private String selectedGender;
    private String city;
    private String country;

    private Button mfinishSetup;
    private Toolbar toolbar;
    private Spinner spinner;
    private TextView birthdayLabel;
    private TextView locationLabel;

    //Access to methods dealing with Firebase.
    private DBConnections dbConnections = new DBConnections();


    private FusedLocationProviderClient mFusedLocationClient;


    private LocationCallback mLocationCallback;
    private LocationManager locationManager = new LocationManager();

    FirebaseAuth mFirebaseAuth;
    FirebaseUser mFirebaseUser;

    private RadioButton termsOfService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_setup);

        termsOfService = (RadioButton) findViewById(R.id.terms_radio);
//        int startPos = termsOfService.getText().toString().indexOf("Terms of Service");

        TextView termsText = (TextView) findViewById(R.id.terms_text);
//        termsText.setPaintFlags(termsText.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
//        termsText.setText(termsText.getText());

        TextView privacyText = (TextView) findViewById(R.id.privacy_text);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Initialize Firebase Auth
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();

        mfinishSetup = (Button) findViewById(R.id.finish_setup);
        spinner = (Spinner) findViewById(R.id.gender_spinner);
        birthdayLabel = (TextView) findViewById(R.id.birthday_label);
//        locationLabel = (TextView) findViewById(R.id.location_label);
        termsText.setOnClickListener(this);
        privacyText.setOnClickListener(this);
        mfinishSetup.setOnClickListener(this);
        spinner.setOnItemSelectedListener(this);


        ArrayList<String> permissions = new ArrayList<>();
        PermissionUtils permissionUtils;

        permissionUtils = new PermissionUtils(AccountSetupActivity.this);

        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);

        permissionUtils.check_permission(permissions, "Need GPS permission for getting your location", 1);

        Amplitude.getInstance().logEvent("Viewing Account Setup screen");


//        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
//
//
//        mLocationCallback = new LocationCallback() {
//            @Override
//            public void onLocationResult(LocationResult locationResult) {
//                for (Location location : locationResult.getLocations()) {
//                    locationManager.storeLocationData(location);
//                    locationLabel.setText(locationManager.getCity() + "/" + locationManager.getCountry());
//                    locationManager.stopLocationUpdates();
//                }
//            }
//        };
//
//
//        locationManager.setmLocationCallback(mLocationCallback);
//
//        // Handles all location logic
//        locationManager.setmFusedLocationClient(mFusedLocationClient);
//
//        locationManager.initialiseLocationRequest(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.privacy_text:
                startActivity(new Intent(AccountSetupActivity.this, PrivacyPolicyActivity.class));
                break;
            case R.id.terms_text:
                startActivity(new Intent(AccountSetupActivity.this, TermsOfServiceActivity.class));
                break;
            case R.id.finish_setup:
                if (((EditText) findViewById(R.id.username)).getText().toString().equals("")) {
                    Toast.makeText(getApplicationContext(),
                            "Please enter a username.", Toast.LENGTH_LONG)
                            .show();
                } else if (birthdayLabel.getText().equals("--/--/----")) {
                    Toast.makeText(getApplicationContext(),
                            "Please enter your date of birth.", Toast.LENGTH_LONG)
                            .show();
//                } else if (locationManager.getCountry() == null || locationManager.getCity() == null) {
//                    Toast.makeText(getApplicationContext(),
//                            "Please turn on your location.", Toast.LENGTH_LONG)
//                            .show();
                } else if (!termsOfService.isChecked()) {
                    Toast.makeText(getApplicationContext(),
                            "You must read and agree to our Terms of Service in order to proceed.", Toast.LENGTH_LONG)
                            .show();
                } else {
                    Identify identify = new Identify()
                            .set("Name", mFirebaseUser.getDisplayName())
                            .set("Gender", selectedGender)
                            .set("Age", calculateAge(birthdayLabel.getText().toString()))
                            .set("Email", mFirebaseUser.getEmail());
                    Amplitude.getInstance().identify(identify);

                    dbConnections.createUserAccount(((EditText) findViewById(R.id.username)), selectedGender, mFirebaseUser.getEmail(), birthdayLabel);
                    //Redirects user to the Home screen.
                    startActivity(new Intent(AccountSetupActivity.this, MainActivity.class));
                    finish();

                    // Amplitude Event Tracking.
                    JSONObject jo = new JSONObject();
                    try {
                        jo.put("User ID", mFirebaseUser.getUid());
                        jo.put("Username", (((EditText) findViewById(R.id.username)).getText().toString()));
                        jo.put("Name", mFirebaseUser.getDisplayName());
                        jo.put("Gender", selectedGender);
                        jo.put("Email", mFirebaseUser.getEmail());
                        jo.put("Age", calculateAge(birthdayLabel.getText().toString()));
                        Amplitude.getInstance().logEvent("Completed Registration", jo);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                break;
        }
    }

    public void showDatePickerDialog(View v) throws ParseException {
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getSupportFragmentManager(), "datePicker");


    }

    public int calculateAge(String birthday) {
        String[] parts = birthday.split("/");
        int year = Integer.parseInt(parts[2]);
        int month = Integer.parseInt(parts[1]);
        int day = Integer.parseInt(parts[0]);
        LocalDate birthdate = new LocalDate(year, month, day);
        LocalDate now = new LocalDate();
        Years age = Years.yearsBetween(birthdate, now);
        return age.getYears();
    }

    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Spinner spinner = (Spinner) parent;
        if (spinner.getId() == R.id.gender_spinner) {
            selectedGender = (String) parent.getItemAtPosition(position);

//            Toast.makeText(getApplicationContext(),
//                    selectedGender, Toast.LENGTH_LONG)
//                    .show();
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onDismiss(DialogInterface dialog) {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void PermissionGranted(int request_code) {

    }

    @Override
    public void PartialPermissionGranted(int request_code, ArrayList<String> granted_permissions) {

    }

    @Override
    public void PermissionDenied(int request_code) {

    }

    @Override
    public void NeverAskAgain(int request_code) {

    }
}

