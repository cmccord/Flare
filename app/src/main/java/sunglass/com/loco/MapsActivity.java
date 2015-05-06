package sunglass.com.loco;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
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
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsoluteLayout;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.AuthData;
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
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static android.telephony.TelephonyManager.*;

public class MapsActivity extends FragmentActivity {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private Firebase mFirebaseRef;
    private String mUserID;
    private SlidingUpPanelLayout mLayout;

    private Button mPingButton, mLeftButton, mRightButton;
    private LocationManager mLocationManager;
    private Criteria mCriteria;
    private String mProvider;
    private LocationListener mLocationListener;
    private Location mLocation;
    private String mImei;
    private boolean mIsNew = true;
    private String mInputText;
    private String[] mMenuStrings;
    private DrawerLayout mLeftDrawer, mRightDrawer;
    private ListView mLeftDrawerList, mRightDrawerList;
    private Application app;
    private boolean droppingPin = false;
    private static MapsActivity inst;
    private Sharer mSharer;

    @Override
    public void onStart() {
        super.onStart();
        inst = this;
    }

    public static MapsActivity instance() {
        return inst;
    }

    @Override
    protected void onPause() {
        if (mLocationManager != null) {
            mLocationManager.removeUpdates(mLocationListener);
        }
        if(droppingPin) {

        }
        //app.setInMapsActivity(null);
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
        app = (Application) this.getApplication();
        Log.v("Application Context", "" + this.getApplicationContext());
        app.setmUserID(mImei);
        setUpMapIfNeeded();
        Log.v("GPS", "initializing GPS");
        app.setUpGPS();
        setUpLeftDrawer();
        app.setUpFactory();
        app.setUpMarkers();
        // check whether or not location is being shared
        Intent intent = new Intent(MapsActivity.this, LocationShareReceiver.class);
        intent.setAction("sunglass.com.loco.LOCATION_SHARE");
        PendingIntent pi = PendingIntent.getBroadcast(MapsActivity.this, 0,
                intent, PendingIntent.FLAG_NO_CREATE);
        boolean alarmUp = (pi != null);
        app.setSharingStatus(alarmUp, this);

//        trackCircles();
        mLayout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
        mLayout.setOverlayed(true);
        app.trackAll(this);
        mPingButton = (Button) findViewById(R.id.topButton);
//        mPingButton.setLayoutParams(new LinearLayout.LayoutParams(mPingButton.getMeasuredHeight(), mPingButton.getMeasuredHeight()));
        mPingButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                Intent intent = new Intent(MapsActivity.this, LocationShareReceiver.class);
                intent.setAction("sunglass.com.loco.LOCATION_SHARE");
                PendingIntent pi = PendingIntent.getBroadcast(MapsActivity.this, 0,
                        intent, PendingIntent.FLAG_NO_CREATE);
                boolean alarmUp = (pi != null);
                if(!alarmUp) {
//                    Intent i = new Intent(MapsActivity.this, ShareActivity.class);
//                    app.notJustOpened();
//                    startActivity(i);
                    if(mLayout.getPanelState().equals(SlidingUpPanelLayout.PanelState.COLLAPSED))
                        mLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
                    else
                        mLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                } else {
                    LocationShareReceiver alarm = new LocationShareReceiver();
                    alarm.CancelAlarm(MapsActivity.this);
                    pi.cancel(); // see if this works, cancel the pending intent after cancelling alarm
//                    stopService(app.getService());
//                    app.setService(null);
                }
//                Intent i2 = new Intent(MapsActivity.this, LocationShareCancel.class);
//                intent.setAction("sunglass.com.loco.LOCATION_SHARE_CANCEL");
//                PendingIntent pi2 = PendingIntent.getBroadcast(MapsActivity.this, 0,
//                        i2, PendingIntent.FLAG_NO_CREATE);
//                if(pi2 != null) {
//                    LocationShareCancel alarm = new LocationShareCancel();
//                    alarm.CancelAlarm(MapsActivity.this);
//                    pi2.cancel();
//                }
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
        mRightButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                //updateLocation();
                Intent i = new Intent(MapsActivity.this, editProfileActivity.class);
//                app.notJustOpened();
                startActivity(i);
            }
        });

        mSharer = new Sharer(this);
    }

    private void createNewUser() {
        Log.v("New User", "Creating new user " + mImei);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Welcome to Flare! What is your name?");

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
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
        app.setInMapsActivity(this);
        Log.v("Application", getApplication().toString());
        app.setShareButton(this);
        try {
            mFirebaseRef = app.getFirebaseRef();
            AuthData authData = mFirebaseRef.getAuth();
            mUserID = authData.getUid();
            mFirebaseRef.child("users").child(mUserID).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.hasChild("requests")) {
                        for(DataSnapshot d : dataSnapshot.child("requests").getChildren()) {
                            String uid = d.getKey();
                            Log.v("Friend Request", uid);
                            friendRequestDialog(uid);
                        }
                    }
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {

                }
            });
        } catch(Exception e) {Log.v("onResume MapsActivity", "Error connecting to firebase");}

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
                ((Application) this.getApplication()).setmMap(mMap);
                LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
                Criteria criteria = new Criteria();
                criteria.setAccuracy(Criteria.ACCURACY_FINE);
                String provider = locationManager.getBestProvider(criteria, true);
                Location l = locationManager.getLastKnownLocation(provider);
                if (l != null)
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(l.getLatitude(), l.getLongitude()), 16));
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
        mMap.setPadding(10, 10, 10, 250);
