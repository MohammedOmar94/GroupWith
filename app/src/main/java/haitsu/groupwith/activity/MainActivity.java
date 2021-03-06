package haitsu.groupwith.activity;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.treebo.internetavailabilitychecker.InternetAvailabilityChecker;
import com.treebo.internetavailabilitychecker.InternetConnectivityListener;

import haitsu.groupwith.R;
import haitsu.groupwith.activity.Account.*;
import haitsu.groupwith.activity.Groups.CreateGroupActivity;
import haitsu.groupwith.activity.Groups.MyGroupsActivity;
import haitsu.groupwith.activity.Search.SearchActivity;
import haitsu.groupwith.fragment.Account.NotificationsFragment;
import haitsu.groupwith.fragment.Account.SettingsFragment;
import haitsu.groupwith.fragment.Groups.CreateGroupFragment;
import haitsu.groupwith.fragment.Groups.InterestsGroupFragment;
import haitsu.groupwith.fragment.Groups.GroupsCreatedFragment;
import haitsu.groupwith.fragment.HomeFragment;
import haitsu.groupwith.other.CircleTransform;
import haitsu.groupwith.other.DBConnections;
import haitsu.groupwith.other.Models.User;

import static android.graphics.Color.WHITE;

public class MainActivity extends AppCompatActivity
        implements
        GoogleApiClient.OnConnectionFailedListener, HomeFragment.OnFragmentInteractionListener,
        InterestsGroupFragment.OnFragmentInteractionListener,
        SettingsFragment.OnFragmentInteractionListener, NotificationsFragment.OnFragmentInteractionListener,
        GroupsCreatedFragment.OnFragmentInteractionListener, CreateGroupFragment.OnFragmentInteractionListener,
        InternetConnectivityListener {

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference databaseRef = database.getReference();//Can access any child element with this

    private static final String TAG = "MainActivity";


    User userInfo;

    private DBConnections dbConnections = new DBConnections();

    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;


    public static String mUsername;
    private String mPhotoUrl;

    private DrawerLayout drawer;
    private NavigationView navigationView;
    private View header;
    private ImageView imgvw;

    // index to identify current nav menu item
    public static int navItemIndex = 0;

    // tags used to attach the fragments
    private Toolbar toolbar;
    private static final String TAG_HOME = "home";
    private static final String TAG_NOTIFICATIONS = "notifications";
    private static final String TAG_MY_GROUPS = "my groups";
    private static final String TAG_SETTINGS = "settings";
    private static final String TAG_MY_CREATE_GROUP = "create group";
    public static String CURRENT_TAG = TAG_HOME;

    // toolbar titles respected to selected nav menu item
    private String[] activityTitles;

    // flag to load home fragment when user presses back key
    private boolean shouldLoadHomeFragOnBackPress = true;
    private Handler mHandler;

    private InternetAvailabilityChecker mInternetAvailabilityChecker;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        InternetAvailabilityChecker.init(this);

        mInternetAvailabilityChecker = InternetAvailabilityChecker.getInstance();
        mInternetAvailabilityChecker.addInternetConnectivityListener(this);

        // Initialize Firebase Auth
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();


        if (FirebaseDatabase.getInstance() == null) {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        }

        // DBHandler db = new DBHandler(MainActivity.this);
        // SQLiteDatabase s = openOrCreateDatabase("MyGroups",MODE_PRIVATE,null);
        // db.onCreate(s);
        // db.dropTable("MyGroups");
        // db.addData();
        // db.displayMessage();

        mHandler = new Handler();

        // load toolbar titles from string resources
        activityTitles = getResources().getStringArray(R.array.nav_item_activity_titles);

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                MainActivity.this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(MainActivity.this);
        if (mFirebaseUser.getPhotoUrl() != null) {
            FirebaseMessaging.getInstance().subscribeToTopic(mFirebaseUser.getUid());
            mPhotoUrl = account.getPhotoUrl().toString();

            header = navigationView.getHeaderView(0);
            imgvw = (ImageView) header.findViewById(R.id.account_image);

            // initializing navigation menu
            setUpNavigationView();  // showing dot next to notifications label
//                navigationView.getMenu().getItem(1).setActionView(R.layout.menu_dot);

            if (savedInstanceState == null) {
                navItemIndex = 0;
                CURRENT_TAG = TAG_HOME;
                loadHomeFragment();
            }
        }
    }

    public void getUser() {
        DatabaseReference username = databaseRef.child("users");
        username.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot snapshot) {
                userInfo = snapshot.child(mFirebaseUser.getUid()).getValue(User.class);//ISSUE IF USER LEAVES APP ON ACCOUNT CREATION, CAN STILL USE APP USING FIREBASE.
                if (snapshot.hasChild((mFirebaseUser.getUid()))) {
                    mUsername = userInfo.getUsername();
                    // load nav menu header data
                    loadNavHeader(imgvw);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void deleteAccount() {
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        databaseRef.child(mFirebaseUser.getUid()).removeValue();
        mFirebaseUser.delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "User account deleted.");
                            System.out.println("DELETED");
                        }
                    }
                });

    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
        Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onResume() {
        super.onResume();
        //Reloads any changes to user details in the nav drawer.
        getUser();

    }


    /*************************************NAVIGATION DRAWER METHODS*************************************/

    /***
     * Load navigation menu header information
     * like background image, profile image
     * name, website, notifications action view (dot)
     */
    private void loadNavHeader(ImageView profilePicture) {
        //Loading image from url into imageView
        Glide.with(this)
                .load(mPhotoUrl)
                .thumbnail(0.8f)
                .bitmapTransform(new CircleTransform(this))
                .into(profilePicture);
        TextView accountName = (TextView) header.findViewById(R.id.account_name);
        TextView accountEmail = (TextView) header.findViewById(R.id.account_email);
        accountName.setText(mUsername);
        accountEmail.setText(mFirebaseUser.getEmail());
    }

    private void loadHomeFragment() {
        // selecting appropriate nav menu item
        selectNavMenu();

        // set toolbar title
        setToolbarTitle();

        // if user select the current navigation menu again, don't do anything
        // just close the navigation drawer
        if (getSupportFragmentManager().findFragmentByTag(CURRENT_TAG) != null) {
            drawer.closeDrawers();
            return;
        }

        // Sometimes, when fragment has huge data, screen seems hanging
        // when switching between navigation menus
        // So using runnable, the fragment is loaded with cross fade effect
        // This effect can be seen in GMail app
        Runnable mPendingRunnable = new Runnable() {
            @Override
            public void run() {
                // update the main content by replacing fragments
                Fragment fragment = getHomeFragment();
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.setCustomAnimations(android.R.anim.fade_in,
                        android.R.anim.fade_out);
                fragmentTransaction.replace(R.id.frame, fragment, CURRENT_TAG);
                fragmentTransaction.commitAllowingStateLoss();
            }
        };

        // If mPendingRunnable is not null, then add to the message queue
        if (mPendingRunnable != null) {
            mHandler.post(mPendingRunnable);
        }


        //Closing drawer on item click
        drawer.closeDrawers();

        // refresh toolbar menu
        invalidateOptionsMenu();
    }

    private Fragment getHomeFragment() {
        switch (navItemIndex) {
            case 0:
                // home
                HomeFragment homeFragment = new HomeFragment();
                return homeFragment;
            case 1:
                // notifications fragment
                NotificationsFragment notificationsFragment = new NotificationsFragment();
                return notificationsFragment;
            // case 2:
            // my groups fragment
            //   GroupsCreatedFragment myGroupsFragment = new GroupsCreatedFragment();
            //    return myGroupsFragment;
            //case 3:
            // settings fragment
            //   SettingsFragment settingsFragment = new SettingsFragment();
            // return settingsFragment;
            default:
                return new HomeFragment();
        }
    }

    private void setToolbarTitle() {
        getSupportActionBar().setTitle(activityTitles[navItemIndex]);
    }

    private void selectNavMenu() {
        navigationView.getMenu().getItem(navItemIndex).setChecked(true);
    }

    private void setUpNavigationView() {
        //Setting Navigation View Item Selected Listener to handle the item click of the navigation menu
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {

            // This method will trigger on item Click of navigation menu
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {

                //Check to see which item was being clicked and perform appropriate action
                switch (menuItem.getItemId()) {
                    //Replacing the main content with ContentFragment Which is our Inbox View;
                    case R.id.nav_home:
                        navItemIndex = 0;
                        CURRENT_TAG = TAG_HOME;
                        break;
//                    case R.id.nav_notifications:
//                        navItemIndex = 1;
//                        CURRENT_TAG = TAG_NOTIFICATIONS;
//                        break;
                    case R.id.nav_my_groups:
                        //navItemIndex = 2;
                        //CURRENT_TAG = TAG_MY_GROUPS;
                        startActivity(new Intent(MainActivity.this, MyGroupsActivity.class));
                        drawer.closeDrawers();
                        return true;
                    case R.id.nav_chats:
                        //navItemIndex = 2;
                        //CURRENT_TAG = TAG_MY_GROUPS;
                        startActivity(new Intent(MainActivity.this, ChatsActivity.class));
                        drawer.closeDrawers();
                        return true;
                    case R.id.nav_settings:
                        //navItemIndex = 3;
                        // CURRENT_TAG = TAG_SETTINGS;
                        // launch new intent instead of loading fragment
                        startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                        drawer.closeDrawers();
                        return true;
                    case R.id.nav_terms_of_service:
                        // launch new intent instead of loading fragment
                        startActivity(new Intent(MainActivity.this, TermsOfServiceActivity.class));
                        drawer.closeDrawers();
                        return true;
                    case R.id.nav_privacy_policy:
                        // launch new intent instead of loading fragment
                        startActivity(new Intent(MainActivity.this, haitsu.groupwith.activity.Account.PrivacyPolicyActivity.class));
                        drawer.closeDrawers();
                        return true;
                    default:
                        navItemIndex = 0;
                }

                //Checking if the item is in checked state or not, if not make it in checked state
                if (menuItem.isChecked()) {
                    menuItem.setChecked(false);
                } else {
                    menuItem.setChecked(true);
                }
                menuItem.setChecked(true);

                loadHomeFragment();

                return true;
            }
        });


        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.openDrawer, R.string.closeDrawer) {

            @Override
            public void onDrawerClosed(View drawerView) {
                // Code here will be triggered once the drawer closes as we dont want anything to happen so we leave this blank
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                // Code here will be triggered once the drawer open as we dont want anything to happen so we leave this blank
                super.onDrawerOpened(drawerView);
            }
        };

        //Setting the actionbarToggle to drawer layout
        drawer.setDrawerListener(actionBarDrawerToggle);

        //calling sync state is necessary or else your hamburger icon wont show up
        actionBarDrawerToggle.syncState();
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawers();
            return;
        }

        // This code loads home fragment when back key is pressed
        // when user is in other fragment than home
        if (shouldLoadHomeFragOnBackPress) {
            // checking if user is on other navigation menu
            // rather than home
            if (navItemIndex != 0) {
                navItemIndex = 0;
                CURRENT_TAG = TAG_HOME;
                loadHomeFragment();
                return;
            }
        }

        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.


        // how menu only when home fragment is selected
        if (navItemIndex == 0) {
            getMenuInflater().inflate(R.menu.main, menu);
            for (int i = 0; i < menu.size(); i++) {
                Drawable drawable = menu.getItem(i).getIcon();
                if (drawable != null) {
                    drawable.mutate();
                    drawable.setColorFilter(WHITE, PorterDuff.Mode.SRC_ATOP);
                }
            }
        }

        // when fragment is notifications, load the menu created for notifications
        if (navItemIndex == 1) {
            getMenuInflater().inflate(R.menu.notifications, menu);
        }

        if (navItemIndex == 4) {
            menu.findItem(R.menu.main).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
      /*  if (id == R.id.action_logout) {
            Toast.makeText(getApplicationContext(), "Logout user!", Toast.LENGTH_LONG).show();
            return true;
        }*/

        // user is in notifications fragment
        // and selected 'Mark all as Read'

        if (id == R.id.home) {
            onBackPressed();
            return true;
        }


        if (id == R.id.action_mark_all_read) {
            Toast.makeText(getApplicationContext(), "All notifications marked as read!", Toast.LENGTH_LONG).show();
        }

        // user is in notifications fragment
        // and selected 'Clear All'
        if (id == R.id.action_clear_notifications) {
            Toast.makeText(getApplicationContext(), "Clear all notifications!", Toast.LENGTH_LONG).show();
        }

        if (id == R.id.action_new_group) {
            CURRENT_TAG = TAG_MY_CREATE_GROUP;
            navItemIndex = 4;

            startActivity(new Intent(MainActivity.this, CreateGroupActivity.class));
            drawer.closeDrawers();
            return true;

            /*
            Fragment fragment = new CreateGroupFragment();

            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.setCustomAnimations(android.R.anim.fade_in,
                    android.R.anim.fade_out);

            fragmentTransaction.replace(R.id.frame, fragment, CURRENT_TAG);
            fragmentTransaction.commitAllowingStateLoss();
            // set toolbar title
            getSupportActionBar().setTitle(activityTitles[navItemIndex]);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);

            // if user select the current navigation menu again, don't do anything
            // just close the navigation drawer
            if (getSupportFragmentManager().findFragmentByTag(CURRENT_TAG) != null) {
                drawer.closeDrawers();
            }
            Toast.makeText(getApplicationContext(), "New group created!", Toast.LENGTH_LONG).show();
            */
        }

        if (id == R.id.action_search) {
            startActivity(new Intent(MainActivity.this, SearchActivity.class));
            drawer.closeDrawers();
            return true;
        }

  /*      if (id == R.id.action_chats) {
            startActivity(new Intent(this, ChatRoomActivity.class));
        }*/


        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    public void onInternetConnectivityChanged(boolean isConnected) {
        if (!isConnected) {
            Toast.makeText(getApplicationContext(), "Your device is not connected to the Internet.", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(getApplicationContext(), SignInActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
    }
}
