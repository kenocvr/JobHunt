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

package com.josecalles.jobhunt.database.handler;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.Process;

import com.josecalles.jobhunt.database.DatabaseManager;
import com.josecalles.jobhunt.database.JobsDbHelper;
import com.josecalles.jobhunt.search.model.JobListing;
import com.josecalles.jobhunt.search.model.JobSearchQuery;


public class DatabaseUpdateHandlerThread extends HandlerThread {

    private static final int SAVE_JOB_MESSAGE = 0;
    private static final int UNSAVE_JOB_MESSAGE = 1;
    private static final int APPLIED_TO_JOB_MESSAGE = 2;
    private static final int NOT_APPLIED_TO_JOB_MESSAGE = 3;
    private static final int SAVE_JOB_QUERY_MESSAGE = 4;

    private Handler handler;


    public DatabaseUpdateHandlerThread() {
        super("DatabaseUpdateHandlerThread",Process.THREAD_PRIORITY_DEFAULT);
    }


    @Override
    protected void onLooperPrepared() {
        super.onLooperPrepared();
        handler = new Handler(getLooper()) {
            @Override
            public void handleMessage(Message message) {
                switch (message.what) {
                    case SAVE_JOB_MESSAGE:
                        JobListing jobToSave = (JobListing) message.obj;
                        saveJobListing(jobToSave);
                        break;
                    case UNSAVE_JOB_MESSAGE:
                        JobListing jobToUnsave = (JobListing) message.obj;
                        unsaveJobListing(jobToUnsave);
                        break;
                    case APPLIED_TO_JOB_MESSAGE:
                        JobListing jobToApply = (JobListing) message.obj;
                        appliedToJob(jobToApply);
                        break;
                    case NOT_APPLIED_TO_JOB_MESSAGE:
                        JobListing jobToNotApply = (JobListing) message.obj;
                        notAppliedToJob(jobToNotApply);
                        break;
                    case SAVE_JOB_QUERY_MESSAGE:
                        JobSearchQuery query = (JobSearchQuery) message.obj;
                        saveRecentSearch(query);
                    default:
                        break;
                }


            }
        };
    }


    public void postJobSavedMessage(JobListing job) {
        Message message = Message.obtain();
        message.obj = job;
        message.what = SAVE_JOB_MESSAGE;
        handler.sendMessage(message);

    }


    public void postJobUnsavedMessage(JobListing job) {
        Message message = Message.obtain();
        message.obj = job;
        message.what = UNSAVE_JOB_MESSAGE;
        handler.sendMessage(message);
    }


    public void postAppliedToJobMessage(JobListing job) {
        Message message = Message.obtain();
        message.obj = job;
        message.what = APPLIED_TO_JOB_MESSAGE;
        handler.sendMessage(message);
    }


    public void postNotAppliedToJobMessage(JobListing job) {
        Message message = Message.obtain();
        message.obj = job;
        message.what = NOT_APPLIED_TO_JOB_MESSAGE;
        handler.sendMessage(message);
    }


    public void postSaveRecentSearchMessage(JobSearchQuery query) {
        Message message = Message.obtain();
        message.obj = query;
        message.what = SAVE_JOB_QUERY_MESSAGE;
        handler.sendMessage(message);
    }


    private void saveJobListing(JobListing jobListing ) {
        ContentValues jobValues = new ContentValues();
        jobValues.put(JobsDbHelper.JOB_KEY, jobListing.jobkey);
        jobValues.put(JobsDbHelper.JOB_TITLE, jobListing.jobtitle);
        jobValues.put(JobsDbHelper.JOB_COMPANY, jobListing.company);
        jobValues.put(JobsDbHelper.JOB_LOCATION, jobListing.formattedLocation);
        jobValues.put(JobsDbHelper.JOB_DATE, jobListing.date);
        jobValues.put(JobsDbHelper.JOB_SNIPPET, jobListing.snippet);
        jobValues.put(JobsDbHelper.JOB_URL, jobListing.url);
        jobValues.put(JobsDbHelper.JOB_SPONSORED, jobListing.sponsored);
        jobValues.put(JobsDbHelper.JOB_LATITUDE, jobListing.latitude);
        jobValues.put(JobsDbHelper.JOB_LONGITUDE, jobListing.longitude);
        jobValues.put(JobsDbHelper.JOB_SAVED, 1);
        jobValues.put(JobsDbHelper.JOB_APPLIED, 0);
        SQLiteDatabase database = DatabaseManager.getInstance().openDatabase();
        database.insert(JobsDbHelper.JOB_TABLE, null,  jobValues);
        DatabaseManager.getInstance().closeDatabase();
    }


    private void appliedToJob(JobListing jobListing) {
        ContentValues values = new ContentValues();
        values.put(JobsDbHelper.JOB_APPLIED, 1);
        values.put(JobsDbHelper.JOB_APPLIED_TIME, System.currentTimeMillis());
        SQLiteDatabase database = DatabaseManager.getInstance().openDatabase();
        database.update(JobsDbHelper.JOB_TABLE, values, JobsDbHelper.JOB_KEY + " = " + "'" + jobListing.jobkey + "'", null);
        DatabaseManager.getInstance().closeDatabase();
    }


    private void notAppliedToJob(JobListing jobListing) {
        ContentValues values = new ContentValues();
        values.put(JobsDbHelper.JOB_APPLIED, 0);
        values.putNull(JobsDbHelper.JOB_APPLIED_TIME);
        SQLiteDatabase database = DatabaseManager.getInstance().openDatabase();
        database.update(JobsDbHelper.JOB_TABLE, values, JobsDbHelper.JOB_KEY + " = " + "'" + jobListing.jobkey + "'", null);
        DatabaseManager.getInstance().closeDatabase();
    }


    private void unsaveJobListing(JobListing jobListing) {
        ContentValues values = new ContentValues();
        values.put(JobsDbHelper.JOB_SAVED, 0);
        SQLiteDatabase database = DatabaseManager.getInstance().openDatabase();
        database.update(JobsDbHelper.JOB_TABLE, values, JobsDbHelper.JOB_KEY + " = " + "'" + jobListing.jobkey + "'", null);
        DatabaseManager.getInstance().closeDatabase();

    }


    private void saveRecentSearch(JobSearchQuery query) {
        ContentValues queryValues = new ContentValues();
        queryValues.put(JobsDbHelper.SEARCHES_TITLE, query.getJobTitle());
        queryValues.put(JobsDbHelper.SEARCHES_TYPE, query.getJobType());
        queryValues.put(JobsDbHelper.SEARCHES_LOCATION, query.getLocation());
        SQLiteDatabase database = DatabaseManager.getInstance().openDatabase();
        database.insert(JobsDbHelper.SEARCHES_TABLE, null, queryValues);
        DatabaseManager.getInstance().closeDatabase();
    }
}
