package haitsu.groupwith.other.Adapters;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import haitsu.groupwith.R;
import haitsu.groupwith.activity.ChatRoomActivity;
import haitsu.groupwith.other.Models.ChatMessage;
import haitsu.groupwith.other.Models.Groups;

/**
 * Created by moham on 26/12/2017.
 */

public class ChatsAdapter extends ArrayAdapter<Groups> {
    public ChatsAdapter(Context context, ArrayList<Groups> group) {
        super(context, 0, group);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        final ChatMessage message = getItem(position).getLastMessage();
        final ListView mListView = (ListView) parent.findViewById(R.id.listview);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.group_chat, parent, false);
        }

        ((TextView) convertView.findViewById(R.id.message_user)).setText(getItem(position).getName());
        if (message != null) {
            ((TextView) convertView.findViewById(R.id.message_text)).setText(message.getMessageUser() + ": " + message.getMessageText());
            if (message.getMessageCount() > 0) {
                ((TextView) convertView.findViewById(R.id.message_count)).setText(Integer.toString(message.getMessageCount()));
                if ((convertView.findViewById(R.id.message_count)).getVisibility() == View.GONE) {
                    (convertView.findViewById(R.id.message_count)).setVisibility(View.VISIBLE);
                }
            } else {
                (convertView.findViewById(R.id.message_count)).setVisibility(View.GONE);
            }
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
                ((TextView) convertView.findViewById(R.id.message_time)).setText(DateFormat.format("HH:mm", messageDate));
            } else if (daysRounded == 1 || (daysFromWeek == 1 && daysRounded == 0)) {
                 System.out.println("ayyy");
                ((TextView) convertView.findViewById(R.id.message_time)).setText("Yesterday " + DateFormat.format("HH:mm", messageDate));
            } else {
                ((TextView) convertView.findViewById(R.id.message_time)).setText(DateFormat.format("dd-MM-yyyy", messageDate));
            }
        } else {
            convertView.findViewById(R.id.message_count).setVisibility(View.GONE);
            ((TextView) convertView.findViewById(R.id.message_time)).setText("");
            ((TextView) convertView.findViewById(R.id.message_text)).setText("Say hello to the group!");
        }

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            public void onItemClick(AdapterView<?> adapter, View v, int position, long id) {
                mListView.setFocusable(true);//HACKS
                Groups group = ((Groups) mListView.getItemAtPosition(position));
                // Need id, maybe usingg snapshot????
                Intent intent = new Intent(getContext(), ChatRoomActivity.class);
                Bundle extras = new Bundle();
                extras.putString("GROUP_ID", group.getGroupId());
                extras.putString("GROUP_NAME", group.getName());
                extras.putString("GROUP_CATEGORY", group.getCategory());
                extras.putString("GROUP_TYPE", group.getType());
                intent.putExtras(extras);
                getContext().startActivity(intent);
            }


        });
        // Return the completed view to render on screen
        return convertView;
    }
}