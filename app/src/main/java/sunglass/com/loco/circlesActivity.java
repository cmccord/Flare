package sunglass.com.loco;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.maps.model.Marker;

import java.util.ArrayList;
import java.util.HashMap;


public class circlesActivity extends Activity {

    private Application app;
    private Firebase ref;
    private String userID;
    private ArrayList<String> circles;
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_circles);

        app = ((Application)getApplication());
        ref = app.getFirebaseRef();
        try {
            userID = ref.getAuth().getUid();
        } catch(Exception e) {userID = "";}

        Button mBackButton = (Button) findViewById(R.id.back_butt);
        mBackButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //updateLocation();
                finish();
            }
        });

        Button addButton = (Button) findViewById((R.id.rightButton));
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(circlesActivity.this, NewCircleActivity.class);
                app.notJustOpened();
                startActivity(i);
            }
        });

        listView = (ListView) findViewById(R.id.listView2);

    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            ref.child("users").child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    int numCircles = 0;
                    circles = new ArrayList<String>();
                    circles.add("All Friends");
                    if(dataSnapshot.hasChild("circles")) {
                        numCircles = (int) dataSnapshot.child("circles").getChildrenCount();
                        DataSnapshot s = dataSnapshot.child("circles");
                        for(DataSnapshot d : s.getChildren()) {
                            circles.add(d.getKey());
                        }
                    }
                    ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(circlesActivity.this,
                            android.R.layout.simple_list_item_single_choice,circles);
                    listView.setAdapter(arrayAdapter);
                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            CheckedTextView textView;
                            for (int i = 0; i < listView.getCount(); i++) {
                                textView= (CheckedTextView) listView.getChildAt(i);
                                if (textView != null) {
                                    textView.setChecked(false);
                                }
                            }
                            textView = (CheckedTextView) view;
                            if (textView != null) {
                                textView.setChecked(true);
                            }
                            app.setCircleSelected(circles.get(position));
                        }
                    });
                    // long click to delete circle
                    listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                        @Override
                        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                            final String circleToRemove = circles.get(position);
                            if(!circleToRemove.equals("All Friends")) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(circlesActivity.this);
                                builder.setTitle("Would you like to remove circle " + circles.get(position) + "?");
                                // Set up the buttons
                                builder.setPositiveButton("Remove", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        // Remove from friends list
                                        try {
                                            ref.child("users").child(userID).child("circles").child(circleToRemove).removeValue();
                                            if (app.getCircleSelected().equals(circleToRemove))
                                                app.setCircleSelected("All Friends");
                                            Toast.makeText(getApplicationContext(), "Circle " + circleToRemove + " removed", Toast.LENGTH_SHORT).show();
                                        } catch (Exception e) {
                                            Log.v("Removing circle error", "Couldn't remove circle");
                                        }
                                        circlesActivity.this.onResume();
                                        dialog.cancel();
                                    }
                                });
                                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                    }
                                });
                                builder.show();
                            }
                            return true;
                        }
                    });
                    int mActivePosition = circles.indexOf(app.getCircleSelected());
                    if(mActivePosition == -1) mActivePosition = 0;
                    Log.v("Active Position", mActivePosition + "");
                    listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
                    listView.setItemChecked(mActivePosition, true);
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {

                }
            });
        } catch(Exception e) {
            Log.v("Error loading circles", e.toString());
        }
    }

}
