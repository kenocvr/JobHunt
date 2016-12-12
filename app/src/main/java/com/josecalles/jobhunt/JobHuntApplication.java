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

package com.josecalles.jobhunt;

import android.app.Application;

import com.josecalles.jobhunt.database.DatabaseManager;
import com.josecalles.jobhunt.database.JobsDbHelper;

import timber.log.Timber;

public class JobHuntApplication extends Application {


  @Override
  public void onCreate() {
    super.onCreate();
    DatabaseManager.initializeInstance(new JobsDbHelper(this));
    if (BuildConfig.DEBUG) {
      Timber.plant(new Timber.DebugTree());
    }
  }


}
