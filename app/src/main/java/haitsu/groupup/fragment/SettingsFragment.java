package haitsu.groupup.fragment;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.data.DataBuffer;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.net.ssl.SNIHostName;

import haitsu.groupup.PermissionUtils;
import haitsu.groupup.R;
import haitsu.groupup.activity.ResultsActivity;
import haitsu.groupup.activity.SignInActivity;
import haitsu.groupup.other.Group;

import static android.R.attr.country;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SettingsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SettingsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener,
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

    double latitude;
    double longitude;


    private String city;
    private String country;

    private FirebaseUser mFirebaseUser;
    private FirebaseAuth mFirebaseAuth;
    private DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference();

    private Preference pref2;


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

        // Initialize Firebase Auth
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        addPreferencesFromResource(R.xml.preferences);

        final EditTextPreference editText = (EditTextPreference) findPreference("display_name");
        pref2 = findPreference("update_location");
        DatabaseReference user = databaseRef.child("users").child(mFirebaseUser.getUid());
        user.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot snapshot) {
                editText.setSummary(snapshot.child("username").getValue(String.class));
                editText.setText(snapshot.child("username").getValue(String.class));
                pref2.setSummary(snapshot.child("city").getValue(String.class) + ", "
                        + snapshot.child("country").getValue(String.class));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        Preference pref = findPreference("sign_out");
        pref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                // TODO Auto-generated method stub
                signOut();
                //finish();
                // Toast.makeText(getActivity(), "Clicked", Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        pref2.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                // TODO Auto-generated method stub
                openDialog();
                //finish();
                // Toast.makeText(getActivity(), "Clicked", Toast.LENGTH_SHORT).show();
                return true;
            }
        });


        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .enableAutoManage((FragmentActivity) getActivity() /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();

        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);
        // calculate margins
        //    button = (Button) view.findViewById(R.id.as);
//        button.setOnClickListener(this);
        return view;
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
    }

    @Override
    public void onResume() {
        super.onResume();
        // Set up a listener whenever a key changes
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        // Unregister the listener whenever a key changes
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
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
        FirebaseAuth.getInstance().signOut();//Sign out of Firebase.
        Auth.GoogleSignInApi.signOut(mGoogleApiClient);//Sign out of Google.
        startActivity(new Intent(getActivity(), SignInActivity.class));
        getActivity().finishAffinity();//Works for Android 4.1 and above only.
        // startActivity(new Intent(getContext(), SignInActivity.class));
        //
    }

    private void openDialog() {
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(getActivity(), R.style.MyAlertDialogStyle);
        } else {
            builder = new AlertDialog.Builder(getActivity());
        }
        builder.setTitle("Update Location")
                .setMessage("Are you sure you want to update your current location?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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
                            databaseRef.child("users").child(mFirebaseUser.getUid()).child("city").setValue(city);
                            databaseRef.child("users").child(mFirebaseUser.getUid()).child("country").setValue(country);
                            databaseRef.child("users").child(mFirebaseUser.getUid()).child("latitude").setValue(latitude);
                            databaseRef.child("users").child(mFirebaseUser.getUid()).child("longitude").setValue(longitude);

                        } else {
                            Toast.makeText(getActivity().getApplicationContext(),
                                    "Please turn on your Location.", Toast.LENGTH_LONG)
                                    .show();
                        }
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                })
                .show();


    }

    public Address getAddress(double latitude, double longitude) {
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(getActivity(), Locale.getDefault());

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


                TextView txt = (TextView) getActivity().findViewById(R.id.location_label);
//                txt.setText(currentLocation);

                pref2.setSummary(currentLocation);
                Toast.makeText(getActivity().getApplicationContext(),
                        "Location is " + currentLocation, Toast.LENGTH_LONG)
                        .show();


            }

        }

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
            databaseRef.child("users").child(mFirebaseUser.getUid()).child("username").setValue(listPref.getText().toString());//Add user}
            pref.setSummary(listPref.getText().toString());
            ((EditTextPreference) pref).setText(listPref.getText().toString());
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
