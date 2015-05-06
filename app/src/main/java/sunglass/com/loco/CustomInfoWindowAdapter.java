package sunglass.com.loco;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.AuthData;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

import java.util.Map;

/**
 * Created by kwdougla on 5/5/15.
 */

public class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

        private final View mymarkerview;
        private Bitmap image;
//        private long time;

        public CustomInfoWindowAdapter(Context context, Bitmap image_to_display) {

//            LayoutInflater inflater = getLayoutInflater();
            LayoutInflater inflater = LayoutInflater.from(context);
//            LayoutInflater inflater = (LayoutInflater) Context.getApplicationContext().getSystemService( Context.LAYOUT_INFLATER_SERVICE );
            mymarkerview = inflater.inflate(R.layout.custom_info_window, null);
            image = image_to_display;
//            time = time_to_display;

//            mymarkerview = getLayoutInflater().inflate(R.layout.custom_info_window, null);
        }

        public View getInfoWindow(Marker marker) {
            render(marker, mymarkerview);
            return mymarkerview;
        }

        public View getInfoContents(Marker marker) {
            return null;
        }

        private void render(Marker marker, View view) {

            ImageView image_disp = (ImageView) mymarkerview.findViewById(R.id.indiv_pro_pic);
//            TextView time_disp = (TextView) mymarkerview.findViewById(R.id.time_to_exp_textview);

            image_disp.setImageBitmap(image);
//            time_disp.setText("Time Remaining: " + time + " s");

        }

}