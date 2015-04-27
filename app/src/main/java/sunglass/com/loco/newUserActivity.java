package sunglass.com.loco;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

import java.util.HashMap;
import java.util.Map;

public class newUserActivity extends Activity {

//    private String mImei;
    private Firebase ref;
    private EditText mEmail;
    private EditText mDisplayName;
    private EditText mPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_user);

        Firebase.setAndroidContext(this);

        Button mBackButton = (Button) findViewById(R.id.back_butt);
        mBackButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //updateLocation();
                Intent i = new Intent(newUserActivity.this, loginActivity.class);
                startActivity(i);
            }
        });

        Button mFinishNewUserButton = (Button) findViewById(R.id.finishNewUser);
        mFinishNewUserButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //updateLocation();

                new AlertDialog.Builder(newUserActivity.this)
                        .setTitle("Join the Fire")
                        .setMessage("All Set?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                                mEmail = (EditText)findViewById(R.id.editEmail);
                                mDisplayName = (EditText)findViewById(R.id.editDisplayName);
                                mPassword = (EditText)findViewById(R.id.editPassword);

                                ref = new Firebase("https://loco-android.firebaseio.com");
                                ref.createUser(mEmail.getText().toString(), mPassword.getText().toString(), new Firebase.ValueResultHandler<Map<String, Object>>() {
                                    @Override
                                    public void onSuccess(Map<String, Object> result) {

//                                        TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
//                                        mImei = telephonyManager.getDeviceId();

                                        if (ref != null) {
                                            Map user = new HashMap<>();
                                            Map dets = new HashMap<>();
                                            dets.put("pos", "");
                                            dets.put("name", mDisplayName.getText().toString());
                                            dets.put("timestamp", System.currentTimeMillis());
                                            dets.put("time_created", System.currentTimeMillis());
                                            user.put(result.get("uid"), dets);

                                            ref.child("users").updateChildren(user);
                                        }

                                        Toast.makeText(getApplicationContext(), "Success! uid: " + result.get("uid"), Toast.LENGTH_SHORT).show();

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
                                    }

                                    @Override
                                    public void onError(FirebaseError firebaseError) {
                                        Log.v("ERRORERROR","ERRORERROR"+firebaseError.getCode());
                                        switch (firebaseError.getCode()) {
                                            case -18:
                                                Toast.makeText(getApplicationContext(), "Email Already in Use", Toast.LENGTH_SHORT).show();
                                                break;
                                            default:
                                                Toast.makeText(getApplicationContext(), "Create New User Failed", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });

                                Intent i = new Intent(newUserActivity.this, MapsActivity.class);
                                startActivity(i);
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // Do nothing.
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();

//                Intent i = new Intent(newUserActivity.this, MapsActivity.class);
//                startActivity(i);
            }
        });

    }

}