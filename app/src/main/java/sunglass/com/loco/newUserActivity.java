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
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.firebase.simplelogin.FirebaseSimpleLoginError;
import com.firebase.simplelogin.FirebaseSimpleLoginUser;
import com.firebase.simplelogin.SimpleLogin;
import com.firebase.simplelogin.SimpleLoginAuthenticatedHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class newUserActivity extends Activity {

    private Firebase ref;
    private SimpleLogin authClient;
    private AuthData authData;

    private EditText mEmail;
    private EditText mDisplayName;
    private EditText mPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_user);

        ref = ((Application) this.getApplication()).getFirebaseRef();
        authClient = ((Application) this.getApplication()).getSimpleLoginRef();

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
                        .setTitle("All Set?")

                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                                mEmail = (EditText) findViewById(R.id.editEmail);
                                mDisplayName = (EditText) findViewById(R.id.editDisplayName);
                                mPassword = (EditText) findViewById(R.id.editPassword);

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
                                            dets.put("email", mEmail.getText().toString());
                                            dets.put("friends", new HashMap<String, String>());
                                            user.put(result.get("uid"), dets);

                                            ref.child("users").updateChildren(user);
                                        }

//                                        Toast.makeText(getApplicationContext(), "Success! uid: " + result.get("uid"), Toast.LENGTH_SHORT).show();

                                        authClient.loginWithEmail(mEmail.getText().toString(), mPassword.getText().toString(), new SimpleLoginAuthenticatedHandler() {
                                            @Override
                                            public void authenticated(FirebaseSimpleLoginError error, FirebaseSimpleLoginUser user) {
                                                if (error != null) {

                                                    switch (error.getCode()) {
                                                        case InvalidEmail:
                                                            Toast.makeText(getApplicationContext(), "Invalid Email", Toast.LENGTH_SHORT).show();
                                                            break;
                                                        case UserDoesNotExist:
                                                            Toast.makeText(getApplicationContext(), "User Does Not Exist", Toast.LENGTH_SHORT).show();
                                                            break;
                                                        case InvalidPassword:
                                                            Toast.makeText(getApplicationContext(), "Invalid Password", Toast.LENGTH_SHORT).show();
                                                            break;
                                                        default:
                                                            Toast.makeText(getApplicationContext(), "Log In Failed", Toast.LENGTH_SHORT).show();
                                                            break;
                                                    }

                                                } else {

                                                    Log.d("authenticated", "Log In Successful");

                                                    authData = ref.getAuth();

                                                    ref.child("users").child(authData.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(DataSnapshot snapshot) {
                                                            Map<String, Object> value = (Map<String, Object>) snapshot.getValue();
                                                            Toast.makeText(getApplicationContext(), "Welcome to Flare, " + (String) value.get("name") + "!", Toast.LENGTH_SHORT).show();
                                                        }

                                                        @Override
                                                        public void onCancelled(FirebaseError firebaseError) {
                                                            // Do nothing.
                                                        }
                                                    });

                                                    Intent i = new Intent(newUserActivity.this, MapsActivity.class);
                                                    startActivity(i);

                                                }
                                            }
                                        });
                                    }

                                    @Override
                                    public void onError(FirebaseError firebaseError) {
                                        Log.v("ERRORERROR", "ERRORERROR" + firebaseError.getCode());
                                        switch (firebaseError.getCode()) {
                                            case FirebaseError.EMAIL_TAKEN:
                                                Toast.makeText(getApplicationContext(), "Email Already in Use", Toast.LENGTH_SHORT).show();
                                                break;
                                            case FirebaseError.INVALID_EMAIL:
                                                Toast.makeText(getApplicationContext(), "Invalid Email", Toast.LENGTH_SHORT).show();
                                                break;
                                            default:
                                                Toast.makeText(getApplicationContext(), "Create New User Failed", Toast.LENGTH_SHORT).show();
                                                break;
                                        }
                                    }
                                });
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // Do nothing.
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();

            }
        });

    }

}