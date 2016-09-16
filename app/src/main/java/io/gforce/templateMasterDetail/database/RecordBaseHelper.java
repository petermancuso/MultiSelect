package io.gforce.templateMasterDetail.database;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import io.gforce.templateMasterDetail.database.RecordDbSchema.RecordTable;

public class RecordBaseHelper extends SQLiteOpenHelper {
    private static final int VERSION = 1;
    private static final String DATABASE_NAME = "recordBase.db";

    public RecordBaseHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + RecordTable.NAME+ "(" +
                " _id integer primary key autoincrement, " +
                RecordTable.Cols.UUID + ", " +
                RecordTable.Cols.TITLE + ", " +
                RecordTable.Cols.DATE + ", " +
                RecordTable.Cols.SOLVED + "," +
                RecordTable.Cols.CONTACT +
                ")"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}

