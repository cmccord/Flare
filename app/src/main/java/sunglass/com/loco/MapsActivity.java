package sunglass.com.loco;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.telephony.TelephonyManager;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.AbsoluteLayout;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.ui.IconGenerator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static android.telephony.TelephonyManager.*;

public class MapsActivity extends FragmentActivity {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private Firebase mFirebaseRef;
    private String mUserID = "jabreezus";

    private Button mPingButton, mLeftButton, mRightButton;
    private LocationManager mLocationManager;
    private Criteria mCriteria;
    private String mProvider;
    private LocationListener mLocationListener;
    private Location mLocation;
    private HashMap<String, Marker> mMarkers;
    private String mImei;
    private boolean mIsNew = true;
    private String mInputText;
    private IconGenerator mIconFactory;
    private String[] mMenuStrings;
    private DrawerLayout mLeftDrawer, mRightDrawer;
    private ListView mLeftDrawerList, mRightDrawerList;

    @Override
    protected void onPause() {
        if (mLocationManager != null){
            mLocationManager.removeUpdates(mLocationListener);
        }
        super.onPause();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        mImei = telephonyManager.getDeviceId();

//        mImei += System.currentTimeMillis() / 1000*60;
        // for now!
        mUserID = mImei;
        setUpMapIfNeeded();
        Log.v("GPS", "initializing GPS");
        setUpGPS();
        setUpLeftDrawer();
        setUpRightDrawer();
        setUpFactory();
        Firebase.setAndroidContext(this);
        mFirebaseRef = new Firebase("https://loco-android.firebaseio.com/");
        mMarkers = new HashMap<String, Marker>();

//        trackCircles();
        trackAll();
        mPingButton = (Button) findViewById(R.id.topButton);
//        mPingButton.setLayoutParams(new LinearLayout.LayoutParams(mPingButton.getMeasuredHeight(), mPingButton.getMeasuredHeight()));
        mPingButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                //updateLocation();
                Intent i = new Intent(MapsActivity.this, ShareActivity.class);
                startActivity(i);
            }
        });

        mLeftButton = (Button) findViewById(R.id.leftButton);
        mLeftButton.setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {
                        mLeftDrawer.openDrawer(mLeftDrawerList);
                    }
                }
        );
        mRightButton = (Button) findViewById(R.id.rightButton);
        mRightButton.setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {
                        mRightDrawer.openDrawer(mRightDrawerList);
                    }
                }
        );
    }

    private void updateLocation() {
        Log.v("GPS", "Firebase GPS updated?");
        if (mFirebaseRef != null) {
            if (mLocation != null) {
//                mFirebaseRef.child("users").child(mUserID).child("pos").setValue(
//                        mLocation.getLatitude() + "," + mLocation.getLongitude()
//                );
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

    private void trackCircles() {
        mFirebaseRef.child("users").child(mUserID).child("circles").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot d: dataSnapshot.getChildren()){
                    String[] peeps = d.getValue().toString().split(",");
                    for (int i = 0; i < peeps.length; i++) {
                        Log.v("Firebase Test", mFirebaseRef.child("users").child(peeps[i]).toString());
                        trackUser(peeps[i]);
                    }
                }
            }
            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    private void trackUser(String u){
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
                if (mMap.getMyLocation() != null) {
                    Location l = mMap.getMyLocation();
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(l.getLatitude(), l.getLongitude()), 14));
                }
//                zoomToCoverAllMarkers();
            }
            @Override
            public void onCancelled(FirebaseError firebaseError) {
            }
        });
    }

    private void createNewUser(){
        Log.v("New User", "Creating new user " + mImei);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Welcome to Loco! What is your name?");

        if (mFirebaseRef != null) {
            User newUser = new User(mImei, mLocation);
            Map user = new HashMap<>();
            Map dets = new HashMap<>();
            dets.put("pos", newUser.pos);
            dets.put("name", mInputText);
            dets.put("timestamp", System.currentTimeMillis());
            dets.put("time_created", System.currentTimeMillis());
            user.put(mImei, dets);

            mFirebaseRef.child("users").updateChildren(user);
        }

        // Set up the input
        final EditText input = new EditText(this);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mInputText = input.getText().toString();
                if (mFirebaseRef != null) {
                    Map m = new HashMap<>();
                    m.put("name", mInputText);
                    mFirebaseRef.child("users").child(mImei).updateChildren(m);
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();

        trackUser(mImei);
    }
    private void trackAll() {
        if (mFirebaseRef != null)
            mFirebaseRef.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot d: dataSnapshot.getChildren()){
                        Log.v("Firebase Test", d.getKey().toString());
                        String curr = d.getKey().toString();
                        if (curr.equals(mImei))
                            mIsNew = false;
                        trackUser(curr);
                    }
                    if (mIsNew)
                        createNewUser();
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {

                }
            });
    }
    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        mMap.setMyLocationEnabled(true);
