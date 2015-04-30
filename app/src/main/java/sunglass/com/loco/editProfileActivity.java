package sunglass.com.loco;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.method.KeyListener;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.AuthData;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.firebase.simplelogin.FirebaseSimpleLoginError;
import com.firebase.simplelogin.SimpleLogin;
import com.firebase.simplelogin.SimpleLoginCompletionHandler;

import java.util.Map;


public class editProfileActivity extends Activity {

    private EditText mEmail;
    private EditText mDisplayName;
    private EditText mPassword;

    private TextView mDescripText;

    private Button mRemoveUser;
    private Button mLogoutButton;
    private Button mSaveChangesButton;
    private Button mEditButton;
    private Button mCancelEditButton;

    private String value;
    private String orig_display_name;
    private String orig_email;

    private AuthData authData;

    private InputMethodManager imm;

    private Firebase ref;

    private SimpleLogin authClient;

//    private Application app;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

//        app = (Application) this.getApplication();

        mEmail = (EditText)findViewById(R.id.editEmail);
        mDisplayName = (EditText)findViewById(R.id.editDisplayName);
        mPassword = (EditText)findViewById(R.id.editPassword);
        mPassword.setVisibility(View.GONE);
        mDescripText = (TextView)findViewById(R.id.descrip_text);
        mRemoveUser = (Button) findViewById(R.id.removeUser_butt);
        mLogoutButton = (Button) findViewById(R.id.logoutButton);
        mSaveChangesButton = (Button) findViewById(R.id.save_changes_butt);
        mEditButton = (Button) findViewById(R.id.edit_butt);
        mCancelEditButton = (Button) findViewById(R.id.cancel_edit_butt);

//        mEmail.setKeyListener(null);
        mEmail.setClickable(false);
        mEmail.setCursorVisible(false);
        mEmail.setFocusable(false);
        mEmail.setFocusableInTouchMode(false);

//        mDisplayName.setKeyListener(null);
        mDisplayName.setClickable(false);
        mDisplayName.setCursorVisible(false);
        mDisplayName.setFocusable(false);
        mDisplayName.setFocusableInTouchMode(false);

        mCancelEditButton.setVisibility(View.GONE);

        ref = ((Application) this.getApplication()).getFirebaseRef();
        authClient = ((Application) this.getApplication()).getSimpleLoginRef();

