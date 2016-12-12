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


package com.josecalles.jobhunt.search.api;

import com.josecalles.jobhunt.BuildConfig;
import com.josecalles.jobhunt.search.model.JobSearchResult;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface IndeedSearchService {

    String BASE_URL = "http://api.indeed.com/ads/";
    String JOB_TITLE_QUERY = "q";
    String JOB_LOCATION_QUERY = "l";
    String JOB_START_QUERY = "start";
    String JOB_RADIUS_QUERY = "radius";
    String JOB_TYPE_QUERY = "jt";

    @GET("apisearch?publisher=" + BuildConfig.INDEED_PUBLISHER_ID + "&limit=25&highlight=0&format=json&v=2")
    Call<JobSearchResult> searchJobWithQuery(@Query(JOB_TITLE_QUERY) String jobTitle,
                                             @Query(JOB_LOCATION_QUERY) String location, @Query(JOB_START_QUERY) int startResult, @Query(JOB_RADIUS_QUERY) int radius, @Query(JOB_TYPE_QUERY) String jobType);


}
