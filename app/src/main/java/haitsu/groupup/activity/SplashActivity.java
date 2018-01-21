package haitsu.groupup.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.ads.MobileAds;
import com.google.firebase.database.FirebaseDatabase;


/**
 * Created by moham on 13/01/2018.
 */

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        MobileAds.initialize(this, "ca-app-pub-7072858762761381~4076592994");
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}