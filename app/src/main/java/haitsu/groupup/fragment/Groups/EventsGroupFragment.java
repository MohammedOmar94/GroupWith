package haitsu.groupup.fragment.Groups;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import haitsu.groupup.R;
import haitsu.groupup.activity.Groups.GroupInfoActivity;
import haitsu.groupup.other.DBConnections;
import haitsu.groupup.other.Models.Group;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link EventsGroupFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link EventsGroupFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EventsGroupFragment extends Fragment {
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
    private DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference();
    private Query groupsFromCategory;

    FirebaseListAdapter<Group> groupAdapter;

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

        groupsFromCategory = databaseRef.child("group").child(groupCategory).orderByChild("type").equalTo("Events");


        groupsFromCategory.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot snapshot) {
                groupAdapter = new FirebaseListAdapter<Group>(getActivity(), Group.class, R.layout.groups_item, groupsFromCategory) {
                    protected void populateView(View view, Group group, int position) {
                        ((TextView) view.findViewById(R.id.group_name)).setText(group.getName());
                        ((TextView) view.findViewById(R.id.group_gender)).setText("Members: " + group.getGenders());
                        ((TextView) view.findViewById(R.id.group_limit)).setText(group.getMemberCount() + "/"
                                + group.getMemberLimit());

                    }

                    @Override
                    public View getView(int position, View convertView, ViewGroup parent) {
                        // Get the Item from ListView
                        View view = super.getView(position, convertView, parent);

                        // Initialize a TextView for ListView each Item
                        TextView tv = (TextView) view.findViewById(R.id.group_name);
                        TextView tv2 = (TextView) view.findViewById(R.id.group_gender);

                        // Set the text color of TextView (ListView Item)
                        tv.setTextColor(Color.BLACK);
                        tv2.setTextColor(Color.BLACK);

                        // Generate ListView Item using TextView
                        return view;
                    }


                };
            }
            //   }


            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        groupsFromCategory.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                mListView.setAdapter(groupAdapter);
                mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                    public void onItemClick(AdapterView<?> adapter, View v, int position, long id) {
                        mListView.setFocusable(true);//HACKS
                        String key = groupAdapter.getRef(position).getKey();//Gets key of listview item
                        Group group = ((Group) mListView.getItemAtPosition(position));
                        selectedGroupID = key;
                        selectedGroupName = group.getName();
                        selectedAdminId = group.getAdminID();
                        Intent intent = new Intent(getActivity(), GroupInfoActivity.class);
                        Bundle extras = new Bundle();
                        //extras.putString("GROUP_ID", selectedGroup);
                        extras.putString("GROUP_ID", selectedGroupID);
                        extras.putString("GROUP_CATEGORY", groupCategory);
                        extras.putString("GROUP_ADMIN", selectedAdminId);
                        intent.putExtras(extras);
                        startActivity(intent);
                        // System.out.println("Group name is " + group.getName() + " ID is " + key);
                    }


                });
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
