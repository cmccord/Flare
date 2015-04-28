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
import android.widget.MultiAutoCompleteTextView;
import android.widget.SeekBar;
import android.widget.TextView;

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

/**
 * Created by cmccord on 4/17/15.
 */
public class ShareActivity extends FragmentActivity {

    private int duration = 60; // in minutes
    private int frequency = 60; // in seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);
        SeekBar durationBar = (SeekBar) findViewById(R.id.seekBarDuration);
        SeekBar frequencyBar = (SeekBar) findViewById(R.id.seekBarFrequency);
        durationBar.setOnSeekBarChangeListener(new durationBarListener());
        frequencyBar.setOnSeekBarChangeListener(new frequencyBarListener());
        Button backButton = (Button) findViewById(R.id.backButton);
        backButton.setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {
                        finish();
                    }
                }
        );

        String[] str={"Andoid","Jelly Bean","Froyo",
                "Ginger Bread","Eclipse Indigo","Eclipse Juno"};

        // set up auto complete box
//        MultiAutoCompleteTextView mt=(MultiAutoCompleteTextView)
//                findViewById(R.id.addFriends);
//        mt.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
//        ArrayAdapter<String> adp=new ArrayAdapter<String>(this,
//                android.R.layout.simple_dropdown_item_1line,str);
//        mt.setThreshold(0);
//        mt.setAdapter(adp);

        Button shareLocation = (Button) findViewById(R.id.shareButton);
        final Application app = (Application) this.getApplication();
        shareLocation.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                //app.updateLocation();
                Intent i = new Intent(ShareActivity.this, LocationService.class);
                i.putExtra("duration", duration + "");
                i.putExtra("frequency", frequency + "");
                app.setService(i);
                startService(i);
                finish();
            }
        });

    }

    private class durationBarListener implements SeekBar.OnSeekBarChangeListener {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            TextView durationSetting = (TextView) findViewById(R.id.durationSetting);
            duration = progress + 1;
            durationSetting.setText(duration + " m");
        }
        @Override
        public void onStartTrackingTouch(SeekBar seekBar){}
        @Override
        public void onStopTrackingTouch(SeekBar seekBar){}
    }

    private class frequencyBarListener implements SeekBar.OnSeekBarChangeListener {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            TextView frequencySetting = (TextView) findViewById(R.id.frequencySetting);
            if(progress < 40) {
                frequency = progress + 20;
                frequencySetting.setText(frequency + " s");
            }
            else if(progress < 99) {
                frequency = (progress - 39) * 60;
                frequencySetting.setText(progress - 39 + " m");
            }
            else {
                frequency = (progress - 98) * 3600;
                frequencySetting.setText(progress - 98 + " hr");
            }
        }
        @Override
        public void onStartTrackingTouch(SeekBar seekBar){}
        @Override
        public void onStopTrackingTouch(SeekBar seekBar){}
    }


}
