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

public class CoursesCursorAdapter extends CursorAdapter {

    public CoursesCursorAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(
                R.layout.course_list_item, parent, false
        );
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        String courseText = cursor.getString(
                cursor.getColumnIndex(DBHelper.COURSE_TITLE));

        int pos = courseText.indexOf(10);
        if (pos != -1) {
            courseText = courseText.substring(0, pos) + " ...";
        }

        TextView tv = (TextView) view.findViewById(R.id.textViewCourse);
        tv.setText(courseText);

    }
}
