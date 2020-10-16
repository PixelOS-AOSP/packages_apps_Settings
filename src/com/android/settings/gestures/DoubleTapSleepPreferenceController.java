/*
 * Copyright (C) 2016 The Android Open Source Project
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

package com.android.settings.gestures;

import android.content.Context;
import android.content.Intent;
import android.provider.Settings;

import com.android.settings.R;
import com.android.settings.core.BasePreferenceController;

public class DoubleTapSleepPreferenceController extends BasePreferenceController {

    private static final String KEY = "double_tap_sleep_summary";
    private final String STATUSBAR = Settings.System.DOUBLE_TAP_SLEEP_GESTURE;
    private final String LOCKSCREEN = Settings.System.DOUBLE_TAP_SLEEP_LOCKSCREEN;

    public DoubleTapSleepPreferenceController(Context context, String key) {
        super(context, key);
    }

    @Override
    public CharSequence getSummary() {
        return mContext.getText(R.string.double_tap_sleep_summary);
    }

    public int getAvailabilityStatus() {
        return AVAILABLE;
    }
}
