package sunglass.com.loco;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

public class LocationService extends Service {

    LocationShareReceiver alarm = new LocationShareReceiver();
    public void onCreate()
    {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        alarm.SetAlarm(LocationService.this);
        return START_STICKY;
    }

    public void onStart(Context context,Intent intent, int startId)
    {
        alarm.SetAlarm(context);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
