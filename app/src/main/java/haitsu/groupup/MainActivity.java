package haitsu.groupup;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
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

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        GoogleApiClient.OnConnectionFailedListener, View.OnClickListener {

    private GoogleApiClient mGoogleApiClient;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference("message");
    DatabaseReference databaseRef = database.getReference();//Can access any child element with this

    private static final String TAG = "MainActivity";

    private SharedPreferences mSharedPreferences;


    private Button mSignOutButton;
    private Button mDeleteAccountButton;
    private Button mSubmitButton;



    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;



    private String mUsername;
    private String mPhotoUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Assign fields
        mSignOutButton = (Button) findViewById(R.id.sign_out);
        //mDeleteAccountButton = (Button) findViewById(R.id.delete_account);
       // mSubmitButton = (Button) findViewById(R.id.submit_group);

        // Set click listeners
        mSignOutButton.setOnClickListener(this);
       // mDeleteAccountButton.setOnClickListener(this);
       // mSubmitButton.setOnClickListener(this);

        // Initialize Firebase Auth
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API)
                .build();
        if (mFirebaseUser == null) {
            // Not signed in, launch the Sign In activity
            startActivity(new Intent(this, SignInActivity.class));
            finish();
            return;
        } else {
            mUsername = mFirebaseUser.getDisplayName();
            if (mFirebaseUser.getPhotoUrl() != null) {
                mPhotoUrl = mFirebaseUser.getPhotoUrl().toString();
            }

            System.out.println("ProvideID : " + mFirebaseUser.getProviderId() + " UserID : " + mFirebaseUser.getUid());
            writeNewUser(mFirebaseUser.getUid(), mFirebaseUser.getDisplayName(), mFirebaseUser.getEmail());

        }
    }


    private void writeNewUser(final String userId, String name, String email) {
        final User user = new User(name, email);
        databaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (!snapshot.hasChild(userId)) {//If the userid doesn't already exist
                    databaseRef.child("users").child(userId).setValue(user);//Add users
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        //Use push() for auto generated id node.
    }

/*
    private void submitNewGroup() {
        RadioButton group = (RadioButton) findViewById(R.id.football_radioButton);
        EditText text = (EditText) findViewById(R.id.editText);
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        databaseRef.child("users").child(mFirebaseUser.getUid()).child("Groups").child("Name").setValue(group.getText());//Add users
        databaseRef.child("users").child(mFirebaseUser.getUid()).child("Groups").child("Category").setValue("Sports");//Add users
        //databaseRef.child("Groups").child("").child("Name").setValue(group.getText());//Add users
        databaseRef.child("Group").child(group.getText().toString()).child("Members").child(mFirebaseUser.getUid()).setValue(true);//Add users
        //Use push() for auto generated id node.
    }
*/

    private void signOut(){
        FirebaseAuth.getInstance().signOut();//Sign out of Firebase.
        Auth.GoogleSignInApi.signOut(mGoogleApiClient);//Sign out of Google.
        startActivity(new Intent(this, SignInActivity.class));
        finish();
    }

    private void deleteAccount(){
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        databaseRef.child(mFirebaseUser.getUid()).removeValue();
        //System.out.println("ProvideID : " + mFirebaseUser.getDisplayName() + " UserID : " + mFirebaseUser.getUid());


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
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sign_out:
                signOut();
                break;
           // case R.id.delete_account:
           //     deleteAccount();
           //     break;
        //    case R.id.submit_group:
       //         submitNewGroup();
       //         break;
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
        Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show();
    }

    //Adds data to Firebase database.
    public void helloWorld(){
        myRef.setValue("Hello, World!");
    }

    ///////////////////////////////////////////////////////////////////////////

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
