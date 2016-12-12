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

import com.josecalles.jobhunt.database.handler.DatabaseQueryHandlerThread;
import com.josecalles.jobhunt.database.handler.DatabaseUpdateHandlerThread;
import com.josecalles.jobhunt.search.model.JobListing;
import com.josecalles.jobhunt.search.model.JobSearchQuery;
import com.josecalles.jobhunt.search.model.JobSearchResult;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public abstract class SearchManager extends BaseDataManager<List<JobListing>> implements DatabaseQueryHandlerThread.OnQueryCompleteListener {

    private JobSearchQuery jobSearchQuery;
    private int startResult = 0;
    private List<Call> inflightCalls;
    private DatabaseQueryHandlerThread queryHandlerThread;
    private DatabaseUpdateHandlerThread updateHandlerThread;


    public SearchManager() {
        super();
        inflightCalls = new ArrayList<>();
        queryHandlerThread = new DatabaseQueryHandlerThread(this);
        updateHandlerThread = new DatabaseUpdateHandlerThread();
        updateHandlerThread.start();
        queryHandlerThread.start();
    }


    public void searchFor(JobSearchQuery jobSearchQuery, boolean fromRecent) {
        if (jobSearchQuery == null) return;
        if (!jobSearchQuery.equals(this.jobSearchQuery)) {
            clear();
            if (!fromRecent) updateHandlerThread.postSaveRecentSearchMessage(jobSearchQuery);
            this.jobSearchQuery = jobSearchQuery;
        } else {
            startResult += 25;
        }
        performJobSearch(jobSearchQuery);

    }


    public void loadMore() {
        searchFor(jobSearchQuery, true);
    }


    public JobSearchQuery getJobSearchQuery() {
        return jobSearchQuery;
    }


    private void performJobSearch(final JobSearchQuery jobSearchQuery) {
        loadStarted();
        final Call<JobSearchResult> jobSearchCall =
                getIndeedSearchService().searchJobWithQuery(jobSearchQuery.getJobTitle(),
                        jobSearchQuery.getLocation(),
                        startResult,
                        jobSearchQuery.getSearchRadius(),
                        jobSearchQuery.getJobType());
        jobSearchCall.enqueue(new Callback<JobSearchResult>() {
            @Override
            public void onResponse(Call<JobSearchResult> call, Response<JobSearchResult> response) {
                if (response.isSuccessful()) {
                    loadFinished();
                    List<JobListing> listings = response.body().results;
                    if (listings != null) {
                        queryHandlerThread.postQueryJobsMessage(listings);
                    }
                    inflightCalls.remove(jobSearchCall);
                } else {
                    failure(jobSearchCall);
                }
            }

            @Override
            public void onFailure(Call<JobSearchResult> call, Throwable t) {
                failure(jobSearchCall);
            }
        });
        inflightCalls.add(jobSearchCall);
    }


    public void stopHandlerThreads() {
        queryHandlerThread.quitSafely();
        updateHandlerThread.quitSafely();
    }


    public void clear() {
        cancelLoading();
        jobSearchQuery = null;
        startResult = 0;
        resetLoadingCount();
    }


    @Override
    public void cancelLoading() {
        if (inflightCalls.size() > 0) {
            for (Call call : inflightCalls) {
                call.cancel();
            }
            inflightCalls.clear();
        }
    }


    private void failure(Call call) {
        loadFinished();
        inflightCalls.remove(call);
    }


    @Override
    public void onQueryComplete(List<JobListing> jobListings) {
        onDataLoaded(jobListings);
    }
}
