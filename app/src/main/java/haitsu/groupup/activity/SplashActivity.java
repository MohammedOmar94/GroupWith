package haitsu.groupup.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.ads.MobileAds;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import haitsu.groupup.activity.Account.AccountSetupActivity;
import haitsu.groupup.activity.Account.SignInActivity;
import haitsu.groupup.other.Models.User;


/**
 * Created by moham on 13/01/2018.
 */

public class SplashActivity extends AppCompatActivity {

    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(FirebaseDatabase.getInstance() == null) {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        }
        MobileAds.initialize(this, "ca-app-pub-7072858762761381~4076592994");


        // Initialize Firebase Auth
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        if (mFirebaseUser == null) {
            // Not signed in, launch the Sign In activity
            startActivity(new Intent(this, SignInActivity.class));
            finish();
            return;
        } else {
            checkUsersDetails();
        }
    }

    public void checkUsersDetails() {
        DatabaseReference usersNodeRef = FirebaseDatabase.getInstance().getReference().child("users");
        usersNodeRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot snapshot) {
                // If the user hasn't setup their account details like Username and DoB...
              if (!snapshot.hasChild((mFirebaseUser.getUid())) || !snapshot.child(mFirebaseUser.getUid()).hasChild("username")) {
                    startActivity(new Intent(SplashActivity.this, AccountSetupActivity.class));
                    finish();
                } else {
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
}