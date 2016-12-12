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

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.SharedElementCallback;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.TransitionRes;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.animation.FastOutLinearInInterpolator;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.transition.TransitionManager;
import android.transition.TransitionSet;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.inputmethod.EditorInfo;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;

import com.josecalles.jobhunt.R;
import com.josecalles.jobhunt.home.HomeActivity;
import com.josecalles.jobhunt.search.api.SearchManager;
import com.josecalles.jobhunt.search.constant.UserPreferenceKey;
import com.josecalles.jobhunt.search.model.JobListing;
import com.josecalles.jobhunt.search.model.JobSearchQuery;
import com.josecalles.jobhunt.transition.CircularReveal;
import com.josecalles.jobhunt.util.ImeUtils;
import com.josecalles.jobhunt.util.TransitionUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SearchActivity extends Activity {


    @BindView(R.id.searchback)
    ImageButton searchBack;
    @BindView(R.id.searchback_container)
    ViewGroup searchBackContainer;
    @BindView(R.id.search_view)
    SearchView jobTitleSearchView;
    @BindView(R.id.location_search_view)
    SearchView locationSearchView;
    @BindView(R.id.search_background)
    View searchBackgroundView;
    @BindView(android.R.id.empty)
    ProgressBar progressBar;
    @BindView(R.id.search_results)
    RecyclerView resultsRecyclerView;
    @BindView(R.id.container)
    ViewGroup container;
    @BindView(R.id.search_toolbar)
    ViewGroup searchToolbar;
    @BindView(R.id.results_container)
    ViewGroup resultsContainer;
    @BindView(R.id.location_toolbar)
    FrameLayout locationToolbar;
    @BindView(R.id.scrim)
    View scrim;
    @BindView(R.id.location_search_icon)
    ImageView locationIcon;


    private TextView noResults;
    private SearchManager searchManager;
    private SparseArray<Transition> transitions = new SparseArray<>();
    private JobListingAdapter jobListingAdapter;
    private SharedPreferences userSearchPreferences;
    private boolean didCompleteInitialAnimation = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        ButterKnife.bind(this);
        setupSearchViewWithHint(jobTitleSearchView, getString(R.string.search_hint));
        setupSearchViewWithHint(locationSearchView, getString(R.string.location_search_hint));
        searchManager = new SearchManager() {
            @Override
            public void onDataLoaded(List<JobListing> data) {
                if (data != null && data.size() > 0) {
                    if (resultsRecyclerView.getVisibility() != View.VISIBLE) {
                        TransitionManager.beginDelayedTransition(container, getTransition(R.transition.auto));
                        progressBar.setVisibility(View.GONE);
                        resultsRecyclerView.setVisibility(View.VISIBLE);
                    }
                    jobListingAdapter.addJobListingsToDataSet(data);
                } else {
                    TransitionManager.beginDelayedTransition(container, getTransition(R.transition.auto));
                    progressBar.setVisibility(View.GONE);
                    setNoResultsVisibility(View.VISIBLE);
                }
            }
        };
        jobListingAdapter = new JobListingAdapter(this, searchManager);
        resultsRecyclerView.setAdapter(jobListingAdapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        resultsRecyclerView.setLayoutManager(layoutManager);
        resultsRecyclerView.addOnScrollListener(new InfiniteScrollListener(layoutManager, searchManager) {
            @Override
            public void onLoadMore() {
                searchManager.loadMore();
            }
        });
        resultsRecyclerView.setHasFixedSize(true);

        userSearchPreferences =
                PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
        setupTransitions();
    }


    @Override
    public void onBackPressed() {
        dismiss();
    }


    @Override
    protected void onPause() {
        overridePendingTransition(0, 0);
        super.onPause();
    }


    @Override
    protected void onDestroy() {
        searchManager.stopHandlerThreads();
        jobListingAdapter.stopHandlerThread();
        searchManager.cancelLoading();
        super.onDestroy();
    }


    @Override
    public void onEnterAnimationComplete() {
        if (!didCompleteInitialAnimation) {
            jobTitleSearchView.requestFocus();
            ImeUtils.showIme(jobTitleSearchView);
            animateLocationToolbarIn();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    JobSearchQuery queryToRun = (JobSearchQuery) getIntent().getSerializableExtra(HomeActivity.RECENT_QUERY_TO_RUN);
                    if (queryToRun != null) {
                        jobTitleSearchView.setQuery(queryToRun.getJobTitle(), false);
                        locationSearchView.setQuery(queryToRun.getLocation(), false);
                        searchFor(queryToRun, true);
                    }
                }
            }, 600);
        } else {
            ImeUtils.hideIme(jobTitleSearchView);
            ImeUtils.hideIme(locationSearchView);
        }
        didCompleteInitialAnimation = true;
    }


    private void animateLocationToolbarIn() {
        AnimatorSet alphaAnimSet = new AnimatorSet();
        ObjectAnimator toolbarFadeInIconAnim = ObjectAnimator.ofFloat(locationIcon, "alpha", 1f);
        ObjectAnimator toolbarFadeInSearchViewAnim = ObjectAnimator.ofFloat(locationSearchView, "alpha", 1f);
        alphaAnimSet.play(toolbarFadeInIconAnim).with(toolbarFadeInSearchViewAnim);

        AnimatorSet slideAnimSet = new AnimatorSet();
        ObjectAnimator toolbarSlideDownAnim = ObjectAnimator.ofFloat(locationToolbar, "y", locationToolbar.getHeight());
        toolbarSlideDownAnim.setInterpolator(new LinearOutSlowInInterpolator());
        slideAnimSet.play(toolbarSlideDownAnim).before(alphaAnimSet);
        slideAnimSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) scrim.getLayoutParams();
                layoutParams.setMargins(0, locationToolbar.getHeight() * 2, 0, 0);
                scrim.setLayoutParams(layoutParams);
            }
        });
        slideAnimSet.start();
    }


    private void animateLocationToolbarOut() {

        AnimatorSet alphaAnimSet = new AnimatorSet();
        ObjectAnimator toolbarFadeInIconAnim = ObjectAnimator.ofFloat(locationIcon, "alpha", 0f);
        ObjectAnimator toolbarFadeInSearchViewAnim = ObjectAnimator.ofFloat(locationSearchView, "alpha", 0f);
        alphaAnimSet.play(toolbarFadeInIconAnim).with(toolbarFadeInSearchViewAnim);

        AnimatorSet slideAnimSet = new AnimatorSet();
        ObjectAnimator toolbarSlideUpAnim = ObjectAnimator.ofFloat(locationToolbar, "y", -locationToolbar.getHeight());
        toolbarSlideUpAnim.setInterpolator(new FastOutLinearInInterpolator());
        slideAnimSet.play(toolbarSlideUpAnim).before(alphaAnimSet);
        slideAnimSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                finishAfterTransition();
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
            }
        });
        slideAnimSet.start();
    }


    @OnClick({R.id.scrim, R.id.searchback})
    protected void dismiss() {
        searchBack.setBackground(null);
        animateLocationToolbarOut();
    }


    void clearResults() {
        TransitionManager.beginDelayedTransition(container, getTransition(R.transition.auto));
        jobListingAdapter.clear();
        searchManager.clear();
        resultsRecyclerView.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
        setNoResultsVisibility(View.GONE);
    }


    void setNoResultsVisibility(int visibility) {
        if (visibility == View.VISIBLE) {
            if (noResults == null) {
                noResults = (TextView) ((ViewStub)
                        findViewById(R.id.stub_no_search_results)).inflate();
                noResults.setCompoundDrawablesWithIntrinsicBounds(null, ContextCompat.getDrawable(this, R.drawable.ic_vect_search_24dp), null, null);
                noResults.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        jobTitleSearchView.setQuery("", false);
                        jobTitleSearchView.requestFocus();
                        ImeUtils.showIme(jobTitleSearchView);
                    }
                });
            }
            String message = String.format(
                    getString(R.string.no_search_results), jobTitleSearchView.getQuery().toString());
            SpannableStringBuilder ssb = new SpannableStringBuilder(message);
            ssb.setSpan(new StyleSpan(Typeface.ITALIC),
                    message.indexOf('“') + 1,
                    message.length() - 1,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            noResults.setText(ssb);
        }
        if (noResults != null) {
            noResults.setVisibility(visibility);
        }
    }


    void searchFor(JobSearchQuery jobSearchQuery, boolean fromRecent) {
        clearResults();
        progressBar.setVisibility(View.VISIBLE);
        ImeUtils.hideIme(jobTitleSearchView);
        ImeUtils.hideIme(locationSearchView);
        jobTitleSearchView.clearFocus();
        locationSearchView.clearFocus();
        searchManager.searchFor(jobSearchQuery, fromRecent);
    }


    Transition getTransition(@TransitionRes int transitionId) {
        Transition transition = transitions.get(transitionId);
        if (transition == null) {
            transition = TransitionInflater.from(this).inflateTransition(transitionId);
            transitions.put(transitionId, transition);
        }
        return transition;
    }


    private void setupSearchViewWithHint(SearchView searchView, String hint) {
        searchView.setQueryHint(hint);
        searchView.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        searchView.setImeOptions(searchView.getImeOptions() | EditorInfo.IME_ACTION_SEARCH |
                EditorInfo.IME_FLAG_NO_EXTRACT_UI | EditorInfo.IME_FLAG_NO_FULLSCREEN);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String searchQuery) {
                String jobTitleString = jobTitleSearchView.getQuery().toString();
                String locationString = locationSearchView.getQuery().toString();
                if (!TextUtils.isEmpty(jobTitleString) && !TextUtils.isEmpty(locationString)) {
                    int searchRadius = Integer.parseInt(userSearchPreferences.getString(UserPreferenceKey.PREF_SEARCH_RADIUS, "25"));
                    String jobType = userSearchPreferences.getString(UserPreferenceKey.PREF_JOB_TYPE, "fulltime");
                    JobSearchQuery jobSearchQuery = new JobSearchQuery(jobTitleString, locationString, searchRadius, jobType);
                    searchFor(jobSearchQuery, false);
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                if (TextUtils.isEmpty(s)) {
                    clearResults();
                }
                return true;
            }
        });
    }


    private void setupTransitions() {
        setEnterSharedElementCallback(new SharedElementCallback() {
            @Override
            public void onSharedElementStart(
                    List<String> sharedElementNames,
                    List<View> sharedElements,
                    List<View> sharedElementSnapshots) {
                if (sharedElements != null && !sharedElements.isEmpty()) {
                    View searchIcon = sharedElements.get(0);
                    if (searchIcon.getId() != R.id.searchback) return;
                    int centerX = (searchIcon.getLeft() + searchIcon.getRight()) / 2;
                    CircularReveal hideResults = (CircularReveal) TransitionUtils.findTransition(
                            (TransitionSet) getWindow().getReturnTransition(),
                            CircularReveal.class, R.id.results_container);
                    if (hideResults != null) {
                        hideResults.setCenter(new Point(centerX, 0));
                    }
                }
            }
        });
    }
}
