package sunglass.com.loco;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
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
import com.firebase.simplelogin.SimpleLoginCompletionHandler;
import com.firebase.simplelogin.enums.FirebaseSimpleLoginErrorCode;

import java.util.Map;

public class loginActivity extends Activity {

    private Firebase ref;

    private EditText mEmail;
    private EditText mDisplayName;
    private EditText mPassword;

    private String email_to_send;

    private InputMethodManager imm;

    private AuthData authData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        ref = ((Application) this.getApplication()).getFirebaseRef();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.red_screen);

        final SimpleLogin authClient = new SimpleLogin(ref, getApplicationContext());
        ((Application) this.getApplication()).setSimpleLogin(authClient);

        authClient.checkAuthStatus(new SimpleLoginAuthenticatedHandler() {
            @Override
            public void authenticated(FirebaseSimpleLoginError error, FirebaseSimpleLoginUser user) {
                if (error != null) {

                    Log.v("authenticated", "Check performance error: " + error);

                } else if (user == null) {

                    Log.v("authenticated", "No user logged in. the user may log in.");

                    setContentView(R.layout.activity_login);


                    Button mLoginButton = (Button) findViewById(R.id.loginButton);
                    mLoginButton.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {

                            mEmail = (EditText)findViewById(R.id.editEmail);
                            mDisplayName = (EditText)findViewById(R.id.editDisplayName);
                            mPassword = (EditText)findViewById(R.id.editPassword);

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

                                    }
                                    else {

                                        Log.d("authenticated", "Log In Successful");

                                        authData = ref.getAuth();

                                        ref.child("users").child(authData.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot snapshot) {
                                                Map<String, Object> value = (Map<String, Object>) snapshot.getValue();
                                                Toast.makeText(getApplicationContext(), "Welcome, " + (String)value.get("name") + "!", Toast.LENGTH_SHORT).show();
                                            }

                                            @Override
                                            public void onCancelled(FirebaseError firebaseError) {
                                                // Do nothing.
                                            }
                                        });

                                        Intent i = new Intent(loginActivity.this, MapsActivity.class);
                                        startActivity(i);

                                    }
                                }
                            });

//                            ref.authWithPassword(mEmail.getText().toString(), mPassword.getText().toString(), new Firebase.AuthResultHandler() {
//                                @Override
//                                public void onAuthenticated(final AuthData authData) {
//
//                                    if (authData != null) {
//
//                                        ref.child("users").child(authData.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
//                                            @Override
//                                            public void onDataChange(DataSnapshot snapshot) {
//                                                Map<String, Object> value = (Map<String, Object>) snapshot.getValue();
//                                                Toast.makeText(getApplicationContext(), "Welcome, " + (String)value.get("name") + "!", Toast.LENGTH_SHORT).show();
//                                            }
//
//                                            @Override
//                                            public void onCancelled(FirebaseError firebaseError) {
//                                                // Do nothing.
//                                            }
//                                        });
//
//                                    }
//
//                                    Intent i = new Intent(loginActivity.this, MapsActivity.class);
//                                    startActivity(i);
//                                }
//
//                                @Override
//                                public void onAuthenticationError(FirebaseError firebaseError) {
//
//                                    switch (firebaseError.getCode()) {
//                                        case FirebaseError.INVALID_EMAIL:
//                                            Toast.makeText(getApplicationContext(), "Invalid Email", Toast.LENGTH_SHORT).show();
//                                            break;
//                                        case FirebaseError.USER_DOES_NOT_EXIST:
//                                            Toast.makeText(getApplicationContext(), "User Does Not Exist", Toast.LENGTH_SHORT).show();
//                                            break;
//                                        case FirebaseError.INVALID_PASSWORD:
//                                            Toast.makeText(getApplicationContext(), "Invalid Password", Toast.LENGTH_SHORT).show();
//                                            break;
//                                        default:
//                                            Toast.makeText(getApplicationContext(), "Log In Failed", Toast.LENGTH_SHORT).show();
//                                            break;
//                                    }
//
//                                }
//                            });

                        }
                    });

                    Button mForgotButton = (Button) findViewById(R.id.forgot_butt);
                    mForgotButton.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {

                            AlertDialog.Builder alert = new AlertDialog.Builder(loginActivity.this);

                            alert.setTitle("Password Reset");
                            alert.setMessage("Enter Email:");

                            final EditText input = new EditText(loginActivity.this);
                            input.setGravity(Gravity.CENTER);
//                          input.setHint("Password");
                            input.setWidth(200);
                            input.requestFocus();
                            input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
//                          if (mEmail.getText() != null)
//                          input.setText(mEmail.getText().toString());
                            imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
//                          alert.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

                            alert.setView(input);

                            alert.setPositiveButton("Send Email", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {

                                    email_to_send = input.getText().toString();

                                    authClient.sendPasswordResetEmail(email_to_send, new SimpleLoginCompletionHandler() {
                                        @Override
                                        public void completed(FirebaseSimpleLoginError error, boolean success) {

                                            if (error != null) {

                                                Toast.makeText(getApplicationContext(), "User Does Not Exist", Toast.LENGTH_SHORT).show();

                                                LinearLayout our_layout = (LinearLayout) loginActivity.this.findViewById(R.id.the_lin_layout);
                                                our_layout.requestFocus();

                                            } else if (success) {
                                                imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);
//                                              imm.hideSoftInputFromWindow(input.getWindowToken(),0);

                                                Toast.makeText(getApplicationContext(), "Success! Please check your email.", Toast.LENGTH_SHORT).show();

                                                LinearLayout our_layout = (LinearLayout) loginActivity.this.findViewById(R.id.the_lin_layout);
                                                our_layout.requestFocus();
                                            }
                                        }
                                    });

                                }
                            });

                            alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    // Do nothing.

                                    imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT,0);
//                                  imm.hideSoftInputFromWindow(input.getWindowToken(),0);

                                    LinearLayout our_layout = (LinearLayout) loginActivity.this.findViewById(R.id.the_lin_layout);
                                    our_layout.requestFocus();
                                }
                            });

                            alert.show();

                        }
                    });

                    Button mNewUserButton = (Button) findViewById(R.id.newUserButton);
                    mNewUserButton.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            Intent i = new Intent(loginActivity.this, newUserActivity.class);
                            startActivity(i);
                        }
                    });



                } else {
                    Log.v("authenticated", "user is logged in");

                    Intent i = new Intent(loginActivity.this, MapsActivity.class);
                    startActivity(i);
                }
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