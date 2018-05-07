package haitsu.groupup.fragment.Search;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.amplitude.api.Amplitude;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONException;
import org.json.JSONObject;

import haitsu.groupup.R;
import haitsu.groupup.activity.Search.ResultsActivity;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SearchFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SearchFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SearchFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener, View.OnClickListener, GoogleApiClient.OnConnectionFailedListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private String genderValue;
    private String categoryValue;
    private String typeValue;
    private int sizeValue;
    private int milesValue;
    private int milesConverted;

    private Button mSignOutButton;
    Button button;

    private GoogleApiClient mGoogleApiClient;

    private FirebaseUser mFirebaseUser;
    private FirebaseAuth mFirebaseAuth;
    private DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference();

    ListPreference listPreference;
    ListPreference listPreference2;
    ListPreference listPreference3;
    ListPreference listPreference4;
    ListPreference listPreference5;


    private OnFragmentInteractionListener mListener;

    public SearchFragment() {
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
    public static SearchFragment newInstance(String param1, String param2) {
        SearchFragment fragment = new SearchFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);// Load the preferences from an XML resource
        setHasOptionsMenu(true);
        // Initialize Firebase Auth
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        addPreferencesFromResource(R.xml.pref_search);


        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        Amplitude.getInstance().logEvent("Opened Search screen");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .enableAutoManage((FragmentActivity) getActivity() /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API)
                .build();
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
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_submit) {
            search();
        }

        return super.onOptionsItemSelected(item);
    }

    public void search() {
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference();
        listPreference = (ListPreference) findPreference("example_list");
        listPreference2 = (ListPreference) findPreference("example_list2");
        listPreference3 = (ListPreference) findPreference("example_list3");
        listPreference4 = (ListPreference) findPreference("example_list4");
        listPreference5 = (ListPreference) findPreference("example_list5");

        CharSequence currText = listPreference.getEntry();
        CharSequence currText2 = listPreference2.getEntry();
        CharSequence currText3 = listPreference3.getEntry();
        CharSequence currText4 = listPreference4.getEntry();
        CharSequence currText5 = listPreference5.getEntry();
        if (currText.toString().equals("Mixed")) {
            genderValue = "Any";
        } else if (currText.toString().equals("Male only")) {
            genderValue = "Male";
        } else if (currText.toString().equals("Female only")) {
            genderValue = "Female";
        }
        categoryValue = currText2.toString();
        typeValue = currText3.toString();
        sizeValue = Integer.parseInt(currText4.toString());
        milesConverted = Integer.parseInt(currText5.toString());
        // With default values already selected, not sure if this case is even possible...
        if (genderValue == null || categoryValue == null || typeValue == null) {
            //
            System.out.println("THIS IS NULL " + genderValue + " " + categoryValue + " " + typeValue + " " + sizeValue);
        } else {
            System.out.println("THIS IS NOT NULL " + genderValue + " " + categoryValue + " " + typeValue + " " + sizeValue);
            Intent intent = new Intent(getActivity(), ResultsActivity.class);
            Bundle extras = new Bundle();
            extras.putString("GROUP_GENDER", genderValue);
            extras.putString("GROUP_CATEGORY", categoryValue);
            extras.putString("GROUP_TYPE", typeValue);
            extras.putInt("MEMBER_LIMIT", sizeValue);
            extras.putInt("MILES_CONVERTED", milesConverted);
            intent.putExtras(extras);
            startActivity(intent);

            JSONObject jo = new JSONObject();
            try {
                jo.put("User ID", mFirebaseUser.getUid());
                jo.put("Member Limit", sizeValue);
                jo.put("Group Gemder", genderValue);
                jo.put("Group Category", categoryValue);
                jo.put("Group Type", typeValue);
                jo.put("Group Distance", milesConverted + " Miles or less");
                Amplitude.getInstance().logEvent("Filter for Gender  -  " + genderValue);
                Amplitude.getInstance().logEvent("Filter for Group Category  -  " + categoryValue);
                Amplitude.getInstance().logEvent("Filter for Member Limit  -  " + sizeValue);
                Amplitude.getInstance().logEvent("Filter for Group Type  -  " + typeValue);
                Amplitude.getInstance().logEvent("Filter for Group Distance  -  " + milesConverted + " Miles or less");
                Amplitude.getInstance().logEvent("Submitted Search Filters", jo);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public double calculateKilometers(String milesStr) {
        double miles = Double.parseDouble(milesStr);                     //Note 1
        //... Compute kilometers.  There are 0.621 miles in a kilometer.
        return miles / 0.621;

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
