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

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Process;

import com.josecalles.jobhunt.database.DatabaseManager;
import com.josecalles.jobhunt.database.JobsDbHelper;
import com.josecalles.jobhunt.search.model.JobListing;

import java.util.List;


public class DatabaseQueryHandlerThread extends HandlerThread {

    private static final int QUERY_JOBS_MESSAGE = 0;
    private Handler handler;
    private OnQueryCompleteListener onQueryCompleteListener;


    public interface OnQueryCompleteListener {
        void onQueryComplete(List<JobListing> jobListings);
    }


    public DatabaseQueryHandlerThread(OnQueryCompleteListener onQueryCompleteListener) {
        super("DatabaseUpdateHandlerThread", Process.THREAD_PRIORITY_DEFAULT);
        this.onQueryCompleteListener = onQueryCompleteListener;
    }


    @SuppressWarnings("unchecked")
    @Override
    protected void onLooperPrepared() {
        super.onLooperPrepared();
        handler = new Handler(getLooper()) {
            @Override
            public void handleMessage(Message message) {
                switch (message.what) {
                    case QUERY_JOBS_MESSAGE:
                        List<JobListing> jobList = (List<JobListing>) message.obj;
                        queryJobListForStatus(jobList);
                        break;
                    default:
                        break;
                }
            }
        };
    }


    public void postQueryJobsMessage(List<JobListing> jobList) {
        Message message = Message.obtain();
        message.obj = jobList;
        message.what = QUERY_JOBS_MESSAGE;
        handler.sendMessage(message);
    }


    private void queryJobListForStatus(final List<JobListing> jobList) {
        SQLiteDatabase database = DatabaseManager.getInstance().openDatabase();
        Cursor cursor = null;
        for (JobListing jobListing : jobList) {
            cursor = database.rawQuery("SELECT " + JobsDbHelper.JOB_SAVED + ", " + JobsDbHelper.JOB_APPLIED
                    + " FROM " + JobsDbHelper.JOB_TABLE + " WHERE " + JobsDbHelper.JOB_KEY
                    + " = " + "'" + jobListing.jobkey + "'", null);
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                int saved = cursor.getInt(cursor.getColumnIndex(JobsDbHelper.JOB_SAVED));
                int applied = cursor.getInt(cursor.getColumnIndex(JobsDbHelper.JOB_APPLIED));
                jobListing.saved = saved == 1;
                jobListing.applied = applied == 1;
            }
        }
        if (cursor != null)
        cursor.close();
        DatabaseManager.getInstance().closeDatabase();
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                onQueryCompleteListener.onQueryComplete(jobList);
            }
        });
    }

}
