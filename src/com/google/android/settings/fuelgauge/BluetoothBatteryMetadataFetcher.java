package com.google.android.settings.fuelgauge;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.ResultReceiver;
import android.util.Log;

import com.android.internal.annotations.VisibleForTesting;
import com.android.settingslib.bluetooth.CachedBluetoothDevice;
import com.android.settingslib.bluetooth.LocalBluetoothManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public final class BluetoothBatteryMetadataFetcher {

    private static final String TAG = "BluetoothBatteryMetadataFetcher";

    private static final String EXTRA_FETCH_ICON = "extra_fetch_icon";

    @VisibleForTesting
    static LocalBluetoothManager mLocalBluetoothManager;

    public static void returnBluetoothDevices(Context context, Intent intent) {
        AsyncTask.execute(() -> returnBluetoothDevicesInner(context, intent));
    }

    public static void returnBluetoothDevicesInner(Context context, Intent intent) {
        ResultReceiver resultReceiver = intent.getParcelableExtra(Intent.EXTRA_RESULT_RECEIVER);
        if (resultReceiver == null) {
            Log.w(TAG, "No result receiver found from intent");
            return;
        }

        if (mLocalBluetoothManager == null) {
            mLocalBluetoothManager = LocalBluetoothManager.getInstance(context, null);
        }

        BluetoothAdapter adapter = context.getSystemService(BluetoothManager.class).getAdapter();
        if (adapter == null || !adapter.isEnabled() || mLocalBluetoothManager == null) {
            Log.w(TAG, "BluetoothAdapter not present or not enabled");
            resultReceiver.send(1, null);
            return;
        }

        sendAndFilterBluetoothData(context, resultReceiver, mLocalBluetoothManager,
                intent.getBooleanExtra(EXTRA_FETCH_ICON, false));
    }

    static void sendAndFilterBluetoothData(Context context,
                                           ResultReceiver resultReceiver,
                                           LocalBluetoothManager localBluetoothManager,
                                           boolean cache) {
        long startTime = System.currentTimeMillis();
        Collection<CachedBluetoothDevice> cachedDevicesCopy =
                localBluetoothManager.getCachedDeviceManager().getCachedDevicesCopy();
        Log.d(TAG, "Cached devices:" + cachedDevicesCopy);

        if (cachedDevicesCopy == null || cachedDevicesCopy.isEmpty()) {
            resultReceiver.send(0, Bundle.EMPTY);
            return;
        }

        List<CachedBluetoothDevice> list = cachedDevicesCopy.stream()
                .filter(CachedBluetoothDevice::isConnected)
                .collect(Collectors.toList());
        Log.d(TAG, "Connected devices:" + list);

        if (list.isEmpty()) {
            resultReceiver.send(0, Bundle.EMPTY);
            return;
        }

        ArrayList<ContentValues> bluetoothWrapDataListKey = new ArrayList<>();
        ArrayList<BluetoothDevice> bluetoothParcelableList = new ArrayList<>();
        cachedDevicesCopy.forEach(cachedBluetoothDevice -> {
            BluetoothDevice device = cachedBluetoothDevice.getDevice();
            bluetoothParcelableList.add(device);
            try {
                bluetoothWrapDataListKey.add(
                        BluetoothUtils.wrapBluetoothData(context, cachedBluetoothDevice, cache));
            } catch (Exception e) {
                Log.e(TAG, "Wrap bluetooth data failed: " + device, e);
            }
        });

        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList("bluetoothParcelableListKey", bluetoothParcelableList);
        if (!bluetoothWrapDataListKey.isEmpty()) {
            bundle.putParcelableArrayList("bluetoothWrapDataListKey", bluetoothWrapDataListKey);
        }

        resultReceiver.send(0, bundle);
        Log.d(TAG, String.format("Send and filter bluetooth data size=%d in %d/ms",
                bluetoothWrapDataListKey.size(), (System.currentTimeMillis() - startTime)));
    }
}
