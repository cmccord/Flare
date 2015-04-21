package sunglass.com.loco;

import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;


public class editProfileActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        Button backButton = (Button) findViewById(R.id.back_butt);
        backButton.setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {
                        finish();
                    }
                }
        );
    }

}
