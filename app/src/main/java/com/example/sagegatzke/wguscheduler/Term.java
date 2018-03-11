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

public class Term extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int TERM_EDITOR_REQUEST_CODE = 103;
    private String termId;
    private Uri savedUri;
    private CursorAdapter cursorAdapter;
    private String termFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_term);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        Intent intent = getIntent();
        if (savedUri == null) {
            savedUri = intent.getParcelableExtra(TermsProvider.TERM_CONTENT_ITEM_TYPE);
        }
        termId = savedUri.getLastPathSegment();
        termFilter = DBHelper.COURSE_TERM_ID + "=" + termId;

        cursorAdapter = new CoursesCursorAdapter(this, null, 0);

        ListView list = (ListView) findViewById(android.R.id.list);
        list.setAdapter(cursorAdapter);


        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(Term.this, Course.class);
                Uri uri = Uri.parse(CoursesProvider.COURSES_CONTENT_URI + "/" + id);
                intent.putExtra(CoursesProvider.COURSE_CONTENT_ITEM_TYPE, uri);
                startActivityForResult(intent, TERM_EDITOR_REQUEST_CODE);
            }
        });


        getLoaderManager().initLoader(0, null, this);
        setTitleOfTerm(termId);
    }

    private void setTitleOfTerm(String termId){
        String filter = DBHelper.TERM_ID + "=" + termId;
        Uri uri = Uri.parse(TermsProvider.TERMS_CONTENT_URI + "/" + termId);
        Cursor cursor = getContentResolver().query(uri,
                DBHelper.ALL_TERMS_COLUMNS, filter, null, null);
        cursor.moveToFirst();
        String title = cursor.getString(cursor.getColumnIndex(DBHelper.TERM_TITLE));
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
        Intent intent = new Intent(this, CourseInfo.class);
        Uri uri = Uri.parse(TermsProvider.TERMS_CONTENT_URI + "/" + termId);
        intent.putExtra(TermsProvider.TERM_CONTENT_ITEM_TYPE, uri);
        startActivityForResult(intent, TERM_EDITOR_REQUEST_CODE);
    }

    public void getInfo(View view) {
        Intent intent = new Intent(view.getContext(), TermInfo.class);
        Uri uri = Uri.parse(TermsProvider.TERMS_CONTENT_URI + "/" + termId);
        intent.putExtra(TermsProvider.TERM_CONTENT_ITEM_TYPE, uri);
        startActivityForResult(intent, TERM_EDITOR_REQUEST_CODE);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, CoursesProvider.COURSES_CONTENT_URI,
                null, termFilter, null, null);
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
        if ((requestCode == TERM_EDITOR_REQUEST_CODE) && resultCode == RESULT_OK) {
            restartLoader();
        }
    }

    private void deleteTerm() {
        if (cursorAdapter.getCount() > 0) {
            showSnack("Cannot delete a term while it has courses");
        } else {
            getContentResolver().delete(TermsProvider.TERMS_CONTENT_URI,
                    DBHelper.TERM_ID + "=" + termId, null);
            //showSnack(getString(R.string.term_delete_snack));
            Toast.makeText(this, getString(R.string.term_delete_snack),
                    Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        }

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_term, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_delete) {
            deleteTerm();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showSnack(String message) {
        Snackbar.make(Term.this.getCurrentFocus(), message, Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
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
