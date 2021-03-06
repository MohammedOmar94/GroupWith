package haitsu.groupwith.activity.Groups;

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import haitsu.groupwith.R;
import haitsu.groupwith.fragment.Groups.CreateGroupFragment;

public class CreateGroupActivity extends AppCompatActivity
        implements CreateGroupFragment.OnFragmentInteractionListener {

    private String activityTitles[];
    private Toolbar toolbar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        activityTitles = getResources().getStringArray(R.array.nav_item_activity_titles);
        getSupportActionBar().setTitle(activityTitles[4]);
        //Setups the back button seen in the toolbar.
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(R.id.frame, new CreateGroupFragment())
                .commit();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            //Handles the back button click.
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
