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
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseListAdapter;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

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

    private String selectedGroupID;
    private String selectedGroupName;

    private String groupCategory;
    private String groupGender;
    private String groupType;




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

        mListView = (ListView) findViewById(R.id.listview);
        String title = "Results";
        getSupportActionBar().setTitle(title);

        final Query searchByType = FirebaseDatabase.getInstance().getReference().child("group").child(groupCategory).orderByChild("type_gender").equalTo(groupType + "_" + groupGender);
        searchByType.
                addListenerForSingleValueEvent(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.getValue() != null) {
                                    usersAdapter = new FirebaseListAdapter<Group>(ResultsActivity.this, Group.class, android.R.layout.two_line_list_item, searchByType) {
                                        protected void populateView(View view, Group groupInfo, int position) {
                                            System.out.println("Group name is " + groupInfo.getName());
                                            ((TextView) view.findViewById(android.R.id.text1)).setText(groupCategory);
                                            ((TextView) view.findViewById(android.R.id.text2)).setText(groupInfo.getName());
                                        }

                                        @Override
                                        public View getView(int position, View convertView, ViewGroup parent) {
                                            // Get the Item from ListView
                                            View view = super.getView(position, convertView, parent);

                                            // Initialize a TextView for ListView each Item
                                            TextView tv = (TextView) view.findViewById(android.R.id.text1);
                                            TextView tv2 = (TextView) view.findViewById(android.R.id.text2);

                                            // Set the text color of TextView (ListView Item)
                                            tv.setTextColor(Color.BLACK);
                                            tv2.setTextColor(Color.BLACK);

                                            // Generate ListView Item using TextView
                                            return view;
                                        }


                                    };

                                    mListView.setAdapter(usersAdapter);
                                } else {
                                    AlertDialog.Builder builder;
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                        builder = new AlertDialog.Builder(ResultsActivity.this, R.style.MyAlertDialogStyle);
                                    } else {
                                        builder = new AlertDialog.Builder(ResultsActivity.this);
                                    }
                                    builder.setTitle("Oops! No matches found")
                                            .setMessage("Maybe you should be the first to create a group of this kind!")
                                            .setPositiveButton("OK", null)
                                            .show();
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        }
                );

        searchByType.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                    public void onItemClick(AdapterView<?> adapter, View v, int position, long id) {
                        mListView.setFocusable(true);//HACKS
                        String key = usersAdapter.getRef(position).getKey();//Gets key of listview item
                        Group group = ((Group) mListView.getItemAtPosition(position));
                        selectedGroupID = key;
                        selectedGroupName = group.getName();
                        Intent intent = new Intent(ResultsActivity.this, GroupInfoActivity.class);
                        Bundle extras = new Bundle();
                        //extras.putString("GROUP_ID", selectedGroup);
                        extras.putString("GROUP_ID", selectedGroupID);
                        extras.putString("GROUP_CATEGORY", groupCategory);
                        intent.putExtras(extras);
                        startActivity(intent);
                        System.out.println("Group name is " + group.getName() + " ID is " + groupCategory);
                    }


                });
            }


            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

        });

    }




    @Override
    public void onFragmentInteraction(Uri uri) {

    }

}