        authData = ref.getAuth();
        if (authData != null) {

//            mEmail.setHint(""+authData.getProviderData().get("email"));

            ref.child("users").child(authData.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    Map<String, Object> value = (Map<String, Object>) snapshot.getValue();
                    orig_display_name = (String) value.get("name");
                    orig_email = (String) value.get("email");
                    mDisplayName.setHint(orig_display_name);
                    mEmail.setHint(orig_email);
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {
                    // Do nothing.
                }
            });

        } else {
            Toast.makeText(getApplicationContext(), "You are not authenticated; log in again.", Toast.LENGTH_SHORT).show();
            Intent i = new Intent(editProfileActivity.this, loginActivity.class);
            startActivity(i);
        }

        Button backButton = (Button) findViewById(R.id.back_butt);
        backButton.setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {
                        finish();
                    }
                }
        );

        mEditButton.setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {

                        AlertDialog.Builder alert = new AlertDialog.Builder(editProfileActivity.this);

                        alert.setTitle("Verify Password:");

                        final EditText input = new EditText(editProfileActivity.this);
                        input.setGravity(Gravity.CENTER);
//                        input.setHint("Password");
                        input.setWidth(200);
                        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                        input.requestFocus();
                        input.setImeOptions(EditorInfo.IME_ACTION_DONE);
                        imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
//                        alert.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

                        alert.setView(input);

                        alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {

                                value = input.getText().toString();

                                if (authData != null) {

                                    authClient.changePassword(orig_email, value, value, new SimpleLoginCompletionHandler() {
                                        public void completed(FirebaseSimpleLoginError error, boolean success) {
                                            if(error != null) {
                                                Toast.makeText(getApplicationContext(), "Invalid Password", Toast.LENGTH_SHORT).show();

                                                imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT,0);
//                                                  imm.hideSoftInputFromWindow(input.getWindowToken(),0);
                                            }
                                            else if (success) {

                                                imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT,0);
//                                                  imm.hideSoftInputFromWindow(input.getWindowToken(),0);

                                                Toast.makeText(getApplicationContext(), "Password Verified", Toast.LENGTH_SHORT).show();

                                                mEditButton.setVisibility(View.GONE);
                                                mCancelEditButton.setVisibility(View.VISIBLE);

                                                mDescripText.setVisibility(View.GONE);

                                                mPassword.setVisibility(View.VISIBLE);

                                                mLogoutButton.setVisibility(View.GONE);
                                                mSaveChangesButton.setVisibility(View.VISIBLE);

                                                mRemoveUser.setVisibility(View.VISIBLE);

//                                                  mEmail.setKeyListener((KeyListener) mEmail.getTag());
                                                mEmail.setClickable(true);
                                                mEmail.setCursorVisible(true);
                                                mEmail.setFocusable(true);
                                                mEmail.setFocusableInTouchMode(true);

//                                                  mDisplayName.setKeyListener((KeyListener) mDisplayName.getTag());
                                                mDisplayName.setClickable(true);
                                                mDisplayName.setCursorVisible(true);
                                                mDisplayName.setFocusable(true);
                                                mDisplayName.setFocusableInTouchMode(true);

                                                mEmail.setText("");
                                                mDisplayName.setText("");
                                                mPassword.setText("");

                                                mEmail.setHint("Update Email");
                                                mDisplayName.setHint("Update Display Name");
                                                mPassword.setHint("Update Password");

//                                                  mEmail.clearFocus();
//                                                  mDisplayName.clearFocus();
//                                                  mPassword.clearFocus();

                                                LinearLayout our_layout = (LinearLayout) editProfileActivity.this.findViewById(R.id.our_lin_layout);
                                                our_layout.requestFocus();

                                                mRemoveUser.setOnClickListener(
                                                        new Button.OnClickListener() {
                                                            public void onClick(View v) {

                                                                AlertDialog.Builder remove_alert = new AlertDialog.Builder(editProfileActivity.this);

                                                                remove_alert.setTitle("Are you sure?");
                                                                remove_alert.setMessage("Deleting your profile will completely remove your profile from Flare. This cannot be undone.");

                                                                remove_alert.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                                                                    public void onClick(DialogInterface dialog, int whichButton) {

                                                                        Intent intent = new Intent(editProfileActivity.this, LocationShareReceiver.class);
                                                                        intent.setAction("sunglass.com.loco.LOCATION_SHARE");
                                                                        PendingIntent pi = PendingIntent.getBroadcast(editProfileActivity.this, 0,
                                                                                intent, PendingIntent.FLAG_NO_CREATE);
                                                                        boolean alarmUp = (pi != null);
                                                                        if(alarmUp) {
                                                                            LocationShareReceiver alarm = new LocationShareReceiver();
                                                                            alarm.CancelAlarm(editProfileActivity.this);
                                                                            pi.cancel();
                                                                        }

                                                                        authClient.removeUser(orig_email, value, new SimpleLoginCompletionHandler() {
                                                                            public void completed(FirebaseSimpleLoginError error, boolean success) {
                                                                                if (error != null) {
                                                                                    Toast.makeText(getApplicationContext(), "You are not authenticated; log in again.", Toast.LENGTH_SHORT).show();
                                                                                    Intent i = new Intent(editProfileActivity.this, loginActivity.class);
                                                                                    startActivity(i);
                                                                                }
                                                                                else if (success) {

                                                                                    authClient.logout();

                                                                                    Toast.makeText(getApplicationContext(), "Profile deleted. Rejoin the fire soon!", Toast.LENGTH_SHORT).show();

                                                                                    ref.child("users").child(authData.getUid()).removeValue();

                                                                                    Intent i = new Intent(editProfileActivity.this, loginActivity.class);
                                                                                    startActivity(i);
                                                                                }
                                                                            }
                                                                        });

                                                                    }
                                                                });

                                                                remove_alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                                                    public void onClick(DialogInterface dialog, int whichButton) {
                                                                        // Do nothing.
                                                                    }
                                                                });

                                                                remove_alert.show();

                                                            }
                                                        }
                                                );

                                                mSaveChangesButton.setOnClickListener(
                                                        new Button.OnClickListener() {
                                                            public void onClick(View v) {

                                                                // ONLY DISPLAY NAME CHANGES.
                                                                if (mEmail.getText().toString().equals("") && !mDisplayName.getText().toString().equals("") && mPassword.getText().toString().equals("")) {

                                                                    ref.child("users").child(authData.getUid()).child("name").setValue(mDisplayName.getText().toString());

                                                                    Toast.makeText(getApplicationContext(), "Success! Display Name Changed", Toast.LENGTH_SHORT).show();

                                                                }

                                                                // ONLY PASSWORD CHANGES.
                                                                if (mEmail.getText().toString().equals("") && mDisplayName.getText().toString().equals("") && !mPassword.getText().toString().equals("")) {

                                                                    authClient.changePassword(orig_email, value, mPassword.getText().toString(), new SimpleLoginCompletionHandler() {
                                                                        public void completed(FirebaseSimpleLoginError error, boolean success) {
                                                                            if (error != null) {
                                                                                Toast.makeText(getApplicationContext(), "Failure; Password Not Changed", Toast.LENGTH_SHORT).show();
                                                                            }
                                                                            else if (success) {
                                                                                Toast.makeText(getApplicationContext(), "Success! Password Changed", Toast.LENGTH_SHORT).show();
                                                                            }
                                                                        }
                                                                    });

                                                                }

                                                                // BOTH DISPLAY NAME AND PASSWORD CHANGE.
                                                                if (mEmail.getText().toString().equals("") && !mDisplayName.getText().toString().equals("") && !mPassword.getText().toString().equals("")) {

                                                                    ref.child("users").child(authData.getUid()).child("name").setValue(mDisplayName.getText().toString());

                                                                    Toast.makeText(getApplicationContext(), "Success! Display Name Changed", Toast.LENGTH_SHORT).show();

                                                                    authClient.changePassword(orig_email, value, mPassword.getText().toString(), new SimpleLoginCompletionHandler() {
                                                                        public void completed(FirebaseSimpleLoginError error, boolean success) {
                                                                            if (error != null) {
                                                                                Toast.makeText(getApplicationContext(), "Failure; Password Not Changed", Toast.LENGTH_SHORT).show();
                                                                            }
                                                                            else if (success) {
                                                                                Toast.makeText(getApplicationContext(), "Success! Password Changed", Toast.LENGTH_SHORT).show();
                                                                            }
                                                                        }
                                                                    });

                                                                }

                                                                // Update display name and email and reset hints.
                                                                ref.child("users").child(authData.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                                                    @Override
                                                                    public void onDataChange(DataSnapshot snapshot) {
                                                                        Map<String, Object> value = (Map<String, Object>) snapshot.getValue();
                                                                        orig_display_name = (String) value.get("name");
                                                                        orig_email = (String) value.get("email");
                                                                        mDisplayName.setHint(orig_display_name);
                                                                        mEmail.setHint(orig_email);
                                                                    }

                                                                    @Override
                                                                    public void onCancelled(FirebaseError firebaseError) {
                                                                        // Do nothing.
                                                                    }
                                                                });

                                                                mEditButton.setVisibility(View.VISIBLE);
                                                                mCancelEditButton.setVisibility(View.GONE);

                                                                mDescripText.setVisibility(View.VISIBLE);

                                                                mRemoveUser.setVisibility(View.GONE);
                                                                mPassword.setVisibility(View.GONE);

                                                                mLogoutButton.setVisibility(View.VISIBLE);
                                                                mSaveChangesButton.setVisibility(View.GONE);

                                                                mEmail.setText("");
                                                                mDisplayName.setText("");
                                                                mPassword.setText("");

//                                                                  mEmail.setKeyListener(null);
                                                                mEmail.setClickable(false);
                                                                mEmail.setCursorVisible(false);
                                                                mEmail.setFocusable(false);
                                                                mEmail.setFocusableInTouchMode(false);

//                                                                  mDisplayName.setKeyListener(null);
                                                                mDisplayName.setClickable(false);
                                                                mDisplayName.setCursorVisible(false);
                                                                mDisplayName.setFocusable(false);
                                                                mDisplayName.setFocusableInTouchMode(false);
                                                            }
                                                        }
                                                );

                                            }
                                        }
                                    });

                                } else {
                                    Toast.makeText(getApplicationContext(), "You are not authenticated; log in again.", Toast.LENGTH_SHORT).show();
                                    Intent i = new Intent(editProfileActivity.this, loginActivity.class);
                                    startActivity(i);
                                }

                            }
                        });

                        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                // Do nothing.

                                imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT,0);
