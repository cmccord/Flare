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

        private View mymarkerview;
        private Bitmap image;
        private boolean isPerson;
        private Context context;

        public CustomInfoWindowAdapter(Context context_given, Bitmap image_to_display, boolean isPerson_given) {

            image = image_to_display;
            isPerson = isPerson_given;
            context = context_given;

        }

        public View getInfoWindow(Marker marker) {

            // If a pin, return null for default.
            if (!isPerson) { return null; }

            // If a person:
            LayoutInflater inflater = LayoutInflater.from(context);
            mymarkerview = inflater.inflate(R.layout.custom_info_window, null);
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