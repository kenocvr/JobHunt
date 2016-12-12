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
import com.josecalles.jobhunt.search.constant.JobType;

import java.io.Serializable;
import java.util.Objects;


public final class JobSearchQuery implements Serializable {

    private final String jobTitle;
    private final String location;
    private final int searchRadius;
    private final String jobType;


    public JobSearchQuery(String jobTitle, String location, int searchRadius, String jobType) {
        this.jobTitle = jobTitle;
        this.location = location;
        this.searchRadius = searchRadius;
        this.jobType = jobType;
    }


    public static JobSearchQuery fromCursor(Cursor cursor) {
        String jobTitle = cursor.getString(cursor.getColumnIndex(JobsDbHelper.SEARCHES_TITLE));
        String jobLocation = cursor.getString(cursor.getColumnIndex(JobsDbHelper.SEARCHES_LOCATION));
        String jobType = cursor.getString(cursor.getColumnIndex(JobsDbHelper.SEARCHES_TYPE));
        return new JobSearchQuery(jobTitle, jobLocation, 25, jobType);
    }


    public int getSearchRadius() {
        return searchRadius;
    }


    public String getJobType() {
        return jobType;
    }


    public String getJobTitle() {
        return jobTitle;
    }


    public String getLocation() {
        return location;
    }


    public String getFormattedJobType() {
        switch (jobType) {
            case JobType.FULL_TIME:
                return "Full-time";
            case JobType.PART_TIME:
                return "Part-time";
            case JobType.CONTRACT:
                return "Contract";
            case JobType.INTERNSHIP:
                return "Internship";
            case JobType.TEMPORARY:
                return "Temporary";
            default:
                return "";
        }
    }


    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof JobSearchQuery)) return false;
        JobSearchQuery jobSearchQuery = (JobSearchQuery) obj;
        return this.searchRadius == jobSearchQuery.getSearchRadius()
                && Objects.equals(this.jobTitle, jobSearchQuery.getJobTitle())
                && Objects.equals(this.location, jobSearchQuery.getLocation())
                && Objects.equals(this.jobTitle, jobSearchQuery.getJobType());
    }


    @Override
    public int hashCode() {
        return Objects.hash(jobTitle, location, searchRadius, jobType);
    }
}