//        mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));
    }

    protected GoogleMap getMap() {
        setUpMapIfNeeded();
        return mMap;
    }

//    private void zoomToCoverAllMarkers()
//    {
//        LatLngBounds existing = this.mMap.getProjection().getVisibleRegion().latLngBounds;
//        boolean all = true;
//
//        LatLngBounds.Builder builder = new LatLngBounds.Builder();
//        for (String marker : mMarkers.keySet())
//        {
//            if (!existing.contains(mMarkers.get(marker).getPosition()))
//                all = false;
//            builder.include(mMarkers.get(marker).getPosition());
//        }
//
//        if (!all) {
//            LatLngBounds bounds = builder.build();
//            int padding = 400; // offset from edges of the map in pixels
//            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
//            mMap.moveCamera(cu);
//            //mMap.animateCamera(cu);
//        }
//    }

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
                Intent intent = new Intent(MapsActivity.this, LocationShareReceiver.class);
                intent.setAction("sunglass.com.loco.LOCATION_SHARE");
                PendingIntent pi = PendingIntent.getBroadcast(MapsActivity.this, 0,
                        intent, PendingIntent.FLAG_NO_CREATE);
                boolean alarmUp = (pi != null);
                if(!alarmUp) {
//                    Intent i = new Intent(MapsActivity.this, ShareActivity.class);
//                    app.notJustOpened();
//                    startActivity(i);
                    if(mLayout.getPanelState().equals(SlidingUpPanelLayout.PanelState.COLLAPSED))
                        mLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
                    else
                        mLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                } else {
                    LocationShareReceiver alarm = new LocationShareReceiver();
                    alarm.CancelAlarm(MapsActivity.this);
                    pi.cancel(); // see if this works, cancel the pending intent after cancelling alarm
//                    stopService(app.getService());
//                    app.setService(null);
                }
                break;
            case 1:
                i = new Intent(this, addFriendsActivity.class);
//                app.notJustOpened();
                startActivity(i);
                break;
            case 2:
                i = new Intent(this, circlesActivity.class);
