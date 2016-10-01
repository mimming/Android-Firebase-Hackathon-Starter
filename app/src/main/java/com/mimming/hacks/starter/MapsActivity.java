package com.mimming.hacks.starter;

import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
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

/**
 * An activity that displays a map, and synchronizes markers with Firebase.
 *
 * Tap the map to create a marker.
 * Tap a marker to delete it.
 */
public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    // Where the map data lives in Firebase
    public static final String MAP_PATH = "/map/";

    // A cache of map points displayed on the map
    private HashMap<String, Marker> markers = new HashMap<String, Marker>();

    public static final String TAG = MapsActivity.class.getSimpleName();
    private GoogleMap mMap;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mFirebaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mFirebaseRef = mFirebaseDatabase.getReference(MAP_PATH);

        mFirebaseRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                LatLng myLatLon = dataSnapshot.getValue(LatLngWrapper.class).toLatLng();

                // stash the key in the title, for recall later

                Marker myMarker = mMap.addMarker(new MarkerOptions()
                        .position(myLatLon).draggable(true).title(dataSnapshot.getKey()));

                // cache the marker locally
                markers.put(dataSnapshot.getKey(), myMarker);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                LatLng myLatLon = dataSnapshot.getValue(LatLngWrapper.class).toLatLng();

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
        // Listen for marker clicks
        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                // Remove map markers from Firebase when tapped
                String firebaseId = marker.getTitle();
                mFirebaseRef.child(firebaseId).removeValue();
                return true;
            }
        });

        // Listen for map clicks
        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(final LatLng latLng) {
                // Taps create new markers in Firebase
                // This works because jackson can figure out LatLng
                mFirebaseRef.push().setValue(new LatLngWrapper(latLng));
            }
        });
        // Listen for marker drags
        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {
                // not implemented
            }

            @Override
            public void onMarkerDrag(Marker marker) {
                // not implemented
            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                mFirebaseRef.child(marker.getTitle()).setValue(new LatLngWrapper(marker.getPosition()));
            }
        });



        // Zoom to device's current location
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            String provider = locationManager.getBestProvider(new Criteria(), true);
            Location myLocation = locationManager.getLastKnownLocation(provider);
            if(myLocation != null) {
                LatLng currentLatLng = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
                mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLatLng));
                mMap.moveCamera(CameraUpdateFactory.zoomTo(14));
            } else {
                Log.v(TAG, "Can't figure out current location :(");
                mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(29.9648943,-90.1095941)));
                mMap.moveCamera(CameraUpdateFactory.zoomTo(14));
            }
        }
    }
}