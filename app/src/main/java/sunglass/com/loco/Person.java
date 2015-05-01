package sunglass.com.loco;

import java.io.Serializable;

/**
 * Created by cmccord on 4/29/15.
 */
public class Person implements Serializable {
    private String name;
    private String email;
    private String uid;

    public Person(String n, String e) { name = n; email = e; }

    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getUid() { return uid; }
    public void setUid(String s) {uid = s;}

    @Override
    public String toString() { return name + "\n" + email; }
}