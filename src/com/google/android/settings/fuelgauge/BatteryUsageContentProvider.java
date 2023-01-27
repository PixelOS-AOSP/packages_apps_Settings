package com.google.android.settings.fuelgauge;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.util.Log;

import com.android.settings.fuelgauge.batteryusage.BatteryChartPreferenceController;
import com.android.settings.fuelgauge.batteryusage.BatteryDiffEntry;

import java.util.List;

public final class BatteryUsageContentProvider extends ContentProvider {

    private static final String TAG = "BatteryUsageContentProvider";
    private static final String BATTERY_USAGE = "com.google.android.settings.fuelgauge.provider";

    private static final UriMatcher URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
    public static List<BatteryDiffEntry> mCacheBatteryDiffEntries;

    static {
        URI_MATCHER.addURI(BATTERY_USAGE, "BatteryUsageState", 1);
        mCacheBatteryDiffEntries = null;
    }

    @Override
    public boolean onCreate() {
        Log.v(TAG, "initialize provider");
        return true;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        throw new UnsupportedOperationException("insert() unsupported!");
    }

    @Override
    public int update(Uri uri, ContentValues values,
            String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException("update() unsupported!");
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException("delete() unsupported!");
    }

    @Override
    public Cursor query(Uri uri, String[] projection,
            String selection, String[] selectionArgs, String sortOrder) {
        Log.d(TAG, "query:" + uri);
        if (URI_MATCHER.match(uri) != 1) {
            return null;
        }
        return getBatteryUsageData();
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    private Cursor getBatteryUsageData() {
        List<BatteryDiffEntry> list = mCacheBatteryDiffEntries;
        if (list == null) {
            list = BatteryChartPreferenceController.getAppBatteryUsageData(getContext());
        }
        if (list == null || list.isEmpty()) {
            Log.w(TAG, "no data found in the getBatterySinceLastFullChargeUsageData()");
            return null;
        }
        final MatrixCursor matrixCursor = new MatrixCursor(BatteryUsageContract.KEYS_BATTERY_USAGE_STATE);
        list.forEach(batteryDiffEntry -> {
            if (batteryDiffEntry.mBatteryHistEntry == null
                    || batteryDiffEntry.getPercentOfTotal() == 0) return;
            addUsageDataRow(matrixCursor, batteryDiffEntry);
        });
        Log.d(TAG, "usage data count:" + matrixCursor.getCount());
        return matrixCursor;
    }

    private static void addUsageDataRow(MatrixCursor matrixCursor, BatteryDiffEntry batteryDiffEntry) {
        String packageName = batteryDiffEntry.getPackageName();
        if (packageName == null) {
            Log.w(TAG, "no package name found for\n" + batteryDiffEntry);
            return;
        }
        matrixCursor.addRow(new Object[]{
                batteryDiffEntry.mBatteryHistEntry.mUserId,
                packageName,
                batteryDiffEntry.getPercentOfTotal(),
                batteryDiffEntry.mForegroundUsageTimeInMs,
                batteryDiffEntry.mBackgroundUsageTimeInMs});
    }
}
