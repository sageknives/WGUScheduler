package com.example.sagegatzke.wguscheduler;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by sagegatzke on 3/4/18.
 */

public class DBHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "schedule.db";

    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_TERMS = "terms";
    public static final String TERM_ID = "_id";
    public static final String TERM_TITLE = "termTitle";
    public static final String TERM_START = "termStart";
    public static final String TERM_END = "termEnd";
    public static final String TERM_CREATED = "termCreated";


    public static final String TABLE_COURSES = "courses";
    public static final String COURSE_ID = "_id";
    public static final String COURSE_TITLE = "courseTitle";
    public static final String COURSE_START = "courseStart";
    public static final String COURSE_END = "courseEnd";
    public static final String COURSE_STATUS = "courseStatus";
    public static final String COURSE_CREATED = "courseCreated";
    public static final String COURSE_TERM_ID = "termId";

    public static final String TABLE_ASSESSMENTS = "assessments";
    public static final String ASSESSMENT_ID = "_id";
    public static final String ASSESSMENT_TITLE = "assessmentTitle";
    public static final String ASSESSMENT_START = "assessmentStart";
    public static final String ASSESSMENT_END = "assessmentEnd";
    public static final String ASSESSMENT_CREATED = "assessmentCreated";
    public static final String ASSESSMENT_COURSE_ID = "courseId";


    public static final String[] ALL_TERMS_COLUMNS =
            {TERM_ID, TERM_TITLE, TERM_START, TERM_END, TERM_CREATED};
    public static final String[] ALL_COURSES_COLUMNS =
            {COURSE_ID, COURSE_TITLE, COURSE_START, COURSE_END, COURSE_STATUS, COURSE_TERM_ID, COURSE_CREATED};
    public static final String[] ALL_ASSESSMENTS_COLUMNS =
            {ASSESSMENT_ID, ASSESSMENT_TITLE, ASSESSMENT_START, ASSESSMENT_END, ASSESSMENT_COURSE_ID, ASSESSMENT_CREATED};

    private static final String TABLE_TERMS_CREATE =
            "CREATE TABLE " + TABLE_TERMS + " (" +
                    TERM_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    TERM_TITLE + " TEXT, " +
                    TERM_START + " TEXT, " +
                    TERM_END + " TEXT, " +
                    TERM_CREATED + " TEXT default CURRENT_TIMESTAMP" +
                    ")";
    private static final String TABLE_COURSES_CREATE =
            "CREATE TABLE " + TABLE_COURSES + " (" +
                    COURSE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COURSE_TITLE + " TEXT, " +
                    COURSE_START + " TEXT, " +
                    COURSE_END + " TEXT, " +
                    COURSE_STATUS + " TEXT, " +
                    COURSE_CREATED + " TEXT default CURRENT_TIMESTAMP," +
                    COURSE_TERM_ID + " INTEGER " +
                    ")";
    private static final String TABLE_ASSESSMENTS_CREATE =
            "CREATE TABLE " + TABLE_ASSESSMENTS + " (" +
                    ASSESSMENT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    ASSESSMENT_TITLE + " TEXT, " +
                    ASSESSMENT_START + " TEXT, " +
                    ASSESSMENT_END + " TEXT, " +
                    ASSESSMENT_CREATED + " TEXT default CURRENT_TIMESTAMP," +
                    ASSESSMENT_COURSE_ID + " INTEGER " +
                    ")";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL(TABLE_TERMS_CREATE);
        db.execSQL(TABLE_COURSES_CREATE);
        db.execSQL(TABLE_ASSESSMENTS_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TERMS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_COURSES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ASSESSMENTS);
        onCreate(db);
    }


}
