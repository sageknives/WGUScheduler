package com.example.sagegatzke.wguscheduler;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

/**
 * Created by sagegatzke on 3/4/18.
 */

public class TermsCursorAdapter extends CursorAdapter {

    public TermsCursorAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(
                R.layout.term_list_item, parent, false
        );
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        String noteText = cursor.getString(
                cursor.getColumnIndex(DBHelper.TERM_TITLE));

        int pos = noteText.indexOf(10);
        if (pos != -1) {
            noteText = noteText.substring(0, pos) + " ...";
        }

        TextView tv = (TextView) view.findViewById(R.id.textViewTerm);
        tv.setText(noteText);

    }
}
