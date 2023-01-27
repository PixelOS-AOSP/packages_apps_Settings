package com.google.android.settings.fuelgauge;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.util.ArraySet;

import androidx.window.R;

import com.android.settings.fuelgauge.PowerUsageFeatureProviderImpl;
import com.android.settings.fuelgauge.batteryusage.BatteryHistEntry;

import java.util.Calendar;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

public class PowerUsageFeatureProviderGoogleImpl extends PowerUsageFeatureProviderImpl {

    private static final String ACTION_RESUME_CHARGING = "PNW.defenderResumeCharging.settings";
    private final String PACKAGE_NAME_SYSTEMUI = "com.android.systemui";

    public static boolean sChartGraphEnabled;

    public PowerUsageFeatureProviderGoogleImpl(Context context) {
        super(context);
    }

    public void setPackageManager(PackageManager packageManager) {
        mPackageManager = packageManager;
    }

    @Override
    public String getAdvancedUsageScreenInfoString() {
        return mContext.getString(R.string.advanced_battery_graph_subtext);
    }

    @Override
    public boolean isChartGraphEnabled(Context context) {
        return sChartGraphEnabled = DatabaseUtils.isContentProviderEnabled(context);
    }

    @Override
    public Intent getResumeChargeIntent(boolean isDockDefender) {
        return new Intent(ACTION_RESUME_CHARGING)
                .setPackage(PACKAGE_NAME_SYSTEMUI)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    }

    @Override
    public Map<Long, Map<String, BatteryHistEntry>> getBatteryHistorySinceLastFullCharge(Context context) {
        return DatabaseUtils.getHistoryMapSinceLastFullCharge(context, Calendar.getInstance());
    }

    @Override
    public Uri getBatteryHistoryUri() {
        return DatabaseUtils.BATTERY_CONTENT_URI;
    }

    @Override
    public Set<CharSequence> getHideBackgroundUsageTimeSet(Context context) {
        Set<CharSequence> timeSet = new ArraySet<>();
        Collections.addAll(timeSet, context.getResources()
                .getTextArray(R.array.allowlist_hide_background_in_battery_usage));
        return timeSet;
    }

    @Override
    public CharSequence[] getHideApplicationEntries(Context context) {
        return context.getResources().getTextArray(R.array.allowlist_hide_entry_in_battery_usage);
    }

    @Override
    public CharSequence[] getHideApplicationSummary(Context context) {
        return context.getResources().getTextArray(R.array.allowlist_hide_summary_in_battery_usage);
    }
}
