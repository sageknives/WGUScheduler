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

public class AssessmentInfo extends AppCompatActivity {

    private String action;
    private EditText courseId;
    private EditText assessmentTitle;
    private EditText assessmentStart;
    private EditText assessmentEnd;
    private String assessmentFilter;
    private String oldTitle;
    private String oldStart;
    private String oldEnd;
    private String oldCourseId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assessment_info);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        courseId = (EditText) findViewById(R.id.courseTitle);
        assessmentTitle = (EditText) findViewById(R.id.assessmentTitle);
        assessmentStart = (EditText) findViewById(R.id.assessmentStart);
        assessmentEnd = (EditText) findViewById(R.id.assessmentEnd);

        Intent intent = getIntent();

        Uri uri = intent.getParcelableExtra(CoursesProvider.COURSE_CONTENT_ITEM_TYPE);

        if (uri != null) {
            courseId.setText(uri.getLastPathSegment());
            action = Intent.ACTION_INSERT;
            setTitle("New Assessment");
            setDatePicker(assessmentStart, "");
            setDatePicker(assessmentEnd, "");


        } else {
            uri = intent.getParcelableExtra(AssessmentsProvider.ASSESSMENT_CONTENT_ITEM_TYPE);

            action = Intent.ACTION_EDIT;
            assessmentFilter = DBHelper.ASSESSMENT_ID + "=" + uri.getLastPathSegment();

            Cursor cursor = getContentResolver().query(uri,
                    DBHelper.ALL_ASSESSMENTS_COLUMNS, assessmentFilter, null, null);
            cursor.moveToFirst();
            oldTitle = cursor.getString(cursor.getColumnIndex(DBHelper.ASSESSMENT_TITLE));
            assessmentTitle.setText(oldTitle);
            setTitle(oldTitle);
            oldStart = cursor.getString(cursor.getColumnIndex(DBHelper.ASSESSMENT_START));
            assessmentStart.setText(oldStart);
            setDatePicker(assessmentStart, oldStart);
            oldEnd = cursor.getString(cursor.getColumnIndex(DBHelper.ASSESSMENT_END));
            assessmentEnd.setText(oldEnd);
            oldCourseId = cursor.getString(cursor.getColumnIndex(DBHelper.ASSESSMENT_COURSE_ID));
            courseId.setText(oldCourseId);
            courseId.requestFocus();
            setDatePicker(assessmentEnd, oldEnd);
            assessmentTitle.requestFocus();
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finishEditing();
            }
        });
    }

    private void updateAssessment(String title, String start, String end, String id) {
        ContentValues values = new ContentValues();
        values.put(DBHelper.ASSESSMENT_TITLE, title);
        values.put(DBHelper.ASSESSMENT_START, start);
        values.put(DBHelper.ASSESSMENT_END, end);
        values.put(DBHelper.ASSESSMENT_COURSE_ID, Integer.parseInt(id));
        getContentResolver().update(AssessmentsProvider.ASSESSMENTS_CONTENT_URI, values, assessmentFilter, null);
        Toast.makeText(this, getString(R.string.assessment_updated_toast), Toast.LENGTH_SHORT).show();
        setResult(RESULT_OK);
    }

    private void insertAssessment(String title, String start, String end, String id) {
        ContentValues values = new ContentValues();
        values.put(DBHelper.ASSESSMENT_TITLE, title);
        values.put(DBHelper.ASSESSMENT_START, start);
        values.put(DBHelper.ASSESSMENT_END, end);
        values.put(DBHelper.ASSESSMENT_COURSE_ID, Integer.parseInt(id));
        getContentResolver().insert(AssessmentsProvider.ASSESSMENTS_CONTENT_URI, values);
        setResult(RESULT_OK);
    }

    private void deleteAssessment() {
        getContentResolver().delete(AssessmentsProvider.ASSESSMENTS_CONTENT_URI,
                assessmentFilter, null);
        Toast.makeText(this, getString(R.string.assessment_delete_snack),
                Toast.LENGTH_SHORT).show();
        setResult(RESULT_OK);
        finish();


    }

    private void finishEditing() {
        String newCourseId = courseId.getText().toString().trim();
        String newTitle = assessmentTitle.getText().toString().trim();
        String newStart = assessmentStart.getText().toString().trim();
        String newEnd = assessmentEnd.getText().toString().trim();

        Log.d("Validate", "validateForm: ");
        boolean isValid = true;
        switch (action) {
            case Intent.ACTION_INSERT:
                if (newTitle.length() == 0) {
                    setResult(RESULT_CANCELED);
                } else {
                    if (validateForm(newTitle, newStart, newEnd, newCourseId)) {
                        insertAssessment(newTitle, newStart, newEnd, newCourseId);
                    } else {
                        isValid = false;
                    }
                }
                break;
            case Intent.ACTION_EDIT:
                if (newTitle.length() == 0 && newStart.length() == 0 && newEnd.length() == 0 && newCourseId.length() == 0) {
                    deleteAssessment();
                } else if (oldTitle.equals(newTitle) && oldStart.equals(newStart) && oldEnd.equals(newEnd)) {
                    setResult(RESULT_CANCELED);
                } else {
                    if (validateForm(newTitle, newStart, newEnd, newCourseId)) {
                        updateAssessment(newTitle, newStart, newEnd, newCourseId);
                    } else {
                        isValid = false;
                    }
                }

        }
        if (isValid) finish();
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

                new DatePickerDialog(AssessmentInfo.this, date, cal
                        .get(Calendar.YEAR), cal.get(Calendar.MONTH),
                        cal.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
    }

    private void showSnack(String message) {
        Snackbar.make(AssessmentInfo.this.getCurrentFocus(), message, Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
    }

    private boolean validateForm(String title, String start, String end, String courseId) {
        Integer isGreater = start.compareTo(end);

        Log.d("Validate", "validateForm: " + isGreater);
        if (courseId.isEmpty()) {
            //toast title required
            showSnack("Course is required");
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

    private void updateLabel(EditText text, Calendar cal) {
        String myFormat = "yyyy-MM-dd";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        text.setText(sdf.format(cal.getTime()));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_assessment_info, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_delete) {
            deleteAssessment();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        finish();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }


}
