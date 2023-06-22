package com.akashsoam.gpslocationapp;

import android.Manifest;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.List;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, View.OnClickListener, LocationListener {

    public static final String TAG = "TAG";
    private static final int REQUEST_CODE = 1000;
    private GoogleApiClient googleApiClient;
//    private Location location;
//    private TextView txtLocation;

    EditText edtAddress, edtMilesPerHour, edtMetresPerMile;
    TextView txtDistance, txtTime;
    Button btnGetTheData;

    private String destinationLocationAddress = "";

    private TaxiManager taxiManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        edtAddress = findViewById(R.id.edtAddress);
        edtMetresPerMile = findViewById(R.id.edtMetresPerMile);
        edtMilesPerHour = findViewById(R.id.edtMilesPerHour);

        txtDistance = findViewById(R.id.txtDistanceValue);
        txtTime = findViewById(R.id.txtTime);
        btnGetTheData = findViewById(R.id.btnGetTheData);

        taxiManager = new TaxiManager();

        btnGetTheData.setOnClickListener(this);

//        txtLocation = (TextView) findViewById(R.id.txtLocation);
        googleApiClient = new GoogleApiClient.Builder(MainActivity.this).addConnectionCallbacks(MainActivity.this).addOnConnectionFailedListener(MainActivity.this).addApi(LocationServices.API).build();
    }

    @Override
    public void onConnected(@androidx.annotation.Nullable Bundle bundle) {
        Log.d(TAG, "onConnected: right");
//        showTheUserLocation();
        FusedLocationProviderApi fusedLocationProviderApi = LocationServices.FusedLocationApi;
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        locationRequest.setSmallestDisplacement(5);

        if (googleApiClient.isConnected()) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 101);
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            fusedLocationProviderApi.requestLocationUpdates(googleApiClient, locationRequest, MainActivity.this);
        } else {
            googleApiClient.connect();
        }

//        if (googleApiClient.isConnected()) {
//            fusedLocationProviderApi.requestLocationUpdates(googleApiClient, locationRequest, MainActivity.this);
//        }else{
//            googleApiClient.connect();
//        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "onConnectionSuspended: wrong");
    }

    @Override
    public void onConnectionFailed(@androidx.annotation.NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed: wrong");
        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(MainActivity.this, REQUEST_CODE);
            } catch (IntentSender.SendIntentException e) {
                Log.d(TAG, e.getStackTrace().toString());
                throw new RuntimeException(e);
            }
        } else {
            Toast.makeText(this, "Google play services is not working, EXIT!", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @androidx.annotation.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            googleApiClient.connect();

        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (googleApiClient != null) {
            googleApiClient.connect();
        }
    }

    @Override
    public void onClick(View v) {
        String addressValue = edtAddress.getText().toString();
        boolean isGeocoding = true;
        if (!addressValue.equals(destinationLocationAddress)) {
//            addressValue = destinationLocationAddress;
            destinationLocationAddress = addressValue;
            Geocoder geocoder = new Geocoder(getApplicationContext());
            try {
                List<Address> myAddresses = geocoder.getFromLocationName(destinationLocationAddress, 4);
                if (myAddresses != null) {
                    double latitude = myAddresses.get(0).getLatitude();
                    double longitude = myAddresses.get(0).getLongitude();

                    Location locationAddress = new Location("MyDestination");
                    locationAddress.setLatitude(latitude);
                    locationAddress.setLongitude(longitude);
                    taxiManager.setDestinationLocation(locationAddress);

                }
            } catch (Exception e) {
                isGeocoding = false;
                e.printStackTrace();
            }
        }
        int permissionCheck = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION);
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            FusedLocationProviderApi fusedLocationProviderApi = LocationServices.FusedLocationApi;
            Location userCurrLocation = fusedLocationProviderApi.getLastLocation(googleApiClient);
            if (userCurrLocation != null && isGeocoding) {
                if (!edtMilesPerHour.getText().toString().equals("") && !edtMetresPerMile.getText().toString().equals("")) {
                    txtDistance.setText(taxiManager.returnTheMilesBetweenCurrLocationAndDestLocation(userCurrLocation, Integer.parseInt(edtMetresPerMile.getText().toString())));
                    txtTime.setText(taxiManager.returnTheTimeLeftToGetDestinationLocation(userCurrLocation, Float.parseFloat(edtMilesPerHour.getText().toString()), Integer.parseInt(edtMetresPerMile.getText().toString())));
                } else {
                    edtMilesPerHour.setText(String.valueOf(35));
                    edtMetresPerMile.setText(String.valueOf(1609));
                    txtDistance.setText(taxiManager.returnTheMilesBetweenCurrLocationAndDestLocation(userCurrLocation, Integer.parseInt(edtMetresPerMile.getText().toString())));
                    txtTime.setText(taxiManager.returnTheTimeLeftToGetDestinationLocation(userCurrLocation, Float.parseFloat(edtMilesPerHour.getText().toString()), Integer.parseInt(edtMetresPerMile.getText().toString())));
                }

            }
        } else {
            txtDistance.setText("You have not allowed this app to access your location services");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 10);
        }
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        onClick(null);

    }

    @Override
    protected void onPause() {
        super.onPause();
        FusedLocationProviderApi fusedLocationProviderApi = LocationServices.FusedLocationApi;
        fusedLocationProviderApi.removeLocationUpdates(googleApiClient, MainActivity.this);

    }
    //Custom methods
//    private void showTheUserLocation() {
//        int permissionCheck = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION);
//        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
//            FusedLocationProviderApi fusedLocationProviderApi = LocationServices.FusedLocationApi;
//            location = fusedLocationProviderApi.getLastLocation(googleApiClient);
//            if (location != null) {
//                double latitude = location.getLatitude();
//                double longitude = location.getLongitude();
//                txtLocation.setText(latitude + ", " + longitude);
//            } else {
//                txtLocation.setText("The app is not able to access the location now, Try again later");
//            }
//
//        } else {
//            txtLocation.setText("You have not allowed this app to access the location");
//            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
//        }
//    }
}