//                                  imm.hideSoftInputFromWindow(input.getWindowToken(),0);
                            }
                        });

                        alert.show();
                    }
                }
        );

        mCancelEditButton.setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {
                        mEditButton.setVisibility(View.VISIBLE);
                        mCancelEditButton.setVisibility(View.GONE);

                        mDescripText.setVisibility(View.VISIBLE);

                        mPassword.setVisibility(View.GONE);

                        mLogoutButton.setVisibility(View.VISIBLE);
                        mSaveChangesButton.setVisibility(View.GONE);

                        mRemoveUser.setVisibility(View.GONE);

                        mEmail.setHint(orig_email);
                        mDisplayName.setHint(orig_display_name);

//                                                            mEmail.setKeyListener(null);
                        mEmail.setClickable(false);
                        mEmail.setCursorVisible(false);
                        mEmail.setFocusable(false);
                        mEmail.setFocusableInTouchMode(false);

//                                                            mDisplayName.setKeyListener(null);
                        mDisplayName.setClickable(false);
                        mDisplayName.setCursorVisible(false);
                        mDisplayName.setFocusable(false);
                        mDisplayName.setFocusableInTouchMode(false);
                    }
                }
        );

        mLogoutButton.setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {
                        Intent intent = new Intent(editProfileActivity.this, LocationShareReceiver.class);
                        intent.setAction("sunglass.com.loco.LOCATION_SHARE");
                        PendingIntent pi = PendingIntent.getBroadcast(editProfileActivity.this, 0,
                                intent, PendingIntent.FLAG_NO_CREATE);
                        boolean alarmUp = (pi != null);
                        if(alarmUp) {
                            LocationShareReceiver alarm = new LocationShareReceiver();
                            alarm.CancelAlarm(editProfileActivity.this);
                            pi.cancel();
                        }

                        authClient.logout();

                        Toast.makeText(getApplicationContext(), "Come Back Soon!", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(editProfileActivity.this, loginActivity.class);
                        startActivity(i);
                    }
                }
        );
    }

}
