package haitsu.groupup.other.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import haitsu.groupup.R;

/**
 * Created by moham on 22/06/2017.
 */

public class ImageAdapter extends BaseAdapter {
    private Context context;
    private final String[] categoryValues;

    public ImageAdapter(Context context, String[] categoryValues) {
        this.context = context;
        this.categoryValues = categoryValues;
    }

    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View gridView;

        if (convertView == null) {

            gridView = new View(context);

            // get layout from category_item.xmltem.xml
            gridView = inflater.inflate(R.layout.category_item, null);

            // set value into textview
            TextView textView = (TextView) gridView
                    .findViewById(R.id.grid_item_label);
            textView.setText(categoryValues[position]);

            // set image based on selected text
            ImageView imageView = (ImageView) gridView
                    .findViewById(R.id.grid_item_image);

            String mobile = categoryValues[position];

            if (mobile.equals("Food")) {
                imageView.setImageResource(R.drawable.icon_restaurant_black);
            } else if (mobile.equals("Charity")) {
                imageView.setImageResource(R.drawable.icon_heart_black);
            } else if (mobile.equals("Gym")) {
                imageView.setImageResource(R.drawable.icon_dumbbell_black);
            } else if (mobile.equals("Gaming")) {
                imageView.setImageResource(R.drawable.icon_game_black);
            } else if (mobile.equals("Language Learning")) {
                imageView.setImageResource(R.drawable.icon_language_black);
            } else if (mobile.equals("Movies")) {
                imageView.setImageResource(R.drawable.icon_movie_black);
            } else if (mobile.equals("Programming")) {
                imageView.setImageResource(R.drawable.icon_code_black);
            } else if (mobile.equals("Sports")) {
                imageView.setImageResource(R.drawable.icon_soccer_black);
            } else if (mobile.equals("Adventures")) {
                imageView.setImageResource(R.drawable.icon_trekking_black);
            } else if (mobile.equals("Books")) {
                imageView.setImageResource(R.drawable.icon_book_black);
            } else if (mobile.equals("Photography")) {
                imageView.setImageResource(R.drawable.ic_menu_camera);
            } else if (mobile.equals("Cultures")) {
                imageView.setImageResource(R.drawable.icon_globe_black);
            } else {
                //imageView.setImageResource(R.drawable.ic_settings_black_24dp);
            }
            /*
            <item>Sports</item>
        <item>Movies</item>
        <item>Sight-seeing</item>
        <item>Programming</item>
        <item>Gaming</item>
        <item>Music</item>
        <item>Food</item>
        <item>Language Learning</item>
        <item>Education</item>
        <item>Exam Prep</item>
        <item>Cultures</item>
        <item>Travel</item>
        */

        } else {
            gridView = (View) convertView;
        }

        return gridView;
    }

    @Override
    public int getCount() {
        return categoryValues.length;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }
}