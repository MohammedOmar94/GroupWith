package haitsu.groupup.other.Adapters;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Date;

import haitsu.groupup.R;
import haitsu.groupup.activity.Groups.GroupInfoActivity;
import haitsu.groupup.other.Models.Group;
import haitsu.groupup.other.Models.Notification;

/**
 * Created by moham on 26/12/2017.
 */

public class NotificationAdapter extends ArrayAdapter<Notification> {
    public NotificationAdapter(Context context, ArrayList<Notification> notifications) {
        super(context, 0, notifications);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        final Notification notification = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.notification, parent, false);
        }

        ((TextView) convertView.findViewById(R.id.message_text)).setText(notification.getMessageText());
        Date messageDate = new Date(notification.getMessageTime());
        Date currentDate = new Date();
        long diff = currentDate.getTime() - messageDate.getTime();
        float days = (diff / (1000 * 60 * 60 * 24));
        int daysRounded = Math.round(days);
        if (daysRounded == 0) {
            ((TextView) convertView.findViewById(R.id.message_time)).setText(DateFormat.format("HH:mm", messageDate));
        } else if (daysRounded == 1) {
            ((TextView) convertView.findViewById(R.id.message_time)).setText("Yesterday " + DateFormat.format("HH:mm", messageDate));
        } else {
            ((TextView) convertView.findViewById(R.id.message_time)).setText(DateFormat.format("dd-MM-yyyy", messageDate));
        }

        // Return the completed view to render on screen
        return convertView;
    }
}