//                app.notJustOpened();
                startActivity(i);
                break;
            case 3:
                dropPin();
                break;
        }
    }

    public void dropPin() {
        if(!droppingPin) {
            droppingPin = true;
            Intent intent2 = new Intent(MapsActivity.this, PinShareReceiver.class);
            intent2.setAction("sunglass.com.loco.PIN_SHARE");
            PendingIntent pi2 = PendingIntent.getBroadcast(MapsActivity.this, 0,
                    intent2, PendingIntent.FLAG_NO_CREATE);
            if(pi2 != null) {
                PinShareReceiver alarm = new PinShareReceiver();
                alarm.CancelAlarm(MapsActivity.this);
                pi2.cancel();
            }
            else {
                Toast.makeText(getApplicationContext(), "Click and Hold to Drop Pin", Toast.LENGTH_LONG).show();
                mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
                    @Override
                    public void onMapLongClick(LatLng latLng) {
                        final double lat = latLng.latitude;
                        final double lon = latLng.longitude;
                        AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
                        LayoutInflater inflater = getLayoutInflater();
                        View convertView = (View) inflater.inflate(R.layout.drop_pin, null);
                        builder.setView(convertView);
                        builder.setTitle("Drop Pin");
                        Button accept = (Button) convertView.findViewById(R.id.acceptButton);
                        Button decline = (Button) convertView.findViewById(R.id.declineButton);
                        final TextView setting = (TextView) convertView.findViewById(R.id.pinSetting);
                        final SeekBar slider = (SeekBar) convertView.findViewById(R.id.pinSlider);
                        final TextView description = (TextView) convertView.findViewById(R.id.description);
                        final AlertDialog dialog = builder.create();
                        slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                            @Override
                            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                                setting.setText(progress + 1 + " m");
                            }

                            @Override
                            public void onStartTrackingTouch(SeekBar seekBar) {

                            }

                            @Override
                            public void onStopTrackingTouch(SeekBar seekBar) {

                            }
                        });
                        accept.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (description.getText().toString().matches("([0-9]|[a-z]|[A-Z]| |_)+") && description.getText().toString().length() <= 15) {
                                    try {
                                        mFirebaseRef.child("users").child(mUserID).child("pin").setValue(lat + "," + lon);
                                        mFirebaseRef.child("users").child(mUserID).child("pin_description").setValue(description.getText().toString());
                                        Intent i = new Intent(MapsActivity.this, PinService.class);
                                        i.putExtra("duration", (slider.getProgress() + 1) + "");
                                        startService(i);
                                    } catch (Exception e) {
                                        Log.v("Drop Pin", "Could not drop pin");
                                    }
                                    mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
                                        @Override
                                        public void onMapLongClick(LatLng latLng) {

                                        }
                                    });
                                    droppingPin = false;
                                    dialog.cancel();
                                } else
                                    Toast.makeText(getApplicationContext(), "Invalid Pin Description", Toast.LENGTH_SHORT).show();
                            }
                        });
                        decline.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
                                    @Override
                                    public void onMapLongClick(LatLng latLng) {

                                    }
                                });
                                droppingPin = false;
                                dialog.cancel();
                            }
                        });
                        dialog.show();
                    }
                });
            }
        }
        else {
            mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
                @Override
                public void onMapLongClick(LatLng latLng) {

                }
            });
            droppingPin = false;
        }
    }

    public void noFriendsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Welcome to Flare! Would you like to add friends now?");

        // Set up the buttons
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent i = new Intent(MapsActivity.this, newFriendsActivity.class);
//                app.notJustOpened();
                startActivity(i);
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }


    private void friendRequestDialog(final String uid) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View convertView = (View) inflater.inflate(R.layout.friend_request, null);
        builder.setView(convertView);
        builder.setTitle("New Friend Request");
        final TextView txtName = (TextView) convertView.findViewById(R.id.txtName);
        final TextView txtEmail = (TextView) convertView.findViewById(R.id.txtEmail);
        final ImageView imageView = (ImageView) convertView.findViewById(R.id.imgIcon);
        Button accept = (Button) convertView.findViewById(R.id.acceptButton);
        Button decline = (Button) convertView.findViewById(R.id.declineButton);
        try {
            mFirebaseRef.child("users").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    try {
                        String name = (String) dataSnapshot.child("name").getValue();
                        String email = (String) dataSnapshot.child("email").getValue();
                        if(dataSnapshot.hasChild("picture"))
                            imageView.setImageBitmap(Application.decodeBase64(dataSnapshot.child("picture").getValue().toString()));
                        txtName.setText(name);
                        txtEmail.setText(email);
                    } catch(Exception e) {Log.v("Friend Request", e.toString());}
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {

                }
            });
            Log.v("Friend Request", "Created listener");
            final AlertDialog dialog = builder.create();
            Log.v("Friend Request", "Created dialog");
            accept.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        mFirebaseRef.child("users").child(mUserID).child("requests").child(uid).removeValue();
                    } catch (Exception e) {
                        Log.v("Friend Request", "Couldn't delete request");
                    }
                    try {
                        mFirebaseRef.child("users").child(mUserID).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.hasChild("friends")) {
                                    Map newFriend = new HashMap<>();
                                    newFriend.put(uid, uid);
                                    mFirebaseRef.child("users").child(mUserID).child("friends").updateChildren(newFriend);
                                } else {
                                    Map friends = new HashMap<>();
                                    Map newFriend = new HashMap<>();
                                    newFriend.put(uid, uid);
                                    friends.put("friends", newFriend);
                                    mFirebaseRef.child("users").child(mUserID).updateChildren(friends);
                                }
                            }

                            @Override
                            public void onCancelled(FirebaseError firebaseError) {

                            }
                        });
                        if(app.getCircleSelected().equals("All Friends"))
                            app.trackUser(uid);
                    } catch (Exception e) {
                        Log.v("Friend Request", "Couldn't accept request");
                    }
                    dialog.dismiss();
                }
            });
            Log.v("Friend Request", "created accept button listener");
            decline.setOnClickListener((new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        mFirebaseRef.child("users").child(mUserID).child("requests").child(uid).removeValue();
                        mFirebaseRef.child("users").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if(dataSnapshot.hasChild("friends") && dataSnapshot.child("friends").hasChild(mUserID))
                                    mFirebaseRef.child("users").child(uid).child("friends").child(mUserID).removeValue();
                            }

                            @Override
                            public void onCancelled(FirebaseError firebaseError) {

                            }
                        });
                    } catch(Exception e) {Log.v("Friend Request", "Couldn't delete request");}
                    dialog.dismiss();
                }
            }));

            dialog.show();
        } catch (Exception e) {Log.v("Friend Request", e.toString());}
    }

    @Override
    public void setTitle(CharSequence title) {
//        mTitle = title;
//        getActionBar().setTitle(mTitle);
    }

    @Override
    public void onBackPressed() {

        Log.v("STATUSSS", ""+app.wasJustOpened());

        if (app.wasJustOpened()) {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
        else {
            super.onBackPressed();
        }

    }

}
