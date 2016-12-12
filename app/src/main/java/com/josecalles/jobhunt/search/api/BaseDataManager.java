/*
 * Copyright 2015 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.josecalles.jobhunt.search.api;

import com.google.gson.Gson;
import com.josecalles.jobhunt.BuildConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public abstract class BaseDataManager<T> implements DataLoadingSubject {

  private final AtomicInteger loadingCount;
  private List<DataLoadingCallback> loadingCallbacks;
  private IndeedSearchService indeedSearchService;

  public BaseDataManager() {
    loadingCount = new AtomicInteger(0);
  }

  public abstract void onDataLoaded(T data);

  public abstract void cancelLoading();


  @Override
  public boolean isDataLoading() {
    return loadingCount.get() > 0;
  }


  @Override
  public void registerCallback(DataLoadingSubject.DataLoadingCallback callback) {
    if (loadingCallbacks == null) {
      loadingCallbacks = new ArrayList<>(1);
    }
    loadingCallbacks.add(callback);
  }


  @Override
  public void unregisterCallback(DataLoadingSubject.DataLoadingCallback callback) {
    if (loadingCallbacks != null && loadingCallbacks.contains(callback)) {
      loadingCallbacks.remove(callback);
    }
  }


  protected void loadStarted() {
    if (0 == loadingCount.getAndIncrement()) {
      dispatchLoadingStartedCallbacks();
    }
  }


  protected void loadFinished() {
    if (0 == loadingCount.decrementAndGet()) {
      dispatchLoadingFinishedCallbacks();
    }
  }


  protected void resetLoadingCount() {
    loadingCount.set(0);
  }


  private void dispatchLoadingStartedCallbacks() {
    if (loadingCallbacks == null || loadingCallbacks.isEmpty()) return;
    for (DataLoadingCallback loadingCallback : loadingCallbacks) {
      loadingCallback.dataStartedLoading();
    }
  }


  private void dispatchLoadingFinishedCallbacks() {
    if (loadingCallbacks == null || loadingCallbacks.isEmpty()) return;
    for (DataLoadingCallback loadingCallback : loadingCallbacks) {
      loadingCallback.dataFinishedLoading();
    }
  }


  public IndeedSearchService getIndeedSearchService() {
    if (indeedSearchService == null) {
      createIndeedSearchService();
    }
    return indeedSearchService;
  }


  private void createIndeedSearchService (){
    HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
    loggingInterceptor.setLevel(BuildConfig.DEBUG ? HttpLoggingInterceptor.Level.BODY : HttpLoggingInterceptor.Level.NONE);
    OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
    httpClient.addInterceptor(loggingInterceptor);

    indeedSearchService = new Retrofit.Builder()
        .baseUrl(IndeedSearchService.BASE_URL)
        .addConverterFactory(GsonConverterFactory.create(new Gson()))
        .client(httpClient.build())
        .build()
        .create(IndeedSearchService.class);
  }
}
