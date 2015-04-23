package sunglass.com.loco;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;


public class newUserActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_user);

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
                                Intent i = new Intent(newUserActivity.this, MapsActivity.class);
                                startActivity(i);
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // Do nothing
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