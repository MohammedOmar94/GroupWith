package haitsu.groupup.fragment;

import android.app.DatePickerDialog;
import android.app.Dialog;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import haitsu.groupup.R;

/**
 * Created by moham on 19/09/2017.
 */

public class DatePickerFragment extends DialogFragment
        implements DatePickerDialog.OnDateSetListener {

    public static int month;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current date as the default date in the picker
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        // Create a new instance of DatePickerDialog and return it
        return new DatePickerDialog(getActivity(), this, year, month, day);
    }

    public void onDateSet(DatePicker view, int year, int month, int day) {
        this.month = month + 1;

        if(day != 0) {
            String selectedDate = day + "/" + this.month + "/" + year;
            Calendar c = Calendar.getInstance();
            Calendar past = Calendar.getInstance();



            DateFormat originalFormat = new SimpleDateFormat("dd/MM/yyyy");
            Date currentDate = new Date();

            try {
                c.setTime(originalFormat.parse(selectedDate));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            past.setTime(currentDate);

            //Calcuates users age.
            int diff = past.get(Calendar.YEAR) - c.get(Calendar.YEAR);
            if (c.get(Calendar.MONTH) > past.get(Calendar.MONTH) ||
                    (c.get(Calendar.MONTH) == past.get(Calendar.MONTH) && c.get(Calendar.DATE) > past.get(Calendar.DATE))) {
                diff--;
            }

            try {
                Date date = originalFormat.parse(selectedDate);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            //Users age
            if(diff >= 18) {
                TextView birthdayLabel = (TextView) getActivity().findViewById(R.id.birthday_label);
                birthdayLabel.setText(selectedDate);
            } else {
                Toast.makeText(getActivity(), "You must be 18 to use this app.", Toast.LENGTH_SHORT).show();
            }
        }


    }


}
