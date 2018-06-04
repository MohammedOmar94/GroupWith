package haitsu.groupwith.other.Adapters;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import haitsu.groupwith.R;
import haitsu.groupwith.activity.Groups.GroupInfoActivity;
import haitsu.groupwith.other.Models.Group;

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
        final ListView mListView = (ListView) parent.findViewById(R.id.listview);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.groups_item, parent, false);
        }

        ((TextView) convertView.findViewById(R.id.group_name)).setText(group.getName());
        ((TextView) convertView.findViewById(R.id.group_gender)).setText("Description: " + group.getDescription());
        ((TextView) convertView.findViewById(R.id.group_limit)).setText(group.getMemberCount() + "/"
                + group.getMemberLimit());

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            public void onItemClick(AdapterView<?> adapter, View v, int position, long id) {
                mListView.setFocusable(true);//HACKS
                Group group = ((Group) mListView.getItemAtPosition(position));
                Intent intent = new Intent(getContext(), GroupInfoActivity.class);
                Bundle extras = new Bundle();
                extras.putString("GROUP_ID", group.getGroupId());
                extras.putString("GROUP_CATEGORY", group.getCategory());
                extras.putString("GROUP_ADMIN", group.getAdminID());
                extras.putString("GROUP_TYPE", group.getType());
                intent.putExtras(extras);
                getContext().startActivity(intent);
            }


        });
        // Return the completed view to render on screen
        return convertView;
    }
}