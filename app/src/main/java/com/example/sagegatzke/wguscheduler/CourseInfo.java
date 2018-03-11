package com.example.sagegatzke.wguscheduler;

import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.icu.util.TimeZone;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

public class CourseInfo extends AppCompatActivity {


    private String action;
    private int termId;
    private EditText termTitle;
    private EditText courseTitle;
    private EditText courseStart;
    private EditText courseEnd;
    private TextView courseAssessmentCount;
    private String courseFilter;
    private String oldTitle;
    private String oldStart;
    private String oldEnd;
    private String oldTermTitle;
    private int oldCount = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_info);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        termTitle = (EditText) findViewById(R.id.termTitle);
        courseTitle = (EditText) findViewById(R.id.courseTitle);
        courseStart = (EditText) findViewById(R.id.courseStart);
        courseEnd = (EditText) findViewById(R.id.courseEnd);
        courseAssessmentCount = (TextView) findViewById(R.id.courseAssessmentCount);
        courseAssessmentCount.setText(oldCount + "");

        Intent intent = getIntent();

        Uri uri = intent.getParcelableExtra(TermsProvider.TERM_CONTENT_ITEM_TYPE);

        if (uri != null) {
            //set termId
            termTitle.setText(uri.getLastPathSegment());
            action = Intent.ACTION_INSERT;
            setTitle("New Course");
            setDatePicker(courseStart, "");
            setDatePicker(courseEnd, "");


        } else {
            uri = intent.getParcelableExtra(CoursesProvider.COURSE_CONTENT_ITEM_TYPE);

            action = Intent.ACTION_EDIT;
            courseFilter = DBHelper.COURSE_ID + "=" + uri.getLastPathSegment();
            String assessmentCourseFilter = DBHelper.ASSESSMENT_COURSE_ID + "=" + uri.getLastPathSegment();
            Cursor cursor = getContentResolver().query(uri,
                    DBHelper.ALL_COURSES_COLUMNS, courseFilter, null, null);
            cursor.moveToFirst();
            oldTitle = cursor.getString(cursor.getColumnIndex(DBHelper.COURSE_TITLE));
            courseTitle.setText(oldTitle);
            setTitle(oldTitle);
            oldStart = cursor.getString(cursor.getColumnIndex(DBHelper.COURSE_START));
            courseStart.setText(oldStart);
            setDatePicker(courseStart, oldStart);
            oldEnd = cursor.getString(cursor.getColumnIndex(DBHelper.COURSE_END));
            courseEnd.setText(oldEnd);
            setDatePicker(courseEnd, oldEnd);
            oldTermTitle = cursor.getString(cursor.getColumnIndex(DBHelper.COURSE_TERM_ID));
            termTitle.setText(oldTermTitle);
            courseTitle.requestFocus();
            DBHelper db = new DBHelper(this);
            cursor = db.getReadableDatabase().query(DBHelper.TABLE_ASSESSMENTS, DBHelper.ALL_ASSESSMENTS_COLUMNS,
                    assessmentCourseFilter, null, null, null,
                    null);
            cursor.moveToFirst();
            oldCount = cursor.getCount();
            courseAssessmentCount.setText(oldCount > 0 ? oldCount + "" : "0");
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finishEditing();
            }
        });

    }

    private void setDatePicker(final EditText dateView, String currentDate) {
        final Calendar cal = Calendar.getInstance(TimeZone.getDefault());

        if (!currentDate.isEmpty()) {
            String[] dateParts = currentDate.split("-");
            if (dateParts.length == 3) {
                String savedMonth = dateParts[1];
                String savedDate = dateParts[2];
                String savedYear = dateParts[0];
                cal.set(Calendar.YEAR, Integer.parseInt(savedYear));
                cal.set(Calendar.MONTH, Integer.parseInt(savedMonth) - 1);
                cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(savedDate));
            }
        }
        final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {


            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                cal.set(Calendar.YEAR, year);
                cal.set(Calendar.MONTH, monthOfYear);
                cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateLabel(dateView, cal);
            }

        };

        dateView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                new DatePickerDialog(CourseInfo.this, date, cal
                        .get(Calendar.YEAR), cal.get(Calendar.MONTH),
                        cal.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }




    private boolean saveTerm(View view) {
        Snackbar.make(view, "Save not Implemented yet.", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
        return false;
    }

    private void showSnack(String message) {
        Snackbar.make(CourseInfo.this.getCurrentFocus(), message, Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
    }


    private void updateCourse(String courseTitle, String courseStart, String courseEnd, String termId) {
        ContentValues values = new ContentValues();
        values.put(DBHelper.COURSE_TITLE, courseTitle);
        values.put(DBHelper.COURSE_START, courseStart);
        values.put(DBHelper.COURSE_END, courseEnd);
        values.put(DBHelper.COURSE_TERM_ID, Integer.parseInt(termId));
        getContentResolver().update(CoursesProvider.COURSES_CONTENT_URI, values, courseFilter, null);
        Toast.makeText(this, getString(R.string.course_updated_toast), Toast.LENGTH_SHORT).show();
        setResult(RESULT_OK);
    }

    private void insertCourse(String courseTitle, String courseStart, String courseEnd, String termId) {
        ContentValues values = new ContentValues();
        values.put(DBHelper.COURSE_TITLE, courseTitle);
        values.put(DBHelper.COURSE_START, courseStart);
        values.put(DBHelper.COURSE_END, courseEnd);
        values.put(DBHelper.COURSE_TERM_ID, Integer.parseInt(termId));
        getContentResolver().insert(CoursesProvider.COURSES_CONTENT_URI, values);
        setResult(RESULT_OK);
    }

    private void finishEditing() {
        String newTermId = termTitle.getText().toString().trim();
        String newTitle = courseTitle.getText().toString().trim();
        String newStart = courseStart.getText().toString().trim();
        String newEnd = courseEnd.getText().toString().trim();
        Log.d("Validate", "validateForm: ");
        boolean isValid = true;
        switch (action) {
            case Intent.ACTION_INSERT:
                if (validateForm(newTitle, newStart, newEnd, newTermId)) {
                    insertCourse(newTitle, newStart, newEnd, newTermId);
                } else {
                    isValid = false;
                }

                break;
            case Intent.ACTION_EDIT:
                if (validateForm(newTitle, newStart, newEnd, newTermId)) {
                    updateCourse(newTitle, newStart, newEnd, newTermId);
                } else {
                    isValid = false;
                }


        }
        if (isValid) finish();
    }

    private boolean validateForm(String title, String start, String end, String termId) {
        Integer isGreater = start.compareTo(end);

        Log.d("Validate", "validateForm: " + isGreater);
        if (termId.isEmpty()) {
            //toast title required
            showSnack("Term is required");
            return false;
        }
        if (title.isEmpty()) {
            //toast title required
            showSnack("Title is required");
            return false;
        }
        if (start.isEmpty()) {
            //toast start required
            showSnack("Start is required");
            return false;
        }
        if (end.isEmpty()) {
            //toast start required
            showSnack("End is required");
            return false;
        }
        if (start.compareTo(end) >= 0) {
            showSnack("Start must be before end");
            return false;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        finish();
    }


    private void updateLabel(EditText text, Calendar cal) {
        String myFormat = "yyyy-MM-dd";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        text.setText(sdf.format(cal.getTime()));
    }

}
