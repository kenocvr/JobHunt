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

package com.josecalles.jobhunt.recent.ui;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.josecalles.jobhunt.R;
import com.josecalles.jobhunt.database.handler.DatabaseUpdateHandlerThread;
import com.josecalles.jobhunt.search.model.JobSearchQuery;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


public class RecentSearchesAdapter extends RecyclerView.Adapter<RecentSearchesAdapter.RecentSearchesHolder> {

    private List<JobSearchQuery> jobSearchesDataSet;
    private OnRecentSearchSelectedListener onRecentSearchSelectedListener;
    private DatabaseUpdateHandlerThread handlerThread;


    public interface OnRecentSearchSelectedListener {
        void onRecentSearchSelected(JobSearchQuery query);
    }


    public RecentSearchesAdapter(OnRecentSearchSelectedListener onRecentSearchSelectedListener) {
        this.onRecentSearchSelectedListener = onRecentSearchSelectedListener;
        jobSearchesDataSet = new ArrayList<>();
        handlerThread = new DatabaseUpdateHandlerThread();
        handlerThread.start();
    }


    @Override
    public void onBindViewHolder(final RecentSearchesHolder holder, int position) {
        final JobSearchQuery searchQuery = jobSearchesDataSet.get(position);
        holder.titleTextView.setText(searchQuery.getJobTitle());
        holder.typeTextView.setText(searchQuery.getFormattedJobType());
        holder.locationTextView.setText(searchQuery.getLocation());
        holder.background.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                JobSearchQuery selectedQuery = jobSearchesDataSet.get(holder.getAdapterPosition());
                onRecentSearchSelectedListener.onRecentSearchSelected(selectedQuery);
            }
        });
    }


    @Override
    public int getItemCount() {
        return jobSearchesDataSet.size();
    }


    public void addJobSearchQueriesToDataset(List<JobSearchQuery> jobListings) {
        jobSearchesDataSet.addAll(jobListings);
        notifyDataSetChanged();
    }


    @Override
    public RecentSearchesHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new RecentSearchesHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recent_search, parent, false));
    }


    public void clearDataSet() {
        jobSearchesDataSet.clear();
        notifyDataSetChanged();
    }


    public void stopHandlerThread(){
        handlerThread.quitSafely();
    }

    static class RecentSearchesHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.item_recents_background)
        public FrameLayout background;
        @BindView(R.id.item_recents_title)
        public TextView titleTextView;
        @BindView(R.id.item_recents_type)
        public TextView typeTextView;
        @BindView(R.id.item_recents_location)
        public TextView locationTextView;

        public RecentSearchesHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
