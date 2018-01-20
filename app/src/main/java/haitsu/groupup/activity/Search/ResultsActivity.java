package haitsu.groupup.activity.Search;

import android.Manifest;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryDataEventListener;
import com.firebase.ui.database.FirebaseListAdapter;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import haitsu.groupup.R;
import haitsu.groupup.fragment.Groups.EventsGroupFragment;
import haitsu.groupup.fragment.Groups.InterestsGroupFragment;
import haitsu.groupup.other.Adapters.ResultsAdapter;
import haitsu.groupup.other.Models.Group;

public class ResultsActivity extends AppCompatActivity implements
        InterestsGroupFragment.OnFragmentInteractionListener,
        EventsGroupFragment.OnFragmentInteractionListener, GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private ViewPager viewPager;
    private TabLayout tabLayout;
    private ListView mListView;
    private ResultsAdapter adapter;

    private ListView mainListView;
    private ArrayAdapter<Group> listAdapter;

    private String selectedGroupID;
    private String selectedGroupName;

    private String groupCategory;
    private String groupGender;
    private String groupType;
    private int memberLimit;
    private double kilometers;


    private FirebaseUser mFirebaseUser;
    private FirebaseAuth mFirebaseAuth;
    private DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference();

    private GeoQuery geoQuery;

    private GoogleApiClient mGoogleApiClient;
    private final static int PLAY_SERVICES_REQUEST = 1000;
    private final static int REQUEST_CHECK_SETTINGS = 2000;
    private Location mLastLocation;
    private LocationRequest mLocationRequest;
    private FusedLocationProviderClient mFusedLocationClient;


    private LocationCallback mLocationCallback;

    double latitude;
    double longitude;


    private String city;
    private String country;


    private FirebaseListAdapter<Group> usersAdapter = null;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    private DatabaseReference groupResults = FirebaseDatabase.getInstance().getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);
        Bundle extras = getIntent().getExtras();
        groupGender = extras.getString("GROUP_GENDER");
        groupCategory = extras.getString("GROUP_CATEGORY");
        groupType = extras.getString("GROUP_TYPE");
        memberLimit = extras.getInt("MEMBER_LIMIT");
        kilometers = extras.getDouble("MILES_CONVERTED");

        mListView = (ListView) findViewById(R.id.listview);
        String title = "Results";
        getSupportActionBar().setTitle(title);


        // Find the ListView resource.
        mainListView = (ListView) findViewById(R.id.listview);


        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    // Update UI with location data
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                    getAddress();
                    stopLocationUpdates();
                    System.out.println("Hey " + city + " " + country + " " + kilometers);
                    searchTest();

                    // ...
                }
            }

            ;
        };

        // Initialize Firebase Auth
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage((FragmentActivity) this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        final LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);
        checkLocationStatus(builder);


        // First filter by groups with the key, containing the latitude and longitude
        // Then in onDataChange, you want to pass the reference for each snapshot (group)


    }

    public void showDialog() {
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(ResultsActivity.this, R.style.MyAlertDialogStyle);
        } else {
            builder = new AlertDialog.Builder(ResultsActivity.this);
        }
        builder.setTitle("Oops! No matches found")
                .setMessage("Maybe you should be the first to create a group of this kind!")
                .setPositiveButton("OK", null)
                .show();
    }

    public void searchTest() {
        final DatabaseReference searchByLocation = FirebaseDatabase.getInstance().getReference().child("group").child(groupCategory);
        final Query searchByFilters = FirebaseDatabase.getInstance().getReference().child("group").child(groupCategory).orderByChild("type_gender_memberLimit").equalTo(groupType + "_" + groupGender + "_" + memberLimit);

        searchByFilters.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        GeoFire geoFire = new GeoFire(searchByLocation);
        // creates a new query around [37.7832, -122.4056] with a radius of 0.6 kilometers
        // Will be done via the users current location, and the radius they selected.
        geoQuery = geoFire.queryAtLocation(new GeoLocation(latitude, longitude), kilometers);


        final ArrayList<Group> planetList = new ArrayList<Group>();


        geoQuery.addGeoQueryDataEventListener(new GeoQueryDataEventListener() {

            GeoFire.CompletionListener abc = new GeoFire.CompletionListener() {
                @Override
                public void onComplete(String key, DatabaseError error) {
                    if (error != null) {
                        System.err.println("There was an error saving the location to GeoFire: " + error);
                    } else {
                        System.out.println("Location saved on server successfully!");
                    }
                }
            };

            // Once they've done that on the groups tree
            @Override
            public void onDataEntered(DataSnapshot dataSnapshot, GeoLocation location) {
                Group group = dataSnapshot.getValue(Group.class);
                group.setGroupId(dataSnapshot.getKey());
                group.setCategory(groupCategory);
                System.out.println("Hey We IN " + String.format("The Key %s entered the search area at [%f,%f]", group.getName(), location.latitude, location.longitude));
                System.out.println("hey " + dataSnapshot.getRef());
                if ((group.getType_gender_memberLimit()).equals(groupType + "_" + groupGender + "_" + memberLimit)) {
                    // Create ArrayAdapter using the planet list.
                    adapter = new ResultsAdapter(ResultsActivity.this, planetList);
                    adapter.add(group);
                    mainListView.setAdapter(adapter);
                }


            }

            @Override
            public void onDataExited(DataSnapshot dataSnapshot) {
                System.out.println("Hey Nothing to see here buddy");
            }

            @Override
            public void onDataMoved(DataSnapshot dataSnapshot, GeoLocation location) {

                System.out.println("Hey just moved within range");
            }

            @Override
            public void onDataChanged(DataSnapshot dataSnapshot, GeoLocation location) {

                System.out.println("Hey data has changed");
            }

            @Override
            public void onGeoQueryReady() {

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

                System.out.println("Hey ERROR");
            }


        });
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 200);
        } else {
            mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                    mLocationCallback,
                    null /* Looper */);
        }
    }

    private void stopLocationUpdates() {
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // remove all event listeners to stop updating in the background
        searchTest();
    }

    @Override
    protected void onStop() {
        if (adapter != null) {
            adapter.clear();
        }
        super.onStop();
        // remove all event listeners to stop updating in the background
        this.geoQuery.removeAllListeners();
    }

    @Override
    protected void onDestroy() {
        if (adapter != null) {
            adapter.clear();
        }
        super.onDestroy();
        // remove all event listeners to stop updating in the background
        this.geoQuery.removeAllListeners();
        mGoogleApiClient.stopAutoManage(this);
        mGoogleApiClient.disconnect();
    }

    public void lastLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 200);
        } else {
            mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {
                                System.out.println("Hey, we have the Last location");
                                mLastLocation = location;
                                // updateLocation();
                                requestLocation();
                            } else {
                                // Pretty busted, added in but not tested properly before. Need to request updates
                                System.out.println("Hey, we don't so we have to Request location");
                                requestLocation();
                            }
                            // Logic to handle location object
                        }
                    });
        }
    }

    public void checkLocationStatus(LocationSettingsRequest.Builder settingsBuilder) {
        SettingsClient client = LocationServices.getSettingsClient(this);
        final Task<LocationSettingsResponse> task = client.checkLocationSettings(settingsBuilder.build());

        task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                // All location settings are satisfied. The client can initialize
                // location requests here.
                // ...
                if (ActivityCompat.checkSelfPermission(ResultsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(ResultsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    System.out.println("Hey no permission");
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    ActivityCompat.requestPermissions(ResultsActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 200);
                } else {
                    lastLocation();
                }

            }
        });

        task.addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                int statusCode = ((ApiException) e).getStatusCode();
                switch (statusCode) {
                    case CommonStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied, but this can be fixed
                        // by showing the user a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            ResolvableApiException resolvable = (ResolvableApiException) e;
                            resolvable.startResolutionForResult(ResultsActivity.this,
                                    REQUEST_CHECK_SETTINGS);
                            lastLocation();
                        } catch (IntentSender.SendIntentException sendEx) {
                            // Ignore the error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way
                        // to fix the settings so we won't show the dialog.
                        break;
                }
            }
        });
    }

    public void requestLocation() {
        startLocationUpdates();
//        System.out.println("Hey we're in request");
//        updateLocation();
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
        System.out.println("Hey lat " + latitude + " long " + longitude);
        Address locationAddress = getAddress(latitude, longitude);

        if (locationAddress != null) {
            city = locationAddress.getLocality();
            country = locationAddress.getCountryName();

            String currentLocation;

            if (!TextUtils.isEmpty(city)) {
                currentLocation = city;

                if (!TextUtils.isEmpty(country))
                    currentLocation += "\n" + country;


//                Toast.makeText(this,
//                        "Location has been updated to " + currentLocation, Toast.LENGTH_LONG)
//                        .show();


            }

        }

    }


    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }
}
