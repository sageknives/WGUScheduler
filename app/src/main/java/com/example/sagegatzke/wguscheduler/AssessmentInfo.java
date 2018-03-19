package com.example.sagegatzke.wguscheduler;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.icu.text.DateFormat;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.icu.util.TimeZone;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import java.text.ParseException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.Locale;

public class AssessmentInfo extends AppCompatActivity {

    private String action;
    private int courseId;
    private int assessmentId = -1;
    private EditText assessmentTitle;
    private EditText assessmentType;
    private EditText assessmentDueDate;
    private String assessmentFilter;
    private Button notificationButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assessment_info);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        assessmentTitle = findViewById(R.id.assessmentTitle);
        assessmentType = findViewById(R.id.assessmentType);
        assessmentDueDate = findViewById(R.id.assessmentDueDate);
        notificationButton = findViewById(R.id.notification_toggle_button);
        notificationButton.setText("Enable Notifications");
        notificationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleNotifications();

            }
        });

        Intent intent = getIntent();

        Uri uri = intent.getParcelableExtra(CoursesProvider.COURSE_CONTENT_ITEM_TYPE);

        if (uri != null) {
            courseId = Integer.parseInt(uri.getLastPathSegment());
            action = Intent.ACTION_INSERT;
            setTitle("New Assessment");
            setDatePicker(assessmentDueDate, "");

        } else {
            uri = intent.getParcelableExtra(AssessmentsProvider.ASSESSMENT_CONTENT_ITEM_TYPE);

            action = Intent.ACTION_EDIT;
            assessmentId = Integer.parseInt(uri.getLastPathSegment());
            assessmentFilter = DBHelper.ASSESSMENT_ID + "=" + assessmentId;

            Cursor cursor = getContentResolver().query(uri,
                    DBHelper.ALL_ASSESSMENTS_COLUMNS, assessmentFilter, null, null);
            cursor.moveToFirst();

            courseId = cursor.getInt(cursor.getColumnIndex(DBHelper.ASSESSMENT_COURSE_ID));


            String savedTitle = cursor.getString(cursor.getColumnIndex(DBHelper.ASSESSMENT_TITLE));
            assessmentTitle.setText(savedTitle);
            setTitle(savedTitle);

            String savedAssessmentType = cursor.getString(cursor.getColumnIndex(DBHelper.ASSESSMENT_TYPE));
            assessmentType.setText(savedAssessmentType);

            String savedDueDate = cursor.getString(cursor.getColumnIndex(DBHelper.ASSESSMENT_DUE_DATE));
            assessmentDueDate.setText(savedDueDate);
            setDatePicker(assessmentDueDate, savedDueDate);

            boolean isEnabled = isNotifying();
            if (isEnabled) {
                notificationButton.setText("Notifications Enabled");
            }

        }

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finishEditing();
            }
        });
    }

    private boolean isNotifying() {
        if (assessmentId == -1) {
            return false;

        } else {
            SharedPreferences sharedPref = AssessmentInfo.this.getSharedPreferences("NotificationPref", Context.MODE_PRIVATE);
            boolean notificationsOn = sharedPref.getBoolean("assessment" + assessmentId, false);
            return notificationsOn;
        }

    }


    private void toggleNotifications() {
        if (assessmentId == -1) {
            showSnack("You must add the assessment before you can be notified.");
        } else {
            SharedPreferences sharedPref = AssessmentInfo.this.getSharedPreferences("NotificationPref", Context.MODE_PRIVATE);
            boolean notificationsOn = sharedPref.getBoolean("assessment" + assessmentId, false);
            if (notificationsOn) {
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putBoolean("assessment" + assessmentId, false);
                editor.commit();
                notificationButton.setText("Disable Notifications");
                String title = assessmentTitle.getText().toString().trim();
                String dueDate = assessmentDueDate.getText().toString().trim();
                String message = "Cancel notification";
                cancelNotification(assessmentId, title, message, dueDate);
            } else {
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putBoolean("assessment" + assessmentId, true);
                editor.commit();
                notificationButton.setText("Notifications Enabled");
                String title = assessmentTitle.getText().toString().trim();
                String dueDate = assessmentDueDate.getText().toString().trim();
                String message = "Your assessment is due!";
                enableNotification(assessmentId, title, message, dueDate);
            }
        }
    }

    private void enableNotification(int id, String title, String message, String datetime) {

        Intent alarmIntent = new Intent(this, NotificationReceiver.class);
        alarmIntent.putExtra("id",id);
        alarmIntent.putExtra("type","assessment");
        alarmIntent.putExtra("message", message);
        alarmIntent.putExtra("title", title);
        Long time = Long.parseLong("0");
        try {
            final Calendar cal = Calendar.getInstance(TimeZone.getDefault());

            String[] dateParts = datetime.split("-");
            if (dateParts.length == 3) {
                String savedMonth = dateParts[1];
                String savedDate = dateParts[2];
                String savedYear = dateParts[0];
                cal.set(Calendar.YEAR, Integer.parseInt(savedYear));
                cal.set(Calendar.MONTH, Integer.parseInt(savedMonth) - 1);
                cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(savedDate));
                cal.set(Calendar.HOUR_OF_DAY, 9);
                time = cal.getTime().getTime();
            } else {
                showSnack("date is in incorrect format");
                return;
            }
        } catch (Exception e) {
            Log.d("datetime-error", e.getStackTrace().toString());
        }
        int alarmId = Integer.parseInt("3" + assessmentId);
        alarmIntent.putExtra("alarmId", alarmId);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, alarmId, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) AssessmentInfo.this.getSystemService(AssessmentInfo.this.ALARM_SERVICE);

        alarmManager.setExact(AlarmManager.RTC_WAKEUP, time, pendingIntent);
    }

    private void cancelNotification(int id, String title, String message, String datetime) {
        Intent alarmIntent = new Intent(this, NotificationReceiver.class);
        alarmIntent.putExtra("id",id);
        alarmIntent.putExtra("type","assessment");
        alarmIntent.putExtra("message", message);
        alarmIntent.putExtra("title", title);

        int alarmId = Integer.parseInt("3" + assessmentId);
        alarmIntent.putExtra("alarmId", alarmId);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, alarmId, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) AssessmentInfo.this.getSystemService(AssessmentInfo.this.ALARM_SERVICE);

        alarmManager.cancel(pendingIntent);
    }


    private void updateAssessment(String title, String type, String dueDate) {
        ContentValues values = new ContentValues();
        values.put(DBHelper.ASSESSMENT_TITLE, title);
        values.put(DBHelper.ASSESSMENT_TYPE, type);
        values.put(DBHelper.ASSESSMENT_DUE_DATE, dueDate);
        values.put(DBHelper.ASSESSMENT_COURSE_ID, courseId);
        getContentResolver().update(AssessmentsProvider.ASSESSMENTS_CONTENT_URI, values, assessmentFilter, null);
        Toast.makeText(this, getString(R.string.assessment_updated_toast), Toast.LENGTH_SHORT).show();
        setResult(RESULT_OK);
    }

    private void insertAssessment(String title, String type, String dueDate) {
        ContentValues values = new ContentValues();
        values.put(DBHelper.ASSESSMENT_TITLE, title);
        values.put(DBHelper.ASSESSMENT_TYPE, type);
        values.put(DBHelper.ASSESSMENT_DUE_DATE, dueDate);
        values.put(DBHelper.ASSESSMENT_COURSE_ID, courseId);
        getContentResolver().insert(AssessmentsProvider.ASSESSMENTS_CONTENT_URI, values);
        Toast.makeText(this, getString(R.string.assessment_created_toast), Toast.LENGTH_SHORT).show();
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
        String title = assessmentTitle.getText().toString().trim();
        String type = assessmentType.getText().toString().trim();
        String dueDate = assessmentDueDate.getText().toString().trim();

        boolean isValid = true;
        switch (action) {
            case Intent.ACTION_INSERT:

                if (validateForm(title, type, dueDate)) {
                    insertAssessment(title, type, dueDate);
                } else {
                    isValid = false;
                }

                break;
            case Intent.ACTION_EDIT:
                if (validateForm(title, type, dueDate)) {
                    updateAssessment(title, type, dueDate);
                } else {
                    isValid = false;
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

    private boolean validateForm(String title, String type, String dueDate) {

        if (title.isEmpty()) {
            //toast title required
            showSnack("Title is required");
            return false;
        }
        if (type.isEmpty()) {
            //toast title required
            showSnack("Type is required");
            return false;
        }
        switch (type) {
            case "performance": {
            }
            case "objective": {
            }
            break;
            default: {
                showSnack("A assessment must has a type of either 'performance' or 'objective'");
                return false;
            }
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
