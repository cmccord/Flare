package sunglass.com.loco;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.maps.model.Marker;

import java.util.HashMap;
import java.util.Map;


/**
 * Created by cmccord on 4/29/15.
 */
public class addFriendsActivity extends Activity {
    private Firebase ref;
    private Application app;
    private String userID;
    private Person[] friends;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addfriends);

        app = ((Application)getApplication());
        ref = app.getFirebaseRef();
        try {
            userID = ref.getAuth().getUid();
        } catch(Exception e) {userID = "";}
        Button mBackButton = (Button) findViewById(R.id.back_button);
        mBackButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });

        Button newFriend = (Button) findViewById(R.id.rightButton);
        newFriend.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent i = new Intent(addFriendsActivity.this, newFriendsActivity.class);
                app.notJustOpened();
                startActivity(i);
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            ref.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    int numFriends = (int) dataSnapshot.child(userID).child("friends").getChildrenCount();
                    friends = new Person[numFriends];
                    int i = 0;
                    for(DataSnapshot d : dataSnapshot.child(userID).child("friends").getChildren()) {
                        String name = (String) dataSnapshot.child(d.getKey()).child("name").getValue();
                        String email = (String) dataSnapshot.child(d.getKey()).child("email").getValue();
                        Bitmap pic;
                        Person person = new Person(name, email);
                        if(dataSnapshot.child(d.getKey()).hasChild("picture")) {
                            pic = Application.decodeBase64(dataSnapshot.child(d.getKey()).child("picture").getValue().toString());
                            person.setImage(pic);
                        }
                        person.setUid(d.getKey());
                        friends[i] = person;
                        i++;
                    }
                    PersonAdapter friendsArrayAdapter = new PersonAdapter(addFriendsActivity.this, R.layout.listview_item_row, friends);
                    ListView listView = (ListView)findViewById(R.id.listView);
                    listView.setAdapter(friendsArrayAdapter);
                    final DataSnapshot s = dataSnapshot;
                    listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                        @Override
                        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(addFriendsActivity.this);
                            builder.setTitle("Would you like to remove " + friends[position].getName() + " as a friend?");
                            final String uid = friends[position].getUid();
                            // Set up the buttons
                            builder.setPositiveButton("Remove", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // Remove from friends list
                                    try {
                                        ref.child("users").child(userID).child("friends").child(uid).removeValue();
                                    } catch(Exception e) {Log.v("Removing friend error", "Couldn't remove friend");}
                                    // Remove from their friends list or cancel request
                                    try {
                                        if(s.child(uid).hasChild("friends") && s.child(uid).child("friends").hasChild(userID))
                                            ref.child("users").child(uid).child("friends").child(userID).removeValue();
                                        else if(s.child(uid).hasChild("requests") && s.child(uid).child("requests").hasChild(userID))
                                            ref.child("users").child(uid).child("requests").child(userID).removeValue();
                                    } catch(Exception e) {Log.v("Removing friends error", "Couldn't remove you from their list");}
                                    app.setCircleSelected(app.getCircleSelected());
                                    addFriendsActivity.this.onResume();
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
                            return true;
                        }
                    });
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {

                }
            });
        } catch(Exception e) {Log.v("addFriendsActivity", e.toString());}
    }
}
