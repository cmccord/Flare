package sunglass.com.loco;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;


public class circlesActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_circles);

        Button mBackButton = (Button) findViewById(R.id.back_butt);
        mBackButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //updateLocation();
                finish();
            }
        });

        Button mSaveChangesButton = (Button) findViewById(R.id.saveChangesButton);
        mSaveChangesButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //updateLocation();
                finish();
            }
        });
    }

}
