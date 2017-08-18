package haitsu.groupup.fragment;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import haitsu.groupup.R;
import haitsu.groupup.other.DBConnections;
import haitsu.groupup.other.Group;

import static haitsu.groupup.fragment.GroupsFragment.selectedGroupInfo;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link GroupInfoFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link GroupInfoFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GroupInfoFragment extends Fragment implements View.OnClickListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;



    private DBConnections dbConnections = new DBConnections();
    private DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference();
    private Query groupsFromCategory;

    private GroupInfoFragment.OnFragmentInteractionListener mListener;

    private Button mJoinButton;
    private Button mDeleteButton;
    private ListView mListView;

    private String selectedGroupCategory;
    private String selectedGroupID;
    private String selectedGroupName;


    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;


    FirebaseListAdapter<Group> groupAdapter;

    public GroupInfoFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment GroupInfoFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static GroupInfoFragment newInstance(String param1, String param2) {
        GroupInfoFragment fragment = new GroupInfoFragment();
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
        final View view = inflater.inflate(R.layout.fragment_group_info, container, false);

        // Initialize Firebase Auth
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();

        mJoinButton = (Button) view.findViewById(R.id.join_button);
        mDeleteButton = (Button) view.findViewById(R.id.delete_button);
        mJoinButton.setOnClickListener(this);
        mDeleteButton.setOnClickListener(this);

        //mListView = (ListView) view.findViewById(R.id.listview);
        //mListView.setFocusable(false);//PREVENTS FROM JUMPING TO BOTTOM OF PAGE

        final DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference();
        final DatabaseReference groups2 = databaseRef.child("group").child(selectedGroupInfo);
        groupsFromCategory = databaseRef.child("group").orderByValue().equalTo(selectedGroupInfo);
        groups2.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot snapshot) {
                Group groupInfo = snapshot.getValue(Group.class);
                selectedGroupName = groupInfo.getName();
                selectedGroupCategory = groupInfo.getCategory();
                ((TextView) view.findViewById(R.id.group_name)).setText(groupInfo.getName());
                ((TextView) view.findViewById(R.id.Members)).setText(groupInfo.getGenders());
                ((TextView) view.findViewById(R.id.group_description)).setText(groupInfo.getDescription());
                ((TextView) view.findViewById(R.id.group_description)).setMovementMethod(new ScrollingMovementMethod());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });






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
            case R.id.join_button:
                System.out.println("Group desc is " + selectedGroupName);
                dbConnections.joinGroup(selectedGroupInfo, selectedGroupName, selectedGroupCategory);
                Toast.makeText(getContext().getApplicationContext(), "You joined the " + selectedGroupName + " group!", Toast.LENGTH_LONG).show();
                break;
            case R.id.delete_button:
                boolean groupAdmin = dbConnections.checkGroup(selectedGroupInfo);
                if(groupAdmin) {
                    dbConnections.deleteGroup(selectedGroupInfo);
                }
                Toast.makeText(getContext().getApplicationContext(), "GROUP ADMIN?  " + groupAdmin + "", Toast.LENGTH_LONG).show();

//                Toast.makeText(getContext().getApplicationContext(), "You deleted the " + selectedGroupName + " group!", Toast.LENGTH_LONG).show();
                break;
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
