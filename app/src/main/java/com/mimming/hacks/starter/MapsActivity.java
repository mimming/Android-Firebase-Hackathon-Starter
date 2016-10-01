package com.mimming.hacks.starter;

import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    // Where the map data lives in Firebase
    public static final String MAP_PATH = "/map/";

    // A cache of map points displayed on the map
    private HashMap<String, Marker> markers = new HashMap<String, Marker>();

    public static final String TAG = MapsActivity.class.getSimpleName();
    private GoogleMap mMap;

    FirebaseDatabase database;
    DatabaseReference firebaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        database = FirebaseDatabase.getInstance();
        firebaseRef = database.getReference(MAP_PATH);

        firebaseRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                HashMap markerMap = (HashMap)dataSnapshot.getValue();

                LatLng myLatLon = new LatLng(
                        (Double)markerMap.get("lat"),
                        (Double)markerMap.get("lon"));

                // stash the key in the title
                Marker myMarker = mMap.addMarker(new MarkerOptions().position(myLatLon).title(dataSnapshot.getKey()));

                // cache the marker locally
                markers.put(dataSnapshot.getKey(), myMarker);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                HashMap markerMap = (HashMap)dataSnapshot.getValue();

                LatLng myLatLon = new LatLng(
                        (Double)markerMap.get("lat"),
                        (Double)markerMap.get("lon"));

                // Move markers on the map if changed on Firebase
                Marker changedMarker = markers.get(dataSnapshot.getKey());
                changedMarker.setPosition(myLatLon);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                // When markers are removed from
                Marker deadMarker = markers.get(dataSnapshot.getKey());
                deadMarker.remove();

                markers.remove(dataSnapshot.getKey());
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                // This won't happen to our simple list, but log just in case
                Log.v(TAG, "moved !" + dataSnapshot.getValue());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Ignore cancelations (but log just in case)
                Log.v(TAG, "canceled!" + databaseError.getMessage());
            }
        });
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                // Remove map markers from Firebase when tapped
                String firebaseId = marker.getTitle();
                firebaseRef.child(firebaseId).removeValue();
                return true;
            }
        });

        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(final LatLng latLng) {
                // Taps create new markers in Firebase
                firebaseRef.push().setValue(new HashMap() {{
                    put("lat", latLng.latitude);
                    put("lon", latLng.longitude);
                }});
            }
        });


        // Zoom to device's current location
        LatLng xavierMarker = new LatLng(29.9648943,-90.1090941);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(xavierMarker, 16.0f));
        mMap.addMarker(
                new MarkerOptions().position(
                        new LatLng(29.9648943,-90.1095941))).remove();
    }
}