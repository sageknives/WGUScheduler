package com.example.sagegatzke.wguscheduler;

import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Color;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.icu.util.TimeZone;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;

public class CourseInfo extends AppCompatActivity {


    private String action;
    private int termId;
    private int courseId = -1;
    private EditText courseTitle;
    private EditText courseStart;
    private EditText courseEnd;
    private EditText courseStatus;
    private EditText courseMentorName;
    private EditText courseMentorEmail;
    private EditText courseMentorNumber;
    private EditText courseNotes;
    private Button notificationButton;


    private String courseFilter;
    private int oldCount = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_info);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        courseTitle = findViewById(R.id.courseTitle);
        courseStart = findViewById(R.id.courseStart);
        courseEnd = findViewById(R.id.courseEnd);
        courseStatus = findViewById(R.id.courseStatus);
        courseMentorName = findViewById(R.id.courseMentorName);
        courseMentorEmail = findViewById(R.id.courseMentorEmail);
        courseMentorNumber = findViewById(R.id.courseMentorNumber);
        courseNotes = findViewById(R.id.courseNotes);
        notificationButton = findViewById(R.id.notification_toggle_button);
        notificationButton.setText("Enable Notifications");
        notificationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleNotifications();

            }
        });
        TextView courseAssessmentCount = findViewById(R.id.courseAssessmentCount);
        courseAssessmentCount.setText(oldCount + "");

        Intent intent = getIntent();

        Uri uri = intent.getParcelableExtra(TermsProvider.TERM_CONTENT_ITEM_TYPE);

        if (uri != null) {
            //set termId
            termId = Integer.parseInt(uri.getLastPathSegment());
            action = Intent.ACTION_INSERT;
            setTitle("New Course");
            setDatePicker(courseStart, "");
            setDatePicker(courseEnd, "");

        } else {
            uri = intent.getParcelableExtra(CoursesProvider.COURSE_CONTENT_ITEM_TYPE);

            action = Intent.ACTION_EDIT;
            courseId = Integer.parseInt(uri.getLastPathSegment());
            courseFilter = DBHelper.COURSE_ID + "=" + courseId;
            String assessmentCourseFilter = DBHelper.ASSESSMENT_COURSE_ID + "=" + uri.getLastPathSegment();
            Cursor cursor = getContentResolver().query(uri,
                    DBHelper.ALL_COURSES_COLUMNS, courseFilter, null, null);
            cursor.moveToFirst();

            termId = cursor.getInt(cursor.getColumnIndex(DBHelper.COURSE_TERM_ID));

            String savedCourseTitle = cursor.getString(cursor.getColumnIndex(DBHelper.COURSE_TITLE));
            courseTitle.setText(savedCourseTitle);
            setTitle(savedCourseTitle);

            String savedCourseStart = cursor.getString(cursor.getColumnIndex(DBHelper.COURSE_START));
            courseStart.setText(savedCourseStart);
            setDatePicker(courseStart, savedCourseStart);

            String savedCourseEnd = cursor.getString(cursor.getColumnIndex(DBHelper.COURSE_END));
            courseEnd.setText(savedCourseEnd);
            setDatePicker(courseEnd, savedCourseEnd);

            String savedCourseStatus = cursor.getString(cursor.getColumnIndex(DBHelper.COURSE_STATUS));
            courseStatus.setText(savedCourseStatus);

            String saveCourseMentorName = cursor.getString(cursor.getColumnIndex(DBHelper.COURSE_MENTOR_NAME));
            courseMentorName.setText(saveCourseMentorName);

            String savedCourseMentorEmail = cursor.getString(cursor.getColumnIndex(DBHelper.COURSE_MENTOR_EMAIL));
            courseMentorEmail.setText(savedCourseMentorEmail);

            String savedCourseMentorNumber = cursor.getString(cursor.getColumnIndex(DBHelper.COURSE_MENTOR_NUMBER));
            courseMentorNumber.setText(savedCourseMentorNumber);

            String savedCourseNotes = cursor.getString(cursor.getColumnIndex(DBHelper.COURSE_NOTES));
            courseNotes.setText(savedCourseNotes);

            boolean isEnabled = isNotifying();
            if(isEnabled){
                notificationButton.setText("Disable notifications");
            }



            DBHelper db = new DBHelper(this);
            cursor = db.getReadableDatabase().query(DBHelper.TABLE_ASSESSMENTS, DBHelper.ALL_ASSESSMENTS_COLUMNS,
                    assessmentCourseFilter, null, null, null,
                    null);
            cursor.moveToFirst();
            oldCount = cursor.getCount();
            courseAssessmentCount.setText(oldCount > 0 ? oldCount + "" : "0");
        }

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finishEditing();
            }
        });

        FloatingActionButton shareFab = new FloatingActionButton(this);
        shareFab.setImageResource(android.R.drawable.ic_menu_share);
        shareFab.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorPrimary)));
        shareFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shareNotes();
            }
        });
        CoordinatorLayout layout = findViewById(R.id.course_info_container);
        layout.addView(shareFab);
        ((CoordinatorLayout.LayoutParams) shareFab.getLayoutParams()).gravity = Gravity.BOTTOM | Gravity.LEFT;
        ((CoordinatorLayout.LayoutParams) shareFab.getLayoutParams()).setMargins(40, 40, 40, 40);
    }

    private void shareNotes() {
        String title = courseTitle.getText().toString().trim();
        String notes = courseNotes.getText().toString().trim();
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("message/rfc822");
//        i.putExtra(Intent.EXTRA_EMAIL  , new String[]{"recipient@example.com"});
        i.putExtra(Intent.EXTRA_SUBJECT, "Notes for course " + title);
        i.putExtra(Intent.EXTRA_TEXT, notes);
        try {
            startActivity(Intent.createChooser(i, "Send mail..."));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(CourseInfo.this, "No Email Apps found.", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isNotifying() {
        if (courseId == -1) {
            return false;

        } else {
            SharedPreferences sharedPref = CourseInfo.this.getPreferences(Context.MODE_PRIVATE);
            boolean notificationsOn = sharedPref.getBoolean("course" + courseId, false);
            return notificationsOn;
        }

    }

    private void toggleNotifications() {
        if (courseId == -1) {
            showSnack("You must add the course before you can be notified.");
        } else {
            SharedPreferences sharedPref = CourseInfo.this.getPreferences(Context.MODE_PRIVATE);
            boolean notificationsOn = sharedPref.getBoolean("course" + courseId, false);
            notificationsOn = !notificationsOn;
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean("course" + courseId, notificationsOn);
            editor.commit();
            if(notificationsOn){
                notificationButton.setText("Disable Notifications");
            }else{
                notificationButton.setText("Enable Notifications");
            }

        }
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

    private void showSnack(String message) {
        Snackbar.make(CourseInfo.this.getCurrentFocus(), message, Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
    }

    private void updateCourse(String courseTitle, String courseStart, String courseEnd, String courseStatus, String courseMentorName, String courseMentorEmail, String courseMentorNumber, String courseNotes) {
        ContentValues values = new ContentValues();
        values.put(DBHelper.COURSE_TITLE, courseTitle);
        values.put(DBHelper.COURSE_START, courseStart);
        values.put(DBHelper.COURSE_END, courseEnd);
        values.put(DBHelper.COURSE_STATUS, courseStatus);
        values.put(DBHelper.COURSE_MENTOR_NAME, courseMentorName);
        values.put(DBHelper.COURSE_MENTOR_EMAIL, courseMentorEmail);
        values.put(DBHelper.COURSE_MENTOR_NUMBER, courseMentorNumber);
        values.put(DBHelper.COURSE_NOTES, courseNotes);
        values.put(DBHelper.COURSE_TERM_ID, termId);
        getContentResolver().update(CoursesProvider.COURSES_CONTENT_URI, values, courseFilter, null);
        Toast.makeText(this, getString(R.string.course_updated_toast), Toast.LENGTH_SHORT).show();
        setResult(RESULT_OK);
    }

    private void insertCourse(String courseTitle, String courseStart, String courseEnd, String courseStatus, String courseMentorName, String courseMentorEmail, String courseMentorNumber, String courseNotes) {
        ContentValues values = new ContentValues();
        values.put(DBHelper.COURSE_TITLE, courseTitle);
        values.put(DBHelper.COURSE_START, courseStart);
        values.put(DBHelper.COURSE_END, courseEnd);
        values.put(DBHelper.COURSE_STATUS, courseStatus);
        values.put(DBHelper.COURSE_MENTOR_NAME, courseMentorName);
        values.put(DBHelper.COURSE_MENTOR_EMAIL, courseMentorEmail);
        values.put(DBHelper.COURSE_MENTOR_NUMBER, courseMentorNumber);
        values.put(DBHelper.COURSE_NOTES, courseNotes);
        values.put(DBHelper.COURSE_TERM_ID, termId);
        getContentResolver().insert(CoursesProvider.COURSES_CONTENT_URI, values);
        setResult(RESULT_OK);
    }

    private void finishEditing() {
        String title = courseTitle.getText().toString().trim();
        String start = courseStart.getText().toString().trim();
        String end = courseEnd.getText().toString().trim();
        String status = courseStatus.getText().toString().trim();
        String name = courseMentorName.getText().toString().trim();
        String email = courseMentorEmail.getText().toString().trim();
        String number = courseMentorNumber.getText().toString().trim();
        String notes = courseNotes.getText().toString().trim();

        boolean isValid = true;
        switch (action) {
            case Intent.ACTION_INSERT:
                if (validateForm(title, start, end, status, name, email, number, notes)) {
                    insertCourse(title, start, end, status, name, email, number, notes);
                } else {
                    isValid = false;
                }

                break;
            case Intent.ACTION_EDIT:
                if (validateForm(title, start, end, status, name, email, number, notes)) {
                    updateCourse(title, start, end, status, name, email, number, notes);
                } else {
                    isValid = false;
                }


        }
        if (isValid) finish();
    }

    private boolean validateForm(String title, String start, String end, String status, String name, String email, String number, String notes) {
        ArrayList<String> list = new ArrayList<>();
        list.add(title);
        list.add(status);
        list.add(name);
        list.add(email);
        list.add(number);
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).isEmpty()) {
                showSnack("Empty field detected, Fill in all fields. Notes are optional.");
                return false;
            }
        }
        if (start.compareTo(end) >= 0) {
            showSnack("Start must be before end");
            return false;
        }
        switch (status) {
            case "in progress": {
            }
            case "completed": {
            }
            case "dropped": {
            }
            case "plan to take":
                break;
            default: {
                showSnack("A status must be 'in progress', 'completed', 'dropped', or 'plan to take'");
                return false;
            }
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
