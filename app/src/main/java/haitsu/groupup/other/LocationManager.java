package haitsu.groupup.other;

import android.Manifest;
import android.app.Activity;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Created by moham on 27/01/2018.
 */

public class LocationManager implements GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {

    private Activity currentActivity;
    private LocationSettingsRequest.Builder builder;

    private GoogleApiClient mGoogleApiClient;
    private final static int PLAY_SERVICES_REQUEST = 1000;
    private final static int REQUEST_CHECK_SETTINGS = 2000;
    private Location mLastLocation;
    private LocationRequest mLocationRequest;
    private FusedLocationProviderClient mFusedLocationClient;


    private LocationCallback mLocationCallback;
    private boolean alreadyRequested;
    private LocationManager currentReference;

    private double latitude;
    private double longitude;
    private double kilometers;

    private String city;
    private String country;

    public LocationManager(){
        if(currentReference == null) {
            currentReference = this;
        }
    }

    public GoogleApiClient connectToGoogleApiClient(Activity activity){
        mGoogleApiClient = new GoogleApiClient.Builder(activity)
                .enableAutoManage((FragmentActivity) activity /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
        setmGoogleApiClient(mGoogleApiClient);
//        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(activity);
//        initialiseLocationRequest(activity);
        return mGoogleApiClient;
    }


    public LocationRequest initialiseLocationRequest(Activity activity){

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);

        currentActivity = activity;
        checkLocationStatus(builder);

        return mLocationRequest;
    }

    public void checkLocationStatus(LocationSettingsRequest.Builder settingsBuilder) {
        SettingsClient client = LocationServices.getSettingsClient(currentActivity);
        final Task<LocationSettingsResponse> task = client.checkLocationSettings(settingsBuilder.build());

        task.addOnSuccessListener(currentActivity, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                // All location settings are satisfied. The client can initialize
                // location requests here.
                // ...
                if (ActivityCompat.checkSelfPermission(currentActivity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(currentActivity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    System.out.println("Hey no permission");
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    ActivityCompat.requestPermissions(currentActivity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 200);
                } else {
                    lastLocation();
                }

            }
        });

        task.addOnFailureListener(currentActivity, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                int statusCode = ((ApiException) e).getStatusCode();
                switch (statusCode) {
                    case CommonStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied, but this can be fixed
                        // by showing the user a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            ResolvableApiException resolvable = (ResolvableApiException) e;
                            resolvable.startResolutionForResult(currentActivity,
                                    REQUEST_CHECK_SETTINGS);
                            lastLocation();
                        } catch (IntentSender.SendIntentException sendEx) {
                            // Ignore the error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way
                        // to fix the settings so we won't show the dialog.
                        break;
                }
            }
        });
    }

    public void requestLocation() {
        startLocationUpdates();
//        System.out.println("Hey we're in request");
//        updateLocation();
    }

    public Address getAddress(double latitude, double longitude) {
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(currentActivity, Locale.getDefault());

        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
            return addresses.get(0);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;

    }

    public void getAddress() {
        Address locationAddress = getAddress(latitude, longitude);

        if (locationAddress != null) {
            city = locationAddress.getLocality();
            country = locationAddress.getCountryName();
            DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference();
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            databaseRef.child("users").child(userId).child("city").setValue(city);
            databaseRef.child("users").child(userId).child("country").setValue(country);
            databaseRef.child("users").child(userId).child("latitude").setValue(latitude);
            databaseRef.child("users").child(userId).child("longitude").setValue(longitude);
        }
    }

    public void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(currentActivity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(currentActivity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            ActivityCompat.requestPermissions(currentActivity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 200);
        } else {
            mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                    mLocationCallback,
                    null /* Looper */);
        }
    }

    public void stopLocationUpdates() {
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
    }

    public void storeLocationData(Location location){
        // Update UI with location data
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        getAddress();
        stopLocationUpdates();
        System.out.println("Hey " + city + " " + country + " " + kilometers);
    }

    public void lastLocation() {
        if (ActivityCompat.checkSelfPermission(currentActivity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(currentActivity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            ActivityCompat.requestPermissions(currentActivity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 200);
        } else {
            mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(currentActivity, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {
                                System.out.println("Hey, we have the Last location");
                                mLastLocation = location;
                                // updateLocation();
                                requestLocation();
                            } else {
                                // Pretty busted, added in but not tested properly before. Need to request updates
                                System.out.println("Hey, we don't so we have to Request location");
                                requestLocation();
                            }
                            // Logic to handle location object
                        }
                    });
        }
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public FusedLocationProviderClient getmFusedLocationClient() {
        return mFusedLocationClient;
    }

    public void setmFusedLocationClient(FusedLocationProviderClient mFusedLocationClient) {
        this.mFusedLocationClient = mFusedLocationClient;
    }

    public LocationCallback getmLocationCallback() {
        return mLocationCallback;
    }

    public void setmLocationCallback(LocationCallback mLocationCallback) {
        this.mLocationCallback = mLocationCallback;
    }

    public LocationRequest getmLocationRequest() {
        return mLocationRequest;
    }

    public void setmLocationRequest(LocationRequest mLocationRequest) {
        this.mLocationRequest = mLocationRequest;
    }

    public boolean isAlreadyRequested() {
        return alreadyRequested;
    }

    public void setAlreadyRequested(boolean alreadyRequested) {
        this.alreadyRequested = alreadyRequested;
    }

    public LocationManager getCurrentReference() {
        return currentReference;
    }

    public void setCurrentReference(LocationManager currentReference) {
        this.currentReference = currentReference;
    }

    public GoogleApiClient getmGoogleApiClient() {
        return mGoogleApiClient;
    }

    public void setmGoogleApiClient(GoogleApiClient mGoogleApiClient) {
        this.mGoogleApiClient = mGoogleApiClient;
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }
}
