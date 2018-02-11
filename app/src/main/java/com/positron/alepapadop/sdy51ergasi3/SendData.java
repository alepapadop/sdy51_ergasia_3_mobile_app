package com.positron.alepapadop.sdy51ergasi3;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SendData extends AppCompatActivity {

    private EditText mLat;
    private EditText mLon;
    private Button mButton;
    private RatingBar mRate;
    private Button mSend;
    private FirebaseDatabase mDatabase;
    private FirebaseAuth mAuth;
    private FirebaseUser mCurrentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_data);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mLat = findViewById(R.id.lat);
        mLon = findViewById(R.id.lon);
        mButton = findViewById(R.id.get_gps_data);
        mRate = findViewById(R.id.rate);
        mSend = findViewById(R.id.send);
        mDatabase = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();



        mSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Float r = mRate.getRating();
                Log.d("Rate", r.toString());
                send_data_to_database();
            }
        });

        final LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Log.d("Location Changes", location.toString());
                mLat.setText(String.valueOf(location.getLatitude()));
                mLon.setText(String.valueOf(location.getLongitude()));
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                Log.d("Status Changed", String.valueOf(status));
            }

            @Override
            public void onProviderEnabled(String provider) {
                Log.d("Provider Enabled", provider);
            }

            @Override
            public void onProviderDisabled(String provider) {
                Log.d("Provider Disabled", provider);
            }
        };

        // Now first make a criteria with your requirements
        // this is done to save the battery life of the device
        // there are various other other criteria you can search for..
        final Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_COARSE);
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setSpeedRequired(false);
        criteria.setCostAllowed(true);
        criteria.setHorizontalAccuracy(Criteria.ACCURACY_HIGH);
        criteria.setVerticalAccuracy(Criteria.ACCURACY_HIGH);

        // Now create a location manager
        final LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // This is the Best And IMPORTANT part
        final Looper looper = null;

        // Now whenever the button is clicked fetch the location one time
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ActivityCompat.checkSelfPermission(SendData.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(SendData.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.

                    Toast.makeText(SendData.this, "Give Location Permision to the app.",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                locationManager.requestSingleUpdate(criteria, locationListener, looper);
            }
        });
    }

    protected void send_data_to_database() {

        DatabaseReference mRef;

        Long timestamp = System.currentTimeMillis() / 1000L;
        Integer traffic = mRate.getNumStars();
        String lon = mLon.getText().toString();
        String lat = mLat.getText().toString();

        mRef = mDatabase.getReference("users");

        mRef.setValue(mCurrentUser.getUid());

        TrafficData trafficData = new TrafficData(timestamp.toString(), traffic.toString(), Double.valueOf(lon), Double.valueOf(lat));
        mRef = mDatabase.getReference("data");
        mRef.child(mCurrentUser.getUid()).push().setValue(trafficData);

    }

}
