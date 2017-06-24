package haitsu.groupup.fragment;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import haitsu.groupup.R;
import haitsu.groupup.other.DBConnections;
import haitsu.groupup.other.Group;
import haitsu.groupup.other.Groups;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link GroupsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link GroupsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GroupsFragment extends Fragment implements View.OnClickListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private Button mJoinButton;
    private ListView mListView;

    public static String filteredCategory;
    private String selectedGroupCategory;
    private String selectedGroupID;
    private String selectedGroupName;

    private OnFragmentInteractionListener mListener;

    private DBConnections dbConnections = new DBConnections();

    FirebaseListAdapter<Group> groupAdapter;

    public GroupsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment GroupsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static GroupsFragment newInstance(String param1, String param2) {
        GroupsFragment fragment = new GroupsFragment();
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
        mJoinButton = (Button) view.findViewById(R.id.join_button);
        mListView = (ListView) view.findViewById(R.id.listview);
        mListView.setFocusable(false);//PREVENTS FROM JUMPING TO BOTTOM OF PAGE
        mJoinButton.setOnClickListener(this);

        final DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference();
        final DatabaseReference us = databaseRef.child("users");
        final DatabaseReference group = databaseRef.child("group");
        final Query allPostFromAuthor = databaseRef.child("group").orderByChild("category").equalTo(filteredCategory);
        allPostFromAuthor.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot snapshot) {
               /* for (DataSnapshot ds : snapshot.getChildren()) {
                    final Group groups = ds.getValue(Group.class);//Go through each category
                    if (groups.getCategory().equals(filteredCategory)) {//If the category selected matches category value
                        System.out.println("Group name is " + groups.getName());
                        array.add(groups.getName());
                        adapter = new ArrayAdapter(getActivity().getApplicationContext(), android.R.layout.simple_list_item_1, array);
                    }
                    ;

                }*/

/*
                for(DataSnapshot ds: snapshot.getChildren()) {
                    Group groups = ds.getValue(Group.class);//Go through each category
                    //String key = snapshot.getKey();
                    //final DatabaseReference group2 = databaseRef.child("group").child(key);
                    if (groups.getCategory().equals(filteredCategory)) {//If the category selected matches category value
                        realGroup = groups;
                    }
                }*/

                //for (DataSnapshot ds : snapshot.getChildren()) {
                   // final Group groups = ds.getValue(Group.class);//Go through each category
                    //System.out.println("Group name is " + groups.getName());
                    groupAdapter = new FirebaseListAdapter<Group>(getActivity(), Group.class, android.R.layout.two_line_list_item, allPostFromAuthor) {
                        protected void populateView(View view, Group chatMessage, int position) {
                         //   if (groups.getCategory().equals(filteredCategory)) {
                                ((TextView) view.findViewById(android.R.id.text1)).setText(chatMessage.getName());
                                ((TextView) view.findViewById(android.R.id.text2)).setText(chatMessage.getCategory());
                       //     }
                        }


                        @Override
                        public View getView(int position, View convertView, ViewGroup parent) {
                            // Get the Item from ListView
                            View view = super.getView(position, convertView, parent);

                            // Initialize a TextView for ListView each Item
                            TextView tv = (TextView) view.findViewById(android.R.id.text1);
                            TextView tv2 = (TextView) view.findViewById(android.R.id.text2);

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

        group.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                //for (DataSnapshot ds : dataSnapshot.getChildren()) {
                //User user = ds.getValue(User.class);
                // final Group group  = ds.getValue(Group.class);
                            /* For a specific user's info.
                            User user = mew User();
                            //user.setUsername(ds.child(mFirebaseUser.getUid()).getValue(User.class).getUsername());
                            //user.setEmail(ds.child(mFirebaseUser.getUid()).getValue(User.class).getEmail());
                            System.out.println("Group name: " + user.getEmail());
                            */


                mListView.setAdapter(groupAdapter);
                mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                    public void onItemClick(AdapterView<?> adapter, View v, int position, long id) {
                        mListView.setFocusable(true);//HACKS
                        String key = groupAdapter.getRef(position).getKey();//Gets key of listview item
                        Group group = ((Group) mListView.getItemAtPosition(position));
                        selectedGroupID = key;
                        selectedGroupName = group.getName();
                        selectedGroupCategory = group.getCategory();
                        //User id2 = (User) mListView.getItemAtPosition(position); //
                        System.out.println("ID IS " + key);
                    }


                });

                //}
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
                dbConnections.joinGroup(selectedGroupID, selectedGroupName, selectedGroupCategory);
                Toast.makeText(getContext().getApplicationContext(), "You joined the " + selectedGroupName + " group!", Toast.LENGTH_LONG).show();
                break;
        }
    }

    public String getFilteredCategory() {
        return filteredCategory;
    }

    public void setFilteredCategory(String filteredCategory) {
        this.filteredCategory = filteredCategory;
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
