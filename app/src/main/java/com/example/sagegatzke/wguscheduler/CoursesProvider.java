package com.example.sagegatzke.wguscheduler;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

/**
 * Created by sagegatzke on 3/4/18.
 */

public class CoursesProvider extends ContentProvider {
    private static final String AUTHORITY = "com.example.sagegatzke.wguscheduler.coursesprovider";
    private static final String BASE_PATH = "courses";
    public static final Uri COURSES_CONTENT_URI =
            Uri.parse("content://" + AUTHORITY + "/" + BASE_PATH);

    // Constant to identify the requested operation
    private static final int COURSES = 1;
    private static final int COURSES_ID = 2;

    private static final UriMatcher uriMatcher =
            new UriMatcher(UriMatcher.NO_MATCH);

    public static final String COURSE_CONTENT_ITEM_TYPE = "Course";

    static {
        uriMatcher.addURI(AUTHORITY, BASE_PATH, COURSES);
        uriMatcher.addURI(AUTHORITY, BASE_PATH + "/#", COURSES_ID);
    }

    private SQLiteDatabase database;

    @Override
    public boolean onCreate() {

        DBHelper helper = new DBHelper(getContext());
        database = helper.getWritableDatabase();
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        if (uriMatcher.match(uri) == COURSES_ID) {
            selection = DBHelper.COURSE_ID + "=" + uri.getLastPathSegment();
        }

        return database.query(DBHelper.TABLE_COURSES, DBHelper.ALL_COURSES_COLUMNS,
                selection, null, null, null,
                DBHelper.COURSE_TITLE + " ASC");
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        Log.d("course", "insert: course");
        long id = database.insert(DBHelper.TABLE_COURSES,
                null, values);
        return Uri.parse(BASE_PATH + "/" + id);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return database.delete(DBHelper.TABLE_COURSES, selection, selectionArgs);
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return database.update(DBHelper.TABLE_COURSES,
                values, selection, selectionArgs);
    }
}