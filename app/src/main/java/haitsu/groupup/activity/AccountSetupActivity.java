package haitsu.groupup.activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import haitsu.groupup.PermissionUtils;
import haitsu.groupup.R;
import haitsu.groupup.fragment.DatePickerFragment;
import haitsu.groupup.other.DBConnections;

public class AccountSetupActivity extends AppCompatActivity
        implements View.OnClickListener, AdapterView.OnItemSelectedListener,
        DialogInterface.OnDismissListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        PermissionUtils.PermissionResultCallback {

    //Age users selects from the dropdown list.
    private String selectedGender;
    private String city;
    private String country;

    private Button mfinishSetup;
    GoogleApiClient mGoogleApiClient;
    private Toolbar toolbar;
    private Spinner spinner;

    //Access to methods dealing with Firebase.
    private DBConnections dbConnections = new DBConnections();

    private final static int PLAY_SERVICES_REQUEST = 1000;
    private final static int REQUEST_CHECK_SETTINGS = 2000;
    private Location mLastLocation;

    double latitude;
    double longitude;

    FirebaseAuth mFirebaseAuth;
    FirebaseUser mFirebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_setup);


        ArrayList<String> permissions = new ArrayList<>();
        PermissionUtils permissionUtils;

        permissionUtils = new PermissionUtils(AccountSetupActivity.this);

        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);

        permissionUtils.check_permission(permissions, "Need GPS permission for getting your location", 1);


        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();

        mGoogleApiClient.connect();

        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);


        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Initialize Firebase Auth
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();

        mfinishSetup = (Button) findViewById(R.id.finish_setup);
        spinner = (Spinner) findViewById(R.id.gender_spinner);
        TextView txt = (TextView) findViewById(R.id.location_label);
        txt.setOnClickListener(this);
        mfinishSetup.setOnClickListener(this);
        spinner.setOnItemSelectedListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.finish_setup:
                //Passes the users details (Username, Email, Age) and stores them in the database.
                if (city == null || country ==  null) {
                    Toast.makeText(getApplicationContext(),
                            "Please retrieve your location.", Toast.LENGTH_LONG)
                            .show();
                } else {
                    dbConnections.createUserAccount(((EditText) findViewById(R.id.username)), selectedGender, mFirebaseUser.getEmail(), (TextView) findViewById(R.id.birthday_label), city, country, latitude, longitude);
                    //Redirects user to the Home screen.
                    startActivity(new Intent(AccountSetupActivity.this, MainActivity.class));
                    finish();
                }
            break;
            case R.id.location_label:
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                mLastLocation = LocationServices.FusedLocationApi
                        .getLastLocation(mGoogleApiClient);

                if (mLastLocation != null) {
                    latitude = mLastLocation.getLatitude();
                    longitude = mLastLocation.getLongitude();
                    getAddress();
                } else {
                    Toast.makeText(getApplicationContext(),
                            "Please turn on your Location.", Toast.LENGTH_LONG)
                            .show();
                }
                break;
        }
    }

    public void showDatePickerDialog(View v) throws ParseException {
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getSupportFragmentManager(), "datePicker");


    }

    private boolean checkPlayServices() {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = googleApiAvailability.isGooglePlayServicesAvailable(this);

        if (resultCode != ConnectionResult.SUCCESS) {
            if (googleApiAvailability.isUserResolvableError(resultCode)) {
                googleApiAvailability.getErrorDialog(this, resultCode,
                        PLAY_SERVICES_REQUEST).show();
            } else {
                Toast.makeText(getApplicationContext(),
                        "This device is not supported.", Toast.LENGTH_LONG)
                        .show();
            }
            return false;
        }
        return true;
    }

    public Address getAddress(double latitude, double longitude) {
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(this, Locale.getDefault());

        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
            return addresses.get(0);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;

    }


    public void getAddress() {
        Address locationAddress = getAddress(latitude, longitude);

        if (locationAddress != null) {
            city = locationAddress.getLocality();
            country = locationAddress.getCountryName();

            String currentLocation;

            if (!TextUtils.isEmpty(city)) {
                currentLocation = city;

                if (!TextUtils.isEmpty(country))
                    currentLocation += "\n" + country;


                TextView txt = (TextView) findViewById(R.id.location_label);
                txt.setText(currentLocation);


                Toast.makeText(getApplicationContext(),
                        "Location is " + currentLocation, Toast.LENGTH_LONG)
                        .show();


            }

        }

    }


    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Spinner spinner = (Spinner) parent;
        if (spinner.getId() == R.id.gender_spinner) {
            selectedGender = (String) parent.getItemAtPosition(position);

            Toast.makeText(getApplicationContext(),
                    selectedGender, Toast.LENGTH_LONG)
                    .show();
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

