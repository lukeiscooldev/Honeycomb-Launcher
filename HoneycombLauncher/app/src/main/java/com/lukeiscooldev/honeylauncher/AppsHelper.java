package com.lukeiscooldev.honeylauncher;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class AppsHelper {

    public static List<AppInfo> loadAllApps(Context context) {
        PackageManager pm = context.getPackageManager();
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        List<ResolveInfo> resolveInfos = pm.queryIntentActivities(mainIntent, 0);
        List<AppInfo> apps = new ArrayList<>();
        String selfPackage = context.getPackageName();

        for (ResolveInfo info : resolveInfos) {
            if (info.activityInfo.packageName.equals(selfPackage)) {
                continue; // don't list ourselves in our own drawer
            }
            ComponentName cn = new ComponentName(
                    info.activityInfo.packageName, info.activityInfo.name);
            CharSequence label = info.loadLabel(pm);
            Drawable icon = info.loadIcon(pm);
            apps.add(new AppInfo(cn, label, icon));
        }

        final Collator collator = Collator.getInstance();
        Collections.sort(apps, new Comparator<AppInfo>() {
            @Override
            public int compare(AppInfo a, AppInfo b) {
                return collator.compare(a.title.toString(), b.title.toString());
            }
        });

        return apps;
    }
}
