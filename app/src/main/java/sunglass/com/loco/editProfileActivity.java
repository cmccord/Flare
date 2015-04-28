package sunglass.com.loco;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.method.KeyListener;
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


public class editProfileActivity extends Activity {

    private EditText mEmail;
    private EditText mDisplayName;
    private EditText mPassword;
    private Button mRemoveUser;
    private Button mLogoutButton;
    private Button mSaveChangesButton;
    private AuthData authData;

    private Application app;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        Firebase.setAndroidContext(this);

        app = (Application) this.getApplication();

        mEmail = (EditText)findViewById(R.id.editEmail);
        mDisplayName = (EditText)findViewById(R.id.editDisplayName);
        mPassword = (EditText)findViewById(R.id.editPassword);
        mRemoveUser = (Button) findViewById(R.id.removeUser_butt);
        mLogoutButton = (Button) findViewById(R.id.logoutButton);
        mSaveChangesButton = (Button) findViewById(R.id.save_changes_butt);

        mEmail.setKeyListener(null);
        mEmail.setClickable(false);
        mEmail.setCursorVisible(false);
        mEmail.setFocusable(false);
        mEmail.setFocusableInTouchMode(false);

        mDisplayName.setKeyListener(null);
        mDisplayName.setClickable(false);
        mDisplayName.setCursorVisible(false);
        mDisplayName.setFocusable(false);
        mDisplayName.setFocusableInTouchMode(false);

        Firebase ref = new Firebase("https://loco-android.firebaseio.com");

        authData = ref.getAuth();
        if (authData != null) {

            mEmail.setText(""+authData.getProviderData().get("email"));

            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    for (DataSnapshot d : snapshot.getChildren()) {
//                        Log.v("Firebase Test", d.getKey().toString());
                        String curr = d.getKey().toString();
                        if (curr.equals(authData.getUid())) {
                            Map<String, Object> value = (Map<String, Object>) snapshot.getValue();
                            mDisplayName.setText((String)value.get("name"));
                        }
                    }
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

        Button editButton = (Button) findViewById(R.id.edit_butt);
        editButton.setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {
                        mRemoveUser.setVisibility(View.GONE);
                        mPassword.setVisibility(View.VISIBLE);

                        mLogoutButton.setVisibility(View.GONE);
                        mSaveChangesButton.setVisibility(View.VISIBLE);

                        mEmail.setKeyListener((KeyListener)mEmail.getTag());
                        mEmail.setClickable(true);
                        mEmail.setCursorVisible(true);
                        mEmail.setFocusable(true);
                        mEmail.setFocusableInTouchMode(true);

                        mDisplayName.setKeyListener((KeyListener)mDisplayName.getTag());
                        mDisplayName.setClickable(true);
                        mDisplayName.setCursorVisible(true);
                        mDisplayName.setFocusable(true);
                        mDisplayName.setFocusableInTouchMode(true);

                        mSaveChangesButton.setOnClickListener(
                                new Button.OnClickListener() {
                                    public void onClick(View v) {
                                        mRemoveUser.setVisibility(View.VISIBLE);
                                        mPassword.setVisibility(View.GONE);

                                        mLogoutButton.setVisibility(View.VISIBLE);
                                        mSaveChangesButton.setVisibility(View.GONE);

                                        mEmail.setKeyListener(null);
                                        mEmail.setClickable(false);
                                        mEmail.setCursorVisible(false);
                                        mEmail.setFocusable(false);
                                        mEmail.setFocusableInTouchMode(false);

                                        mDisplayName.setKeyListener(null);
                                        mDisplayName.setClickable(false);
                                        mDisplayName.setCursorVisible(false);
                                        mDisplayName.setFocusable(false);
                                        mDisplayName.setFocusableInTouchMode(false);
                                    }
                                }
                        );
                    }
                }
        );

        mLogoutButton.setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {
                        Toast.makeText(getApplicationContext(), "Come Back Soon!", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(editProfileActivity.this, loginActivity.class);
                        startActivity(i);
                    }
                }
        );
    }

}
