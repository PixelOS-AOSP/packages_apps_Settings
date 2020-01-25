/*
 * Copyright (C) 2020 The AOSPA Project
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
 * limitations under the License
 */

package com.android.settings.gestures;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.settings.SettingsEnums;
import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Switch;
import android.widget.CompoundButton;

import com.android.settings.R;
import com.android.settings.core.instrumentation.InstrumentedDialogFragment;

public class ThreeButtonNavigationInvertDialog extends InstrumentedDialogFragment {
    private static final String TAG = "ThreeButtonNavigationInvertDialog";
    private static final String NAV_BAR_INVERSE = "sysui_nav_bar_inverse";

    private static Context mContext;

    public static void show(SystemNavigationGestureSettings parent) {
        if (!parent.isAdded()) {
            return;
        }

        mContext = parent.getContext();
        final ThreeButtonNavigationInvertDialog dialog =
                new ThreeButtonNavigationInvertDialog();
        final Bundle bundle = new Bundle();
        dialog.setArguments(bundle);
        dialog.setTargetFragment(parent, 0);
        dialog.show(parent.getFragmentManager(), TAG);
    }

    @Override
    public int getMetricsCategory() {
        return SettingsEnums.SETTINGS_GESTURE_NAV_BACK_SENSITIVITY_DLG;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final ContentResolver mContentResolver = mContext.getContentResolver();
        final View view = getActivity().getLayoutInflater().inflate(
                R.layout.dialog_invert_navbar_layout, null);
        final Switch sw = view.findViewById(R.id.invert_navbar_switch);
        sw.setChecked(Settings.Secure.getInt(mContentResolver, NAV_BAR_INVERSE, 0) != 0);
        sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Settings.Secure.putInt(mContentResolver, NAV_BAR_INVERSE, isChecked ? 1 : 0);
            }
        });
        return new AlertDialog.Builder(getContext())
                .setTitle(R.string.navbar_invert_layout_title)
                .setView(view)
                .setPositiveButton(R.string.okay, null)
                .create();
    }

}
