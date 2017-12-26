package haitsu.groupup.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryDataEventListener;
import com.firebase.geofire.GeoQueryEventListener;
import com.firebase.ui.database.FirebaseListAdapter;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;

import haitsu.groupup.R;
import haitsu.groupup.fragment.EventsGroupFragment;
import haitsu.groupup.fragment.InterestsGroupFragment;
import haitsu.groupup.other.ChatMessage;
import haitsu.groupup.other.Group;
import haitsu.groupup.other.GroupTypePagerAdapter;
import haitsu.groupup.other.Groups;

import static android.graphics.Color.WHITE;

public class ResultsActivity extends AppCompatActivity implements
        InterestsGroupFragment.OnFragmentInteractionListener,
        EventsGroupFragment.OnFragmentInteractionListener {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private ViewPager viewPager;
    private TabLayout tabLayout;
    private ListView mListView;

    private ListView mainListView;
    private ArrayAdapter<String> listAdapter;

    private String selectedGroupID;
    private String selectedGroupName;

    private String groupCategory;
    private String groupGender;
    private String groupType;
    private int memberLimit;

    private GeoQuery geoQuery;


    private FirebaseListAdapter<Group> usersAdapter = null;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    private DatabaseReference groupResults = FirebaseDatabase.getInstance().getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);
        Bundle extras = getIntent().getExtras();
        groupGender = extras.getString("GROUP_GENDER");
        groupCategory = extras.getString("GROUP_CATEGORY");
        groupType = extras.getString("GROUP_TYPE");
        memberLimit = extras.getInt("MEMBER_LIMIT");

        mListView = (ListView) findViewById(R.id.listview);
        String title = "Results";
        getSupportActionBar().setTitle(title);


        // Find the ListView resource.
        mainListView = (ListView) findViewById(R.id.listview);


        // First filter by groups with the key, containing the latitude and longitude
        // Then in onDataChange, you want to pass the reference for each snapshot (group)
        searchTest();


    }


    public void searchTest() {
        final DatabaseReference searchByLocation = FirebaseDatabase.getInstance().getReference().child("group").child(groupCategory);
        GeoFire geoFire = new GeoFire(searchByLocation);
        // creates a new query around [37.7832, -122.4056] with a radius of 0.6 kilometers
        // Will be done via the users current location, and the radius they selected.
        geoQuery = geoFire.queryAtLocation(new GeoLocation(51.5561476, -0.3187798), 0.6);


        final ArrayList<String> planetList = new ArrayList<String>();


        System.out.println("Hey JUST BEFORE QUERY LISTENER");
        geoQuery.addGeoQueryDataEventListener(new GeoQueryDataEventListener() {

            GeoFire.CompletionListener abc = new GeoFire.CompletionListener() {
                @Override
                public void onComplete(String key, DatabaseError error) {
                    if (error != null) {
                        System.err.println("There was an error saving the location to GeoFire: " + error);
                    } else {
                        System.out.println("Location saved on server successfully!");
                    }
                }
            };

            // Once they've done that on the groups tree
            @Override
            public void onDataEntered(DataSnapshot dataSnapshot, GeoLocation location) {
                Group group = dataSnapshot.getValue(Group.class);
                System.out.println("Hey We IN " + String.format("The Key %s entered the search area at [%f,%f]", group.getName(), location.latitude, location.longitude));
                System.out.println("hey " + dataSnapshot.getRef());
                if ((group.getType_gender_memberLimit()).equals(groupType + "_" + groupGender + "_" + memberLimit)) {
                    // Create ArrayAdapter using the planet list.
                    listAdapter = new ArrayAdapter<String>(ResultsActivity.this, R.layout.simplerow, planetList);
                    planetList.addAll(Arrays.asList(group.getName()));
                    mainListView.setAdapter(listAdapter);
                }


            }

            @Override
            public void onDataExited(DataSnapshot dataSnapshot) {
                System.out.println("Hey Nothing to see here buddy");
            }

            @Override
            public void onDataMoved(DataSnapshot dataSnapshot, GeoLocation location) {

                System.out.println("Hey just moved within range");
            }

            @Override
            public void onDataChanged(DataSnapshot dataSnapshot, GeoLocation location) {

                System.out.println("Hey data has changed");
            }

            @Override
            public void onGeoQueryReady() {

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

                System.out.println("Hey ERROR");
            }


        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        // remove all event listeners to stop updating in the background
        this.geoQuery.removeAllListeners();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // remove all event listeners to stop updating in the background
        this.geoQuery.removeAllListeners();
    }


    @Override
    public void onFragmentInteraction(Uri uri) {

    }

}
