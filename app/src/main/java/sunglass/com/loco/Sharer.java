package sunglass.com.loco;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;

public class Sharer{

    private int duration = 60; // in minutes
    private int frequency = 60; // in seconds
    private Activity mParent;
    private SeekBar frequencyBar;
    public Sharer(final Activity parent) {
        mParent = parent;
        SeekBar durationBar = (SeekBar) parent.findViewById(R.id.seekBarDuration);
        frequencyBar = (SeekBar) parent.findViewById(R.id.seekBarFrequency);
        durationBar.setOnSeekBarChangeListener(new durationBarListener());
        frequencyBar.setOnSeekBarChangeListener(new frequencyBarListener());

        Button shareLocation = (Button) parent.findViewById(R.id.shareButton);
        final Application app = (Application) parent.getApplication();
        shareLocation.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                Intent intent = new Intent(parent, LocationShareReceiver.class);
                intent.setAction("sunglass.com.loco.LOCATION_SHARE");
                PendingIntent pi = PendingIntent.getBroadcast(parent, 0,
                        intent, PendingIntent.FLAG_NO_CREATE);
                boolean alarmUp = (pi != null);
                if(!alarmUp) {
                    Intent i = new Intent(parent, LocationService.class);
                    i.putExtra("duration", duration + "");
                    i.putExtra("frequency", frequency + "");
                    app.setService(i);
                    parent.startService(i);
                    ((SlidingUpPanelLayout) parent.findViewById(R.id.sliding_layout)).setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                } else {
                    LocationShareReceiver alarm = new LocationShareReceiver();
                    alarm.CancelAlarm(parent);
                    pi.cancel(); // see if this works, cancel the pending intent after cancelling alarm
//                    stopService(app.getService());
//                    app.setService(null);
                }
            }
        });

    }


    private class durationBarListener implements SeekBar.OnSeekBarChangeListener {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            TextView durationSetting = (TextView) mParent.findViewById(R.id.durationSetting);
            duration = progress + 1;
            durationSetting.setText(duration + " m");
            int max;
            if(progress < 60)
                max = progress + 55;
            else if(progress < 119)
                max = 114;
            else
                max = 115;
            frequencyBar.setMax(max);
        }
        @Override
        public void onStartTrackingTouch(SeekBar seekBar){}
        @Override
        public void onStopTrackingTouch(SeekBar seekBar){}
    }

    private class frequencyBarListener implements SeekBar.OnSeekBarChangeListener {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            TextView frequencySetting = (TextView) mParent.findViewById(R.id.frequencySetting);
            if(progress < 55) {
                frequency = progress + 5;
                frequencySetting.setText(frequency + " s");
            }
            else if(progress < 114) {
                frequency = (progress - 54) * 60;
                frequencySetting.setText(progress - 54 + " m");
            }
            else {
                frequency = (progress - 113) * 3600;
                frequencySetting.setText(progress - 113 + " hr");
            }
        }
        @Override
        public void onStartTrackingTouch(SeekBar seekBar){}
        @Override
        public void onStopTrackingTouch(SeekBar seekBar){}
    }


}