//        mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));
    }

    protected GoogleMap getMap() {
        setUpMapIfNeeded();
        return mMap;
    }


    private boolean setUpGPS() {
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

    private void zoomToCoverAllMarkers()
    {
        LatLngBounds existing = this.mMap.getProjection().getVisibleRegion().latLngBounds;
        boolean all = true;

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (String marker : mMarkers.keySet())
        {
            if (!existing.contains(mMarkers.get(marker).getPosition()))
                all = false;
            builder.include(mMarkers.get(marker).getPosition());
        }

        if (!all) {
            LatLngBounds bounds = builder.build();
            int padding = 400; // offset from edges of the map in pixels
            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
//        mMap.moveCamera(cu);
            mMap.animateCamera(cu);
        }
    }
    private void setUpFactory() {
        mIconFactory = new IconGenerator(this);
        mIconFactory.setStyle(IconGenerator.STYLE_GREEN);
        mIconFactory.setRotation(90);
        mIconFactory.setContentRotation(-90);
        mIconFactory.setTextAppearance(R.style.marker);
    }

    private void setUpLeftDrawer() {
        mMenuStrings = getResources().getStringArray(R.array.leftmenu);
        mLeftDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        mLeftDrawerList = (ListView) findViewById(R.id.left_drawer);

        // Set the adapter for the list view
        mLeftDrawerList.setAdapter(new ArrayAdapter<String>(this,
                R.layout.drawer_list_item, mMenuStrings));
        // Set the list's click listener
        mLeftDrawerList.setOnItemClickListener(new LeftDrawerItemClickListener());
    }

    private class LeftDrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView parent, View view, int position, long id) {
            selectLeftItem(position);
        }
    }

    private class RightDrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView parent, View view, int position, long id) {
            selectRightItem(position);
        }
    }

    private void setUpRightDrawer() {
        mMenuStrings = getResources().getStringArray(R.array.rightmenu);
        mRightDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        mRightDrawerList = (ListView) findViewById(R.id.right_drawer);

        // Set the adapter for the list view
        mRightDrawerList.setAdapter(new ArrayAdapter<String>(this,
                R.layout.drawer_list_item, mMenuStrings));
        // Set the list's click listener
        mRightDrawerList.setOnItemClickListener(new RightDrawerItemClickListener());
    }

    /** Swaps fragments in the main content view */
    private void selectLeftItem(int position) {
        // Create a new fragment and specify the planet to show based on position
//        Fragment fragment = new PlanetFragment();
//        Bundle args = new Bundle();
//        args.putInt(PlanetFragment.ARG_PLANET_NUMBER, position);
//        fragment.setArguments(args);
//
//        // Insert the fragment by replacing any existing fragment
//        FragmentManager fragmentManager = getFragmentManager();
//        fragmentManager.beginTransaction()
//                .replace(R.id.content_frame, fragment)
//                .commit();

        // Highlight the selected item, update the title, and close the drawer
        mLeftDrawerList.setItemChecked(position, true);
        setTitle(mMenuStrings[position]);
        mLeftDrawer.closeDrawer(mLeftDrawerList);
        Intent i;
        switch(position){
            case 0:
                i = new Intent(this, ShareActivity.class);
                startActivity(i);
                break;
        }
    }

    private void selectRightItem(int position) {
        // Create a new fragment and specify the planet to show based on position
//        Fragment fragment = new PlanetFragment();
//        Bundle args = new Bundle();
//        args.putInt(PlanetFragment.ARG_PLANET_NUMBER, position);
//        fragment.setArguments(args);
//
//        // Insert the fragment by replacing any existing fragment
//        FragmentManager fragmentManager = getFragmentManager();
//        fragmentManager.beginTransaction()
//                .replace(R.id.content_frame, fragment)
//                .commit();

        // Highlight the selected item, update the title, and close the drawer
        mRightDrawerList.setItemChecked(position, true);
        setTitle(mMenuStrings[position]);
        mRightDrawer.closeDrawer(mRightDrawerList);
        Intent i;
        switch(position){
            case 0:
                i = new Intent(this, editProfileActivity.class);
                startActivity(i);
                break;
        }
    }

    @Override
    public void setTitle(CharSequence title) {
//        mTitle = title;
//        getActionBar().setTitle(mTitle);
    }
}
