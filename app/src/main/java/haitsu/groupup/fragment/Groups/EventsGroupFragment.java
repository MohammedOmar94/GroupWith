package haitsu.groupup.fragment.Groups;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.location.Location;
import android.net.Uri;
import android.opengl.Visibility;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryDataEventListener;
import com.firebase.ui.database.FirebaseListAdapter;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import haitsu.groupup.R;
import haitsu.groupup.activity.Groups.GroupsActivity;
import haitsu.groupup.other.Adapters.GroupsAdapter;
import haitsu.groupup.other.DBConnections;
import haitsu.groupup.other.LocationManager;
import haitsu.groupup.other.Models.Group;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link EventsGroupFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link EventsGroupFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EventsGroupFragment extends Fragment implements GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks
        , AbsListView.OnScrollListener, View.OnClickListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private Button mJoinButton;
    private Button mDeleteButton;
    private ListView mListView;
    private View mainContent;
    private TextView mNoGroupsText;
    private ProgressBar progressSpinner;
    private int lastItem;

    private int preLast;
    private int mPageLimit = 9;
    int mPageEndOffset = 0;


    private int mShortAnimationDuration;


    private String selectedGroupCategory;
    private String selectedGroupID;
    private String selectedAdminId;
    private String selectedGroupName;
    private String groupCategory;

    private boolean dataHasLoaded = false;

    private OnFragmentInteractionListener mListener;

    private DBConnections dbConnections = new DBConnections();
    private FirebaseUser mFirebaseUser;
    private FirebaseAuth mFirebaseAuth;
    private DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference();
    private Query eventsByLocation;

    private String lastKey = "";

    private GeoQuery geoQuery;

    private GoogleApiClient mGoogleApiClient;
    private final static int PLAY_SERVICES_REQUEST = 1000;
    private final static int REQUEST_CHECK_SETTINGS = 2000;
    private Location mLastLocation;
    private LocationRequest mLocationRequest;
    private FusedLocationProviderClient mFusedLocationClient;

    private View footerView;

    private LocationCallback mLocationCallback;

    FirebaseListAdapter<Group> groupAdapter;
    private LocationManager locationManager;
    private LocationManager lm = new LocationManager();
    private GroupsAdapter adapter;
    final ArrayList<Group> groupsList = new ArrayList<>();

    private AdView mAdView;

    public EventsGroupFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment InterestsGroupFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static EventsGroupFragment newInstance(String param1, String param2) {
        EventsGroupFragment fragment = new EventsGroupFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_groups, container, false);
        GroupsActivity groupsActivity = (GroupsActivity) getActivity();
        System.out.println("Groups activity is " + groupsActivity);
        // So both Events and Interest fragments use the same reference.
        locationManager = groupsActivity.locationManager;

        mAdView = (AdView) view.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        mainContent = view.findViewById(R.id.content);
        progressSpinner = (ProgressBar) view.findViewById(R.id.loading_spinner);
        mNoGroupsText = (TextView) view.findViewById(R.id.no_groups);
        mainContent.setVisibility(View.GONE);
        // Retrieve and cache the system's default "short" animation time.
        mShortAnimationDuration = getResources().getInteger(
                android.R.integer.config_shortAnimTime);

        mListView = (ListView) view.findViewById(R.id.listview);
        mListView.setOnScrollListener(this);
        mListView.setFocusable(false);//PREVENTS FROM JUMPING TO BOTTOM OF PAGE


        footerView = ((LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.footer_layout, null, false);
        footerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                eventsByLocation = databaseRef.child("group").child(groupCategory).child("Events").orderByKey().startAt(lastKey).limitToFirst(mPageLimit);
//                groupsList.clear();
                System.out.println("Last item " + lastItem);
                getGroups();
            }
        });


        Bundle extras = getActivity().getIntent().getExtras();
        groupCategory = extras.getString("GROUP_CATEGORY");


        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());




        // Initialize Firebase Auth
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();

        // Handles all location logic
        lm.setmFusedLocationClient(mFusedLocationClient);

        if (locationManager.getmGoogleApiClient() == null) {
            locationManager.connectToGoogleApiClient(getActivity());
            mGoogleApiClient = locationManager.getmGoogleApiClient();
        } else {
            mGoogleApiClient = locationManager.getmGoogleApiClient();
        }

        eventsByLocation = databaseRef.child("group").child(groupCategory).child("Events").orderByKey().limitToFirst(mPageLimit);
        eventsByLocation.keepSynced(true);
        return view;
    }

    private void getGroups() {
        eventsByLocation.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mNoGroupsText.setText("");
                // Somehow need to get a 2nd value.
                boolean hasGroups = false;
                mListView.removeFooterView(footerView);
                if (dataSnapshot.getChildrenCount() == 9) {
                    mListView.addFooterView(footerView);
                }
                System.out.println("Activity is " + getActivity() + " in Events " + groupCategory);
                for (final DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    System.out.println("snap " + snapshot.getValue());
                    Group group = snapshot.getValue(Group.class);
                    System.out.println("type " + group.getType());
                    Location groupsLocation = new Location("Groups location");
                    groupsLocation.setLatitude(group.getLatitude());
                    groupsLocation.setLongitude(group.getLongitude());

                    Location currentLocation = new Location("Current location");
                    currentLocation.setLatitude(lm.getLatitude());
                    currentLocation.setLongitude(lm.getLongitude());

                    System.out.println("Distance between groups is " + (groupsLocation.distanceTo(currentLocation) * 0.00062137));
                    System.out.println("Lang " + lm.getLongitude());
                    System.out.println("Lang " + lm.getLatitude());

                    double distanceInMiles = groupsLocation.distanceTo(currentLocation) * 0.00062137;

                    group.setGroupId(snapshot.getKey());
                    group.setCategory(groupCategory);
                    if (group.getType().equals("Events")) {
                        if (distanceInMiles < 15) {
                            // Prevents adding the group again when pressing "See more", we only need it as a starting point.
                            if (!lastKey.equals(snapshot.getKey())) {
                                System.out.println("Group name is " + group.getName());
                                groupsList.add(group);
                            }
                            if(getActivity() == null) {
                                System.out.println("Return activity");
                                return;
                            }
                            adapter = new GroupsAdapter(getActivity(), groupsList);
                            mListView.setAdapter(adapter);
                            hasGroups = true;
                            lastKey = snapshot.getKey();
                        }
                    }
                }
                crossfade(mainContent, hasGroups);
                mListView.setSelection(lastItem);
                dataHasLoaded = true;
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void crossfade(final View contentView, boolean hasGroups) {

        // Set the content view to 0% opacity but visible, so that it is visible
        // (but fully transparent) during the animation.
        contentView.setAlpha(0f);
        contentView.setVisibility(View.VISIBLE);
        if (!hasGroups) {
            mNoGroupsText.setText("No groups found.");
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

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onClick(View v) {
    }

    public void reloadData() {
        mainContent.setVisibility(View.GONE);
        groupsList.clear();
        lastKey = "";
        lastItem = 0;
        eventsByLocation = databaseRef.child("group").child(groupCategory).child("Events").orderByKey().limitToFirst(mPageLimit);
        lm.stopLocationUpdates();
    }

    @Override
    public void onPause() {
        super.onPause();
        System.out.println("Fragment pause");
        reloadData();
//        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
//            mGoogleApiClient.stopAutoManage(getActivity());
//            mGoogleApiClient.disconnect();
//        }
        // remove all event listeners to stop updating in the background
        if (geoQuery != null) {
            this.geoQuery.removeAllListeners();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        groupsList.clear();
        System.out.println("Fragment resume");
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    System.out.println("The location is off now");
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    lm.storeLocationData(location);
                    System.out.println("Updating");
                    getGroups();
                    lm.stopLocationUpdates();


                }
            }
        };

        lm.setmLocationCallback(mLocationCallback);

        lm.initialiseLocationRequest(getActivity());
    }

    @Override
    public void onStop() {
        super.onStop();
        System.out.println("Fragment stop");
        reloadData();
//        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
//            mGoogleApiClient.stopAutoManage(getActivity());
//            mGoogleApiClient.disconnect();
//        }
        // remove all event listeners to stop updating in the background
        if (geoQuery != null) {
            this.geoQuery.removeAllListeners();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        System.out.println("Fragment destroy");
        reloadData();
//        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
//            mGoogleApiClient.stopAutoManage(getActivity());
//            mGoogleApiClient.disconnect();
//        }
        // remove all event listeners to stop updating in the background
        if (geoQuery != null) {
            this.geoQuery.removeAllListeners();
        }
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
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

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        lastItem = firstVisibleItem + visibleItemCount;
        if (lastItem == totalItemCount) {
            if (lastKey != null && preLast != lastItem) {
                System.out.println("Last " + lastItem + " total " + totalItemCount + " " + lastKey);

//                mPageLimit = mPageLimit + 10;
//                mPageEndOffset = mPageEndOffset + 1;
//                eventsByLocation = databaseRef.child("group").child(groupCategory).orderByKey().limitToFirst(mPageLimit).startAt(lastKey);
//                groupsList.clear();
//                getGroups();
//                preLast = lastItem;
            }
        }
    }


    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
