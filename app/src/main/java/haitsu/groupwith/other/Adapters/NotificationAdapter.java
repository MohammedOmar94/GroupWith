package haitsu.groupwith.other.Adapters;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import haitsu.groupwith.R;
import haitsu.groupwith.other.Models.Notification;

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
            ((TextView) convertView.findViewById(R.id.message_time)).setText("Yesterday " + DateFormat.format("HH:mm", messageDate));
        } else {
            ((TextView) convertView.findViewById(R.id.message_time)).setText(DateFormat.format("dd-MM-yyyy", messageDate));
        }

        // Return the completed view to render on screen
        return convertView;
    }
}