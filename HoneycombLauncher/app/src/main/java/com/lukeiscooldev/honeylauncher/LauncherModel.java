package com.lukeiscooldev.honeylauncher;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashSet;
import java.util.Set;

/**
 * Very small persistence layer: remembers which app sits in which
 * page/cell so the home screen layout survives relaunches. Not a full
 * SQLite-backed provider like the real AOSP launcher model - a
 * SharedPreferences string set is enough for this scope.
 */
public class LauncherModel {

    private static final String PREFS = "honeylauncher_layout";
    private static final String KEY_ITEMS = "items";

    public static class Entry {
        public int page, cellX, cellY;
        public String flattenedComponent;
    }

    public static Set<Entry> load(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        Set<String> raw = prefs.getStringSet(KEY_ITEMS, new HashSet<String>());
        Set<Entry> entries = new HashSet<>();
        for (String s : raw) {
            String[] parts = s.split("\\|", 4);
            if (parts.length != 4) continue;
            try {
                Entry e = new Entry();
                e.page = Integer.parseInt(parts[0]);
                e.cellX = Integer.parseInt(parts[1]);
                e.cellY = Integer.parseInt(parts[2]);
                e.flattenedComponent = parts[3];
                entries.add(e);
            } catch (NumberFormatException ignored) {
            }
        }
        return entries;
    }

    public static void saveAll(Context context, Set<Entry> entries) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        Set<String> raw = new HashSet<>();
        for (Entry e : entries) {
            raw.add(e.page + "|" + e.cellX + "|" + e.cellY + "|" + e.flattenedComponent);
        }
        prefs.edit().putStringSet(KEY_ITEMS, raw).apply();
    }
}
