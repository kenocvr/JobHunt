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

package com.josecalles.jobhunt.applied.ui;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.josecalles.jobhunt.R;
import com.josecalles.jobhunt.database.handler.DatabaseUpdateHandlerThread;
import com.josecalles.jobhunt.search.model.JobListing;
import com.josecalles.jobhunt.util.ChromeTabUtils;
import com.josecalles.jobhunt.util.TimeUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


public class AppliedJobListingAdapter extends RecyclerView.Adapter<AppliedJobListingAdapter.AppliedJobViewHolder> {

    private List<JobListing> jobListingDataSet;
    private DatabaseUpdateHandlerThread handlerThread;


    public AppliedJobListingAdapter() {
        jobListingDataSet = new ArrayList<>();
        handlerThread = new DatabaseUpdateHandlerThread();
        handlerThread.start();
    }


    @Override
    public void onBindViewHolder(final AppliedJobViewHolder holder, int position) {
        final JobListing jobListing = jobListingDataSet.get(position);
        holder.companyTextView.setText(jobListing.company);
        holder.snippetTextView.setText(jobListing.snippet);
        holder.jobTitleTextView.setText(jobListing.jobtitle + " in " + jobListing.formattedLocation);
        holder.appliedTimeTextView.setText(TimeUtils.getTimeAgo(jobListing.appliedTime));
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                JobListing selectedJobListing = jobListingDataSet.get(holder.getAdapterPosition());
                ChromeTabUtils.openTabWithUrl(view.getContext(), selectedJobListing.url);
            }
        });
    }


    @Override
    public int getItemCount() {
        return jobListingDataSet.size();
    }


    public void addJobListingsToDataSet(List<JobListing> jobListings) {
        jobListingDataSet.addAll(jobListings);
        notifyDataSetChanged();
    }


    @Override
    public AppliedJobViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new AppliedJobViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_applied_job_listing, parent, false));
    }


    public void clearDataSet() {
        jobListingDataSet.clear();
        notifyDataSetChanged();
    }



    public void stopHandlerThread(){
        handlerThread.quitSafely();
    }

    static class AppliedJobViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.item_applied_job_background)
        public CardView cardView;
        @BindView(R.id.item_applied_job_company)
        public TextView companyTextView;
        @BindView(R.id.item_applied_job_title)
        public TextView jobTitleTextView;
        @BindView(R.id.item_applied_job_snippet)
        public TextView snippetTextView;
        @BindView(R.id.item_applied_time_ago)
        public TextView appliedTimeTextView;

        public AppliedJobViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
