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
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

import com.firebase.client.AuthData;
import com.firebase.client.Firebase;

/**
 * Created by cmccord on 5/5/15.
 */
public class PinShareReceiver extends BroadcastReceiver {
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
        PinShareReceiver alarm = new PinShareReceiver();
        alarm.CancelAlarm(context);
        wl.release();
    }

    public void SetAlarm(Context context, long expiration)
    {
        //iterations = 60 * d / interval;
        AlarmManager am =( AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(context, PinShareReceiver.class);
        i.putExtra("expiration", expiration + "");
        i.setAction("sunglass.com.loco.PIN_SHARE");
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
        am.set(AlarmManager.RTC_WAKEUP, expiration, pi); // Millisec * Second * Minute
        //am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), 1000 * interval, pi);
        Firebase.setAndroidContext(context);
        mFirebaseRef = new Firebase("https://loco-android.firebaseio.com/");
        if(mFirebaseRef != null) {
            authData = mFirebaseRef.getAuth();
            if(authData != null)
                mUserID = authData.getUid();
        }
        if(mFirebaseRef != null && authData != null) {
            mFirebaseRef.child("users").child(mUserID).child("pin_expiration").setValue(expiration);
        }
    }

    public void CancelAlarm(Context context)
    {
        Firebase.setAndroidContext(context);
        mFirebaseRef = new Firebase("https://loco-android.firebaseio.com/");
        if(mFirebaseRef != null) {
            authData = mFirebaseRef.getAuth();
            if(authData != null)
                mUserID = authData.getUid();
        }
        if(mFirebaseRef != null && authData != null) {
            mFirebaseRef.child("users").child(mUserID).child("pin").setValue("");
            mFirebaseRef.child("users").child(mUserID).child("pin_expiration").setValue((long) 0);
            Toast.makeText(context, "Pin Cancelled", Toast.LENGTH_SHORT).show();
        }
    }
}
