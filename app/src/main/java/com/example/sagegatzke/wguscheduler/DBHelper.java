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


    public static final String TABLE_COURSES = "courses";
    public static final String COURSE_ID = "_id";
    public static final String COURSE_TITLE = "courseTitle";
    public static final String COURSE_START = "courseStart";
    public static final String COURSE_END = "courseEnd";
    public static final String COURSE_STATUS = "courseStatus";
    public static final String COURSE_MENTOR_NAME = "courseMentorName";
    public static final String COURSE_MENTOR_EMAIL = "courseMentorEmail";
    public static final String COURSE_MENTOR_NUMBER = "courseMentorNumber";
    public static final String COURSE_NOTES = "courseNotes";
    public static final String COURSE_TERM_ID = "termId";

    public static final String TABLE_ASSESSMENTS = "assessments";
    public static final String ASSESSMENT_ID = "_id";
    public static final String ASSESSMENT_TITLE = "assessmentTitle";
    public static final String ASSESSMENT_DUE_DATE = "assessmentDueDate";
    public static final String ASSESSMENT_TYPE = "assessmentType";
    public static final String ASSESSMENT_COURSE_ID = "courseId";


    public static final String[] ALL_TERMS_COLUMNS =
            {TERM_ID, TERM_TITLE, TERM_START, TERM_END};
    public static final String[] ALL_COURSES_COLUMNS =
            {COURSE_ID, COURSE_TITLE, COURSE_START, COURSE_END, COURSE_STATUS, COURSE_MENTOR_NAME, COURSE_MENTOR_EMAIL, COURSE_MENTOR_NUMBER, COURSE_NOTES, COURSE_TERM_ID};
    public static final String[] ALL_ASSESSMENTS_COLUMNS =
            {ASSESSMENT_ID, ASSESSMENT_TITLE, ASSESSMENT_DUE_DATE, ASSESSMENT_TYPE, ASSESSMENT_COURSE_ID};

    private static final String TABLE_TERMS_CREATE =
            "CREATE TABLE " + TABLE_TERMS + " (" +
                    TERM_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    TERM_TITLE + " TEXT, " +
                    TERM_START + " TEXT, " +
                    TERM_END + " TEXT " +
                    ")";
    private static final String TABLE_COURSES_CREATE =
            "CREATE TABLE " + TABLE_COURSES + " (" +
                    COURSE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COURSE_TITLE + " TEXT, " +
                    COURSE_START + " TEXT, " +
                    COURSE_END + " TEXT, " +
                    COURSE_STATUS + " TEXT, " +
                    COURSE_MENTOR_NAME + " TEXT, " +
                    COURSE_MENTOR_EMAIL + " TEXT, " +
                    COURSE_MENTOR_NUMBER + " TEXT, " +
                    COURSE_NOTES + " TEXT, " +
                    COURSE_TERM_ID + " INTEGER " +
                    ")";
    private static final String TABLE_ASSESSMENTS_CREATE =
            "CREATE TABLE " + TABLE_ASSESSMENTS + " (" +
                    ASSESSMENT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    ASSESSMENT_TITLE + " TEXT, " +
                    ASSESSMENT_DUE_DATE + " TEXT, " +
                    ASSESSMENT_TYPE + " TEXT, " +
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
