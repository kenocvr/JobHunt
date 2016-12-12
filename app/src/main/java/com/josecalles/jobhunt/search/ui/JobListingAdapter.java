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


package com.josecalles.jobhunt.search.ui;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.josecalles.jobhunt.R;
import com.josecalles.jobhunt.search.api.DataLoadingSubject;
import com.josecalles.jobhunt.search.model.JobListing;
import com.josecalles.jobhunt.database.handler.DatabaseUpdateHandlerThread;
import com.josecalles.jobhunt.util.ChromeTabUtils;
import com.josecalles.jobhunt.util.TimeUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


public class JobListingAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements DataLoadingSubject.DataLoadingCallback {

    private static final int TYPE_LOADING_MORE = -1;

    private DataLoadingSubject dataLoadingSubject;
    private boolean showLoadingMore = false;
    private List<JobListing> jobListingDataSet;
    private DatabaseUpdateHandlerThread handlerThread;
    private Context context;


    public JobListingAdapter(Context context, DataLoadingSubject dataLoadingSubject) {
        this.context = context;
        this.dataLoadingSubject = dataLoadingSubject;
        this.dataLoadingSubject.registerCallback(this);
        jobListingDataSet = new ArrayList<>();
        handlerThread = new DatabaseUpdateHandlerThread();
        handlerThread.start();
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == R.layout.item_job_listing) {
            return new JobViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_job_listing, parent, false));
        } else {
            return new LoadingMoreHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_infinite_loading, parent, false));
        }
    }


    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == TYPE_LOADING_MORE) {
            bindLoadingViewHolder((LoadingMoreHolder) holder, position);
        } else {
            bindJobListingViewHolder((JobViewHolder) holder, position);
        }
    }


    public void clear() {
        jobListingDataSet.clear();
        notifyDataSetChanged();
    }


    public int getDataItemCount() {
        return jobListingDataSet.size();
    }


    @Override
    public int getItemCount() {
        return getDataItemCount() + (showLoadingMore ? 1 : 0);
    }


    @Override
    public int getItemViewType(int position) {
        if (position < getDataItemCount()
                && getDataItemCount() > 0) {
            return R.layout.item_job_listing;
        }
        return TYPE_LOADING_MORE ;
    }


    @Override
    public void onViewRecycled(RecyclerView.ViewHolder holder) {
        super.onViewRecycled(holder);
        if (holder instanceof JobViewHolder) {
            JobViewHolder jobViewHolder = (JobViewHolder) holder;
            jobViewHolder.saveButton.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.asl_trimclip_heart));
        }
    }


    public void addJobListingsToDataSet(List<JobListing> jobListings) {
        jobListingDataSet.addAll(jobListings);
        notifyDataSetChanged();
    }


    public void clearDataSet() {
        jobListingDataSet.clear();
        notifyDataSetChanged();
    }


    private int getLoadingMoreItemPosition() {
        return showLoadingMore ? getItemCount() - 1 : RecyclerView.NO_POSITION;
    }


    @Override
    public void dataStartedLoading() {
        if (showLoadingMore) return;
        showLoadingMore = true;
        notifyItemInserted(getLoadingMoreItemPosition());
    }


    @Override
    public void dataFinishedLoading() {
        if (!showLoadingMore) return;
        final int loadingPos = getLoadingMoreItemPosition();
        showLoadingMore = false;
        notifyItemRemoved(loadingPos);
    }


    public void stopHandlerThread(){
        handlerThread.quitSafely();
    }

    private void bindLoadingViewHolder(LoadingMoreHolder holder, int position) {
        holder.progress.setVisibility((position > 0 && dataLoadingSubject.isDataLoading())
                ? View.VISIBLE : View.INVISIBLE);
    }

    private void bindJobListingViewHolder(final JobViewHolder holder, int position) {

        JobListing jobListing = jobListingDataSet.get(position);

        holder.companyTextView.setText(TextUtils.isEmpty(jobListing.company) ? "N/A" : jobListing.company);
        holder.jobTitleTextView.setText(jobListing.jobtitle);
        holder.dateTextView.setText(TimeUtils.getFormattedDate(jobListing.date));
        if (jobListing.sponsored) {
            holder.snippetTextView.setText("Sponsored");
            holder.snippetTextView.setTextColor(ContextCompat.getColor(context, R.color.accent));
        } else
            holder.snippetTextView.setText(jobListing.snippet);
        if (jobListing.saved) {
            holder.saveButton.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.vd_trimclip_heart_break));
        }


        holder.itemBackgroundFrameLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                JobListing selectedJobListing = jobListingDataSet.get(holder.getAdapterPosition());
                ChromeTabUtils.openTabWithUrl(view.getContext(), selectedJobListing.url);
            }
        });

        holder.saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                JobListing selectedJobListing = jobListingDataSet.get(holder.getAdapterPosition());
                selectedJobListing.saved = !selectedJobListing.saved;
                if (selectedJobListing.saved) {
                    final int[] stateSet = {android.R.attr.state_checked};
                    holder.saveButton.setImageState(stateSet, true);
                    handlerThread.postJobSavedMessage(selectedJobListing);
                } else {
                    holder.saveButton.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.asl_trimclip_heart));
                    final int[] stateSet = {android.R.attr.state_checked * -1};
                    holder.saveButton.setImageState(stateSet, true);
                    handlerThread.postJobUnsavedMessage(selectedJobListing);
                }
            }
        });
    }


    public static class JobViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.item_job_background)
        public FrameLayout itemBackgroundFrameLayout;
        @BindView(R.id.item_job_company_text_view)
        public TextView companyTextView;
        @BindView(R.id.item_job_title_text_view)
        public TextView jobTitleTextView;
        @BindView(R.id.item_job_snippet_text_view)
        public TextView snippetTextView;
        @BindView(R.id.item_job_save_button)
        public ImageButton saveButton;
        @BindView(R.id.item_job_date_text_view)
        public TextView dateTextView;

        public JobViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }


    private static class LoadingMoreHolder extends RecyclerView.ViewHolder {

        ProgressBar progress;

        LoadingMoreHolder(View itemView) {
            super(itemView);
            progress = (ProgressBar) itemView;
        }

    }
}
