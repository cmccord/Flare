package sunglass.com.loco;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

/**
 * Created by cmccord on 4/29/15.
 */
public class addFriendsActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addfriends);

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

        // get array of friend names
        String[] friends = {"Bob", "Kyle", "Joe", "Chris"};
        ArrayAdapter<String> friendsArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, friends);

        ListView listView = (ListView)findViewById(R.id.listView);
        listView.setAdapter(friendsArrayAdapter);

    }
}
