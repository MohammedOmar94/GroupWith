package haitsu.groupup;

import android.support.annotation.NonNull;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.ActivityUnitTestCase;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import haitsu.groupup.activity.Account.SignInActivity;
import haitsu.groupup.activity.MainActivity;
import haitsu.groupup.other.DBConnections;
import haitsu.groupup.other.Models.User;

import static android.content.ContentValues.TAG;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by moham on 22/01/2018.
 */

@RunWith(AndroidJUnit4.class)
public class dbConnectionsTest extends ActivityUnitTestCase<SignInActivity> {

    private User user;
    private DBConnections dbConnections;
    private DatabaseReference databaseRef;
    private DatabaseReference userRef;
    private FirebaseUser mFirebaseUser;
    private FirebaseAuth mFirebaseAuth;
    private CountDownLatch lock;
    private CountDownLatch lock2;
    private Object databaseNode;

    public dbConnectionsTest() {
        super(SignInActivity.class);
    }

    @Rule
    public ActivityTestRule<SignInActivity> mActivityRule = new ActivityTestRule<>(
            SignInActivity.class);


    @Before
    public void setUp() throws Exception {
        lock = new CountDownLatch(1);
        lock2 = new CountDownLatch(1);
        dbConnections = new DBConnections();

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
                            userRef = databaseRef.child("users").child(mFirebaseUser.getUid());

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
//            userRef.removeValue();
            mFirebaseAuth.getCurrentUser().delete();
            mFirebaseAuth = null;
        }

    }

    @Test
    public void databaseExists() throws ExecutionException, InterruptedException {
        databaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                databaseNode = dataSnapshot.getValue();
                lock.countDown();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        lock.await(2000, TimeUnit.MILLISECONDS);
        assertNotNull(databaseNode);
        assertNotNull(databaseRef.getDatabase());
    }

    @Test
    public void writeToDatabase() throws ExecutionException, InterruptedException {
        databaseRef.child("users").child(mFirebaseUser.getUid()).child("username").setValue("test bro");
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                user = dataSnapshot.getValue(User.class);
                lock.countDown();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        lock.await(2000, TimeUnit.MILLISECONDS);
        assertEquals("test bro", user.getUsername());
    }
}
