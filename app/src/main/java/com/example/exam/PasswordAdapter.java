package com.example.exam;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import java.util.List;

public class PasswordAdapter extends ArrayAdapter<PasswordEntry> {
    public PasswordAdapter(Context context, List<PasswordEntry> passwords) {
        super(context, 0, passwords);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        PasswordEntry entry = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(android.R.layout.simple_list_item_1, parent, false);
        }

        TextView textView = convertView.findViewById(android.R.id.text1);
        textView.setText(entry.getService() + " (" + entry.getLogin() + ")");

        return convertView;
    }
}