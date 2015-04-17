package sunglass.com.loco;

import android.location.Location;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by jabreezy on 3/4/15.
 */
public class User {
//    String id;
    String pos;
//    Map<String, List<String>> circles;

    public User(String id){
//        circles = new HashMap<String, List<String>>();
        pos = "";
    }
    public User(String id, Location l){
        this(id);
        updateLocation(l);
    }

    public void updateLocation(Location l){
        if (l != null)
            this.pos = l.getLatitude() + "," + l.getLongitude();
    }

}
