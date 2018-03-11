package com.example.sagegatzke.wguscheduler;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

/**
 * Created by sagegatzke on 3/4/18.
 */

public class TermsProvider extends ContentProvider {
    private static final String AUTHORITY = "com.example.sagegatzke.wguscheduler.termsprovider";
    private static final String BASE_PATH = "terms";
    public static final Uri TERMS_CONTENT_URI =
            Uri.parse("content://" + AUTHORITY + "/" + BASE_PATH );

    // Constant to identify the requested operation
    private static final int TERMS = 1;
    private static final int TERMS_ID = 2;

    private static final UriMatcher uriMatcher =
            new UriMatcher(UriMatcher.NO_MATCH);

    public static final String TERM_CONTENT_ITEM_TYPE = "Term";

    static {
        uriMatcher.addURI(AUTHORITY, BASE_PATH, TERMS);
        uriMatcher.addURI(AUTHORITY, BASE_PATH +  "/#", TERMS_ID);
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

        if (uriMatcher.match(uri) == TERMS_ID) {
            selection = DBHelper.TERM_ID + "=" + uri.getLastPathSegment();
        }

        return database.query(DBHelper.TABLE_TERMS, DBHelper.ALL_TERMS_COLUMNS,
                selection, null, null, null,
                DBHelper.TERM_TITLE + " ASC");
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        long id = database.insert(DBHelper.TABLE_TERMS,
                null, values);
        return Uri.parse(BASE_PATH + "/" + id);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return database.delete(DBHelper.TABLE_TERMS, selection, selectionArgs);
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return database.update(DBHelper.TABLE_TERMS,
                values, selection, selectionArgs);
    }



}
