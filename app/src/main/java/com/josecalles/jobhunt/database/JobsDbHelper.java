/*
 *
 * Copyright 2016,  Jose Calles
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.josecalles.jobhunt.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class JobsDbHelper extends SQLiteOpenHelper {

    private static final int DB_VERSION = 1;
    private static final String DB_NAME = "Jobs.db";

    public static final String JOB_TABLE = "jobs_table";
    public static final String JOB_ID = "_id";
    public static final String JOB_KEY = "job_key";
    public static final String JOB_TITLE = "job_title";
    public static final String JOB_COMPANY = "company";
    public static final String JOB_LOCATION = "location";
    public static final String JOB_DATE = "date";
    public static final String JOB_SNIPPET = "snippet";
    public static final String JOB_URL = "url";
    public static final String JOB_SPONSORED = "sponsored";
    public static final String JOB_LATITUDE = "latitude";
    public static final String JOB_LONGITUDE = "longitude";
    public static final String JOB_SAVED = "saved";
    public static final String JOB_APPLIED = "applied";
    public static final String JOB_APPLIED_TIME = "applied_time";

    public static final String SEARCHES_TABLE = "searches_table";
    public static final String SEARCHES_ID = "_id";
    public static final String SEARCHES_TITLE = "search_title";
    public static final String SEARCHES_LOCATION = "search_location";
    public static final String SEARCHES_TYPE = "search_type";


    public JobsDbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "CREATE TABLE " + JOB_TABLE + "(" +
                        JOB_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL" + ", " +
                        JOB_KEY + " TEXT NOT NULL" + ", " +
                        JOB_TITLE + " TEXT NOT NULL" + ", " +
                        JOB_COMPANY + " TEXT NOT NULL"  + ", " +
                        JOB_LOCATION + " TEXT NOT NULL" + ", " +
                        JOB_DATE + " TEXT NOT NULL" + ", " +
                        JOB_SNIPPET + " TEXT NOT NULL" + ", " +
                        JOB_URL + " TEXT NOT NULL" + ", " +
                        JOB_SPONSORED + " INTEGER NOT NULL" + ", " +
                        JOB_LATITUDE + " REAL" +  ", " +
                        JOB_LONGITUDE + " REAL" + ", " +
                        JOB_SAVED + " INTEGER NOT NULL" + ", " +
                        JOB_APPLIED + " INTEGER NOT NULL" + ", " +
                        JOB_APPLIED_TIME + " INTEGER" +
                        ")"
        );

        db.execSQL(
                "CREATE TABLE " + SEARCHES_TABLE + "(" +
                        SEARCHES_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL" + ", " +
                        SEARCHES_TITLE + " TEXT NOT NULL" + ", " +
                        SEARCHES_LOCATION + " TEXT NOT NULL" + ", " +
                        SEARCHES_TYPE + " TEXT NOT NULL" +
                        ")"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + JOB_TABLE);
            db.execSQL("DROP TABLE IF EXISTS " + SEARCHES_TABLE);
            onCreate(db);
    }
}
