/*
 * Copyright (C) 2022 Project Kaleidoscope
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

package com.android.settings.custom;

import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceGroup;
import androidx.preference.PreferenceScreen;
import androidx.preference.PreferenceCategory;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.PreferenceFragment;
import androidx.preference.SwitchPreference;

import com.android.internal.logging.nano.MetricsProto;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;

import java.util.Locale;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;

import com.android.settings.custom.preference.SystemSettingSwitchPreference;
import com.android.settings.custom.preference.SystemSettingMainSwitchPreference;

public class NetworkTrafficSettings extends SettingsPreferenceFragment implements
        OnPreferenceChangeListener {

    private SystemSettingSwitchPreference mThreshold;
    private SystemSettingMainSwitchPreference mNetMonitor;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        addPreferencesFromResource(R.xml.network_traffic_settings);

        final ContentResolver resolver = getActivity().getContentResolver();

        boolean isNetMonitorEnabled = Settings.System.getIntForUser(resolver,
                Settings.System.NETWORK_TRAFFIC_STATE, 1, UserHandle.USER_CURRENT) == 1;
        mNetMonitor = (SystemSettingMainSwitchPreference) findPreference("network_traffic_state");
        mNetMonitor.setChecked(isNetMonitorEnabled);
        mNetMonitor.setOnPreferenceChangeListener(this);

        boolean isThresholdEnabled = Settings.System.getIntForUser(resolver,
                Settings.System.NETWORK_TRAFFIC_AUTOHIDE_THRESHOLD, 0, UserHandle.USER_CURRENT) == 1;
        mThreshold = (SystemSettingSwitchPreference) findPreference("network_traffic_autohide_threshold");
        mThreshold.setChecked(isThresholdEnabled);
        mThreshold.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object objValue) {
        if (preference == mNetMonitor) {
            boolean value = (Boolean) objValue;
            Settings.System.putIntForUser(getActivity().getContentResolver(),
                    Settings.System.NETWORK_TRAFFIC_STATE, value ? 1 : 0,
                    UserHandle.USER_CURRENT);
            mNetMonitor.setChecked(value);
            mThreshold.setChecked(value);
            return true;
        } else if (preference == mThreshold) {
            boolean value = (Boolean) objValue;
            Settings.System.putIntForUser(getContentResolver(),
                    Settings.System.NETWORK_TRAFFIC_AUTOHIDE_THRESHOLD, value ? 1 : 0,
                    UserHandle.USER_CURRENT);
            return true;
        }
        return false;
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.CUSTOM;
    }
}
