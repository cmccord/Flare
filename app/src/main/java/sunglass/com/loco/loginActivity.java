package sunglass.com.loco;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;


public class loginActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Button mLoginButton = (Button) findViewById(R.id.loginButton);
        mLoginButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //updateLocation();
                Intent i = new Intent(loginActivity.this, MapsActivity.class);
                startActivity(i);
            }
        });

        Button mNewUserButton = (Button) findViewById(R.id.newUserButton);
        mNewUserButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //updateLocation();
                Intent i = new Intent(loginActivity.this, newUserActivity.class);
                startActivity(i);
            }
        });
    }

}