package haitsu.groupwith;

import android.location.Location;
import android.support.annotation.NonNull;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.ActivityUnitTestCase;
import android.util.Log;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryDataEventListener;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import haitsu.groupwith.activity.Account.SignInActivity;
import haitsu.groupwith.other.DBConnections;
import haitsu.groupwith.other.LocationManager;
import haitsu.groupwith.other.Models.User;

import static android.content.ContentValues.TAG;

/**
 * Created by moham on 22/01/2018.
 */

@RunWith(AndroidJUnit4.class)
public class locationManagerTest extends ActivityUnitTestCase<SignInActivity> {

    private User user;
    private DBConnections dbConnections;
    private LocationManager locationManager;
    private DatabaseReference databaseRef;
    private DatabaseReference userRef;
    private DatabaseReference groupRef;
    private FirebaseUser mFirebaseUser;
    private FirebaseAuth mFirebaseAuth;
    private int groupsFound;
    private CountDownLatch lock;
    private CountDownLatch lock2;
    private CountDownLatch locationLock;
    private Object databaseNode;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationCallback mLocationCallback;

    public locationManagerTest() {
        super(SignInActivity.class);
    }

    @Rule
    public ActivityTestRule<SignInActivity> mActivityRule = new ActivityTestRule<>(
            SignInActivity.class);


    @Before
    public void setUp() throws Exception {
        lock = new CountDownLatch(1);
        lock2 = new CountDownLatch(1);
        locationLock = new CountDownLatch(1);
        dbConnections = new DBConnections();
        locationManager = new LocationManager();



        // Initialize Firebase Auth
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseAuth.createUserWithEmailAndPassword("test_boyz@fakemailz.com", "notASafePasswordAyyLmao")
                .addOnCompleteListener(mActivityRule.getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            mFirebaseUser = mFirebaseAuth.getCurrentUser();
                            databaseRef = FirebaseDatabase.getInstance().getReference();
                            databaseRef.child("users").child(mFirebaseUser.getUid()).child("username").setValue("test bro");
                            userRef = databaseRef.child("users").child(mFirebaseUser.getUid());
                            groupRef = databaseRef.child("group").child("Test");

                            lock2.countDown();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                        }
                    }
                });
        lock2.await(2000, TimeUnit.MILLISECONDS);
    }

    @After
    public void tearDown() throws Exception {
        super.tearDown();
        if (mFirebaseAuth != null) {
            // Delete user instance.
            userRef.removeValue();
            groupRef.removeValue();
            mFirebaseAuth.getCurrentUser().delete();
            mFirebaseAuth = null;
        }

    }



    // Test case for users from London, searching for groups within a 15 mile radius.
    @Test
    public void twoNearbyGroups() throws ExecutionException, InterruptedException {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(mActivityRule.getActivity());
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {

                    // London, Stratford
                    user = new User("test boy", "male", "fakeboys@gmail.com", "21", "London", "UK", null, 51.550174,-0.003371);
                    dbConnections.submitNewGroup("Test","Events", "","", "","5",user);

                    // London, Harrow
                    user = new User("test boy", "male", "fakeboys@gmail.com", "21", "London", "UK", null, 51.580559,-0.341995);
                    dbConnections.submitNewGroup("Test","Events", "","", "","5",user);

                    // Scotland
                    user = new User("test boy", "male", "fakeboys@gmail.com", "21", "Scot-city", "UK", null, 56.490671,-4.202646);
                    dbConnections.submitNewGroup("Test","Events", "","", "","5",user);

                    // Japan, Tokyo
                    user = new User("test boy", "male", "fakeboys@gmail.com", "21", "Tokyo", "UK", null, 35.689487,-139.691706);
                    dbConnections.submitNewGroup("Test","Events", "","", "","5",user);

                    // Chigwell (just outside of 15 mile radius)
                    user = new User("test boy", "male", "fakeboys@gmail.com", "21", "London", "UK", null, 51.626281,0.080647);
                    dbConnections.submitNewGroup("Test","Events", "","", "","5",user);

                    GeoFire geoFire = new GeoFire(groupRef);
                    // creates a new query around [37.7832, -122.4056] with a radius of 0.6 kilometers
                    // Will be done via the users current location, and the radius they selected.
                    GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(location.getLatitude(), location.getLongitude()), 24);


                    geoQuery.addGeoQueryDataEventListener(new GeoQueryDataEventListener() {

                        GeoFire.CompletionListener abc = new GeoFire.CompletionListener() {
                            @Override
                            public void onComplete(String key, DatabaseError error) {
                                if (error != null) {
                                    locationLock.countDown();
                                    System.err.println("There was an error saving the location to GeoFire: " + error);
                                } else {
                                    System.out.println("Location saved on server successfully!");
                                }
                            }
                        };

                        // Once they've done that on the groups tree
                        @Override
                        public void onDataEntered(DataSnapshot dataSnapshot, GeoLocation location) {
                            groupsFound++;
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
            }
        };

        locationManager.setmLocationCallback(mLocationCallback);
        locationManager.setmFusedLocationClient(mFusedLocationClient);
        locationManager.initialiseLocationRequest(mActivityRule.getActivity());



        locationLock.await(2000, TimeUnit.MILLISECONDS);
        assertEquals(2, groupsFound);
    }
}
