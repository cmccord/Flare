package sunglass.com.loco;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class LocationService extends Service {

    LocationShareReceiver alarm = new LocationShareReceiver();
    public void onCreate()
    {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        int interval = Integer.parseInt(intent.getStringExtra("frequency"));
        int d = Integer.parseInt(intent.getStringExtra("duration"));
        long expiration = System.currentTimeMillis() + 1000*60*d;
        Log.v("duration", d + "");
        alarm.SetAlarm(LocationService.this, interval, expiration);
        return START_NOT_STICKY;
    }

    public void onStart(Context context,Intent intent, int startId)
    {
        int interval = Integer.parseInt(intent.getStringExtra("frequency"));
        int d = Integer.parseInt(intent.getStringExtra("duration"));
        alarm.SetAlarm(context, interval, d);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
