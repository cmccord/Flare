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
import android.os.PowerManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.firebase.client.Firebase;

/**
 * Created by cmccord on 4/23/15.
 */
public class LocationShareReceiver extends BroadcastReceiver{
    private Location mLocation;
    private LocationManager mLocationManager;
    private String mProvider;
    private Firebase mFirebaseRef;
    @Override
    public void onReceive(Context context, Intent intent) {

        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "");
        wl.acquire();
        mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        Criteria mCriteria = new Criteria();
        mCriteria.setAccuracy(Criteria.ACCURACY_FINE);
        mProvider = mLocationManager.getBestProvider(mCriteria, true);
        TelephonyManager telephonyManager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
        String mUserID = telephonyManager.getDeviceId();
        boolean isEnabled = mLocationManager.isProviderEnabled(mProvider);
        if(isEnabled) {
            LocationListener mLocationListener = new LocationListener() {
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
            mLocationManager.requestLocationUpdates(mProvider, 1000, 1, mLocationListener);
            Firebase.setAndroidContext(context);
            mFirebaseRef = new Firebase("https://loco-android.firebaseio.com/");
            if (mFirebaseRef != null) {
                if (mLocation != null) {
                    mFirebaseRef.child("users").child(mUserID).child("pos").setValue(
                            mLocation.getLatitude() + "," + mLocation.getLongitude()
                    );
                }
                mFirebaseRef.child("users").child(mUserID).child("timestamp").setValue(System.currentTimeMillis());
                Log.v("GPS", "Firebase GPS updated.");
                Toast.makeText(context, "Location Updated", Toast.LENGTH_SHORT).show();
            }
            mLocationManager.removeUpdates(mLocationListener);
        }
        wl.release();
    }

    public void SetAlarm(Context context)
    {
        AlarmManager am =( AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(context, LocationShareReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);
        am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 1000 * 20, pi); // Millisec * Second * Minute
    }

    public void CancelAlarm(Context context)
    {
        Intent intent = new Intent(context, LocationShareReceiver.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender);
    }
}
