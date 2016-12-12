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

package com.josecalles.jobhunt.database.fetch;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Process;

import com.josecalles.jobhunt.database.DatabaseManager;
import com.josecalles.jobhunt.database.JobsDbHelper;
import com.josecalles.jobhunt.search.api.BaseDataManager;
import com.josecalles.jobhunt.search.model.JobSearchQuery;

import java.util.ArrayList;
import java.util.List;


public abstract class DatabaseSearchFetcher extends BaseDataManager<List<JobSearchQuery>> {

    private final static int QUERY_COMPLETE_MSG = 0;

    private Handler mainHandler;

    public DatabaseSearchFetcher() {
        super();
        initializeHandler();
    }

    @SuppressWarnings (value="unchecked")
    private void initializeHandler() {
        mainHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case QUERY_COMPLETE_MSG:
                        List<JobSearchQuery> jobListings = (List<JobSearchQuery>) msg.obj;
                        onDataLoaded(jobListings);
                        break;
                }
            }
        };
    }


    public void fetchAllRecentSearches() {
        new Runnable() {
            @Override
            public void run() {
                Process.setThreadPriority(Process.THREAD_PRIORITY_DEFAULT);
                SQLiteDatabase database = DatabaseManager.getInstance().openDatabase();
                Cursor cursor = database.rawQuery("SELECT * FROM " + JobsDbHelper.SEARCHES_TABLE, null);
                List<JobSearchQuery> recentSearches = new ArrayList<>(cursor.getCount());
                //cursor.moveToLast();
                while (cursor.moveToNext()) {
                    recentSearches.add(JobSearchQuery.fromCursor(cursor));
                }
                cursor.close();
                DatabaseManager.getInstance().closeDatabase();
                Message message = mainHandler.obtainMessage();
                message.what = QUERY_COMPLETE_MSG;
                message.obj = recentSearches;
                mainHandler.sendMessage(message);
            }
        }.run();
    }


    @Override
    public void cancelLoading() {

    }

}
