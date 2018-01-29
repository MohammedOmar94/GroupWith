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

import java.util.Calendar;
import java.util.Date;

import haitsu.groupup.R;
import haitsu.groupup.activity.ChatRoomActivity;
import haitsu.groupup.other.DBHandler;
import haitsu.groupup.other.Models.ChatMessage;
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


    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;

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
        final Query us = databaseRef.child("users").child(mFirebaseUser.getUid()).child("groups").orderByChild("userApproved").equalTo(true);
        us.keepSynced(true);
        final DatabaseReference group = databaseRef.child("group");
        final DatabaseReference chats = databaseRef.child("chats");
        final FirebaseListAdapter<Groups> usersAdapter = new FirebaseListAdapter<Groups>(getActivity(), Groups.class, R.layout.group_chat, us) {
            protected void populateView(View view, Groups groupInfo, int position) {
                // Map<String,String> lastMessage = groupInfo.getLastMessage();
                // System.out.println("Group name is " + lastMessage);
                ChatMessage message = groupInfo.getLastMessage();
                if (message != null) {
                    // db.addMessage(mFirebaseUser.getUid(),message.getMessageText(), message.getMessageTime(), message.getMessageUser());
                }
                //db.displayMessage();

                ((TextView) view.findViewById(R.id.message_user)).setText(groupInfo.getName());
                if (groupInfo.getLastMessage() != null) {
                    ((TextView) view.findViewById(R.id.message_text)).setText(message.getMessageUser() + ": " + message.getMessageText());
                    Date messageDate = new Date(message.getMessageTime());
                    Date currentDate = new Date();
                    Calendar cal1 = Calendar.getInstance();
                    Calendar cal2 = Calendar.getInstance();

                    cal1.setTime(currentDate);
                    cal2.setTime(messageDate);

                    int today = cal1.get(Calendar.DAY_OF_WEEK);
                    int notificationDay = cal2.get(Calendar.DAY_OF_WEEK);

                    int daysFromWeek = today - notificationDay;

                    long diff = currentDate.getTime() - messageDate.getTime();
                    float daysFromTime = (diff / (1000 * 60 * 60 * 24));
                    int daysRounded = Math.round(daysFromTime);

                    if (daysFromWeek == 0 && daysRounded == 0) {
                        ((TextView) view.findViewById(R.id.message_time)).setText(DateFormat.format("HH:mm", messageDate));
                    } else if (daysRounded == 1 || (daysFromWeek == 1 && daysRounded == 0)) {
                        ((TextView) view.findViewById(R.id.message_time)).setText("Yesterday " + DateFormat.format("HH:mm", messageDate));
                    } else {
                        ((TextView) view.findViewById(R.id.message_time)).setText(DateFormat.format("dd-MM-yyyy", messageDate));
                    }
                } else {
                    ((TextView) view.findViewById(R.id.message_text)).setText("Say hello to the group!");
                }
                //  (
            }
/*
            @Override
            public View getView(int position, View convertView, ViewGroup parent){
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
*/

        };
        final FirebaseListAdapter<Groups> usersAdapter2 = new FirebaseListAdapter<Groups>(getActivity(), Groups.class, R.layout.messages, us) {
            protected void populateView(View view, Groups groupInfo, int position) {
                // Map<String,String> lastMessage = groupInfo.getLastMessage();
                // System.out.println("Group name is " + lastMessage);
                ((TextView) view.findViewById(R.id.message_user)).setText(groupInfo.getName());
                // ((TextView) view.findViewById(R.id.message_text)).setText(groupInfo.getCategory());
                // ((TextView) view.findViewById(R.id.message_time)).setText("aaa");
            }


        };
        us.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                mListView.setAdapter(usersAdapter);
                mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                    public void onItemClick(AdapterView<?> adapter, View v, int position, long id) {
                        mListView.setFocusable(true);//HACKS
                        String key = usersAdapter.getRef(position).getKey();//Gets key of listview item
                        Groups group = ((Groups) mListView.getItemAtPosition(position));
                        selectedGroup = key;
                        selectedGroupName = group.getName();
                        Intent intent = new Intent(getActivity(), ChatRoomActivity.class);
                        Bundle extras = new Bundle();
                        extras.putString("GROUP_ID", selectedGroup);
                        extras.putString("GROUP_NAME", selectedGroupName);
                        intent.putExtras(extras);
                        startActivity(intent);
                        //User id2 = (User) mListView.getItemAtPosition(position); //
                        System.out.println("ID IS " + key);
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
