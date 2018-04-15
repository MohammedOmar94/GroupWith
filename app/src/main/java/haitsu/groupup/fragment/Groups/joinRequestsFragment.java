package haitsu.groupup.fragment.Groups;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseListAdapter;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.joda.time.LocalDate;
import org.joda.time.Years;

import haitsu.groupup.R;
import haitsu.groupup.other.DBConnections;
import haitsu.groupup.other.Models.UserRequest;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link joinRequestsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link joinRequestsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class joinRequestsFragment extends Fragment {
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

    private View mainContent;
    private TextView mNoGroupsText;
    private ProgressBar progressSpinner;


    private int mShortAnimationDuration;


    private ListView mListView;


    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;


    private DBConnections dbConnections = new DBConnections();

    private DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference();

    private AdView mAdView;

    public joinRequestsFragment() {
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
    public static joinRequestsFragment newInstance(String param1, String param2) {
        joinRequestsFragment fragment = new joinRequestsFragment();
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

        mAdView = (AdView) view.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        // Initialize Firebase Auth
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();

        mainContent = view.findViewById(R.id.content);
        progressSpinner = (ProgressBar) view.findViewById(R.id.loading_spinner);
        mNoGroupsText = (TextView) view.findViewById(R.id.no_groups);
        mainContent.setVisibility(View.GONE);
        // Retrieve and cache the system's default "short" animation time.
        mShortAnimationDuration = getResources().getInteger(
                android.R.integer.config_shortAnimTime);


        mListView = (ListView) view.findViewById(R.id.listview);
        mListView.setFocusable(false);//PREVENTS FROM JUMPING TO BOTTOM OF PAGE

        final DatabaseReference groupRef = databaseRef.child("users").child(mFirebaseUser.getUid()).child("userRequest");
        groupRef.keepSynced(true);
        final FirebaseListAdapter<UserRequest> usersAdapter = new FirebaseListAdapter<UserRequest>(getActivity(), UserRequest.class, R.layout.group_chat, groupRef) {
            protected void populateView(View view, UserRequest request, int position) {
                System.out.println("ayy " + request.getGroupCategory());
                int age = calculateAge(request.getAge());
                view.findViewById(R.id.message_count).setVisibility(View.GONE);
                ((TextView) view.findViewById(R.id.message_user)).setText(request.getGroupName().toString());
                ((TextView) view.findViewById(R.id.message_text)).setText("User: " + request.getUsername() + " Age: " + age + " City: " + request.getCity() + "/ " + request.getCountry());
                ((TextView) view.findViewById(R.id.message_time)).setText((DateFormat.format("dd-MM-yyyy", request.getTimeOfRequest())));
                // ((TextView) view.findViewById(R.id.message_text)).setText("Be the first to say Hello!");
                //    ((TextView) view.findViewById(R.id.message_time)).setText(DateFormat.format("HH:mm:ss", message.getMessageTime()));
            }


        };

        groupRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                mListView.setAdapter(usersAdapter);
                crossfade(mainContent, dataSnapshot);
                mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                    public void onItemClick(AdapterView<?> adapter, View v, int position, long id) {
                        mListView.setFocusable(true);//HACKS
                        final String requestId = usersAdapter.getRef(position).getKey();//Gets key of listview item
                        final UserRequest request = ((UserRequest) mListView.getItemAtPosition(position));
//                        selectedGroupName = group.getName();
//                        Intent intent = new Intent(getActivity(), ChatRoomActivity.class);
//                        Bundle extras = new Bundle();
//                        extras.putString("GROUP_ID", selectedGroup);
//                        extras.putString("GROUP_NAME", selectedGroupName);
//                        intent.putExtras(extras);
//                        startActivity(intent);
                        //User id2 = (User) mListView.getItemAtPosition(position); //

                        AlertDialog.Builder builder;

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            builder = new AlertDialog.Builder(getActivity(), R.style.MyAlertDialogStyle);
                        } else {
                            builder = new AlertDialog.Builder(getActivity());
                        }
                        builder.setTitle("Accept request to join group")
                                .setMessage(Html.fromHtml(joinRequestDialogMessage(request)))
                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        acceptJoinRequest(requestId, request);
                                    }
                                })
                                .setNegativeButton(R.string.option_decline, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        declineJoinRequest(requestId, request);
                                    }
                                })
                                .show();
                        System.out.println("ID IS " + requestId);
                    }


                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

        });


        return view;
    }

    public int calculateAge(String birthday) {
        String[] parts = birthday.split("/");
        int year = Integer.parseInt(parts[2]);
        int month = Integer.parseInt(parts[1]);
        int day = Integer.parseInt(parts[0]);
        LocalDate birthdate = new LocalDate(year, month, day);
        LocalDate now = new LocalDate();
        Years age = Years.yearsBetween(birthdate, now);
        return age.getYears();
    }

    public String joinRequestDialogMessage(UserRequest request) {
        int age = calculateAge(request.getAge());
        String message = "Username: <b>" + request.getUsername() + "</b><br>" +
                "Gender: <b>" + request.getGender() + "</b><br>" +
                "Age: <b>" + age + "</b><br>" +
                "Location: <b>" + request.getCity() + "/" + request.getCountry() + "</b><br>" +
                "Requesting to join: <b>" + request.getGroupName();
        return message;
    }

    public void acceptJoinRequest(String requestId, UserRequest request) {
        databaseRef.child("users").child(mFirebaseUser.getUid()).child("userRequest").child(requestId).removeValue();
        databaseRef.child("users").child(request.getUserId()).child("groups").child(request.getGroupId()).child("userApproved").setValue(true);
        databaseRef.child("group").child(request.getGroupCategory()).child(request.getType()).child(request.getGroupId()).child("members").child(request.getUserId()).setValue(true);

        // Need to add groups tree for that user who was accepted to join.
    }

    public void declineJoinRequest(String requestId, final UserRequest request) {
        // Remove request from admin.
        databaseRef.child("users").child(mFirebaseUser.getUid()).child("userRequest").child(requestId).removeValue();
        dbConnections.removeUser(request.getUserId(), requestId, request.getGroupCategory(), request.getType());
    }

    private void crossfade(final View contentView, DataSnapshot dataSnapshot) {

        // Set the content view to 0% opacity but visible, so that it is visible
        // (but fully transparent) during the animation.
        contentView.setAlpha(0f);
        contentView.setVisibility(View.VISIBLE);
        if (!dataSnapshot.exists()) {
            mNoGroupsText.setAlpha(0f);
            mNoGroupsText.setVisibility(View.VISIBLE);
            mNoGroupsText.setText("No new requests found.");
        }

        // Animate the loading view to 0% opacity. After the animation ends,
        // set its visibility to GONE as an optimization step (it won't
        // participate in layout passes, etc.)
        progressSpinner.animate()
                .alpha(0f)
                // Used so the transition doesn't interfere with the activity's transition on start up.
                .setDuration(mShortAnimationDuration)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        progressSpinner.setVisibility(View.GONE);
                        // Animate the content view to 100% opacity, and clear any animation
                        // listener set on the view.
                        contentView.animate()
                                .alpha(1f)
                                // 1000ms used so the transition happens after the activity's transition on start up.
                                .setDuration(mShortAnimationDuration)
                                .setListener(new AnimatorListenerAdapter() {
                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        mNoGroupsText.animate()
                                                .alpha(1f)
                                                // 1000ms used so the transition happens after the activity's transition on start up.
                                                .setDuration(mShortAnimationDuration)
                                                .setListener(null);
                                    }
                                });
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
