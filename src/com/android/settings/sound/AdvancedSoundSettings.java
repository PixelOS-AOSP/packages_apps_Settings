package com.android.settings.sound;

import com.android.internal.logging.nano.MetricsProto;

import android.os.Bundle;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.UserHandle;
import com.android.settings.notification.*;
import android.content.ContentResolver;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.content.res.Resources;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceGroup;
import androidx.preference.PreferenceScreen;
import androidx.preference.PreferenceCategory;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.PreferenceFragment;
import androidx.preference.SwitchPreference;
import android.provider.Settings;
import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;
import java.util.Locale;
import android.text.TextUtils;
import android.content.Context;
import androidx.fragment.app.Fragment;
import android.view.View;
import android.os.UserHandle;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import android.util.Log;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.lifecycle.Lifecycle;
import org.json.JSONException;
import org.json.JSONObject;
import static android.os.UserHandle.USER_SYSTEM;
import android.os.RemoteException;
import android.os.ServiceManager;
import static android.os.UserHandle.USER_CURRENT;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import com.android.settings.widget.PreferenceCategoryController;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.SearchIndexable;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
import java.util.regex.Pattern;

@SearchIndexable(forTarget = SearchIndexable.ALL & ~SearchIndexable.ARC)
public class AdvancedSoundSettings extends DashboardFragment implements
        OnPreferenceChangeListener {

    public static final String TAG = "AdvancedSoundSettings";
    private Context mContext;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        final ContentResolver resolver = getActivity().getContentResolver();

        PreferenceScreen prefSet = getPreferenceScreen();

    }

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.sound_settings_advanced;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        return false;
    }

    @Override
    protected List<AbstractPreferenceController> createPreferenceControllers(Context context) {
        return buildPreferenceControllers(context, this, getSettingsLifecycle());
    }
        
    private static List<AbstractPreferenceController> buildPreferenceControllers(Context context,
            AdvancedSoundSettings fragment, Lifecycle lifecycle) {
        final List<AbstractPreferenceController> controllers = new ArrayList<>();
            
          // === Other Sound Settings ===
        final DialPadTonePreferenceController dialPadTonePreferenceController =
                new DialPadTonePreferenceController(context, fragment, lifecycle);
        final ScreenLockSoundPreferenceController screenLockSoundPreferenceController =
                new ScreenLockSoundPreferenceController(context, fragment, lifecycle);
        final ChargingSoundPreferenceController chargingSoundPreferenceController =
                new ChargingSoundPreferenceController(context, fragment, lifecycle);
        final DockingSoundPreferenceController dockingSoundPreferenceController =
                new DockingSoundPreferenceController(context, fragment, lifecycle);
        final TouchSoundPreferenceController touchSoundPreferenceController =
                new TouchSoundPreferenceController(context, fragment, lifecycle);
        final DockAudioMediaPreferenceController dockAudioMediaPreferenceController =
                new DockAudioMediaPreferenceController(context, fragment, lifecycle);
        final BootSoundPreferenceController bootSoundPreferenceController =
                new BootSoundPreferenceController(context);
        final EmergencyTonePreferenceController emergencyTonePreferenceController =
                new EmergencyTonePreferenceController(context, fragment, lifecycle);
        final ScreenshotSoundPreferenceController screenshotSoundPreferenceController =
                new ScreenshotSoundPreferenceController(context, fragment, lifecycle);

        controllers.add(dialPadTonePreferenceController);
        controllers.add(screenLockSoundPreferenceController);
        controllers.add(chargingSoundPreferenceController);
        controllers.add(dockingSoundPreferenceController);
        controllers.add(touchSoundPreferenceController);
        controllers.add(dockAudioMediaPreferenceController);
        controllers.add(bootSoundPreferenceController);
        controllers.add(emergencyTonePreferenceController);
        controllers.add(screenshotSoundPreferenceController);
        controllers.add(new PreferenceCategoryController(context,
                "other_sounds_and_vibrations_category").setChildren(
                Arrays.asList(dialPadTonePreferenceController,
                        screenLockSoundPreferenceController,
                        chargingSoundPreferenceController,
                        dockingSoundPreferenceController,
                        touchSoundPreferenceController,
                        dockAudioMediaPreferenceController,
                        bootSoundPreferenceController,
                        emergencyTonePreferenceController,
                        screenshotSoundPreferenceController)));

        return controllers;
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }
    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.CUSTOM;
    }

    /**
     * For Search.
     */
    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider(R.xml.sound_settings_advanced);
}
