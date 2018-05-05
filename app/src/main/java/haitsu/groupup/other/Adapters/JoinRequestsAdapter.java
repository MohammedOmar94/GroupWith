package haitsu.groupup.other.Adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.joda.time.LocalDate;
import org.joda.time.Years;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import haitsu.groupup.R;
import haitsu.groupup.other.DBConnections;
import haitsu.groupup.other.Models.DataModel;
import haitsu.groupup.other.Models.User;
import haitsu.groupup.other.Models.UserRequest;

/**
 * Created by moham on 26/12/2017.
 */

public class JoinRequestsAdapter extends ArrayAdapter<DataModel> {
    private DBConnections dbConnections = new DBConnections();
    private DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference();

    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;

    public JoinRequestsAdapter(Context context, ArrayList<DataModel> membersList) {
        super(context, 0, membersList);
        System.out.println("adapter " + membersList);
    }

    @Override
    public View getView(final int position, View view, ViewGroup parent) {
        // Get the data item for this position
        System.out.println("Hey you!");
        final UserRequest request = getItem(position).getJoinRequestSnapshot().getValue(UserRequest.class);
        final ListView mListView = (ListView) parent.findViewById(R.id.listview);
        System.out.println("Request is " + request.getGroupName());
        // Check if an existing view is being reused, otherwise inflate the view
        if (view == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.requests_item, parent, false);
        }


        // Initialize Firebase Auth
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();

        System.out.println("ayy " + request.getGroupCategory());
        int age = calculateAge(request.getAge());
        ((TextView) view.findViewById(R.id.group_name)).setText(request.getGroupName().toString());
        ((TextView) view.findViewById(R.id.users_details)).setText(request.getUsername() + " wants to join your group!");
//                ((TextView) view.findViewById(R.id.time_label)).setText((DateFormat.format("dd-MM-yyyy", request.getTimeOfRequest())));

        Date requestDate = new Date(request.getTimeOfRequest());
        Date currentDate = new Date();
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();

        cal1.setTime(currentDate);
        cal2.setTime(requestDate);

        int today = cal1.get(Calendar.DAY_OF_WEEK);
        int notificationDay = cal2.get(Calendar.DAY_OF_WEEK);

        int daysFromWeek = today - notificationDay;

        long diff = currentDate.getTime() - requestDate.getTime();
        float daysFromTime = (diff / (1000 * 60 * 60 * 24));
        int daysRounded = Math.round(daysFromTime);

        if (daysFromWeek == 0 && daysRounded == 0) {
            ((TextView) view.findViewById(R.id.time_label)).setText(DateFormat.format("HH:mm", requestDate));
        } else if (daysRounded == 1 || (daysFromWeek == 1 && daysRounded == 0)) {
            System.out.println("ayyy");
            ((TextView) view.findViewById(R.id.time_label)).setText("Yesterday " + DateFormat.format("HH:mm", requestDate));
        } else {
            ((TextView) view.findViewById(R.id.time_label)).setText(DateFormat.format("dd-MM-yyyy", requestDate));
        }

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            public void onItemClick(AdapterView<?> adapter, View v, int position, long id) {
                mListView.setFocusable(true);//HACKS
                User user = ((DataModel) mListView.getItemAtPosition(position)).getUserSnapshot().getValue(User.class);
                final String userId = ((DataModel) mListView.getItemAtPosition(position)).getUserSnapshot().getKey();
//                final String groupId = ((DataModel) mListView.getItemAtPosition(position)).getGroupId();
//                final String adminId = ((DataModel) mListView.getItemAtPosition(position)).getGroupAdminId();
//                final String groupCategory = ((DataModel) mListView.getItemAtPosition(position)).getGroupCategory();
//                final String groupType = ((DataModel) mListView.getItemAtPosition(position)).getType();
//                System.out.println("group type issss " + userId + " " + mFirebaseUser.getUid() + " admin " + adminId);

                AlertDialog.Builder builder;
                final UserRequest request = ((DataModel) mListView.getItemAtPosition(position)).getJoinRequestSnapshot().getValue(UserRequest.class);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    builder = new AlertDialog.Builder(getContext(), R.style.MyAlertDialogStyle);
                } else {
                    builder = new AlertDialog.Builder(getContext());
                }
                builder.setTitle("Accept request to join group")
                        .setMessage(Html.fromHtml(joinRequestDialogMessage(request)))
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                acceptJoinRequest(request);
                            }
                        })
                        .setNegativeButton(R.string.option_decline, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                declineJoinRequest(request);
                            }
                        })
                        .show();
                System.out.println("ID IS " + request.getGroupId());

            }


        });


        // Return the completed view to render on screen
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

    public void acceptJoinRequest(UserRequest request) {
        // Remove request from admin.
        databaseRef.child("users").child(mFirebaseUser.getUid()).child("userRequest").child(request.getGroupId()).
                child(request.getUserId()).removeValue();

        // Approve user.
        databaseRef.child("users").child(request.getUserId()).child("groups").child(request.getGroupId()).child("userApproved").setValue(true);
        databaseRef.child("group").child(request.getGroupCategory()).child(request.getType()).child(request.getGroupId()).child("members").child(request.getUserId()).setValue(true);

        // Need to add groups tree for that user who was accepted to join.
    }

    public void declineJoinRequest(UserRequest request) {
        // Remove request from admin.
        databaseRef.child("users").child(mFirebaseUser.getUid()).child("userRequest").child(request.getGroupId())
                .child(request.getUserId()).removeValue();

        // Remove user from group.
        dbConnections.removeUser(request.getUserId(), request.getGroupId(), request.getGroupCategory(), request.getType());
    }


}