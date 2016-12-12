/*
 * From Dmytro Danylyk. http://www.dmytrodanylyk.com/concurrent-database-access/
 */

package com.josecalles.jobhunt.database;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import timber.log.Timber;


public class DatabaseManager {

    private int mOpenCounter;
    private static DatabaseManager instance;
    private static SQLiteOpenHelper mDatabaseHelper;
    private SQLiteDatabase mDatabase;


    public static synchronized void initializeInstance(SQLiteOpenHelper helper) {
        if (instance == null) {
            instance = new DatabaseManager();
            mDatabaseHelper = helper;
        }
    }


    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException(DatabaseManager.class.getSimpleName() +
                    " is not initialized, call initializeInstance(..) method first.");
        }
        return instance;
    }


    public synchronized SQLiteDatabase openDatabase() {
        mOpenCounter++;
        if(mOpenCounter == 1) {
            mDatabase = mDatabaseHelper.getWritableDatabase();
        }
        return mDatabase;
    }


    public synchronized void closeDatabase() {
        mOpenCounter--;
        if(mOpenCounter == 0) {
            mDatabase.close();

        }
    }


    public static void printSavedJobTable() {
        SQLiteDatabase db = getInstance().openDatabase();
        String tableString = String.format("Table %s:\n", JobsDbHelper.JOB_TABLE);
        Cursor allRows  = db.rawQuery("SELECT * FROM " + JobsDbHelper.JOB_TABLE, null);
        if (allRows.moveToFirst() ){
            String[] columnNames = allRows.getColumnNames();
            do {
                for (String name: columnNames) {
                    tableString += String.format("%s: %s\n", name,
                            allRows.getString(allRows.getColumnIndex(name)));
                }
                tableString += "\n";

            } while (allRows.moveToNext());
        }
        allRows.close();
        getInstance().closeDatabase();
        Timber.i(tableString);
    }


    public static void printSavedSearchesTable() {
        SQLiteDatabase db = getInstance().openDatabase();
        String tableString = String.format("Table %s:\n", JobsDbHelper.SEARCHES_TABLE);
        Cursor allRows  = db.rawQuery("SELECT * FROM " + JobsDbHelper.SEARCHES_TABLE, null);
        if (allRows.moveToFirst() ){
            String[] columnNames = allRows.getColumnNames();
            do {
                for (String name: columnNames) {
                    tableString += String.format("%s: %s\n", name,
                            allRows.getString(allRows.getColumnIndex(name)));
                }
                tableString += "\n";

            } while (allRows.moveToNext());
        }
        allRows.close();
        getInstance().closeDatabase();
        Timber.i(tableString);
    }
}
