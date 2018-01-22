package haitsu.groupup.fragment.Account;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.Date;

import haitsu.groupup.R;
import haitsu.groupup.other.Models.Notification;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link NotificationsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link NotificationsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class NotificationsFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private ListView mListView;


    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;

    private OnFragmentInteractionListener mListener;

    public NotificationsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment NotificationsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static NotificationsFragment newInstance(String param1, String param2) {
        NotificationsFragment fragment = new NotificationsFragment();
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
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_my_groups, container, false);

        // Initialize Firebase Auth
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();

        mListView = (ListView) view.findViewById(R.id.listview);
        mListView.setFocusable(false);//PREVENTS FROM JUMPING TO BOTTOM OF PAGE

        final DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference();
        final DatabaseReference us = databaseRef.child("users").child(mFirebaseUser.getUid()).child("groups");
        final Query us2 = databaseRef.child("users").child(mFirebaseUser.getUid()).child("groups").orderByChild("lastMessage");
        final DatabaseReference group = databaseRef.child("group");
        final DatabaseReference chats = databaseRef.child("chats");
        final DatabaseReference notifications = databaseRef.child("notifications").child(mFirebaseUser.getUid());
        notifications.keepSynced(true);
        final FirebaseListAdapter<Notification> usersAdapter = new FirebaseListAdapter<Notification>(getActivity(), Notification.class, R.layout.notification, notifications) {
            protected void populateView(View view, Notification notification, int position) {
                System.out.println("Notification is" + notification.getMessageText());
                ((TextView) view.findViewById(R.id.message_text)).setText(notification.getMessageText());
                Date messageDate = new Date(notification.getMessageTime());
                Date currentDate = new Date();
                long diff = currentDate.getTime() - messageDate.getTime();
                float days = (diff / (1000 * 60 * 60 * 24));
                int daysRounded = Math.round(days);
                if (daysRounded == 0) {
                    ((TextView) view.findViewById(R.id.message_time)).setText(DateFormat.format("HH:mm", messageDate));
                } else if (daysRounded == 1) {
                    ((TextView) view.findViewById(R.id.message_time)).setText("Yesterday " + DateFormat.format("HH:mm", messageDate));
                } else {
                    ((TextView) view.findViewById(R.id.message_time)).setText(DateFormat.format("dd-MM-yyyy", messageDate));
                }

                // ((TextView) view.findViewById(R.id.message_text)).setText("Be the first to say Hello!");
                //    ((TextView) view.findViewById(R.id.message_time)).setText(DateFormat.format("HH:mm:ss", message.getMessageTime()));
            }

            //  (


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
        mListView.setAdapter(usersAdapter);
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
