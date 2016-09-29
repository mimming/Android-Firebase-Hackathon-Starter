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

    private HashMap<String, Marker> markers = new HashMap<String, Marker>();

    public static final String TAG = "MapsActivity";
    private GoogleMap mMap;
//    Firebase firebaseRef = new Firebase("https://friendlychat-cdbf4.firebaseio.com/");
    FirebaseDatabase database;
    DatabaseReference myRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // start Firebase
//        Firebase.setAndroidContext(this);
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("/");


        myRef.addChildEventListener(new ChildEventListener() {
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

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Marker deadMarker = markers.get(dataSnapshot.getKey());
                deadMarker.remove();

                markers.remove(dataSnapshot.getKey());

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                Log.v(TAG, "moved !" + dataSnapshot.getValue());

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.v(TAG, "canceled!" + databaseError.getMessage());
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
        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                /**
                 * remove markers when clicked
                 */
//                marker.remove();
                String firebaseId = marker.getTitle();
                // Remove from Firebase
                myRef.child(firebaseId).removeValue();
                // remove from map
                return true;
            }
        });

        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(final LatLng latLng) {
                // send to Firebase
                myRef.push().setValue(new HashMap() {{
                    put("lat", latLng.latitude);
                    put("lon", latLng.longitude);
                }});
//                mMap.addMarker(
//                        new MarkerOptions().position(latLng));
            }
        });


        // Add a marker in Sydney and move the camera
        LatLng xavierMarker = new LatLng(29.9648943,-90.1090941);
//        mMap.addMarker(new MarkerOptions().position(xavierMarker).title("Xavier"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(xavierMarker, 16.0f));

        mMap.addMarker(
                new MarkerOptions().position(
                        new LatLng(29.9648943,-90.1095941))).remove();



    }
}
