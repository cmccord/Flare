package sunglass.com.loco;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;
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
        private Context context;

        private Firebase ref;

        private Application app;
        private Marker marker;

    public CustomInfoWindowAdapter(Context context_given) {

            context = context_given;
            app = (Application) context;
            ref = app.getFirebaseRef();

    }

        public View getInfoWindow(Marker marker) {

            // If a pin, return null for default.
            if (marker.getAlpha() == (float) .99) { return null; }

            // If a person:
            LayoutInflater inflater = LayoutInflater.from(context);
            mymarkerview = inflater.inflate(R.layout.custom_info_window, null);
            this.marker = marker;
            render();
            return mymarkerview;
        }

        public View getInfoContents(Marker marker) {
            return null;
        }

        private void render() {

//            Log.v("NAME", marker.getTitle());
//
////            final ImageView image_disp = (ImageView) mymarkerview.findViewById(R.id.indiv_pro_pic);
//
//            ref.child("users").child(marker.getTitle()).addListenerForSingleValueEvent(new ValueEventListener() {
//                @Override
//                public void onDataChange(DataSnapshot dataSnapshot) {
//                    Map<String, Object> value = (Map<String, Object>) dataSnapshot.getValue();
//
//                    Log.v("NAME",value.get("picture").toString());
//
//
//
//
//                }
//
//                @Override
//                public void onCancelled(FirebaseError firebaseError) {
//                }
//            });

            if (!marker.getTitle().equals("")) {
                ImageView image_disp = (ImageView) mymarkerview.findViewById(R.id.indiv_pro_pic);
                image_disp.setImageBitmap(Application.decodeBase64(marker.getTitle()));
            }

        }

}