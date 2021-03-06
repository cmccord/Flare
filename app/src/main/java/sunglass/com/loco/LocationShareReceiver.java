package sunglass.com.loco;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.os.Parcelable;
import android.os.PowerManager;
import android.os.SystemClock;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.firebase.client.AuthData;
import com.firebase.client.Firebase;

import java.io.Serializable;

/**
 * Created by cmccord on 4/23/15.
 */
public class LocationShareReceiver extends BroadcastReceiver{
    private Location mLocation;
    private LocationManager mLocationManager;
    private String mProvider;
    private Firebase mFirebaseRef;
    private Application app;
    private AuthData authData;
    private String mUserID;

    @Override
    public void onReceive(Context context, Intent intent) {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "");
        wl.acquire();
        Firebase.setAndroidContext(context);
        mFirebaseRef = new Firebase("https://loco-android.firebaseio.com/");
        if(mFirebaseRef != null) {
            authData = mFirebaseRef.getAuth();
            if(authData != null)
                mUserID = authData.getUid();
        }
        long expiration = Long.parseLong(intent.getStringExtra("expiration"));
        if(System.currentTimeMillis() >= expiration) {
            LocationShareReceiver alarm = new LocationShareReceiver();
            alarm.CancelAlarm(context);
            Toast.makeText(context, "Location Share Cancelled", Toast.LENGTH_SHORT).show();
        }
        else {
            mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            Criteria mCriteria = new Criteria();
            mCriteria.setAccuracy(Criteria.ACCURACY_FINE);
            mProvider = mLocationManager.getBestProvider(mCriteria, true);
            final Context c = context;
            boolean isEnabled = mLocationManager.isProviderEnabled(mProvider);
            if (isEnabled) {

                LocationListener mLocationListener = new LocationListener() {
                    public void onLocationChanged(Location location) {
                        // Called when a new location is found by the network location provider.
                        Log.v("LocationNews", location.getLatitude() + " " + location.getLongitude());
                        mLocation = location;
                        if (mLocation == null)
                            mLocation = mLocationManager.getLastKnownLocation(mProvider);
                        if (mLocation != null) {
                            if (mFirebaseRef != null) {
                                if (mLocation != null && authData != null) {
                                    mFirebaseRef.child("users").child(mUserID).child("pos").setValue(
                                            mLocation.getLatitude() + "," + mLocation.getLongitude()
                                    );
                                    Toast.makeText(c, "Location Updated", Toast.LENGTH_SHORT).show();
                                } else
                                    Toast.makeText(c, "Location Update Failed", Toast.LENGTH_SHORT).show();
                                mFirebaseRef.child("users").child(mUserID).child("timestamp").setValue(System.currentTimeMillis());
                                Log.v("GPS", "Firebase GPS updated.");
                            }
                        }
                    }

                    public void onStatusChanged(String provider, int status, Bundle extras) {
                    }

                    public void onProviderEnabled(String provider) {
                    }

                    public void onProviderDisabled(String provider) {
                    }
                };
                mLocationManager.requestSingleUpdate(mProvider, mLocationListener, Looper.myLooper());
            }
        }
        wl.release();
    }

    public void SetAlarm(Context context, int interval, long expiration)
    {
        //iterations = 60 * d / interval;
        AlarmManager am =( AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(context, LocationShareReceiver.class);
        i.putExtra("expiration", expiration + "");
        i.setAction("sunglass.com.loco.LOCATION_SHARE");
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
        am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 1000 * interval, pi); // Millisec * Second * Minute
        //am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), 1000 * interval, pi);
        app = Application.instance();
        Log.v("application", app.toString());
        if(app != null) {
            app.setSharingStatus(true, context);
        }
        Firebase.setAndroidContext(context);
        mFirebaseRef = new Firebase("https://loco-android.firebaseio.com/");
        if(mFirebaseRef != null) {
            authData = mFirebaseRef.getAuth();
            if(authData != null)
                mUserID = authData.getUid();
        }
        if(mFirebaseRef != null && authData != null) {
            mFirebaseRef.child("users").child(mUserID).child("expiration").setValue(expiration);
        }
    }

    public void CancelAlarm(Context context)
    {
        Intent intent = new Intent(context, LocationShareReceiver.class);
        intent.setAction("sunglass.com.loco.LOCATION_SHARE");
        PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender);
        sender.cancel();
        app = Application.instance();
        Log.v("Application Context LSR", "" + app);
        if(app != null) {
            Log.v("setting sharing status", "to false");
            app.setSharingStatus(false, context);
        }
        Firebase.setAndroidContext(context);
        mFirebaseRef = new Firebase("https://loco-android.firebaseio.com/");
        if(mFirebaseRef != null) {
            authData = mFirebaseRef.getAuth();
            if(authData != null)
                mUserID = authData.getUid();
        }
        if(mFirebaseRef != null && authData != null) {
            mFirebaseRef.child("users").child(mUserID).child("pos").setValue("");
            mFirebaseRef.child("users").child(mUserID).child("expiration").setValue((long) 0);

        }
    }
}
