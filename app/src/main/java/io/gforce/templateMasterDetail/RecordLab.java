package io.gforce.templateMasterDetail;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import io.gforce.templateMasterDetail.database.RecordBaseHelper;
import io.gforce.templateMasterDetail.database.RecordCursorWrapper;
import io.gforce.templateMasterDetail.database.RecordDbSchema;

/*To create a singleton, you create a class with a private constructor and a get() method. If the instance
already exists, then get() simply returns the instance. If the instance does not exist yet, then get() will
call the constructor to create it.*/

public class RecordLab {

    private static RecordLab sRecordLab;
    // List is an interface that supports an ordered list of objects of a given type
    // Commonly used implementation of List is ArrayList, which uses regular Java array to store the elements

    // Database vars
    private Context mContext;
    private SQLiteDatabase mDatabase;

    public static RecordLab get(Context context) {
        if (sRecordLab == null) {
            sRecordLab = new RecordLab(context);
        }
        return sRecordLab;
    }
    private RecordLab(Context context) {
        // Diamond notation is short hand for mRecords = new ArrayList<Record>();

        //Open Database
        mContext = context.getApplicationContext();
        //Open /data/date/gforce.analog.android.templateMasterDetail/databases/RecordBase.db
        //Creating a new file if DNE. If first time, call onCreate(SQLiteDatabase), save version
        // If not first time, check versions number, if version number in RecordOpenHelper is higher
        // call onUpgrade(SQLiteDatabase, int, int(.
        mDatabase = new RecordBaseHelper(mContext)
                .getWritableDatabase();

        /*for (int i = 0; i < 100; i++) {
            Record c = new Record();
            record.setTitle("Record #" + i);
            record.setCheck0(i % 2 == 0); // Every other one
            record.setPosition(i);
            mRecordss.add(record);
        }*/
    }
    public List<Record> getRecords() {
        //Query for all records, iterate using the cursor, populate record list
        List<Record> records = new ArrayList<>();
        RecordCursorWrapper cursor = queryRecords(null, null);
        try {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                records.add(cursor.getRecord());
                cursor.moveToNext();
            }
        } finally {
            cursor.close();
        }
        return records;
    }

    public void addRecord(Record c) {
        ContentValues values = getContentValues(c);
        // insert(String, String, ContentValues)
        //First argument is table to insert into, last argument is the value
        // Second argument is nullColumnHack. Allows for empty value.
        mDatabase.insert(RecordDbSchema.RecordTable.NAME, null, values);

    }
    public Record getRecord(UUID id) {
        RecordCursorWrapper cursor = queryRecords(
                RecordDbSchema.RecordTable.Cols.UUID + " = ?",
                new String[] { id.toString() }
        );
        try {
            if (cursor.getCount() == 0) {
                return null;
            }
            cursor.moveToFirst();
            return cursor.getRecord();
        } finally {
            cursor.close();
        }
    }
    public File getPhotoFile(Record record) {
        File externalFilesDir = mContext
                .getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (externalFilesDir == null) {
            return null;
        }
        return new File(externalFilesDir, record.getPhotoFilename());
    }

    public void updateRecord(Record record) {
        String uuidString = record.getId().toString();
        ContentValues values = getContentValues(record);
        // update(String, ContentValues, String, String[])
        // update( table, values, where clause, values of arguments in where clause)
        // ? is treated as string, avoid SQL inject attack.
        mDatabase.update(RecordDbSchema.RecordTable.NAME, values,
                RecordDbSchema.RecordTable.Cols.UUID + " = ?",
                new String[] { uuidString });
    }

    private static ContentValues getContentValues(Record record) {
        ContentValues values = new ContentValues();
        values.put(RecordDbSchema.RecordTable.Cols.UUID, record.getId().toString());
        values.put(RecordDbSchema.RecordTable.Cols.TITLE, record.getTitle());
        values.put(RecordDbSchema.RecordTable.Cols.DATE, record.getDate().getTime());
        values.put(RecordDbSchema.RecordTable.Cols.SOLVED, record.isCheck0() ? 1 : 0);
        values.put(RecordDbSchema.RecordTable.Cols.CONTACT, record.getContact());
        return values;
    }

    private RecordCursorWrapper queryRecords(String whereClause, String[] whereArgs) {
        Cursor cursor = mDatabase.query(
                RecordDbSchema.RecordTable.NAME,
                null, // Columns - null selects all columns
                whereClause,
                whereArgs,
                null, // groupBy
                null, // having
                null // orderBy
        );
        return new RecordCursorWrapper(cursor);
    }
}
