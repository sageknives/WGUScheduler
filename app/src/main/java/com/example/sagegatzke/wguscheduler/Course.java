package com.example.sagegatzke.wguscheduler;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class Course extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int COURSE_EDITOR_REQUEST_CODE = 104;
    private String courseId;
    private Uri savedUri;
    private CursorAdapter cursorAdapter;
    private String courseFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        Intent intent = getIntent();
        if (savedUri == null) {
            savedUri = intent.getParcelableExtra(CoursesProvider.COURSE_CONTENT_ITEM_TYPE);
        }
        courseId = savedUri.getLastPathSegment();
        courseFilter = DBHelper.ASSESSMENT_COURSE_ID + "=" + courseId;

        cursorAdapter = new AssessmentsCursorAdapter(this, null, 0);

        ListView list = (ListView) findViewById(android.R.id.list);
        list.setAdapter(cursorAdapter);
        cursorAdapter.getCount();


//        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                Intent intent = new Intent(Course.this, Assessment.class);
//                Uri uri = Uri.parse(AssessmentsProvider.ASSESSMENTS_CONTENT_URI + "/" + id);
//                intent.putExtra(AssessmentsProvider.ASSESSMENT_CONTENT_ITEM_TYPE, uri);
//                startActivityForResult(intent, COURSE_EDITOR_REQUEST_CODE);
//            }
//        });
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(Course.this, AssessmentInfo.class);
                Uri uri = Uri.parse(AssessmentsProvider.ASSESSMENTS_CONTENT_URI + "/" + id);
                intent.putExtra(AssessmentsProvider.ASSESSMENT_CONTENT_ITEM_TYPE, uri);
                startActivityForResult(intent, COURSE_EDITOR_REQUEST_CODE);
            }
        });


        getLoaderManager().initLoader(0, null, this);
        setTitleOfCourse(courseId);
    }

    private void setTitleOfCourse(String courseId){
        String filter = DBHelper.COURSE_ID + "=" + courseId;
        Uri uri = Uri.parse(CoursesProvider.COURSES_CONTENT_URI + "/" + courseId);
        Cursor cursor = getContentResolver().query(uri,
                DBHelper.ALL_COURSES_COLUMNS, filter, null, null);
        cursor.moveToFirst();
        String title = cursor.getString(cursor.getColumnIndex(DBHelper.COURSE_TITLE));
        setTitle(title);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString("savedUri", savedUri.toString());
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        savedUri = Uri.parse(savedInstanceState.getString("savedUri"));
        super.onRestoreInstanceState(savedInstanceState);
    }

    public void create(View view) {
        Intent intent = new Intent(this, AssessmentInfo.class);
        Uri uri = Uri.parse(CoursesProvider.COURSES_CONTENT_URI + "/" + courseId);
        intent.putExtra(CoursesProvider.COURSE_CONTENT_ITEM_TYPE, uri);
        startActivityForResult(intent, COURSE_EDITOR_REQUEST_CODE);
    }

    public void getInfo(View view) {
        Intent intent = new Intent(view.getContext(), CourseInfo.class);
        Uri uri = Uri.parse(CoursesProvider.COURSES_CONTENT_URI + "/" + courseId);
        intent.putExtra(CoursesProvider.COURSE_CONTENT_ITEM_TYPE, uri);
        startActivityForResult(intent, COURSE_EDITOR_REQUEST_CODE);
    }

    private void showSnack(String message) {
        Snackbar.make(Course.this.getCurrentFocus(), message, Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
    }

    private void deleteCourse() {
        if (cursorAdapter.getCount() > 0) {
            showSnack("Cannot delete a Course while it has assessments");
        } else {
            getContentResolver().delete(CoursesProvider.COURSES_CONTENT_URI,
                    DBHelper.COURSE_ID + "=" + courseId, null);
            Toast.makeText(this, getString(R.string.course_delete_snack),
                    Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_course, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_delete) {
            deleteCourse();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, AssessmentsProvider.ASSESSMENTS_CONTENT_URI,
                null, courseFilter, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        cursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        cursorAdapter.swapCursor(null);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if ((requestCode == COURSE_EDITOR_REQUEST_CODE) && resultCode == RESULT_OK) {
            restartLoader();
        }
    }

    private void restartLoader() {
        getLoaderManager().restartLoader(0, null, this);
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