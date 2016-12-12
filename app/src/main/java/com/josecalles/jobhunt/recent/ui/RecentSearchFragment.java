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

import android.app.Fragment;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.TextView;

import com.josecalles.jobhunt.R;
import com.josecalles.jobhunt.database.fetch.DatabaseSearchFetcher;
import com.josecalles.jobhunt.search.model.JobSearchQuery;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


public class RecentSearchFragment extends Fragment {

    @BindView(R.id.stub_no_recent_searches)
    ViewStub noRecentSearchesStubView;

    @BindView(R.id.recent_search_recycler_view)
    RecyclerView recyclerView;

    private TextView noRecentSearchesTextView;

    private Context context;
    private RecentSearchesAdapter adapter;
    private DatabaseSearchFetcher databaseFetcher;
    private RecentSearchesAdapter.OnRecentSearchSelectedListener onRecentSearchSelectedListener;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
        onRecentSearchSelectedListener = (RecentSearchesAdapter.OnRecentSearchSelectedListener) getActivity();
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        databaseFetcher = new DatabaseSearchFetcher() {
            @Override
            public void onDataLoaded(List<JobSearchQuery> data) {
                if (data != null && data.size() > 0) {
                    if (recyclerView.getVisibility() != View.VISIBLE) {
                        recyclerView.setVisibility(View.VISIBLE);
                        if (noRecentSearchesTextView != null) {
                            noRecentSearchesTextView.setVisibility(View.GONE);
                        }
                    }
                    adapter.clearDataSet();
                    adapter.addJobSearchQueriesToDataset(data);
                } else {
                    setNoResultsVisibility(View.VISIBLE);
                }
            }
        };
        adapter = new RecentSearchesAdapter(onRecentSearchSelectedListener);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recent_searches, container, false);
        ButterKnife.bind(this, view);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);
        return view;
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }


    void setNoResultsVisibility(int visibility) {
        if (visibility == View.VISIBLE) {
            if (noRecentSearchesTextView == null) {
                Drawable icon = ContextCompat.getDrawable(context, R.drawable.ic_vect_recent_search_tinted_24dp);
                noRecentSearchesTextView = (TextView) noRecentSearchesStubView.inflate();
                noRecentSearchesTextView.setCompoundDrawablesWithIntrinsicBounds(null, icon, null, null);
            }
            String message = getString(R.string.no_recent_searches);
            noRecentSearchesTextView.setText(message);
        }
        if (noRecentSearchesTextView != null) {
            noRecentSearchesTextView.setVisibility(visibility);
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        databaseFetcher.fetchAllRecentSearches();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        adapter.stopHandlerThread();
    }

}
