package sunglass.com.loco;

import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.plus.Plus;
import com.google.maps.android.ui.IconGenerator;

import java.util.HashMap;

/**
 * Created by cmccord on 4/21/15.
 */
public class Application extends android.app.Application {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private Firebase mFirebaseRef;
    private IconGenerator mIconFactory;
    private LocationManager mLocationManager;
    private LocationListener mLocationListener;
    private Location mLocation;
    private Criteria mCriteria;
    private String mProvider;
    private HashMap<String, Marker> mMarkers;
    private String mUserID;
    private Intent service = null;

    public void setUpFirebase() {
        Firebase.setAndroidContext(this);
        mFirebaseRef = new Firebase("https://loco-android.firebaseio.com/");
    }

    public void setmUserID(String m) {
        mUserID = m;
    }

    public void setUpMarkers() {
        mMarkers = new HashMap<String, Marker>();
    }

    public void setmMap(GoogleMap m){
        mMap = m;
    }

    public void setService(Intent i) {
        service = i;
    }

    public Intent getService() {
        return service;
    }


    public void updateLocation() {
        Log.v("GPS", "Firebase GPS updated?");
        if (mFirebaseRef != null) {
            if (mLocation != null) {
                mFirebaseRef.child("users").child(mUserID).child("pos").setValue(
                        mLocation.getLatitude() + "," + mLocation.getLongitude()
                );
            }
            else if (mMap != null) {
                Log.v("GPS", "GPS listener hasn't activated, used other option");

                Location l = mMap.getMyLocation();
                mFirebaseRef.child("users").child(mUserID).child("pos").setValue(
                        l.getLatitude() + "," + l.getLongitude()
                );
            }

            mFirebaseRef.child("users").child(mUserID).child("timestamp").setValue(System.currentTimeMillis());
            Log.v("GPS", "Firebase GPS updated.");
        }
    }
//
//    public void trackCircles() {
//        mFirebaseRef.child("users").child(mUserID).child("circles").addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                for (DataSnapshot d: dataSnapshot.getChildren()){
//                    String[] peeps = d.getValue().toString().split(",");
//                    for (int i = 0; i < peeps.length; i++) {
//                        Log.v("Firebase Test", mFirebaseRef.child("users").child(peeps[i]).toString());
//                        trackUser(peeps[i]);
//                    }
//                }
//            }
//            @Override
//            public void onCancelled(FirebaseError firebaseError) {
//
//            }
//        });
//    }
//
    public void trackUser(String u){
        mFirebaseRef.child("users").child(u).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot d2) {
                if (d2.getValue() != null) {
                    Log.v("Track", d2.toString());
                    String name = "";
                    String pos = "";
                    for (DataSnapshot deets : d2.getChildren()){
                        Log.v("Track", deets.toString());
                        if (deets.getKey().equals("name")){
                            name = deets.getValue().toString();
                        }
                        else if (deets.getKey().equals("pos")){
                            pos = deets.getValue().toString();
                        }
                    }
                    if (name.length() == 0){
                        name = d2.getKey();
                    }
                    String[] loc = pos.split(",");
                    LatLng l;
                    if (loc.length > 1) {
                        l = new LatLng(Double.parseDouble(loc[0]), Double.parseDouble(loc[1]));
                    }
                    else {
                        l = new LatLng(0.0, 0.0);
                    }
                    if (mMarkers.containsKey(name)){
                        mMarkers.get(name).setPosition(l);
                    }
                    else {

                        Marker new_m = mMap.addMarker(new MarkerOptions().
                                icon(BitmapDescriptorFactory.fromBitmap(mIconFactory.makeIcon(name))).
                                position(l).
                                anchor(mIconFactory.getAnchorU(), mIconFactory.getAnchorV()).
                                title(name));
                        mMarkers.put(name, new_m);

                    }

                    Log.v("Firebase Test", d2.getRef().getParent().getKey() + " moved to " + d2.getValue());
                }
//                if (mMap.getMyLocation() != null) {
//                    Location l = mMap.getMyLocation();
//                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(l.getLatitude(), l.getLongitude()), 14));
//                }
//                zoomToCoverAllMarkers();
            }
            @Override
            public void onCancelled(FirebaseError firebaseError) {
            }
        });
    }

    public boolean setUpGPS() {
        mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        mCriteria = new Criteria();
        mCriteria.setAccuracy(Criteria.ACCURACY_FINE);
        Log.v("LocationNews", mCriteria.toString());

        mProvider = mLocationManager.getBestProvider(mCriteria, true);
        Log.v("LocationNews", mProvider);

        boolean isEnabled = mLocationManager.isProviderEnabled(mProvider);
        Log.v("LocationNews", String.valueOf(isEnabled));


        if (isEnabled) {
            // Define a listener that responds to location updates
            mLocationListener = new LocationListener() {
                public void onLocationChanged(Location location) {
                    // Called when a new location is found by the network location provider.
                    Log.v("LocationNews", location.getLatitude() + " " + location.getLongitude());
                    mLocation = location;
                }

                public void onStatusChanged(String provider, int status, Bundle extras) {
                }

                public void onProviderEnabled(String provider) {
                }

                public void onProviderDisabled(String provider) {
                }
            };

            // Register the listener with the Location Manager to receive location updates
            mLocationManager.requestLocationUpdates(mProvider, 0, 1, mLocationListener);
            return true;
        }
        else
            return false;
    }

    public void setUpFactory() {
        mIconFactory = new IconGenerator(this);
        mIconFactory.setStyle(IconGenerator.STYLE_GREEN);
        mIconFactory.setRotation(90);
        mIconFactory.setContentRotation(-90);
        mIconFactory.setTextAppearance(R.style.marker);
    }
    public void trackAll() {
        if (mFirebaseRef != null)
            mFirebaseRef.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    boolean mIsNew = true;
                    for (DataSnapshot d: dataSnapshot.getChildren()){
                        Log.v("Firebase Test", d.getKey().toString());
                        String curr = d.getKey().toString();
                        if (curr.equals(mUserID))
                            mIsNew = false;
                        trackUser(curr);
                    }
//                    if (mIsNew)
//                        createNewUser();
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {

                }
            });
    }

}
