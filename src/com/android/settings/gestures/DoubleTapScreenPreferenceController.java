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

import static android.provider.Settings.Secure.DOZE_DOUBLE_TAP_GESTURE;

import android.annotation.UserIdInt;
import android.content.Context;
import android.content.ContentResolver;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.hardware.display.AmbientDisplayConfiguration;
import android.net.Uri;
import android.os.Handler;
import android.os.UserHandle;
import android.provider.Settings;
import android.text.TextUtils;

import androidx.annotation.VisibleForTesting;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;

import com.android.settings.R;
import com.android.settings.core.TogglePreferenceController;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnStart;
import com.android.settingslib.core.lifecycle.events.OnStop;
import com.android.settingslib.PrimarySwitchPreference;

public class DoubleTapScreenPreferenceController extends TogglePreferenceController
        implements LifecycleObserver, OnStart, OnStop {

    private final int ON = 1;
    private final int OFF = 0;

    private static final String SECURE_KEY = DOZE_DOUBLE_TAP_GESTURE;
    private static final String AMBIENT_SECURE_KEY = "doze_double_tap_gesture_ambient";

    private AmbientDisplayConfiguration mAmbientConfig;
    @UserIdInt
    private final int mUserId;

    private PrimarySwitchPreference mPreference;
    private SettingObserver mSettingObserver;

    public DoubleTapScreenPreferenceController(Context context, String key) {
        super(context, key);
        mUserId = UserHandle.myUserId();
    }

    public DoubleTapScreenPreferenceController setConfig(AmbientDisplayConfiguration config) {
        mAmbientConfig = config;
        return this;
    }

    public static boolean isSuggestionComplete(Context context, SharedPreferences prefs) {
        return isSuggestionComplete(new AmbientDisplayConfiguration(context), prefs);
    }

    @VisibleForTesting
    static boolean isSuggestionComplete(AmbientDisplayConfiguration config,
            SharedPreferences prefs) {
        return !config.doubleTapSensorAvailable()
                || prefs.getBoolean(DoubleTapScreenSettings.PREF_KEY_SUGGESTION_COMPLETE, false);
    }

    @Override
    public int getAvailabilityStatus() {
        // No hardware support for Double Tap
        if (!getAmbientConfig().doubleTapSensorAvailable()) {
            return UNSUPPORTED_ON_DEVICE;
        }

        return AVAILABLE;
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        mPreference = screen.findPreference(getPreferenceKey());
        mSettingObserver = new SettingObserver(mPreference);
    }

    @Override
    public void updateState(Preference preference) {
        super.updateState(preference);
        final ContentResolver resolver = mContext.getContentResolver();
        final boolean enabled =
                Settings.Secure.getInt(resolver, SECURE_KEY, ON) == ON;
        String summary;
        if (enabled) {
            summary = mContext.getString(R.string.gesture_setting_on) + " ("
                    + (Settings.Secure.getInt(resolver, AMBIENT_SECURE_KEY, 0) == 1
                    ? mContext.getString(R.string.gesture_wake_ambient)
                    : mContext.getString(R.string.gesture_wake)) + ")";
        } else {
            summary = mContext.getString(R.string.gesture_setting_off);
        }
        preference.setSummary(summary);
    }

    @Override
    public boolean isSliceable() {
        return TextUtils.equals(getPreferenceKey(), "gesture_double_tap_screen");
    }

    @Override
    public boolean isPublicSlice() {
        return true;
    }

    @Override
    public boolean setChecked(boolean isChecked) {
        return Settings.Secure.putInt(mContext.getContentResolver(), SECURE_KEY,
                isChecked ? ON : OFF);
    }

    @Override
    public boolean isChecked() {
        return getAmbientConfig().doubleTapGestureEnabled(mUserId);
    }

    private AmbientDisplayConfiguration getAmbientConfig() {
        if (mAmbientConfig == null) {
            mAmbientConfig = new AmbientDisplayConfiguration(mContext);
        }
        return mAmbientConfig;
    }

    @Override
    public int getSliceHighlightMenuRes() {
        return NO_RES;
    }

    @Override
    public void onStart() {
        if (mSettingObserver != null) {
            mSettingObserver.register(mContext.getContentResolver());
            mSettingObserver.onChange(false, null);
        }
    }

    @Override
    public void onStop() {
        if (mSettingObserver != null) {
            mSettingObserver.unregister(mContext.getContentResolver());
        }
    }

    private class SettingObserver extends ContentObserver {
        private final Uri mUri = Settings.Secure.getUriFor(SECURE_KEY);
        private final Uri mAmbientUri = Settings.Secure.getUriFor(AMBIENT_SECURE_KEY);

        private final Preference mPreference;

        SettingObserver(Preference preference) {
            super(Handler.getMain());
            mPreference = preference;
        }

        public void register(ContentResolver cr) {
            cr.registerContentObserver(mUri, false, this);
            cr.registerContentObserver(mAmbientUri, false, this);
        }

        public void unregister(ContentResolver cr) {
            cr.unregisterContentObserver(this);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            super.onChange(selfChange, uri);
            if (uri == null || mUri.equals(uri) || mAmbientUri.equals(uri)) {
                updateState(mPreference);
            }
        }
    }
}
