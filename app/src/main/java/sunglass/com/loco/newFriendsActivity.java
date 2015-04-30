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

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.tokenautocomplete.TokenCompleteTextView;

import java.util.Map;

/**
 * Created by cmccord on 4/29/15.
 */
public class newFriendsActivity extends Activity {
    private boolean changesMade = false;
    private Firebase ref;
    private Application app;
    private Person[] people;
    private ArrayAdapter<Person> adapter;
    private ContactsCompletionView completionView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newfriends);

        Button saveButton = (Button) findViewById(R.id.save_button);
        saveButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(!changesMade) {
                    finish();
                }
                else {
                    finish();
                }
            }
        });
        app = (Application) this.getApplication();
        ref = app.getFirebaseRef();

        ref.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                people = new Person[(int) snapshot.getChildrenCount()];
                int i = 0;
                // MAKE SURE YOU CAN'T FRIEND YOURSELF
                for (DataSnapshot d : snapshot.getChildren()) {
                    String email;
                    try {email = d.child("email").getValue().toString();}catch(Exception e){email = "";}
                    String name = d.child("name").getValue().toString();
                    people[i] = new Person(name, email);
                    Log.v("Getting users", people[i].toString());
                    i++;
                }
                adapter = new ArrayAdapter<Person>(newFriendsActivity.this, android.R.layout.simple_list_item_1, people);
                completionView = (ContactsCompletionView)findViewById(R.id.searchView);
                completionView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                // Do nothing.
            }
        });

    }
}
