package sunglass.com.loco;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.firebase.client.AuthData;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by cmccord on 5/1/15.
 */
public class NewCircleActivity extends Activity {
    private Firebase ref;
    private Application app;
    private Person[] people;
    private ContactsCompletionView completionView;
    private AuthData authData;
    private String userID;
    private Button saveButton;
    private EditText circleName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newcircle);
        app = (Application) this.getApplication();
        ref = app.getFirebaseRef();
        if(ref != null) {
            authData = ref.getAuth();
            if(authData != null)
                userID = authData.getUid();
        }

        saveButton = (Button) findViewById(R.id.save_button);
        circleName = (EditText) findViewById(R.id.circleName);

        if(ref != null && authData != null) {
            ref.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    boolean hasFriends = snapshot.child(userID).hasChild("friends");
                    if(!hasFriends) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(NewCircleActivity.this);
                        builder.setTitle("Add friends first!");

                        // Set up the buttons
                        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                NewCircleActivity.this.finish();
                            }
                        });
                        builder.show();
                    }
                    else {
                        int numFriends = (int) snapshot.child(userID).child("friends").getChildrenCount();
                        people = new Person[numFriends];
                        int i = 0;
                        DataSnapshot s = snapshot.child(userID).child("friends");
                        for (DataSnapshot d : s.getChildren()) {
                            String uid = d.getKey();
                            String email;
                            try {
                                email = snapshot.child(uid).child("email").getValue().toString();
                            } catch (Exception e) {
                                email = "";
                            }
                            String name = snapshot.child(uid).child("name").getValue().toString();
                            people[i] = new Person(name, email);
                            people[i].setUid(uid);
                            if(snapshot.child(uid).hasChild("picture"))
                                people[i].setImage(Application.decodeBase64(snapshot.child(uid).child("picture").getValue().toString()));
                            Log.v("Getting friends", people[i].toString());
                            i++;
                        }
                        PersonAdapter adapter = new PersonAdapter(NewCircleActivity.this, R.layout.listview_item_row, people);
                        //adapter = new ArrayAdapter<Person>(NewCircleActivity.this, android.R.layout.simple_list_item_1, people);
                        completionView = (ContactsCompletionView) findViewById(R.id.searchView);
                        completionView.setAdapter(adapter);
                        final DataSnapshot s1 = snapshot;
                        saveButton.setOnClickListener(new View.OnClickListener() {
                            public void onClick(View v) {
                                if(circleName.getText().toString().matches("([0-9]|[a-z]|[A-Z]| |_)+") && circleName.getText().toString().length() <= 15) {
                                    try {
                                        Map friends = new HashMap<>();
                                        int numFriendsAdded = 0;
                                        for (Object token : completionView.getObjects()) {
                                            String uid = ((Person) token).getUid();
                                            // make sure its a real user
                                            if (!s1.hasChild(uid))
                                                continue;
                                            friends.put(uid, uid);
                                            Log.v("Adding Friend to Circle", uid);
                                            numFriendsAdded++;
                                        }
                                        Map circle = new HashMap<>();
                                        circle.put(circleName.getText().toString(), friends);
                                        if (s1.child(userID).hasChild("circles")) {
                                            ref.child("users").child(userID).child("circles").updateChildren(circle);
                                        } else {
                                            Map update = new HashMap<>();
                                            update.put("circles", circle);
                                            ref.child("users").child(userID).updateChildren(update);
                                        }
                                        if (numFriendsAdded > 0)
                                            Toast.makeText(getApplicationContext(), "New Circle Created!", Toast.LENGTH_SHORT).show();
                                    } catch (Exception e) {
                                        Log.v("Error creating circle", e.toString());
                                        Toast.makeText(getApplicationContext(), "Could not create circle", Toast.LENGTH_SHORT).show();
                                    }
                                    finish();
                                }
                                else if (circleName.getText().toString().length()==0) {
                                    Toast.makeText(getApplicationContext(), "Circle Name Must Have At Least One Character", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(getApplicationContext(), "Invalid Circle Name", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {
                    // Do nothing.
                }
            });
        }

    }
}
