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


package com.josecalles.jobhunt.search.model;

import android.database.Cursor;
import com.josecalles.jobhunt.database.JobsDbHelper;

public class JobListing {

    public String jobkey;
    public String jobtitle;
    public String company;
    public String formattedLocation;
    public String date;
    public String snippet;
    public String url;
    public boolean sponsored;
    public float latitude;
    public float longitude;

    public boolean saved;
    public boolean applied;
    public long appliedTime;


    public static JobListing fromCursor(Cursor cursor) {
        JobListing jobListing = new JobListing();
        jobListing.jobkey = cursor.getString(cursor.getColumnIndex(JobsDbHelper.JOB_KEY));
        jobListing.jobtitle = cursor.getString(cursor.getColumnIndex(JobsDbHelper.JOB_TITLE));
        jobListing.company = cursor.getString(cursor.getColumnIndex(JobsDbHelper.JOB_COMPANY));
        jobListing.formattedLocation = cursor.getString(cursor.getColumnIndex(JobsDbHelper.JOB_LOCATION));
        jobListing.date = cursor.getString(cursor.getColumnIndex(JobsDbHelper.JOB_DATE));
        jobListing.snippet = cursor.getString(cursor.getColumnIndex(JobsDbHelper.JOB_SNIPPET));
        jobListing.url = cursor.getString(cursor.getColumnIndex(JobsDbHelper.JOB_URL));
        jobListing.saved = cursor.getInt(cursor.getColumnIndex(JobsDbHelper.JOB_SAVED)) == 1;
        jobListing.applied = cursor.getInt(cursor.getColumnIndex(JobsDbHelper.JOB_APPLIED)) == 1;
        int appliedTimeIndex = cursor.getColumnIndex(JobsDbHelper.JOB_APPLIED_TIME);
        if (!cursor.isNull(appliedTimeIndex)) {
            jobListing.appliedTime = cursor.getLong(appliedTimeIndex);
        }
        return jobListing;
    }

}
