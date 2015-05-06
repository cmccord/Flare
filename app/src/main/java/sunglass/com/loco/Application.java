package sunglass.com.loco;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.InputType;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.firebase.client.AuthData;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.firebase.simplelogin.SimpleLogin;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.plus.Plus;
import com.google.maps.android.ui.IconGenerator;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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
    private boolean sharingStatus;
    private Activity inMapsActivity = null;
    private static Application inst;
    private SimpleLogin authClient;
    private AuthData authData;
    private HashMap<String,ValueEventListener> listeners = new HashMap<>();
    private String circleSelected = "All Friends";
    private ArrayList<String> friendsInCircle;
    private boolean justOpened = true;
    private HashMap<String, Marker> pinMarkers;

    @Override
    public void onCreate() {
        super.onCreate();
        inst = this;
        setUpFirebase();
    }

    public void notJustOpened() { justOpened = false; }

    public boolean wasJustOpened() {
        return justOpened;
    }

    public HashMap<String,Marker> getMarkers() {
        return mMarkers;
    }

    public static Application instance() {
        return inst;
    }

    public void setUpFirebase() {
        Firebase.setAndroidContext(this);
        mFirebaseRef = new Firebase("https://loco-android.firebaseio.com/");
    }

    public Location getmLocation() {
        return mLocation;
    }

    public String getCircleSelected() {
        return circleSelected;
    }

    public void setCircleSelected(String s) {
        if(!circleSelected.equals(s)) {
            try {
                circleSelected = s;
                friendsInCircle = new ArrayList<>();
                if (circleSelected.equals("All Friends")) {
                    mFirebaseRef.child("users").child(mUserID).child("friends").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for (DataSnapshot d : dataSnapshot.getChildren())
                                friendsInCircle.add(d.getKey());
                            updateMarkersToCircle();
                        }

                        @Override
                        public void onCancelled(FirebaseError firebaseError) {

                        }
                    });
                } else {
                    mFirebaseRef.child("users").child(mUserID).child("circles").child(circleSelected).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for (DataSnapshot d : dataSnapshot.getChildren())
                                friendsInCircle.add(d.getKey());
                            updateMarkersToCircle();
                        }

                        @Override
                        public void onCancelled(FirebaseError firebaseError) {

                        }
                    });
                }
                Log.v("friendsInCircle", friendsInCircle.toString());
            } catch(Exception e) {Log.v("Error updating circle", e.toString());}
        }
    }

    private void updateMarkersToCircle() {
        for(String s : mMarkers.keySet()) {
            if(!friendsInCircle.contains((s)) && !mUserID.equals(s)) {
                cancelTracking(s);
            }
        }
        for(String s : friendsInCircle) {
            if(!mMarkers.containsKey(s)) {
                trackUser(s);
            }
        }
    }

    public void refreshCircleSelected() {
        try {
            friendsInCircle = new ArrayList<>();
            if (circleSelected.equals("All Friends")) {
                mFirebaseRef.child("users").child(mUserID).child("friends").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot d : dataSnapshot.getChildren())
                            friendsInCircle.add(d.getKey());
                        updateMarkersToCircle();
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {

                    }
                });
            } else {
                mFirebaseRef.child("users").child(mUserID).child("circles").child(circleSelected).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot d : dataSnapshot.getChildren())
                            friendsInCircle.add(d.getKey());
                        updateMarkersToCircle();
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {

                    }
                });
            }
            Log.v("friendsInCircle", friendsInCircle.toString());
        } catch(Exception e) {Log.v("Error updating circle", e.toString());}
    }

    public Firebase getFirebaseRef() {
        return mFirebaseRef;
    }

    public void setSimpleLogin(SimpleLogin simpleLogin) {
        authClient = simpleLogin;
    }

    public SimpleLogin getSimpleLoginRef() {
        return authClient;
    }

    public void setmUserID(String m) {
        mUserID = m;
    }

    public void setUpMarkers() {
        mMarkers = new HashMap<String, Marker>();
        pinMarkers = new HashMap<String, Marker>();
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

    public HashMap<String, ValueEventListener> getListeners() {
        return listeners;
    }

    public void setSharingStatus(boolean b, Context context) {
        sharingStatus = b;
        //if(getInMapsActivity()) {
            Log.v("cancelling share", "inside setSharingStatus");
            //Activity activity = (Activity) context;
            setShareButton(context);
        //}
    }

    // sets color of share button in maps activity
    public void setShareButton(Context context) {
        Activity activity = MapsActivity.instance();
        try {
            //Activity activity = (Activity) context;
            Button mainButton = (Button) activity.findViewById(R.id.topButton);
            Button bottomButton = (Button) activity.findViewById(R.id.shareButton);
            if (mainButton != null) {
                Log.v("Sharing Status", "" + getSharingStatus());
                if (getSharingStatus()) {
                    mainButton.setBackgroundResource(R.drawable.button_green);
                    bottomButton.setBackgroundResource(R.drawable.button_green);
                    bottomButton.setText("Cancel");
                }
                else {
                    mainButton.setBackgroundResource(R.drawable.button_red);
                    bottomButton.setBackgroundResource(R.drawable.button_red);
                    bottomButton.setText("Share");
                }
            }
        }
        catch(Exception e){Log.v("cancelling share", "couldn't cast context as activity");}
    }

    public boolean getSharingStatus() {
        return sharingStatus;
    }

    public void setInMapsActivity(Activity activity) {
        inMapsActivity = activity;
        Log.v("setInMapsActivity", activity + "");
    }

    public Activity getInMapsActivity() {
        return inMapsActivity;
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
//                        Log.v("authData.getUid()", mFirebaseRef.child("users").child(peeps[i]).toString());
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
        listeners.put(u, new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot d2) {
                if (d2.getValue() != null) {
                    Log.v("Track", d2.toString());
                    String name = "";
                    String pos = "";
                    String pic_string = "";
                    String pin = "";
                    String pin_description = "";
                    for (DataSnapshot deets : d2.getChildren()) {
                        Log.v("Track", deets.toString());
                        if (deets.getKey().equals("name")){
                            name = deets.getValue().toString();
                        }
                        else if (deets.getKey().equals("pos")){
                            pos = deets.getValue().toString();
                        }
                        else if (deets.getKey().equals("picture")) {
                            pic_string = deets.getValue().toString();
                        }
                        else if(deets.getKey().equals("pin")) {
                            pin = deets.getValue().toString();
                        }
                        else if(deets.getKey().equals("pin_description")) {
                            pin_description = deets.getValue().toString();
                        }
                    }
                    if (name.length() == 0){
                        name = d2.getKey();
                    }
                    String uid = d2.getKey();
                    String[] loc = pos.split(",");
                    LatLng l;
                    if (loc.length > 1) {
                        l = new LatLng(Double.parseDouble(loc[0]), Double.parseDouble(loc[1]));
                        if (mMarkers.containsKey(uid)){
                            mMarkers.get(uid).setPosition(l);
                        }
                        else {

                            Marker new_m;

                            new_m = mMap.addMarker(new MarkerOptions().
                                    icon(BitmapDescriptorFactory.fromBitmap(mIconFactory.makeIcon(name))).
                                    position(l).
                                    anchor(mIconFactory.getAnchorU(), mIconFactory.getAnchorV()).
                                    title(name));

                            mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter(getApplicationContext(), Application.decodeBase64(pic_string)));

                            mMarkers.put(uid, new_m);

                        }

//                        class Yourcustominfowindowadpater implements GoogleMap.InfoWindowAdapter {
//                            private final View mymarkerview;
//
//                            Yourcustominfowindowadpater() {
//                                mymarkerview = getLayoutInflater().inflate(R.layout.custominfowindow, null);
//                            }
//
//                            public View getInfoWindow(Marker marker) {
//                                render(marker, mymarkerview);
//                                return mymarkerview;
//                            }
//
//                            public View getInfoContents(Marker marker) {
//                                return null;
//                            }
//
//                            private void render(Marker marker, View view) {
//                                // Add the code to set the required values
//                                // for each element in your custominfowindow layout file
//                            }
//                        }
//
//                        GoogleMap.setInfoWindowAdapter(Yourcustominfowindowadpater);

                        Log.v("Firebase Test", d2.getRef().getParent().getKey() + " moved to " + d2.getValue());
                    }
                    else {
                        // if location is gone, remove marker from map
                        if (mMarkers.containsKey(uid)) {
                            mMarkers.get(uid).remove();
                            mMarkers.remove(uid);
                        }
                    }
                    String[] pinLoc = pin.split(",");
                    LatLng pinL;
                    if(pinLoc.length > 1) {
                        pinL = new LatLng(Double.parseDouble(pinLoc[0]), Double.parseDouble(pinLoc[1]));
                        if(pinMarkers.containsKey(uid)) {
                            pinMarkers.get(uid).remove();
                            pinMarkers.remove(uid);
                        }
                        Marker marker = mMap.addMarker(new MarkerOptions().position(
                                new LatLng(pinL.latitude, pinL.longitude)).title(name).snippet(pin_description));
                        marker.setAlpha((float) .99);
                        pinMarkers.put(uid, marker);
                    }
                    else {
                        if(pinMarkers.containsKey(uid)) {
                            pinMarkers.get(uid).remove();
                            pinMarkers.remove(uid);
                        }
                    }
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
        mFirebaseRef.child("users").child(u).addValueEventListener(listeners.get(u));
    }

    public void cancelTracking(String uid) {
        try {
            mFirebaseRef.child("users").child(uid).removeEventListener(listeners.get(uid));
        } catch(Exception e) {Log.v("Removing friends error", "Couldn't remove listener from friend");}
        if(!uid.equals(mUserID) && mMarkers.containsKey(uid)) {
            Log.v("Removing marker", uid);
            mMarkers.get(uid).remove();
            mMarkers.remove(uid);
        }
        for(String key : mMarkers.keySet()) {
            Log.v("Marker", key);
        }
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

    public void trackAll(final Context context) {
        if (mFirebaseRef != null) {
            authData = mFirebaseRef.getAuth();
            if(authData == null) {
                Toast.makeText(context, "Please Log In", Toast.LENGTH_SHORT).show();
                Intent i = new Intent(context, loginActivity.class);
                startActivity(i);
            }
            mUserID = authData.getUid();
            mFirebaseRef.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    boolean noFriends = !dataSnapshot.child(mUserID).hasChild("friends");
                    // track the current user
                    trackUser(mUserID);
//                    for (DataSnapshot d : dataSnapshot.getChildren()) {
//                        Log.v("Firebase Test", d.getKey().toString());
//                        String curr = d.getKey().toString();
//                        trackUser(curr);
//                    }
//                    if (mIsNew)
//                        createNewUser();
                    if (noFriends) {
                        ((MapsActivity) context).noFriendsDialog();
                    }
                    // track friends
                    else {
                        for(DataSnapshot d : dataSnapshot.child(mUserID).child("friends").getChildren()) {
                            String curr = d.getKey();
                            trackUser(curr);
                        }
                    }
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {

                }
            });
        }
    }

    public static String encodeTobase64(Bitmap image)
    {
        Bitmap immagex=image;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        immagex.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] b = baos.toByteArray();
        String imageEncoded = Base64.encodeToString(b, Base64.DEFAULT);

        Log.v("Encoding Bitmap", imageEncoded);
        return imageEncoded;
    }
    public static Bitmap decodeBase64(String input)
    {
        byte[] decodedByte = Base64.decode(input, 0);
        return BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.length);
    }

}
