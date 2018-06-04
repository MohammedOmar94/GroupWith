package haitsu.groupwith.activity;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.amplitude.api.Amplitude;
import com.amplitude.api.Identify;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.joda.time.LocalDate;
import org.joda.time.Years;

import haitsu.groupwith.activity.Account.AccountSetupActivity;
import haitsu.groupwith.activity.Account.SignInActivity;
import haitsu.groupwith.other.Models.User;


/**
 * Created by moham on 13/01/2018.
 */

public class SplashActivity extends AppCompatActivity {

    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Amplitude.getInstance().initialize(this, "945da593068312c2f8521a681f457c2b").enableForegroundTracking(getApplication());
        Amplitude.getInstance().logEvent("App started");
        super.onCreate(savedInstanceState);
        MobileAds.initialize(this, "ca-app-pub-7072858762761381~4076592994");


        // Initialize Firebase Auth
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        if (mFirebaseUser == null || !hasInternetConnection()) {
            // Not signed in, launch the Sign In activity
            startActivity(new Intent(this, SignInActivity.class));
            finish();
            return;
        } else {
            checkUsersDetails();
        }
    }

    public boolean hasInternetConnection() {
        ConnectivityManager cm =
                (ConnectivityManager)this.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        return isConnected;
    }


    public void checkUsersDetails() {
        DatabaseReference usersNodeRef = FirebaseDatabase.getInstance().getReference().child("users");
        usersNodeRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot snapshot) {
                Amplitude.getInstance().setUserId(mFirebaseUser.getEmail());
                // If the user hasn't setup their account details like Username and DoB...
                if (!snapshot.hasChild((mFirebaseUser.getUid())) || !snapshot.child(mFirebaseUser.getUid()).hasChild("username")) {
                    startActivity(new Intent(SplashActivity.this, AccountSetupActivity.class));
                    finish();
                } else {
                    User user = snapshot.child(mFirebaseUser.getUid()).getValue(User.class);
                    Identify identify = new Identify()
                            .set("Name", mFirebaseUser.getDisplayName())
                            .set("Gender", user.getGender())
                            .set("Age", calculateAge(user.getAge()))
                            .set("Email", mFirebaseUser.getEmail());
                    Amplitude.getInstance().identify(identify);
                    Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public int calculateAge(String birthday) {
        String[] parts = birthday.split("/");
        int year = Integer.parseInt(parts[2]);
        int month = Integer.parseInt(parts[1]);
        int day = Integer.parseInt(parts[0]);
        LocalDate birthdate = new LocalDate(year, month, day);
        LocalDate now = new LocalDate();
        Years age = Years.yearsBetween(birthdate, now);
        return age.getYears();
    }

}