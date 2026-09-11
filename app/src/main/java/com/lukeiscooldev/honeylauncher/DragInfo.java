package com.lukeiscooldev.honeylauncher;

/**
 * Carried as the "local state" of an in-progress icon drag so the drop
 * targets (workspace pages, the remove zone) know what's being moved and
 * whether it's a brand new placement from the drawer or a move of an
 * existing home-screen icon.
 */
public class DragInfo {
    public final AppInfo appInfo;
    public final boolean isNew;
    public CellLayout sourcePage;
    public int sourceCellX = -1;
    public int sourceCellY = -1;

    public DragInfo(AppInfo appInfo, boolean isNew) {
        this.appInfo = appInfo;
        this.isNew = isNew;
    }
}
