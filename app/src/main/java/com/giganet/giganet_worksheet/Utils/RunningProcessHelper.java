package com.giganet.giganet_worksheet.Utils;

import android.app.ActivityManager;
import android.content.Context;

import java.util.List;

public class RunningProcessHelper {
    //https://stackoverflow.com/questions/4212992/how-can-i-check-if-an-app-running-on-android
    public static boolean isApplicationRunning(final Context context, final String packageName) {
        final ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        final List<ActivityManager.RunningAppProcessInfo> procInfos = activityManager.getRunningAppProcesses();
        if (procInfos != null) {
            for (final ActivityManager.RunningAppProcessInfo processInfo : procInfos) {
                if (processInfo.processName.equals(packageName)) {
                    return true;
                }
            }
        }
        return false;
    }
}
