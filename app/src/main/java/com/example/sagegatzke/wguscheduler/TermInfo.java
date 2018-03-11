package com.example.sagegatzke.wguscheduler;

import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteOpenHelper;
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
import android.widget.CursorAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;


public class TermInfo extends AppCompatActivity {

    private String action;
    private EditText termTitle;
    private EditText termStart;
    private EditText termEnd;
    private TextView termCourseCount;
    private String termFilter;
    private String oldTitle;
    private String oldStart;
    private String oldEnd;
    private int oldCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_term_info);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        termTitle = (EditText) findViewById(R.id.termTitle);
        termStart = (EditText) findViewById(R.id.termStart);
        termEnd = (EditText) findViewById(R.id.termEnd);
        termCourseCount = (TextView) findViewById(R.id.termCourseCount);
        termCourseCount.setText(oldCount + "");

        Intent intent = getIntent();

        Uri uri = intent.getParcelableExtra(TermsProvider.TERM_CONTENT_ITEM_TYPE);

        if (uri == null) {
            action = Intent.ACTION_INSERT;
            setTitle("New Term");
            setDatePicker(termStart, "");
            setDatePicker(termEnd, "");


        } else {
            action = Intent.ACTION_EDIT;
            termFilter = DBHelper.TERM_ID + "=" + uri.getLastPathSegment();

            Cursor cursor = getContentResolver().query(uri,
                    DBHelper.ALL_TERMS_COLUMNS, termFilter, null, null);
            cursor.moveToFirst();
            oldTitle = cursor.getString(cursor.getColumnIndex(DBHelper.TERM_TITLE));
            termTitle.setText(oldTitle);
            setTitle(oldTitle);
            oldStart = cursor.getString(cursor.getColumnIndex(DBHelper.TERM_START));
            termStart.setText(oldStart);
            setDatePicker(termStart, oldStart);
            oldEnd = cursor.getString(cursor.getColumnIndex(DBHelper.TERM_END));
            termEnd.setText(oldEnd);
            setDatePicker(termEnd, oldEnd);
            termTitle.requestFocus();
            oldCount = getCourseCount(uri.getLastPathSegment());
            termCourseCount.setText(oldCount > 0 ? oldCount + "" : "0");
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finishEditing();
            }
        });


    }



    private int getCourseCount(String termId) {
        Cursor cursor = getContentResolver().query(CoursesProvider.COURSES_CONTENT_URI, new String[]{"count(*) as Count"}, DBHelper.COURSE_TERM_ID + "=" + termId, null, null);
        return cursor.getCount();
//        cursor.moveToFirst();
//        return cursor.getColumnIndex("Count");
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

                new DatePickerDialog(TermInfo.this, date, cal
                        .get(Calendar.YEAR), cal.get(Calendar.MONTH),
                        cal.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
    }


    private boolean saveTerm(View view) {
        Snackbar.make(view, "Save not Implemented yet.", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
        return false;
    }

    private void showSnack(String message) {
        Snackbar.make(TermInfo.this.getCurrentFocus(), message, Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
    }


    private void updateTerm(String termTitle, String termStart, String termEnd) {
        ContentValues values = new ContentValues();
        values.put(DBHelper.TERM_TITLE, termTitle);
        values.put(DBHelper.TERM_START, termStart);
        values.put(DBHelper.TERM_END, termEnd);
        getContentResolver().update(TermsProvider.TERMS_CONTENT_URI, values, termFilter, null);
        Toast.makeText(this, getString(R.string.term_updated_toast), Toast.LENGTH_SHORT).show();
        setResult(RESULT_OK);
    }

    private void insertTerm(String termTitle, String termStart, String termEnd) {
        ContentValues values = new ContentValues();
        values.put(DBHelper.TERM_TITLE, termTitle);
        values.put(DBHelper.TERM_START, termStart);
        values.put(DBHelper.TERM_END, termEnd);
        getContentResolver().insert(TermsProvider.TERMS_CONTENT_URI, values);
        setResult(RESULT_OK);
    }

    private void finishEditing() {
        String newTitle = termTitle.getText().toString().trim();
        String newStart = termStart.getText().toString().trim();
        String newEnd = termEnd.getText().toString().trim();
        Log.d("Validate", "validateForm: ");
        boolean isValid = true;
        switch (action) {
            case Intent.ACTION_INSERT:
                if (validateForm(newTitle, newStart, newEnd)) {
                    insertTerm(newTitle, newStart, newEnd);
                } else {
                    isValid = false;
                }

                break;
            case Intent.ACTION_EDIT:
                if (validateForm(newTitle, newStart, newEnd)) {
                    updateTerm(newTitle, newStart, newEnd);
                } else {
                    isValid = false;
                }


        }
        if (isValid) finish();
    }

    private boolean validateForm(String title, String start, String end) {
        Integer isGreater = start.compareTo(end);

        Log.d("Validate", "validateForm: " + isGreater);
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


    private void updateLabel(EditText text, Calendar cal) {
        String myFormat = "yyyy-MM-dd";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        text.setText(sdf.format(cal.getTime()));
    }



    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        finish();
        //finishEditing();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
