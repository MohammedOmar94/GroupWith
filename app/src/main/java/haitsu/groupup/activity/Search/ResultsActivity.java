package haitsu.groupup.activity.Search;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
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
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.amplitude.api.Amplitude;
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
import haitsu.groupup.other.Adapters.GroupsAdapter;
import haitsu.groupup.other.Adapters.ResultsAdapter;
import haitsu.groupup.other.LocationManager;
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
    private View mainContent;
    private TextView mNoGroupsText;
    private ProgressBar progressSpinner;
    private ArrayAdapter<Group> listAdapter;

    private String selectedGroupID;
    private String selectedGroupName;

    private String groupCategory;
    private String groupGender;
    private String groupType;
    private int memberLimit;
    private int selectedDistance;

    private int mShortAnimationDuration;

    private FirebaseUser mFirebaseUser;
    private FirebaseAuth mFirebaseAuth;
    private DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference();
    private Query searchByLocation;

    private GeoQuery geoQuery;

    private GoogleApiClient mGoogleApiClient;
    private final static int PLAY_SERVICES_REQUEST = 1000;
    private final static int REQUEST_CHECK_SETTINGS = 2000;
    private Location mLastLocation;
    private LocationRequest mLocationRequest;
    private FusedLocationProviderClient mFusedLocationClient;


    private LocationCallback mLocationCallback;

    private FirebaseListAdapter<Group> usersAdapter = null;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;
    private LocationManager locationManager = new LocationManager();

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
        selectedDistance = extras.getInt("MILES_CONVERTED");

        mListView = (ListView) findViewById(R.id.listview);
        mainContent = findViewById(R.id.content);
        progressSpinner = (ProgressBar) findViewById(R.id.loading_spinner);
        mNoGroupsText = (TextView) findViewById(R.id.no_groups);
        mainContent.setVisibility(View.GONE);
        // Retrieve and cache the system's default "short" animation time.
        mShortAnimationDuration = getResources().getInteger(
                android.R.integer.config_shortAnimTime);

        String title = "Results";
        getSupportActionBar().setTitle(title);


        // Find the ListView resource.
        mainListView = (ListView) findViewById(R.id.listview);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        searchByLocation = databaseRef.child("group").child(groupCategory).child(groupType).orderByChild("gender_memberLimit").equalTo(groupGender + "_" + memberLimit);

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    locationManager.storeLocationData(location);
                    getResults();
                    locationManager.stopLocationUpdates();
                }
            }
        };

        locationManager.setmLocationCallback(mLocationCallback);

        // Initialize Firebase Auth
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage((FragmentActivity) this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();

        // Handles all location logic
        locationManager.setmFusedLocationClient(mFusedLocationClient);


        // First filter by groups with the key, containing the latitude and longitude
        // Then in onDataChange, you want to pass the reference for each snapshot (group)


    }

    private void crossfade(final View contentView, DataSnapshot dataSnapshot, List<Group> groupsList) {

        // Set the content view to 0% opacity but visible, so that it is visible
        // (but fully transparent) during the animation.
        contentView.setAlpha(0f);
        contentView.setVisibility(View.VISIBLE);
        if (!dataSnapshot.exists() || groupsList.size() == 0) {
            mNoGroupsText.setAlpha(0f);
            mNoGroupsText.setVisibility(View.VISIBLE);
        }

        // Animate the loading view to 0% opacity. After the animation ends,
        // set its visibility to GONE as an optimization step (it won't
        // participate in layout passes, etc.)
        progressSpinner.animate()
                .alpha(0f)
                // Used so the transition doesn't interfere with the activity's transition on start up.
                .setDuration(mShortAnimationDuration)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        progressSpinner.setVisibility(View.GONE);
                        // Animate the content view to 100% opacity, and clear any animation
                        // listener set on the view.
                        contentView.animate()
                                .alpha(1f)
                                // 1000ms used so the transition happens after the activity's transition on start up.
                                .setDuration(mShortAnimationDuration)
                                .setListener(new AnimatorListenerAdapter() {
                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        mNoGroupsText.animate()
                                                .alpha(1f)
                                                // 1000ms used so the transition happens after the activity's transition on start up.
                                                .setDuration(mShortAnimationDuration)
                                                .setListener(null);
                                    }
                                });
                    }
                });
    }

    public void getResults() {
        searchByLocation.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final ArrayList<Group> groupsList = new ArrayList<>();
                for (final DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Group group = snapshot.getValue(Group.class);
                    Location groupsLocation = new Location("Groups location");
                    groupsLocation.setLatitude(group.getLatitude());
                    groupsLocation.setLongitude(group.getLongitude());

                    Location currentLocation = new Location("Current location");
                    currentLocation.setLatitude(locationManager.getLatitude());
                    currentLocation.setLongitude(locationManager.getLongitude());

                    System.out.println("Distance between groups is " + (groupsLocation.distanceTo(currentLocation) * 0.00062137));
                    System.out.println("Lang " + locationManager.getLongitude());
                    System.out.println("Lang " + locationManager.getLatitude());

                    double distanceInMiles = groupsLocation.distanceTo(currentLocation) * 0.00062137;

                    group.setGroupId(snapshot.getKey());
                    group.setCategory(groupCategory);
                    if (distanceInMiles < selectedDistance) {
                        System.out.println("Final is " + distanceInMiles + " " + selectedDistance);
                        groupsList.add(group);
                        adapter = new ResultsAdapter(ResultsActivity.this, groupsList);
                        mListView.setAdapter(adapter);
                    }
                }
                crossfade(mainContent, dataSnapshot, groupsList);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        Amplitude.getInstance().logEvent("Viewing Results screen");
        System.out.println("Fragment resume");
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    locationManager.storeLocationData(location);
                    getResults();
                    System.out.println("Updating");


                }
            }
        };

        locationManager.setmLocationCallback(mLocationCallback);

        locationManager.initialiseLocationRequest(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        locationManager.stopLocationUpdates();
    }

    @Override
    protected void onPause() {
        super.onPause();
        locationManager.stopLocationUpdates();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mGoogleApiClient.stopAutoManage(this);
        mGoogleApiClient.disconnect();
        locationManager.stopLocationUpdates();
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
