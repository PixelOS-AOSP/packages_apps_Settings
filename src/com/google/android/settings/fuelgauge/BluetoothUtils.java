package com.google.android.settings.fuelgauge;

import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.ContentValues;
import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import com.android.settingslib.bluetooth.CachedBluetoothDevice;

final class BluetoothUtils {
    private static String emptyIfNull(String value) {
        return value == null ? "" : value;
    }

    public static ContentValues wrapBluetoothData(
            Context context, CachedBluetoothDevice cachedBluetoothDevice,
            boolean values) {
        BluetoothDevice device = cachedBluetoothDevice.getDevice();

        ContentValues contentValues = new ContentValues();
        contentValues.put("type", device.getType());
        contentValues.put("name", emptyIfNull(device.getName()));
        contentValues.put("alias", emptyIfNull(device.getAlias()));
        contentValues.put("address", emptyIfNull(device.getAddress()));
        contentValues.put("batteryLevel", device.getBatteryLevel());

        putStringMetadata(contentValues, "hardwareVersion", device.getMetadata(
                BluetoothDevice.METADATA_HARDWARE_VERSION));
        putStringMetadata(contentValues, "batteryLevelRight", device.getMetadata(
                BluetoothDevice.METADATA_UNTETHERED_RIGHT_BATTERY));
        putStringMetadata(contentValues, "batteryLevelLeft", device.getMetadata(
                BluetoothDevice.METADATA_UNTETHERED_LEFT_BATTERY));
        putStringMetadata(contentValues, "batteryLevelCase", device.getMetadata(
                BluetoothDevice.METADATA_UNTETHERED_CASE_BATTERY));
        putStringMetadata(contentValues, "batteryChargingRight", device.getMetadata(
                BluetoothDevice.METADATA_UNTETHERED_RIGHT_CHARGING));
        putStringMetadata(contentValues, "batteryChargingLeft", device.getMetadata(
                BluetoothDevice.METADATA_UNTETHERED_LEFT_CHARGING));
        putStringMetadata(contentValues, "batteryChargingCase", device.getMetadata(
                BluetoothDevice.METADATA_UNTETHERED_CASE_CHARGING));
        putStringMetadata(contentValues, "batteryChargingMain", device.getMetadata(
                BluetoothDevice.METADATA_MAIN_CHARGING));
        if (values) {
            putStringMetadata(contentValues, "deviceIconMain", device.getMetadata(
                    BluetoothDevice.METADATA_MAIN_ICON));
            putStringMetadata(contentValues, "deviceIconCase", device.getMetadata(
                    BluetoothDevice.METADATA_UNTETHERED_CASE_ICON));
            putStringMetadata(contentValues, "deviceIconLeft", device.getMetadata(
                    BluetoothDevice.METADATA_UNTETHERED_LEFT_ICON));
            putStringMetadata(contentValues, "deviceIconRight", device.getMetadata(
                    BluetoothDevice.METADATA_UNTETHERED_RIGHT_ICON));
        }
        BluetoothClass bluetoothClass = device.getBluetoothClass();
        if (bluetoothClass != null) {
            contentValues.put("bluetoothClass", marshall(bluetoothClass));
        }
        return contentValues;
    }

    static byte[] marshall(Parcelable parcelable) {
        Parcel obtain = Parcel.obtain();
        parcelable.writeToParcel(obtain, 0);
        byte[] marshall = obtain.marshall();
        obtain.recycle();
        return marshall;
    }

    private static void putStringMetadata(
            ContentValues contentValues, String key, byte[] value) {
        if (value == null || value.length == 0) return;
        contentValues.put(key, new String(value));
    }
}
