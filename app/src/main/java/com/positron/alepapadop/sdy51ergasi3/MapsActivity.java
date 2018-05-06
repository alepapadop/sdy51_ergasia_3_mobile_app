package com.positron.alepapadop.sdy51ergasi3;

import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;


import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener
{

    private GoogleMap mMap = null;
    private FirebaseDatabase mDatabase;
    private static final String TAG = "SDY51";
    private ArrayList<DataSnapshot> mTrafficSnapshotUser = new ArrayList<DataSnapshot>();
    private Boolean mMarkersSet = false;
    private AlertDialog mDialog = null;
    MarkerTagData mMarkerTagDataLast = new MarkerTagData();
    private Boolean mMarkerLast = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mDatabase = FirebaseDatabase.getInstance();

        DatabaseReference ref = mDatabase.getReference("data");

        //ref.addListenerForSingleValueEvent(new ValueEventListener() {
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //TrafficData data = dataSnapshot.getValue(TrafficData.class);

                //Log.d(TAG, "Database data");
                //Log.d(TAG, dataSnapshot.toString());
                //Log.d(TAG, "Children: " + dataSnapshot.getChildrenCount());

                mTrafficSnapshotUser.clear();
                mMap.clear();
                mMarkersSet = false;

                for (DataSnapshot trafficSnapshotUser : dataSnapshot.getChildren()) {

                    //Log.d(TAG, "Children2: " + trafficSnapshot.getChildrenCount());

                    if (trafficSnapshotUser != null) {
                        mTrafficSnapshotUser.add(trafficSnapshotUser);
                    }

                }

                addMarkersToMap();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.d(TAG, "loadPost:onCancelled", databaseError.toException());

            }

        });

    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        int count = 0;

        //Log.d(TAG, "Map is ready");

        addMarkersToMap();

    }

    public void addMarkersToMap() {
        if (!mMarkersSet && mMap != null && !mTrafficSnapshotUser.isEmpty()) {

            //Log.d(TAG, "AddMarkers");

            int count = 0;
            mMarkersSet = true;

            for (DataSnapshot trafficSnapshotUser : mTrafficSnapshotUser) {

                //Log.d(TAG, trafficSnapshotUser.toString());

                for (DataSnapshot trafficSnapShotMessage : trafficSnapshotUser.getChildren()) {

                    MarkerTagData markerTagData = new MarkerTagData();
                    markerTagData.user_id = trafficSnapshotUser.getKey();
                    Log.d(TAG, trafficSnapshotUser.getKey());

                    //Log.d(TAG, "traffic child: " + trafficSnapShotMessage.child("traffic").toString());

                    markerTagData.message_id = trafficSnapShotMessage.getKey();
                    Log.d(TAG, trafficSnapShotMessage.getKey());

                    TrafficData trafficData = new TrafficData();
                    trafficData.traffic = trafficSnapShotMessage.child("traffic").getValue().toString();
                    trafficData.latitude = trafficSnapShotMessage.child("latitude").getValue(Double.class);
                    trafficData.longitude = trafficSnapShotMessage.child("longitude").getValue(Double.class);
                    trafficData.timestamp = trafficSnapShotMessage.child("timestamp").getValue().toString();
                    trafficData.pos_feedback = trafficSnapShotMessage.child("pos_feedback").getValue(Integer.class);
                    trafficData.neg_feedback = trafficSnapShotMessage.child("neg_feedback").getValue(Integer.class);

                    markerTagData.trafficData = trafficData;

                    Date date = new java.util.Date(Long.valueOf(trafficData.timestamp).longValue() * 1000);
                    // the format of your date
                    SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    // give a timezone reference for formatting (see comment at the bottom)
                    //sdf.setTimeZone(java.util.TimeZone.getTimeZone("GMT+3"));
                    String formattedDate = sdf.format(date);

                    trafficData.timestamp = formattedDate;


                    String title = "Traffic: " + trafficData.traffic;

                    String msg = "Traffic: " + trafficData.traffic + "\nTime: " + trafficData.timestamp +
                            "\nPositive: " + trafficData.pos_feedback + "\nNegative: " + trafficData.neg_feedback;

                    LatLng mark = new LatLng(trafficData.latitude, trafficData.longitude);

                    //mMap.addMarker(new MarkerOptions().position(mark).title(title).snippet(msg)).setTag(markerTagData);
                    Marker marker = mMap.addMarker(new MarkerOptions().position(mark).title(title).snippet(msg));
                    marker.setTag(markerTagData);

                    if (mMarkerLast) {
                        if (markerTagData.message_id.equals(mMarkerTagDataLast.message_id) &&
                            markerTagData.user_id.equals(mMarkerTagDataLast.user_id)) {
                            marker.showInfoWindow();
                            mMarkerLast = false;
                        }
                    }

                    mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

                        @Override
                        public View getInfoWindow(Marker arg0) {
                            return null;
                        }

                        @Override
                        public View getInfoContents(Marker marker) {

                            LinearLayout info = new LinearLayout(MapsActivity.this);
                            info.setOrientation(LinearLayout.VERTICAL);

                            TextView title = new TextView(MapsActivity.this);
                            title.setTextColor(Color.BLACK);
                            title.setGravity(Gravity.CENTER);
                            title.setTypeface(null, Typeface.BOLD);
                            title.setText(marker.getTitle());

                            TextView snippet = new TextView(MapsActivity.this);
                            snippet.setTextColor(Color.GRAY);
                            snippet.setText(marker.getSnippet());

                            info.addView(title);
                            info.addView(snippet);

                            return info;
                        }
                    });

                    mMap.setOnInfoWindowClickListener(this);
                    if (count == 0) {
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mark, 10));
                    }
                    ++count;
                }
            }
        }

        mMarkerLast = false;
    }

    @Override
    public void onInfoWindowClick(final Marker marker) {
        Log.d(TAG, "Click marker");

        LinearLayout button_layout = new LinearLayout(MapsActivity.this);
        button_layout.setOrientation(LinearLayout.HORIZONTAL);

        ImageButton pos = new ImageButton(MapsActivity.this);
        pos.setImageResource(R.drawable.ic_thumb_up_black_24px);
        pos.setClickable(true);
        pos.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)
            {
                Log.d(TAG, "Up pressed");
                final MarkerTagData markerTagData = (MarkerTagData) marker.getTag();

                Integer pos = markerTagData.trafficData.pos_feedback + 1;

                Log.d(TAG, markerTagData.message_id);
                Log.d(TAG, markerTagData.trafficData.timestamp);

                try {
                    mDatabase.getReference().child("data").child(markerTagData.user_id).child(markerTagData.message_id).child("pos_feedback").setValue(pos);
                    if (mDialog != null) {
                        mDialog.dismiss();
                        mDialog = null;
                        mMarkerTagDataLast.user_id = markerTagData.user_id;
                        mMarkerTagDataLast.message_id = markerTagData.message_id;
                        mMarkerLast = true;

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        ImageButton neg = new ImageButton(MapsActivity.this);
        neg.setImageResource(R.drawable.ic_thumb_down_black_24px);
        neg.setClickable(true);
        neg.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)
            {
                Log.d(TAG, "Down pressed");
                final MarkerTagData markerTagData = (MarkerTagData) marker.getTag();

                Integer neg = markerTagData.trafficData.neg_feedback + 1;

                Log.d(TAG, markerTagData.message_id);
                Log.d(TAG, markerTagData.trafficData.timestamp);

                try {
                    mDatabase.getReference().child("data").child(markerTagData.user_id).child(markerTagData.message_id).child("neg_feedback").setValue(neg);
                    if (mDialog != null) {
                        mDialog.dismiss();
                        mDialog = null;
                        mMarkerTagDataLast.user_id = markerTagData.user_id;
                        mMarkerTagDataLast.message_id = markerTagData.message_id;
                        mMarkerLast = true;
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        button_layout.addView(pos);
        button_layout.addView(neg);

        AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
        builder.setTitle(marker.getTitle());
        builder.setMessage(marker.getSnippet());
        builder.setView(button_layout);
        builder.create();
        mDialog = builder.show();

    }
}
