package sunglass.com.loco;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.firebase.client.AuthData;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.Map;

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
                    public void onAuthenticated(final AuthData authData) {
//                      Toast.makeText(getApplicationContext(), "Welcome, " + authData.getUid() + ", Password: " + authData.getProvider(), Toast.LENGTH_SHORT).show();

                        if (authData != null) {

                            ref.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot snapshot) {
                                    for (DataSnapshot d : snapshot.getChildren()) {
                                        Log.v("FireFire", d.getKey().toString() + " la" + authData.getUid());
                                        String curr = d.getKey().toString();
                                        if (curr.equals((String)authData.getUid())) {
                                            Map<String, Object> value = (Map<String, Object>) d.getValue();
                                            Toast.makeText(getApplicationContext(), "Welcome, " + (String)value.get("name") + "!", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(FirebaseError firebaseError) {
                                    // Do nothing.
                                }
                            });

                        }


//                        Toast.makeText(getApplicationContext(), "Welcome, " + authData.getUid() + "!", Toast.LENGTH_SHORT).show();

                        Intent i = new Intent(loginActivity.this, MapsActivity.class);
                        startActivity(i);
                    }

                    @Override
                    public void onAuthenticationError(FirebaseError firebaseError) {

                        switch (firebaseError.getCode()) {
                            case FirebaseError.INVALID_EMAIL:
                                Toast.makeText(getApplicationContext(), "Invalid Email", Toast.LENGTH_SHORT).show();
                                break;
                            case FirebaseError.USER_DOES_NOT_EXIST:
                                Toast.makeText(getApplicationContext(), "User Does Not Exist", Toast.LENGTH_SHORT).show();
                                break;
                            case FirebaseError.INVALID_PASSWORD:
                                Toast.makeText(getApplicationContext(), "Invalid Password", Toast.LENGTH_SHORT).show();
                                break;
                            default:
                                Toast.makeText(getApplicationContext(), "Log In Failed", Toast.LENGTH_SHORT).show();
                                break;
                        }

                    }
                });

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

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

}