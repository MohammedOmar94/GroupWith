package haitsu.groupup_test.activity.Groups;

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import haitsu.groupup_test.R;
import haitsu.groupup_test.fragment.Groups.GroupInfoFragment;

public class GroupInfoActivity extends AppCompatActivity implements GroupInfoFragment.OnFragmentInteractionListener {

    private String activityTitles[];
    private Toolbar toolbar;


    private String groupCategory;
    private String groupID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_info);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        activityTitles = getResources().getStringArray(R.array.nav_item_activity_titles);
        getSupportActionBar().setTitle(activityTitles[5]);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);


        // Display the fragment as the main content.
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.frame, new GroupInfoFragment())
                .commit();
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
