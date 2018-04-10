package haitsu.groupup.other.Adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;

import haitsu.groupup.R;
import haitsu.groupup.activity.Groups.GroupInfoActivity;
import haitsu.groupup.other.DBConnections;
import haitsu.groupup.other.Models.Group;
import haitsu.groupup.other.Models.Groups;
import haitsu.groupup.other.Models.Notification;

/**
 * Created by moham on 26/12/2017.
 */

public class GroupsAdapter extends ArrayAdapter<Group> {
    private DBConnections dbConnections = new DBConnections();

    public GroupsAdapter(Context context, ArrayList<Group> group) {
        super(context, 0, group);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        final Group group = getItem(position);
        final ListView mListView = (ListView) parent.findViewById(R.id.listview);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.groups_item, parent, false);
        }

        ((TextView) convertView.findViewById(R.id.group_name)).setText(group.getName());
        ((TextView) convertView.findViewById(R.id.group_gender)).setText("Members: " + group.getGenders());
        ((TextView) convertView.findViewById(R.id.group_limit)).setText(group.getMemberCount() + "/"
                + group.getMemberLimit());


        // Initialize a TextView for ListView each Item
        TextView tv = (TextView) convertView.findViewById(R.id.group_name);
        TextView tv2 = (TextView) convertView.findViewById(R.id.group_gender);

        // Set the text color of TextView (ListView Item)
        tv.setTextColor(Color.BLACK);
        tv2.setTextColor(Color.BLACK);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            public void onItemClick(AdapterView<?> adapter, View v, int position, long id) {
                mListView.setFocusable(true);//HACKS
                Group group = ((Group) mListView.getItemAtPosition(position));
                if (group != null) {
                    groupExists(group);
                }
            }


        });


        // Return the completed view to render on screen
        return convertView;
    }


    public void groupExists(final Group group) {
        final AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(getContext(), R.style.MyAlertDialogStyle);
        } else {
            builder = new AlertDialog.Builder(getContext());
        }

        DatabaseReference groupRef = FirebaseDatabase.getInstance().getReference().child("group").child(group.getCategory()).child(group.getType());
        groupRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.hasChild(group.getGroupId())) {
                    // Group doesn't exist anymore
                    builder.setTitle("Group not found")
                            .setMessage("The group may have been removed by the admin.")
                            .setPositiveButton(android.R.string.yes, null)
                            .show();
                } else {
                    boolean groupIsFull = dataSnapshot.child(group.getGroupId()).child("type").getValue(String.class).contains("FULL");
                    if (groupIsFull) {
                        builder.setTitle("Group is now full")
                                .setMessage("Uh-oh! Someone else may have taken that last spot.")
                                .setPositiveButton(android.R.string.yes, null)
                                .show();
                    } else {
                        Intent intent = new Intent(getContext(), GroupInfoActivity.class);
                        Bundle extras = new Bundle();
                        extras.putString("GROUP_ID", group.getGroupId());
                        extras.putString("GROUP_CATEGORY", group.getCategory());
                        extras.putString("GROUP_ADMIN", group.getAdminID());
                        extras.putString("GROUP_TYPE", group.getType());
                        intent.putExtras(extras);
                        getContext().startActivity(intent);

                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}