/*
 * Copyright (C) 2015-2020 PixelExperience
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

package com.android.settings.network;

import android.app.ActionBar;
import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.SearchIndexableResource;
import android.provider.Settings;

import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import androidx.preference.Preference.OnPreferenceChangeListener;

import com.android.settings.R;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.Indexable;
import com.android.settingslib.search.SearchIndexable;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;

import com.android.internal.logging.nano.MetricsProto;

import com.android.settings.custom.preference.CustomSeekBarPreference;
import com.android.settings.custom.preference.SecureSettingListPreference;
import com.android.settings.custom.preference.SecureSettingSwitchPreference;

import java.util.ArrayList;
import java.util.List;

@SearchIndexable
public class NetworkTraffic extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener, Indexable {

    private static final String TAG = "NetworkTrafficSettings";

    private CustomSeekBarPreference mNetTrafficAutohideThreshold;
    private CustomSeekBarPreference mNetTrafficRefreshInterval;
    private SecureSettingListPreference mNetTrafficLocation;
    private SecureSettingListPreference mNetTrafficMode;
    private SecureSettingListPreference mNetTrafficUnits;
    private SecureSettingSwitchPreference mNetTrafficAutohide;
    private SecureSettingSwitchPreference mNetTrafficHideArrow;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.traffic);
        
        final ActionBar actionBar = getActivity().getActionBar();
        actionBar.setTitle(getResources().getString(R.string.network_traffic_settings_title));

        PreferenceScreen prefSet = getPreferenceScreen();
        final ContentResolver resolver = getActivity().getContentResolver();

        int location = Settings.Secure.getIntForUser(resolver,
                Settings.Secure.NETWORK_TRAFFIC_LOCATION, 0, UserHandle.USER_CURRENT);
        mNetTrafficLocation = (SecureSettingListPreference) findPreference("network_traffic_location");
		mNetTrafficLocation.setValue(String.valueOf(location));
        mNetTrafficLocation.setSummary(mNetTrafficLocation.getEntry());
        mNetTrafficLocation.setOnPreferenceChangeListener(this);

        int mode = Settings.Secure.getIntForUser(resolver,
                Settings.Secure.NETWORK_TRAFFIC_MODE, 0, UserHandle.USER_CURRENT);
        mNetTrafficMode = (SecureSettingListPreference) findPreference("network_traffic_mode");
        mNetTrafficMode.setValue(String.valueOf(mode));
        mNetTrafficMode.setSummary(mNetTrafficMode.getEntry());
        mNetTrafficMode.setOnPreferenceChangeListener(this);

        int thresholdValue = Settings.Secure.getIntForUser(resolver,
                Settings.Secure.NETWORK_TRAFFIC_AUTOHIDE_THRESHOLD, 1, UserHandle.USER_CURRENT);
        mNetTrafficAutohideThreshold = (CustomSeekBarPreference) findPreference("network_traffic_autohide_threshold");
        mNetTrafficAutohideThreshold.setValue(thresholdValue);
        mNetTrafficAutohideThreshold.setOnPreferenceChangeListener(this);

        int intervalValue = Settings.Secure.getInt(resolver,
                Settings.Secure.NETWORK_TRAFFIC_REFRESH_INTERVAL, 2);
        mNetTrafficRefreshInterval =
                (CustomSeekBarPreference) findPreference("network_traffic_refresh_interval");
        mNetTrafficRefreshInterval.setValue(intervalValue);
        mNetTrafficRefreshInterval.setOnPreferenceChangeListener(this);

        int unitValue = Settings.Secure.getIntForUser(resolver,
                Settings.Secure.NETWORK_TRAFFIC_UNITS, 0, UserHandle.USER_CURRENT);
        mNetTrafficUnits = (SecureSettingListPreference) findPreference("network_traffic_units");
        mNetTrafficUnits.setValue(String.valueOf(unitValue));
        mNetTrafficUnits.setSummary(mNetTrafficUnits.getEntry());
        mNetTrafficUnits.setOnPreferenceChangeListener(this);

        mNetTrafficAutohide = (SecureSettingSwitchPreference) findPreference("network_traffic_autohide");
        boolean autoHide = Settings.Secure.getIntForUser(resolver,
                Settings.Secure.NETWORK_TRAFFIC_AUTOHIDE, 0, UserHandle.USER_CURRENT) != 0;
        mNetTrafficAutohide.setChecked(autoHide);
        mNetTrafficAutohide.setOnPreferenceChangeListener(this);

        mNetTrafficHideArrow = (SecureSettingSwitchPreference) findPreference("network_traffic_hidearrow");
        boolean hideArrows = Settings.Secure.getIntForUser(resolver,
                Settings.Secure.NETWORK_TRAFFIC_HIDEARROW, 0, UserHandle.USER_CURRENT) != 0;
        mNetTrafficHideArrow.setChecked(hideArrows);
        mNetTrafficHideArrow.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getContext().getContentResolver();
        if (preference == mNetTrafficLocation) {
            int location = Integer.valueOf((String) newValue);
            int index = mNetTrafficLocation.findIndexOfValue((String) newValue);
            Settings.Secure.putIntForUser(getActivity().getContentResolver(),
                    Settings.Secure.NETWORK_TRAFFIC_LOCATION,
                    location, UserHandle.USER_CURRENT);
            mNetTrafficLocation.setSummary(mNetTrafficLocation.getEntries()[index]);
            // Preference enablement checks
            mNetTrafficMode.setEnabled(netTrafficEnabled());
            mNetTrafficHideArrow.setEnabled(netTrafficEnabled());
            mNetTrafficAutohideThreshold.setEnabled(netTrafficEnabled());
            mNetTrafficRefreshInterval.setEnabled(netTrafficEnabled());
            mNetTrafficUnits.setEnabled(netTrafficEnabled());
            mNetTrafficAutohide.setEnabled(netTrafficEnabled());
            return true;
        } else if (preference == mNetTrafficMode) {
            int mode = Integer.valueOf((String) newValue);
            int index = mNetTrafficMode.findIndexOfValue((String) newValue);
            Settings.Secure.putIntForUser(getActivity().getContentResolver(),
                    Settings.Secure.NETWORK_TRAFFIC_MODE,
                    mode, UserHandle.USER_CURRENT);
            mNetTrafficMode.setSummary(mNetTrafficMode.getEntries()[index]);
            mNetTrafficRefreshInterval.setEnabled(netTrafficEnabled());
            return true;
        } else if (preference == mNetTrafficUnits) {
            int mode = Integer.valueOf((String) newValue);
            int index = mNetTrafficUnits.findIndexOfValue((String) newValue);
            Settings.Secure.putIntForUser(getActivity().getContentResolver(),
                    Settings.Secure.NETWORK_TRAFFIC_UNITS,
                    mode, UserHandle.USER_CURRENT);
            mNetTrafficUnits.setSummary(mNetTrafficUnits.getEntries()[index]);
            mNetTrafficRefreshInterval.setEnabled(netTrafficEnabled());
            return true;
        } else if (preference == mNetTrafficAutohideThreshold) {
            int thresholdValue = (Integer) newValue;
            Settings.Secure.putIntForUser(getContentResolver(),
                    Settings.Secure.NETWORK_TRAFFIC_AUTOHIDE_THRESHOLD,
                    thresholdValue, UserHandle.USER_CURRENT);
            return true;
        } else if (preference == mNetTrafficRefreshInterval) {
            int interval = (Integer) newValue;
            Settings.Secure.putIntForUser(getContentResolver(),
                    Settings.Secure.NETWORK_TRAFFIC_REFRESH_INTERVAL,
                    interval, UserHandle.USER_CURRENT);
            return true;
        } else if (preference == mNetTrafficAutohide) {
            boolean autoHide = (Boolean) newValue;
            Settings.Secure.putIntForUser(resolver,
                Settings.Secure.NETWORK_TRAFFIC_AUTOHIDE, autoHide ? 1 : 0, UserHandle.USER_CURRENT);
            return true;
        } else if (preference == mNetTrafficHideArrow) {
            boolean hideArrows = (Boolean) newValue;
            Settings.Secure.putIntForUser(resolver,
                Settings.Secure.NETWORK_TRAFFIC_HIDEARROW, hideArrows ? 1 : 0, UserHandle.USER_CURRENT);
            return true;
        }
        return false;
    }

    private boolean netTrafficEnabled() {
        final ContentResolver resolver = getActivity().getContentResolver();
        return Settings.Secure.getInt(resolver,
                Settings.Secure.NETWORK_TRAFFIC_LOCATION, 0) != 0;
	}

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.CUSTOM_SETTINGS;
    }

    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
        new BaseSearchIndexProvider() {
            @Override
            public List<SearchIndexableResource> getXmlResourcesToIndex(Context context,
                    boolean enabled) {
                final ArrayList<SearchIndexableResource> result = new ArrayList<>();
                final SearchIndexableResource sir = new SearchIndexableResource(context);
                sir.xmlResId = R.xml.traffic;
                result.add(sir);
                return result;
            }

            @Override
            public List<String> getNonIndexableKeys(Context context) {
                final List<String> keys = super.getNonIndexableKeys(context);
                return keys;
            }
    };
}
