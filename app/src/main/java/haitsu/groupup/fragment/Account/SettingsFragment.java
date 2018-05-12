package haitsu.groupup.fragment.Account;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;

import com.amplitude.api.Amplitude;
import com.amplitude.api.Identify;
import com.takisoft.fix.support.v7.preference.EditTextPreference;
import com.takisoft.fix.support.v7.preference.PreferenceFragmentCompat;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import haitsu.groupup.PermissionUtils;
import haitsu.groupup.R;
import haitsu.groupup.activity.Account.SignInActivity;
import haitsu.groupup.other.DBConnections;
import haitsu.groupup.other.Models.User;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SettingsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SettingsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener,
        com.google.android.gms.location.LocationListener,
        View.OnClickListener, GoogleApiClient.OnConnectionFailedListener,
        DialogInterface.OnDismissListener, GoogleApiClient.ConnectionCallbacks,
        PermissionUtils.PermissionResultCallback {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private Button mSignOutButton;
    Button button;

    private GoogleApiClient mGoogleApiClient;
    private final static int PLAY_SERVICES_REQUEST = 1000;
    private final static int REQUEST_CHECK_SETTINGS = 2000;
    private Location mLastLocation;
    private LocationRequest mLocationRequest;
    private FusedLocationProviderClient mFusedLocationClient;

    /**
     * Callback for Location events.
     */
    private LocationCallback mLocationCallback;

    /**
     * Represents a geographical location.
     */
    private Location mCurrentLocation;

    double latitude;
    double longitude;


    private String city;
    private String country;

    private FirebaseUser mFirebaseUser;
    private FirebaseAuth mFirebaseAuth;
    private DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference();
    private DatabaseReference user;

    private ValueEventListener listener;

    private EditTextPreference editText;

    private String currentUsername;

    private Boolean mRequestingLocationUpdates;
    private final String REQUESTING_LOCATION_UPDATES_KEY = "requesting-location-updates-key";

    private Preference pref2;
    private Preference pref3;


    private OnFragmentInteractionListener mListener;

    public SettingsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SettingsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SettingsFragment newInstance(String param1, String param2) {
        SettingsFragment fragment = new SettingsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);// Load the preferences from an XML resource


        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        Amplitude.getInstance().logEvent("Viewing Settings screen");
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

    }

    @Override
    public void onCreatePreferencesFix(@Nullable Bundle savedInstanceState, String rootKey) {

        // Initialize Firebase Auth
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();

        setPreferencesFromResource(R.xml.preferences, rootKey);

        editText = (EditTextPreference) findPreference("username");
        user = databaseRef.child("users").child(mFirebaseUser.getUid());


        Preference pref = findPreference("sign_out");
        pref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                // TODO Auto-generated method stub
                Amplitude.getInstance().logEvent("Logged out");
                Amplitude.getInstance().setUserId("");
                Identify identify = new Identify()
                        .unset("Name")
                        .unset("Gender")
                        .unset("Age")
                        .unset("Email");
                Amplitude.getInstance().identify(identify);
                databaseRef.child("users").child((mFirebaseUser.getUid())).child("lastLogout").setValue(new Date().getTime());
                signOut();
                //finish();
                // Toast.makeText(getActivity(), "Clicked", Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        Preference delete_pref = findPreference("delete_account");
        delete_pref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                final AlertDialog.Builder builder;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    builder = new AlertDialog.Builder(getActivity(), R.style.MyAlertDialogStyle);
                } else {
                    builder = new AlertDialog.Builder(getActivity());
                }
                // TODO Auto-generated method stub
                builder.setTitle("Delete account")
                        .setMessage("Are you sure you want to completely delete your account?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Amplitude.getInstance().logEvent("Deleted account");
                                new DBConnections().deleteAccount();

                                Amplitude.getInstance().setUserId("");
                                Identify identify = new Identify()
                                        .unset("Name")
                                        .unset("Gender")
                                        .unset("Age")
                                        .unset("Email");
                                Amplitude.getInstance().identify(identify);
//                                signOut();
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // do nothing
                            }
                        })
                        .show();
                //finish();
                // Toast.makeText(getActivity(), "Clicked", Toast.LENGTH_SHORT).show();
                return true;
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        pref3 = findPreference("date_of_birth");
        pref2 = findPreference("update_location");
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .enableAutoManage((FragmentActivity) getActivity() /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);


        return view;
    }

    public void setListener() {
        listener = user.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot snapshot) {
                if (snapshot.getChildrenCount() == 0) {
                    System.out.println("snake " + snapshot.getValue());
                    signOut();
                } else {
                    User user = snapshot.getValue(User.class);
                    currentUsername = user.getUsername();
                    editText.setSummary(user.getUsername());
                    editText.setText(user.getUsername());
                    pref3.setSummary(user.getAge());
                    if (user.getCity() != null && user.getCountry() != null) {
                        pref2.setSummary(user.getCity() + ", " + user.getCountry());
                    } else {
                        pref2.setSummary("-");
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

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
    public void onDestroy() {
        super.onDestroy();
        mGoogleApiClient.stopAutoManage((FragmentActivity) getActivity());
        mGoogleApiClient.disconnect();
        user.removeEventListener(listener);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Set up a listener whenever a key changes
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
        setListener();
        System.out.println("Started listener");
//        if (mRequestingLocationUpdates) {
//            startLocationUpdates();
//        }
    }

    @Override
    public void onPause() {
        super.onPause();
        // Unregister the listener whenever a key changes
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
        user.removeEventListener(listener);
    }

    @Override
    public void onStop() {
        super.onStop();
        // Unregister the listener whenever a key changes
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
        System.out.println("Stopped listener");
        user.removeEventListener(listener);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            //    case R.id.sign:
            //         System.out.println("Dat press tho");
            //          break;
            //     //case R.id.sign_out:
            //    signOut();
            //    break;
        }
    }

    private void signOut() {
        Amplitude.getInstance().setUserId("");
        FirebaseAuth.getInstance().signOut();//Sign out of Firebase.
        Auth.GoogleSignInApi.signOut(mGoogleApiClient);//Sign out of Google.
        startActivity(new Intent(getActivity(), SignInActivity.class));
        getActivity().finishAffinity();//Works for Android 4.1 and above only.
        // startActivity(new Intent(getContext(), SignInActivity.class));
        //
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Preference pref = findPreference(key);

        if (pref instanceof ListPreference) {
            ListPreference listPref = (ListPreference) pref;
            pref.setSummary(listPref.getEntry());
        }

        if (pref instanceof EditTextPreference) {
            EditTextPreference listPref = (EditTextPreference) pref;
            // Regexp ensures that at least one non-blank character is used.
            if (Pattern.compile("\\S").matcher(listPref.getText().toString()).find()) {
                databaseRef.child("users").child(mFirebaseUser.getUid()).child("username").setValue(listPref.getText().toString());//Add user}
                pref.setSummary(listPref.getText().toString());
                ((EditTextPreference) pref).setText(listPref.getText().toString());

                // Amplitude Event Tracking.
                JSONObject jo = new JSONObject();
                try {
                    jo.put("Old Username", currentUsername);
                    jo.put("New Username", listPref.getText().toString());
                    Amplitude.getInstance().logEvent("Cbanged Username", jo);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(getActivity().getApplicationContext(), "Username can't be left blank.", Toast.LENGTH_LONG).show();
            }
        }


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

    @Override
    public void onLocationChanged(Location location) {

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
