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

package com.josecalles.jobhunt.home;

import android.app.Activity;
import android.app.ActivityOptions;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toolbar;

import com.josecalles.jobhunt.R;
import com.josecalles.jobhunt.applied.ui.AppliedJobsFragment;
import com.josecalles.jobhunt.recent.ui.RecentSearchFragment;
import com.josecalles.jobhunt.recent.ui.RecentSearchesAdapter;
import com.josecalles.jobhunt.saved.ui.SavedJobsFragment;
import com.josecalles.jobhunt.search.constant.JobType;
import com.josecalles.jobhunt.search.constant.UserPreferenceKey;
import com.josecalles.jobhunt.search.model.JobSearchQuery;
import com.josecalles.jobhunt.search.ui.SearchActivity;

import butterknife.BindView;
import butterknife.ButterKnife;

public class HomeActivity extends Activity implements RecentSearchesAdapter.OnRecentSearchSelectedListener {

    public static final String RECENT_QUERY_TO_RUN = "query_to_run";


    @BindView(R.id.drawer)
    DrawerLayout drawer;

    @BindView(R.id.navigation_view)
    NavigationView navigationView;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.bottom_navigation_view)
    BottomNavigationView bottomNavigationView;

    private FragmentManager fragmentManager;
    private int selectedBottomMenuItem;
    private SharedPreferences userSearchPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        ButterKnife.bind(this);
        setActionBar(toolbar);
        fragmentManager = getFragmentManager();
        getActionBar().setDisplayShowTitleEnabled(false);
        setUpBottomNavigationView();
        setUpNavigationView();
        if (savedInstanceState == null) setDefaultFragment();
        userSearchPreferences =
                PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_search:
                View searchMenuView = toolbar.findViewById(R.id.menu_search);
                Bundle options = ActivityOptions.makeSceneTransitionAnimation(this, searchMenuView,
                        getString(R.string.transition_search_back)).toBundle();
                startActivity(new Intent(this, SearchActivity.class), options);
                return true;
            case R.id.menu_option:
                drawer.openDrawer(GravityCompat.END);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }


    private void setDefaultFragment() {
        RecentSearchFragment recentSearchFragment = new RecentSearchFragment();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, recentSearchFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
        selectedBottomMenuItem = R.id.bottom_menu_recent_searches;
    }


    private void setUpBottomNavigationView(){
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == selectedBottomMenuItem) return false;
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.setCustomAnimations(R.animator.fade_in, R.animator.fade_out);
                fragmentTransaction.addToBackStack(null);
                switch (item.getItemId()) {
                    case R.id.bottom_menu_recent_searches:
                        RecentSearchFragment recentSearchFragment = new RecentSearchFragment();
                        fragmentTransaction.replace(R.id.fragment_container, recentSearchFragment);
                        fragmentTransaction.commit();
                        selectedBottomMenuItem = item.getItemId();
                        return true;
                    case R.id.bottom_menu_saved:
                        SavedJobsFragment savedJobsFragment = new SavedJobsFragment();
                        fragmentTransaction.replace(R.id.fragment_container, savedJobsFragment);
                        fragmentTransaction.commit();
                        selectedBottomMenuItem = item.getItemId();
                        return true;
                    case R.id.bottom_menu_applied:
                        AppliedJobsFragment appliedJobsFragment = new AppliedJobsFragment();
                        fragmentTransaction.replace(R.id.fragment_container, appliedJobsFragment);
                        fragmentTransaction.commit();
                        selectedBottomMenuItem = item.getItemId();
                        return true;
                }
                return false;
            }
        });
    }


    private void setUpNavigationView() {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        menuItem.setChecked(true);
                        switch (menuItem.getItemId()) {
                            case R.id.menu_type_full_time:
                                userSearchPreferences.edit().putString(UserPreferenceKey.PREF_JOB_TYPE, JobType.FULL_TIME).apply();
                                break;
                            case R.id.menu_type_part_time:
                                userSearchPreferences.edit().putString(UserPreferenceKey.PREF_JOB_TYPE, JobType.PART_TIME).apply();
                                break;
                            case R.id.menu_type_internship:
                                userSearchPreferences.edit().putString(UserPreferenceKey.PREF_JOB_TYPE, JobType.INTERNSHIP).apply();
                                break;
                            case R.id.menu_type_contract:
                                userSearchPreferences.edit().putString(UserPreferenceKey.PREF_JOB_TYPE, JobType.CONTRACT).apply();
                                break;
                        }
                        drawer.closeDrawers();
                        return true;
                    }
                });
    }


    @Override
    public void onRecentSearchSelected(JobSearchQuery query) {
        Intent intent = new Intent(this, SearchActivity.class);
        intent.putExtra(RECENT_QUERY_TO_RUN, query);
        View searchMenuView = toolbar.findViewById(R.id.menu_search);
        Bundle options = ActivityOptions.makeSceneTransitionAnimation(this, searchMenuView,
                getString(R.string.transition_search_back)).toBundle();
        startActivity(intent, options);
    }
}
