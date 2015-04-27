package sunglass.com.loco;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

public class loginActivity extends Activity {

    private Firebase ref;
    private EditText mEmail;
    private EditText mDisplayName;
    private EditText mPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Firebase.setAndroidContext(this);

        Button mLoginButton = (Button) findViewById(R.id.loginButton);
        mLoginButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                mEmail = (EditText)findViewById(R.id.editEmail);
                mDisplayName = (EditText)findViewById(R.id.editDisplayName);
                mPassword = (EditText)findViewById(R.id.editPassword);

                ref = new Firebase("https://loco-android.firebaseio.com");

                ref.authWithPassword(mEmail.getText().toString(), mPassword.getText().toString(), new Firebase.AuthResultHandler() {
                    @Override
                    public void onAuthenticated(AuthData authData) {
                        Toast.makeText(getApplicationContext(), "User ID: " + authData.getUid() + ", Password: " + authData.getProvider(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onAuthenticationError(FirebaseError firebaseError) {
                        Toast.makeText(getApplicationContext(), "Log In Failed", Toast.LENGTH_SHORT).show();
                    }
                });

                Intent i = new Intent(loginActivity.this, MapsActivity.class);
                startActivity(i);
            }
        });

        Button mNewUserButton = (Button) findViewById(R.id.newUserButton);
        mNewUserButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent i = new Intent(loginActivity.this, newUserActivity.class);
                startActivity(i);
            }
        });
    }

}