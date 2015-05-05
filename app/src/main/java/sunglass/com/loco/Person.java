package sunglass.com.loco;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;

import java.io.Serializable;

/**
 * Created by cmccord on 4/29/15.
 */
public class Person implements Serializable {
    private String name;
    private String email;
    private String uid;
    private Bitmap image = null;

    public Person(String n, String e) { name = n; email = e; }

    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getUid() { return uid; }
    public void setUid(String s) {uid = s;}
    public Bitmap getImage(Context context) {
        if(image != null)
            return image;
        else
            return BitmapFactory.decodeResource(context.getResources(), R.mipmap.unknownuser);
    }
    public void setImage(Bitmap b) {
        image = b;
    }

    @Override
    public String toString() { return email; }
}
