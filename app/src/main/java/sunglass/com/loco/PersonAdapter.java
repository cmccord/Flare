package sunglass.com.loco;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by cmccord on 4/29/15.
 */
public class PersonAdapter extends ArrayAdapter<Person> {
    Context context;
    int layoutResourceId;
    Person data[] = null;

    public PersonAdapter(Context context, int layoutResourceId, Person[] data) {
        super(context, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = data;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        PersonHolder holder = null;

        if(row == null)
        {
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);

            holder = new PersonHolder();
            holder.imgIcon = (ImageView)row.findViewById(R.id.imgIcon);
            holder.txtName = (TextView)row.findViewById(R.id.txtName);
            holder.txtEmail = (TextView)row.findViewById(R.id.txtEmail);

            row.setTag(holder);
        }
        else
        {
            holder = (PersonHolder)row.getTag();
        }

        Person person = data[position];
        holder.txtName.setText(person.getName());
        holder.txtEmail.setText(person.getEmail());
        //holder.imgIcon.setImageResource(weather.icon);

        return row;
    }

    static class PersonHolder
    {
        ImageView imgIcon;
        TextView txtName;
        TextView txtEmail;
    }

}
