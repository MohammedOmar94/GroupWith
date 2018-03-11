package haitsu.groupup_test.other.Adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.joda.time.LocalDate;
import org.joda.time.Years;

import java.util.ArrayList;

import haitsu.groupup_test.R;
import haitsu.groupup_test.other.DBConnections;
import haitsu.groupup_test.other.Models.DataModel;
import haitsu.groupup_test.other.Models.User;
import haitsu.groupup_test.other.Models.UserRequest;

/**
 * Created by moham on 26/12/2017.
 */

public class UsersAdapter extends ArrayAdapter<DataModel> {
    private DBConnections dbConnections = new DBConnections();
    private DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference();

    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;

    public UsersAdapter(Context context, ArrayList<DataModel> membersList) {
        super(context, 0, membersList);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        final User user = getItem(position).getUserSnapshot().getValue(User.class);
        final ListView mListView = (ListView) parent.findViewById(R.id.listview);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.users_item, parent, false);
        }


        // Initialize Firebase Auth
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();

        ((TextView) convertView.findViewById(R.id.user_name)).setText(user.getUsername());
        ((TextView) convertView.findViewById(R.id.users_details)).setText("Location: " + user.getCity() + "/" + user.getCountry());
        if (getItem(position).getUserSnapshot().getKey().equals(mFirebaseUser.getUid())) {
            ((TextView) convertView.findViewById(R.id.you_label)).setText("You");
        }


        // Initialize a TextView for ListView each Item
        TextView tv = (TextView) convertView.findViewById(R.id.user_name);
        TextView tv2 = (TextView) convertView.findViewById(R.id.users_details);

        // Set the text color of TextView (ListView Item)
        tv.setTextColor(Color.BLACK);
        tv2.setTextColor(Color.BLACK);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            public void onItemClick(AdapterView<?> adapter, View v, int position, long id) {
                mListView.setFocusable(true);//HACKS
                User user = ((DataModel) mListView.getItemAtPosition(position)).getUserSnapshot().getValue(User.class);
                final String userId = ((DataModel) mListView.getItemAtPosition(position)).getUserSnapshot().getKey();
                final String groupId = ((DataModel) mListView.getItemAtPosition(position)).getGroupId();
                final String groupCategory = ((DataModel) mListView.getItemAtPosition(position)).getGroupCategory();
                AlertDialog.Builder builder;

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    builder = new AlertDialog.Builder(getContext(), R.style.MyAlertDialogStyle);
                } else {
                    builder = new AlertDialog.Builder(getContext());
                }
                if (!mFirebaseUser.getUid().equals(userId)) {
                    builder.setTitle(groupId + " " + groupCategory)
                            .setMessage(Html.fromHtml(removeMemberDialogMessage(user)))
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    removeUser(userId, groupId, groupCategory);

                                }
                            })
                            .setNegativeButton(android.R.string.no, null)
                            .show();
                }
            }


        });


        // Return the completed view to render on screen
        return convertView;
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

    public String removeMemberDialogMessage(User user) {
        int age = calculateAge(user.getAge());
        String message = "Username: <b>" + user.getUsername() + "</b><br>" +
                "Gender: <b>" + user.getGender() + "</b><br>" +
                "Age: <b>" + age + "</b><br>" +
                "Location: <b>" + user.getCity() + "/" + user.getCountry() + "</b><br>";
        return message;
    }

    public void acceptJoinRequest(String requestId, UserRequest request) {
        databaseRef.child("users").child(mFirebaseUser.getUid()).child("userRequest").child(requestId).removeValue();
        databaseRef.child("users").child(request.getUserId()).child("groups").child(request.getGroupId()).child("userApproved").setValue(true);
        databaseRef.child("group").child(request.getGroupCategory()).child(request.getGroupId()).child("members").child(request.getUserId()).setValue(true);

        // Need to add groups tree for that user who was accepted to join.
    }

    public void removeUser(String userId, final String groupId, final String groupCategory) {
        databaseRef.child("users").child(userId).child("groups").child(groupId).removeValue();
        databaseRef.child("group").child(groupCategory).child(groupId).child("members").child(userId).removeValue();
        // Updates member count
        databaseRef.child("group").child(groupCategory).child(groupId).
                addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        long memberCount = dataSnapshot.child("members").getChildrenCount();
                        databaseRef.child("group").child(groupCategory).child(groupId).child("memberCount").setValue(memberCount);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
        //Reverts group type from full if already so.
        dbConnections.revertGroupType(groupCategory, groupId);
    }

}