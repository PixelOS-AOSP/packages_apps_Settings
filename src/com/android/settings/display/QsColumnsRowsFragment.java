/*
 * Copyright (C) 2017 The Android Open Source Project
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

package com.android.settings.display;

import android.app.settings.SettingsEnums;
import android.content.Context;
import android.hardware.display.AmbientDisplayConfiguration;
import android.provider.SearchIndexableResource;

import androidx.annotation.VisibleForTesting;

import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.android.settingslib.search.SearchIndexable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Settings screen for lock screen preference
 */
@SearchIndexable
public class QsColumnsRowsFragment extends DashboardFragment {

    private static final String TAG = "QsColumnsRowsFragment";

    @Override
    public int getMetricsCategory() {
        return SettingsEnums.PAGE_UNKNOWN;
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.display_qs_columns_rows;
    }

    /*@Override
    public int getHelpResource() {
        return R.string...;
    }*/

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

   /* @Override
    protected List<AbstractPreferenceController> createPreferenceControllers(Context context) {
        final List<AbstractPreferenceController> controllers = new ArrayList<>();
        final Lifecycle lifecycle = getSettingsLifecycle();
        final LockScreenNotificationPreferenceController notificationController =
                new LockScreenNotificationPreferenceController(context,
                        KEY_LOCK_SCREEN_NOTIFICATON,
                        KEY_LOCK_SCREEN_NOTIFICATON_WORK_PROFILE_HEADER,
                        KEY_LOCK_SCREEN_NOTIFICATON_WORK_PROFILE);
        lifecycle.addObserver(notificationController);
        controllers.add(notificationController);
        mOwnerInfoPreferenceController =
                new OwnerInfoPreferenceController(context, this, lifecycle);
        controllers.add(mOwnerInfoPreferenceController);

        return controllers;
    }

    @Override
    public void onOwnerInfoUpdated() {
        if (mOwnerInfoPreferenceController != null) {
            mOwnerInfoPreferenceController.updateSummary();
        }
    }*/

    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider() {
                @Override
                public List<SearchIndexableResource> getXmlResourcesToIndex(
                        Context context, boolean enabled) {
                    final SearchIndexableResource sir = new SearchIndexableResource(context);
                    sir.xmlResId = R.xml.display_qs_columns_rows;
                    return Arrays.asList(sir);
                }

                @Override
                protected boolean isPageSearchEnabled(Context context) {
                    return true;
                }
            };
}
