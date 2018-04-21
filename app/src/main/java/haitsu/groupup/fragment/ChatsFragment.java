package haitsu.groupup.fragment;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

import haitsu.groupup.R;
import haitsu.groupup.activity.ChatRoomActivity;
import haitsu.groupup.other.Adapters.ChatsAdapter;
import haitsu.groupup.other.Adapters.GroupsAdapter;
import haitsu.groupup.other.DBHandler;
import haitsu.groupup.other.Models.ChatMessage;
import haitsu.groupup.other.Models.Group;
import haitsu.groupup.other.Models.Groups;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ChatsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ChatsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ChatsFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private String selectedGroup;
    private String selectedGroupName;

    private OnFragmentInteractionListener mListener;


    private ListView mListView;

    private Query us;
    private ValueEventListener listener;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;


    private ChatsAdapter adapter;
    final ArrayList<Groups> groupsList = new ArrayList<>();

    public ChatsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment GroupsCreatedFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ChatsFragment newInstance(String param1, String param2) {
        ChatsFragment fragment = new ChatsFragment();
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
        View view = inflater.inflate(R.layout.fragment_my_groups, container, false);

        // Initialize Firebase Auth
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();

        mListView = (ListView) view.findViewById(R.id.listview);
        mListView.setFocusable(false);//PREVENTS FROM JUMPING TO BOTTOM OF PAGE
        final DBHandler db = new DBHandler(getActivity());
        //db.dropTable("MyGroups");
        // SQLiteDatabase s = getActivity().openOrCreateDatabase("GroupUp",MODE_PRIVATE,null);
        // db.onCreate(s);

        final DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference();
        us = databaseRef.child("users").child(mFirebaseUser.getUid()).child("groups");
        us.keepSynced(true);
        final DatabaseReference group = databaseRef.child("group");
        final DatabaseReference chats = databaseRef.child("chats");



//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//
//        });


        return view;
    }

    public void setListener() {
        listener = us.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                ArrayList<Groups> groupsWithMsg = new ArrayList<>();
                groupsList.clear();
                System.out.println("heyyyyy");
                ChatMessage previousMessage = null;
                Groups previousGroup = null;
                for (final DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Groups group = snapshot.getValue(Groups.class);
                    ChatMessage chatMessage = group.getLastMessage();
                    group.setGroupId(snapshot.getKey());

                    if (group.getUserApproved()) {

//                        if (chatMessage != null) {
//                            if (previousMessage != null) {
//                                // Current msg happened after previous
//                                boolean compareMsgTimes = chatMessage.getMessageTime() > previousMessage.getMessageTime();
//                                if (compareMsgTimes) {
//                                    groupsList.add(group);
//                                    groupsList.add(previousGroup);
//                                } else {
//                                    groupsList.add(previousGroup);
//                                    groupsList.add(group);
//                                }
//                            }
//
//                            previousMessage = chatMessage;
//                            previousGroup = group;
//                        } else {
//                            // New groups no msgs have been recieved for.
//                        if (chatMessage != null) {
//                            System.out.println("group " + snapshot.getValue());
//                            if (previousMessage != null) {
//                                System.out.println("previous " + previousGroup.getName() + " current " + group.getName());
//                                System.out.println("current is newest? " + (chatMessage.getMessageTime() > previousMessage.getMessageTime()));
//                                if (chatMessage.getMessageTime() > previousMessage.getMessageTime()) {
//                                    groupsList.add(0, group);
//                                } else {
//                                    groupsList.add(group);
//                                }
//                            } else {
//                                groupsList.add(group);
//                            }
//                            previousMessage = chatMessage;
//                            previousGroup = group;
//
//                        } else {
//                            groupsList.add(group);
//                        }
////                        }
//                    }
                        if (chatMessage != null) {
                            groupsWithMsg.add(0, group);
                        } else {
                            groupsList.add(group);
                        }
                    }



                }
                Collections.sort(groupsWithMsg, new Comparator<Groups>() {
                    @Override
                    public int compare(Groups o1, Groups o2) {
                        return o1.compareTo(o2);
                    }
                });


                for (Groups d : groupsWithMsg) {
                    System.out.println("groups msg " + d.getLastMessage().getMessageTime());
                    groupsList.add(0, d);
                    //something here
                }
                adapter = new ChatsAdapter(getActivity(), groupsList);
                mListView.setAdapter(adapter);
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
    public void onResume() {
        super.onResume();
        System.out.println("Started listener");
        setListener();
    }

    @Override
    public void onStop() {
        super.onStop();
        System.out.println("Stopped listener");
        us.removeEventListener(listener);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        us.removeEventListener(listener);
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
