package haitsu.groupup.fragment.Groups;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryDataEventListener;
import com.firebase.ui.database.FirebaseListAdapter;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;

import haitsu.groupup.R;
import haitsu.groupup.activity.Groups.GroupInfoActivity;
import haitsu.groupup.activity.Search.ResultsActivity;
import haitsu.groupup.other.Adapters.GroupsAdapter;
import haitsu.groupup.other.Adapters.NotificationAdapter;
import haitsu.groupup.other.Adapters.ResultsAdapter;
import haitsu.groupup.other.DBConnections;
import haitsu.groupup.other.LocationManager;
import haitsu.groupup.other.Models.Group;
import haitsu.groupup.other.Models.Notification;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link EventsGroupFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link EventsGroupFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EventsGroupFragment extends Fragment implements GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {
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


    private String selectedGroupCategory;
    private String selectedGroupID;
    private String selectedAdminId;
    private String selectedGroupName;
    private String groupCategory;

    private OnFragmentInteractionListener mListener;

    private DBConnections dbConnections = new DBConnections();
    private FirebaseUser mFirebaseUser;
    private FirebaseAuth mFirebaseAuth;
    private DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference();
    private DatabaseReference eventsByLocation;

    private GeoQuery geoQuery;

    private GoogleApiClient mGoogleApiClient;
    private final static int PLAY_SERVICES_REQUEST = 1000;
    private final static int REQUEST_CHECK_SETTINGS = 2000;
    private Location mLastLocation;
    private LocationRequest mLocationRequest;
    private FusedLocationProviderClient mFusedLocationClient;


    private LocationCallback mLocationCallback;

    FirebaseListAdapter<Group> groupAdapter;
    private LocationManager locationManager = new LocationManager();
    private GroupsAdapter adapter;

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
        mListView = (ListView) view.findViewById(R.id.listview);
        mListView.setFocusable(false);//PREVENTS FROM JUMPING TO BOTTOM OF PAGE


        Bundle extras = getActivity().getIntent().getExtras();
        groupCategory = extras.getString("GROUP_CATEGORY");


        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());



        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    locationManager.storeLocationData(location);
                    getSearchResults();
                }
            }
        };

        locationManager.setmLocationCallback(mLocationCallback);

        // Initialize Firebase Auth
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();

        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .enableAutoManage((FragmentActivity) getActivity() /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();

        // Handles all location logic
        locationManager.setmFusedLocationClient(mFusedLocationClient);

        return view;
    }

    public void getSearchResults() {
        eventsByLocation = databaseRef.child("group").child(groupCategory);
        eventsByLocation.keepSynced(true);
        GeoFire geoFire = new GeoFire(eventsByLocation);
        // creates a new query around [37.7832, -122.4056] with a radius of 0.6 kilometers
        // Will be done via the users current location, and the radius they selected.
        geoQuery = geoFire.queryAtLocation(new GeoLocation(locationManager.getLatitude(), locationManager.getLongitude()), 24);


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
                if ((group.getType()).equals("Events")) {
                    // Create ArrayAdapter using the planet list.
                    planetList.add(group);

                    adapter = new GroupsAdapter(getContext(), planetList);
                    mListView.setAdapter(adapter);
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

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        locationManager.initialiseLocationRequest(getActivity());
    }

    @Override
    public void onPause() {
        super.onPause();
        // remove all event listeners to stop updating in the background
        if(geoQuery != null) {
            this.geoQuery.removeAllListeners();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        // remove all event listeners to stop updating in the background
        if(geoQuery != null) {
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
