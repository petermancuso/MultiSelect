package io.gforce.templateMasterDetail.database;

import android.database.Cursor;
import android.database.CursorWrapper;

import java.util.Date;
import java.util.UUID;

import io.gforce.templateMasterDetail.Record;


public class RecordCursorWrapper extends CursorWrapper {
    public RecordCursorWrapper(Cursor cursor) {
        super(cursor);
    }

    public Record getRecord() {
        //Parse underlying cursor
        String uuidString = getString(getColumnIndex(RecordDbSchema.RecordTable.Cols.UUID));
        String title = getString(getColumnIndex(RecordDbSchema.RecordTable.Cols.TITLE));
        long date = getLong(getColumnIndex(RecordDbSchema.RecordTable.Cols.DATE));
        int isSolved = getInt(getColumnIndex(RecordDbSchema.RecordTable.Cols.SOLVED));
        String suspect = getString(getColumnIndex(RecordDbSchema.RecordTable.Cols.CONTACT));

        //Create, populate and return new record.
        Record record = new Record(UUID.fromString(uuidString));
        record.setTitle(title);
        record.setDate(new Date(date));
        record.setCheck0(isSolved != 0);
        record.setContact(suspect);
        return record;
    }
}
