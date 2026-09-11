package com.lukeiscooldev.honeylauncher;

import android.content.ComponentName;
import android.graphics.drawable.Drawable;

public class AppInfo {
    public final ComponentName componentName;
    public final CharSequence title;
    public final Drawable icon;

    public AppInfo(ComponentName componentName, CharSequence title, Drawable icon) {
        this.componentName = componentName;
        this.title = title;
        this.icon = icon;
    }

    public String serialize() {
        return componentName.flattenToString();
    }

    public static ComponentName deserialize(String flattened) {
        return ComponentName.unflattenFromString(flattened);
    }
}
