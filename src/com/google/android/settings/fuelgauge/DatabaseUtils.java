package com.google.android.settings.fuelgauge;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.BatteryUsageStats;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.os.UserHandle;
import android.os.UserManager;
import android.util.Log;

import com.android.settings.fuelgauge.batteryusage.BatteryEntry;
import com.android.settings.fuelgauge.batteryusage.BatteryHistEntry;
import com.android.settings.fuelgauge.batteryusage.ConvertUtils;
import com.android.settingslib.fuelgauge.BatteryStatus;

import java.time.Clock;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class DatabaseUtils {

    private static final String TAG = "DatabaseUtils";

    private static final long CLEAR_MEMORY_THRESHOLD_MS = Duration.ofMinutes(5).toMillis();
    private static final long CLEAR_MEMORY_DELAYED_MS = Duration.ofSeconds(2).toMillis();

    private static final String SETTINGS_INTELLIGENCE_PKG =
            "com.google.android.settings.intelligence";
    private static final String BATTERY_PROVIDER =
            SETTINGS_INTELLIGENCE_PKG + ".modules.battery.provider";
    private static final String BATTERY_SETTINGS_CONTENT_PROVIDER =
            SETTINGS_INTELLIGENCE_PKG + ".modules.battery.impl.BatterySettingsContentProvider";
    private static final String LAST_FULL_TIMESTAMP = "last_full_charge_timestamp_key";

    public static final Uri BATTERY_CONTENT_URI = new Uri.Builder()
            .scheme("content")
            .authority(BATTERY_PROVIDER)
            .appendPath("BatteryState")
            .build();

    public static boolean isContentProviderEnabled(Context context) {
        return context.getPackageManager().getComponentEnabledSetting(new ComponentName(
                SETTINGS_INTELLIGENCE_PKG,
                BATTERY_SETTINGS_CONTENT_PROVIDER)
        ) == PackageManager.COMPONENT_ENABLED_STATE_ENABLED;
    }

    public static List<ContentValues> sendBatteryEntryData(
            Context context,
            List<BatteryEntry> list,
            final BatteryUsageStats batteryUsageStats) {
        long startTime = System.currentTimeMillis();
        final List<ContentValues> arrayList = new ArrayList<>();
        int size = 1;
        Intent batteryIntent = getBatteryIntent(context);
        if (batteryIntent == null) {
            Log.e(TAG, "sendBatteryEntryData(): cannot fetch battery intent");
            clearMemory();
            return null;
        }
        final int batteryLevel = getBatteryLevel(batteryIntent);
        final int status = batteryIntent.getIntExtra("status", 1);
        final int health = batteryIntent.getIntExtra("health", 1);
        long millis = Clock.systemUTC().millis();
        final long elapsedRealtime = SystemClock.elapsedRealtime();
        if (list != null) {
            list.stream().filter(batteryEntry -> {
                long timeInForegroundMs = batteryEntry.getTimeInForegroundMs();
                long timeInBackgroundMs = batteryEntry.getTimeInBackgroundMs();
                if (batteryEntry.getConsumedPower() == 0.0d &&
                        (timeInForegroundMs != 0 || timeInBackgroundMs != 0)) {
                    Log.w(TAG, String.format("no consumed power but has running time for %s time=%d|%d",
                            batteryEntry.getLabel(), timeInForegroundMs, timeInBackgroundMs));
                }
                return (batteryEntry.getConsumedPower() != 0.0d ||
                        timeInForegroundMs != 0 || timeInBackgroundMs != 0);
            }).forEach(batteryEntry -> arrayList.add(
                    ConvertUtils.convertToContentValues(batteryEntry, batteryUsageStats,
                            batteryLevel, status, health, elapsedRealtime, millis)));
        }
        ContentResolver contentResolver = context.getContentResolver();
        if (!arrayList.isEmpty()) {
            try {
                size = contentResolver.bulkInsert(BATTERY_CONTENT_URI,
                        arrayList.toArray(new ContentValues[arrayList.size()]));
            } catch (Exception e) {
                Log.e(TAG, "bulkInsert() data into database error:\n" + e);
            }
        } else {
            ContentValues convert = ConvertUtils.convertToContentValues(null, null, batteryLevel,
                    status, health, elapsedRealtime, millis);
            try {
                contentResolver.insert(BATTERY_CONTENT_URI, convert);
            } catch (Exception e2) {
                Log.e(TAG, "insert() data into database error:\n" + e2);
            }
            arrayList.add(convert);
        }
        saveLastFullChargeTimestampPref(context, status, batteryLevel, millis);
        contentResolver.notifyChange(BATTERY_CONTENT_URI, null);
        Log.d(TAG, String.format("sendBatteryEntryData() size=%d in %d/ms",
                size, (System.currentTimeMillis() - startTime)));
        clearMemory();
        return arrayList;
    }

    static void saveLastFullChargeTimestampPref(
            Context context, int status, int level, long timestamp) {
        if (BatteryStatus.isCharged(status, level) && !getSharedPreferences(context)
                .edit().putLong(LAST_FULL_TIMESTAMP, timestamp).commit()) {
            Log.w(TAG, "saveLastFullChargeTimestampPref() fail: value=" + timestamp);
        }
    }

    static long getLastFullChargeTimestampPref(Context context) {
        return getSharedPreferences(context).getLong(LAST_FULL_TIMESTAMP, 0L);
    }

    public static Map<Long, Map<String, BatteryHistEntry>> getHistoryMapSinceLastFullCharge(
            Context context, Calendar calendar) {
        long startTime = System.currentTimeMillis();
        Map<Long, Map<String, BatteryHistEntry>> loadHistoryMapFromContentProvider =
                loadHistoryMapFromContentProvider(context, new Uri.Builder()
                        .scheme("content")
                        .authority(BATTERY_PROVIDER)
                        .appendPath("BatteryState")
                        .appendQueryParameter("timestamp",
                                Long.toString(getStartTimestampForLastFullCharge(context, calendar)))
                        .build());
        if (loadHistoryMapFromContentProvider == null || loadHistoryMapFromContentProvider.isEmpty()) {
            Log.d(TAG, "getHistoryMapSinceLastFullCharge() returns empty or null");
        } else {
            Log.d(TAG, String.format("getHistoryMapSinceLastFullCharge() size=%d in %d/ms",
                    loadHistoryMapFromContentProvider.size(), (System.currentTimeMillis() - startTime)));
        }
        return loadHistoryMapFromContentProvider;
    }

    static long getStartTimestampForLastFullCharge(Context context, Calendar calendar) {
        return Math.max(getLastFullChargeTimestampPref(context), getTimestampSixDaysAgo(calendar));
    }

    private static Map<Long, Map<String, BatteryHistEntry>> loadHistoryMapFromContentProvider(
            Context context, Uri uri) {
        HashMap<Long, Map<String, BatteryHistEntry>> hashMap = new HashMap<>();
        boolean isWorkProfileUser = isWorkProfileUser(context);
        Log.d(TAG, "loadHistoryMapFromContentProvider() isWorkProfileUser:" + isWorkProfileUser);
        if (isWorkProfileUser) {
            try {
                context = context.createPackageContextAsUser(context.getPackageName(), 0, UserHandle.OWNER);
            } catch (PackageManager.NameNotFoundException e) {
                Log.e(TAG, "context.createPackageContextAsUser() fail:" + e);
                return null;
            }
        }
        if (isContentProviderEnabled(context)) {
            try (Cursor query = context.getContentResolver().query(uri, null, null, null);) {
                if (query.getCount() != 0) {
                    while (query.moveToNext()) {
                        BatteryHistEntry batteryHistEntry = new BatteryHistEntry(query);
                        long timestamp = batteryHistEntry.mTimestamp;
                        String entryKey = batteryHistEntry.getKey();
                        Map<String, BatteryHistEntry> historyMap =
                                hashMap.computeIfAbsent(timestamp, k -> new HashMap<>());
                        historyMap.put(entryKey, batteryHistEntry);
                    }
                }
            }
        }
        return hashMap;
    }

    private static Intent getBatteryIntent(Context context) {
        return context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    }

    private static int getBatteryLevel(Intent intent) {
        int level = intent.getIntExtra("level", -1);
        int scale = intent.getIntExtra("scale", 0);
        if (scale == 0) {
            return -1;
        }
        return Math.round((level / scale) * 100.0f);
    }

    private static SharedPreferences getSharedPreferences(Context context) {
        return context.getApplicationContext().createDeviceProtectedStorageContext().getSharedPreferences("battery_module_preference", 0);
    }

    private static long getTimestampSixDaysAgo(Calendar calendar) {
        Calendar sixDayAgoCalendar = calendar == null
                ? Calendar.getInstance() : (Calendar) calendar.clone();
        sixDayAgoCalendar.add(Calendar.DAY_OF_YEAR, -6);
        sixDayAgoCalendar.set(Calendar.HOUR_OF_DAY, 0);
        sixDayAgoCalendar.set(Calendar.MINUTE, 0);
        sixDayAgoCalendar.set(Calendar.SECOND, 0);
        sixDayAgoCalendar.set(Calendar.MILLISECOND, 0);
        return sixDayAgoCalendar.getTimeInMillis();
    }

    static boolean isWorkProfileUser(Context context) {
        UserManager userManager = (UserManager) context.getSystemService(UserManager.class);
        return userManager.isManagedProfile() && !userManager.isSystemUser();
    }

    private static void clearMemory() {
        if (SystemClock.uptimeMillis() > CLEAR_MEMORY_THRESHOLD_MS) return;
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            System.gc();
            System.runFinalization();
            System.gc();
            Log.w(TAG, "invoke clearMemory()");
        }, CLEAR_MEMORY_DELAYED_MS);
    }
}
