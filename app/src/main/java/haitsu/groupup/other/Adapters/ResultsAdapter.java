package haitsu.groupup.other.Adapters;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import haitsu.groupup.R;
import haitsu.groupup.activity.Groups.GroupInfoActivity;
import haitsu.groupup.other.Models.Group;

/**
 * Created by moham on 26/12/2017.
 */

public class ResultsAdapter extends ArrayAdapter<Group> {
    public ResultsAdapter(Context context, ArrayList<Group> groups) {
        super(context, 0, groups);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        final Group group = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.groups_item, parent, false);
        }

        ((TextView) convertView.findViewById(R.id.group_name)).setText(group.getName());
        ((TextView) convertView.findViewById(R.id.group_gender)).setText("Members: " + group.getGenders());
        ((TextView) convertView.findViewById(R.id.group_limit)).setText(group.getMemberCount() + "/"
                + group.getMemberLimit());
        
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                int position = (Integer) view.getTag();
                // Access the row position here to get the correct data item
                Group group1 = getItem(position);
                System.out.println("Hey! " + group.getName());
                Intent intent = new Intent(getContext(), GroupInfoActivity.class);
                Bundle extras = new Bundle();
                //extras.putString("GROUP_ID", selectedGroup);
                extras.putString("GROUP_ID", group.getGroupId());
                extras.putString("GROUP_CATEGORY", group.getCategory());
                intent.putExtras(extras);
                getContext().startActivity(intent);
                System.out.println("Group name is " + group.getName() + " ID is " + group.getCategory());
                // Do what you want here...
            }
        });
        // Return the completed view to render on screen
        return convertView;
    }
}