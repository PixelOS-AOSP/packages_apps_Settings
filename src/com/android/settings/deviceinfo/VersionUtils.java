
package com.android.settings.deviceinfo;

import android.os.SystemProperties;

public class VersionUtils {
    public static String getcustomVersion(){
        return SystemProperties.get("ro.custom.version","");
    }
}
