package sunglass.com.loco;

import android.app.Activity;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.MultiAutoCompleteTextView;
import android.widget.Toast;

import com.firebase.client.AuthData;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.tokenautocomplete.TokenCompleteTextView;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by cmccord on 4/29/15.
 */
public class newFriendsActivity extends Activity {
    private Firebase ref;
    private Application app;
    private Person[] people;
    private ArrayAdapter<Person> adapter;
    private ContactsCompletionView completionView;
    private AuthData authData;
    private String userID;
    private Button saveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newfriends);
        app = (Application) this.getApplication();
        ref = app.getFirebaseRef();
        if(ref != null) {
            authData = ref.getAuth();
            if(authData != null)
                userID = authData.getUid();
        }

        saveButton = (Button) findViewById(R.id.save_button);

        if(ref != null && authData != null) {
            ref.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    boolean hasFriends = snapshot.child(userID).hasChild("friends");
                    int numFriends = 0;
                    if(hasFriends)
                        numFriends = (int) snapshot.child(userID).child("friends").getChildrenCount();
                    people = new Person[(int) snapshot.getChildrenCount() - 1 - numFriends];
                    int i = 0;
                    // MAKE SURE YOU CAN'T FRIEND YOURSELF OR A CURRENT FRIEND
                    for (DataSnapshot d : snapshot.getChildren()) {
                        if(d.getKey().equals(userID))
                            continue;
                        if(hasFriends && snapshot.child(userID).child("friends").hasChild(d.getKey()))
                            continue;
                        String email;
                        try {
                            email = d.child("email").getValue().toString();
                        } catch (Exception e) {
                            email = "";
                        }
                        String name = d.child("name").getValue().toString();
                        String uid = d.getKey();
                        people[i] = new Person(name, email);
                        people[i].setUid(uid);
                        Log.v("Getting users", people[i].toString());
                        i++;
                    }
                    adapter = new ArrayAdapter<Person>(newFriendsActivity.this, android.R.layout.simple_list_item_1, people);
                    completionView = (ContactsCompletionView) findViewById(R.id.searchView);
                    completionView.setAdapter(adapter);
                    final DataSnapshot s = snapshot;

                    saveButton.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            try {
                                Map friends = new HashMap<>();
                                int numFriendsAdded = 0;
                                for (Object token : completionView.getObjects()) {
                                    String uid = ((Person) token).getUid();
                                    // make sure its a real user
                                    if (!s.hasChild(uid))
                                        continue;
                                    friends.put(uid, uid);
                                    app.trackUser(uid);
                                    if (!s.child(uid).hasChild("friends") || !s.child(uid).child("friends").hasChild(userID)) {
                                        Map requests;
                                        if (s.child(uid).hasChild("requests"))
                                            requests = (Map) s.child(uid).child("requests").getValue();
                                        else
                                            requests = new HashMap<>();
                                        requests.put(userID, userID);
                                        Map update = new HashMap<>();
                                        update.put("requests", requests);
                                        ref.child("users").child(uid).updateChildren(update);

                                    }
                                    if (s.child(userID).hasChild("requests") && s.child(userID).child("requests").hasChild(uid))
                                        ref.child("users").child(userID).child("requests").child(uid).removeValue();
                                    Log.v("Adding Friend", uid);
                                    numFriendsAdded++;
                                }
                                if (s.child(userID).hasChild("friends")) {
                                    ref.child("users").child(userID).child("friends").updateChildren(friends);
                                } else {
                                    Map update = new HashMap<>();
                                    update.put("friends", friends);
                                    ref.child("users").child(userID).updateChildren(update);
                                }
                                if (numFriendsAdded > 1)
                                    Toast.makeText(getApplicationContext(), "Friends Added!", Toast.LENGTH_SHORT).show();
                                else if (numFriendsAdded > 0)
                                    Toast.makeText(getApplicationContext(), "Friend Added!", Toast.LENGTH_SHORT).show();
                            } catch(Exception e) {
                                Log.v("Error adding friends", e.toString());
                                Toast.makeText(getApplicationContext(), "Could not add friends", Toast.LENGTH_SHORT).show();
                            }
                            finish();
                        }
                    });
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {
                    // Do nothing.
                }
            });
        }

    }
}
