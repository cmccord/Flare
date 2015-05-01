package sunglass.com.loco;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;


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
                startActivity(i);
            }
        });

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
                        Person person = new Person(name, email);
                        person.setUid(d.getKey());
                        friends[i] = person;
                        i++;
                    }
                    PersonAdapter friendsArrayAdapter = new PersonAdapter(addFriendsActivity.this, R.layout.listview_item_row, friends);
                    ListView listView = (ListView)findViewById(R.id.listView);
                    listView.setAdapter(friendsArrayAdapter);
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {

                }
            });
        } catch(Exception e) {Log.v("addFriendsActivity", e.toString());}

        // get array of friend names
        //String[] friends = {"Bob", "Kyle", "Joe", "Chris"};
        //ArrayAdapter<String> friendsArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, friends);
//        PersonAdapter friendsArrayAdapter = new PersonAdapter(this, R.layout.listview_item_row, friends);
//        ListView listView = (ListView)findViewById(R.id.listView);
//        listView.setAdapter(friendsArrayAdapter);

    }
}
