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

package com.josecalles.jobhunt.saved.ui;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.josecalles.jobhunt.R;
import com.josecalles.jobhunt.database.handler.DatabaseUpdateHandlerThread;
import com.josecalles.jobhunt.search.model.JobListing;
import com.josecalles.jobhunt.util.ChromeTabUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


public class SavedJobListingAdapter extends RecyclerView.Adapter<SavedJobListingAdapter.SavedJobViewHolder> {

    private List<JobListing> jobListingDataSet;
    private Context context;
    private DatabaseUpdateHandlerThread handlerThread;

    public SavedJobListingAdapter(Context context) {
        this.context = context;
        jobListingDataSet = new ArrayList<>();
        handlerThread = new DatabaseUpdateHandlerThread();
        handlerThread.start();
    }


    @Override
    public void onBindViewHolder(final SavedJobViewHolder holder, int position) {
        final JobListing jobListing = jobListingDataSet.get(position);
        holder.companyTextView.setText(jobListing.company);
        holder.snippetTextView.setText(jobListing.snippet);
        holder.jobTitleTextView.setText(jobListing.jobtitle + " in " + jobListing.formattedLocation);
        holder.removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                jobListingDataSet.remove(holder.getAdapterPosition());
                notifyItemRemoved(holder.getAdapterPosition());
                handlerThread.postJobUnsavedMessage(jobListing);
            }
        });
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                JobListing selectedJobListing = jobListingDataSet.get(holder.getAdapterPosition());
                ChromeTabUtils.openTabWithUrl(view.getContext(), selectedJobListing.url);
            }
        });

        if (jobListing.applied) {
            Drawable appliedCheck = ContextCompat.getDrawable(context, R.drawable.ic_vect_check_24dp);
            holder.appliedButton.setCompoundDrawablesWithIntrinsicBounds(null, null, appliedCheck, null);
        }

        holder.appliedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                JobListing selectedJobListing = jobListingDataSet.get(holder.getAdapterPosition());
                if (!selectedJobListing.applied) {
                    selectedJobListing.applied = true;
                    handlerThread.postAppliedToJobMessage(selectedJobListing);
                    Drawable appliedCheck = ContextCompat.getDrawable(context, R.drawable.ic_vect_check_24dp);
                    holder.appliedButton.setCompoundDrawablesWithIntrinsicBounds(null, null, appliedCheck, null);
                } else {
                    selectedJobListing.applied = false;
                    handlerThread.postNotAppliedToJobMessage(selectedJobListing);
                    Drawable notChecked = ContextCompat.getDrawable(context, R.drawable.ic_vect_close_24dp);
                    holder.appliedButton.setCompoundDrawablesWithIntrinsicBounds(null, null, notChecked, null);
                }
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
    public SavedJobViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new SavedJobViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_saved_job_listing, parent, false));
    }


    public void clearDataSet() {
        jobListingDataSet.clear();
        notifyDataSetChanged();
    }


    public void stopHandlerThread(){
        handlerThread.quitSafely();
    }

    static class SavedJobViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.item_saved_job_background)
        public CardView cardView;
        @BindView(R.id.item_saved_job_company)
        public TextView companyTextView;
        @BindView(R.id.item_saved_job_title)
        public TextView jobTitleTextView;
        @BindView(R.id.item_saved_job_snippet)
        public TextView snippetTextView;
        @BindView(R.id.item_saved_job_remove_button)
        public Button removeButton;
        @BindView(R.id.item_saved_job_applied_button)
        public Button appliedButton;

        public SavedJobViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
