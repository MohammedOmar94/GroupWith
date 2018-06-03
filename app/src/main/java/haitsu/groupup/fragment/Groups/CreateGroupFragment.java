package haitsu.groupup.fragment.Groups;

import android.app.Fragment;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.amplitude.api.Amplitude;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

import java.util.regex.Pattern;

import haitsu.groupup.R;
import haitsu.groupup.other.DBConnections;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link CreateGroupFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link CreateGroupFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CreateGroupFragment extends Fragment implements View.OnClickListener, AdapterView.OnItemSelectedListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private String selectedCategory;
    private String selectedGender;
    private String selectedGroupType;
    private String selectedMemberCount;


    private OnFragmentInteractionListener mListener;

    private Button mSubmitButton;
    private Spinner spinner3;
    private EditText groupName;
    private EditText groupDescription;


    private DBConnections dbConnections = new DBConnections();


    private InterstitialAd mInterstitialAd;

    public CreateGroupFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment CreateGroupFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CreateGroupFragment newInstance(String param1, String param2) {
        CreateGroupFragment fragment = new CreateGroupFragment();
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
        Amplitude.getInstance().logEvent("Opened Create Group screen");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_create_group, container, false);

        mSubmitButton = (Button) view.findViewById(R.id.submit_button);

        mInterstitialAd = new InterstitialAd(getActivity());
        mInterstitialAd.setAdUnitId("ca-app-pub-7072858762761381/1400425438");
        mInterstitialAd.loadAd(new AdRequest.Builder().build());

        mSubmitButton.setOnClickListener(this);
        Spinner spinner = (Spinner) view.findViewById(R.id.spinner);
//        Spinner spinner2 = (Spinner) view.findViewById(R.id.group_gender);
        Spinner memberCount = (Spinner) view.findViewById(R.id.member_count);
        spinner3 = (Spinner) view.findViewById(R.id.spinner3);
        groupName = ((EditText) view.findViewById(R.id.group_name));
        groupDescription = ((EditText) view.findViewById(R.id.group_description));

        spinner.setOnItemSelectedListener(this);
//        spinner2.setOnItemSelectedListener(this);
        spinner3.setOnItemSelectedListener(this);
        memberCount.setOnItemSelectedListener(this);



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
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.submit_button:
                String groupNameText = groupName.getText().toString();
                String groupDescriptionText = groupDescription.getText().toString();
                // Regexp ensures that at least one non-blank character is used.
                if(Pattern.compile("\\S").matcher(groupNameText).find() && Pattern.compile("\\S").matcher(groupDescriptionText).find()) {
                    dbConnections.newGroupRequest(selectedCategory, selectedGroupType, groupNameText, groupDescriptionText, selectedMemberCount);
                    Toast.makeText(getActivity().getApplicationContext(), "New group created!", Toast.LENGTH_LONG).show();
                    mInterstitialAd.show();
                    getActivity().finish();
                } else {
                    Toast.makeText(getActivity().getApplicationContext(), "All fields must be filled in.", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        // An item was selected. You can retrieve the selected item using
        Spinner spinner = (Spinner) parent;
        if (spinner.getId() == R.id.spinner) {
            selectedCategory = (String) parent.getItemAtPosition(pos);
//            if (selectedCategory.equals("Gaming")) {
//                ArrayAdapter<CharSequence> spinnerArrayAdapter = ArrayAdapter.createFromResource(
//                        getActivity(),
//                        R.array.games_type_arrays, //<!--Your Array -->
//                        android.R.layout.simple_spinner_item);
//                spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//                spinner3.setAdapter(spinnerArrayAdapter);
//            } else {
                ArrayAdapter<CharSequence> spinnerArrayAdapter = ArrayAdapter.createFromResource(
                        getActivity(),
                        R.array.group_type_arrays, //<!--Your Array -->
                        android.R.layout.simple_spinner_item);
                spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner3.setAdapter(spinnerArrayAdapter);
//            }
            //do this
//        } else if (spinner.getId() == R.id.group_gender) {
//            selectedGender = (String) parent.getItemAtPosition(pos);
//            //do this
        } else if (spinner.getId() == R.id.spinner3) {
            selectedGroupType = (String) parent.getItemAtPosition(pos);
            //  parent.getItemAtPosition(pos);
            //   System.out.println("Category is " + selectedCategory);


        } else if (spinner.getId() == R.id.member_count) {
            selectedMemberCount = (String) parent.getItemAtPosition(pos);
            //  parent.getItemAtPosition(pos);
            //   System.out.println("Category is " + selectedCategory);


        }
    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
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
