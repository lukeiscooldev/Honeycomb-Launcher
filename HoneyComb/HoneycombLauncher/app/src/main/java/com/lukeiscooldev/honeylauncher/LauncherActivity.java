package com.lukeiscooldev.honeylauncher;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LauncherActivity extends Activity {

    private static final int PAGE_COUNT = 5;

    private Workspace workspace;
    private LinearLayout pageIndicator;
    private FrameLayout allAppsContainer;
    private GridView allAppsGrid;
    private View removeZone;
    private int removeZoneHeight;
    private List<AppInfo> allApps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);

        workspace = findViewById(R.id.workspace);
        pageIndicator = findViewById(R.id.page_indicator);
        allAppsContainer = findViewById(R.id.all_apps_container);
        allAppsGrid = findViewById(R.id.all_apps_grid);
        removeZone = findViewById(R.id.remove_zone);
        removeZoneHeight = getResources().getDimensionPixelSize(R.dimen.remove_zone_height);
        removeZone.setTranslationY(-removeZoneHeight);

        findViewById(R.id.search_box).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { launchGlobalSearch(); }
        });
        findViewById(R.id.apps_button).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { toggleAllApps(); }
        });
        findViewById(R.id.add_button).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { showAddDialog(); }
        });

        setUpWorkspace();
        setUpAllAppsGrid();
        setUpDragHandling();
        loadSavedLayout();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (allAppsContainer.getVisibility() == View.VISIBLE) {
            hideAllApps();
        } else {
            workspace.snapToPage(PAGE_COUNT / 2);
        }
    }

    @Override
    public void onBackPressed() {
        if (allAppsContainer.getVisibility() == View.VISIBLE) {
            hideAllApps();
        } else {
            super.onBackPressed();
        }
    }

    private void setUpWorkspace() {
        for (int i = 0; i < PAGE_COUNT; i++) {
            CellLayout page = new CellLayout(this);
            workspace.addView(page);

            ImageView dot = new ImageView(this);
            dot.setImageResource(i == PAGE_COUNT / 2
                    ? R.drawable.dot_active : R.drawable.dot_inactive);
            int dotSize = getResources().getDimensionPixelSize(R.dimen.page_dot_size);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dotSize, dotSize);
            lp.setMargins(8, 0, 8, 0);
            pageIndicator.addView(dot, lp);
        }
        workspace.snapToPage(PAGE_COUNT / 2);

        workspace.setPageChangeListener(new Workspace.PageChangeListener() {
            @Override public void onPageChanged(int page) { updatePageIndicator(page); }
        });

        workspace.setDropListener(new Workspace.WorkspaceDropListener() {
            @Override
            public void onIconDropped(CellLayout page, int cellX, int cellY, DragEvent event, boolean isNewFromDrawer) {
                placeIcon(page, cellX, cellY, event);
            }
        });
    }

    private void setUpAllAppsGrid() {
        allApps = AppsHelper.loadAllApps(this);
        allAppsGrid.setAdapter(new AppsAdapter(this, allApps));
        allAppsGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                AppInfo info = allApps.get(position);
                launchApp(info.componentName);
                hideAllApps();
            }
        });
        allAppsGrid.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                AppInfo info = allApps.get(position);
                startIconDrag(view, new DragInfo(info, true));
                hideAllApps();
                return true;
            }
        });
    }

    private void setUpDragHandling() {
        workspace.setOnDragListener(new View.OnDragListener() {
            @Override
            public boolean onDrag(View v, DragEvent event) {
                switch (event.getAction()) {
                    case DragEvent.ACTION_DRAG_STARTED:
                        removeZone.animate().translationY(0).alpha(1f).setDuration(150).start();
                        break;
                    case DragEvent.ACTION_DRAG_ENDED:
                        removeZone.animate().translationY(-removeZoneHeight).alpha(0f).setDuration(150).start();
                        break;
                }
                return workspace.handleDragEvent(event);
            }
        });

        removeZone.setOnDragListener(new View.OnDragListener() {
            @Override
            public boolean onDrag(View v, DragEvent event) {
                switch (event.getAction()) {
                    case DragEvent.ACTION_DRAG_ENTERED:
                        removeZone.setActivated(true);
                        return true;
                    case DragEvent.ACTION_DRAG_EXITED:
                        removeZone.setActivated(false);
                        return true;
                    case DragEvent.ACTION_DROP:
                        removeZone.setActivated(false);
                        Object state = event.getLocalState();
                        if (state instanceof DragInfo) {
                            DragInfo info = (DragInfo) state;
                            if (!info.isNew && info.sourcePage != null) {
                                removeIconFromWorkspace(info);
                            }
                        }
                        return true;
                    default:
                        return true;
                }
            }
        });
    }

    private void startIconDrag(View view, DragInfo dragInfo) {
        ClipData data = ClipData.newPlainText("icon", dragInfo.appInfo.serialize());
        View.DragShadowBuilder shadow = new View.DragShadowBuilder(view);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            view.startDragAndDrop(data, shadow, dragInfo, 0);
        } else {
            //noinspection deprecation
            view.startDrag(data, shadow, dragInfo, 0);
        }
    }

    private void placeIcon(CellLayout page, int cellX, int cellY, DragEvent event) {
        Object state = event.getLocalState();
        DragInfo dragInfo = (state instanceof DragInfo) ? (DragInfo) state : null;
        if (dragInfo == null) return;
        AppInfo appInfo = dragInfo.appInfo;

        if (!dragInfo.isNew && dragInfo.sourcePage != null) {
            View oldView = findIconView(dragInfo.sourcePage, dragInfo.sourceCellX, dragInfo.sourceCellY);
            if (oldView != null) dragInfo.sourcePage.removeViewFromCell(oldView);
        }

        addIconView(page, cellX, cellY, appInfo);
        persistLayout();
    }

    private void addIconView(final CellLayout page, int cellX, int cellY, final AppInfo appInfo) {
        BubbleTextView iconView = (BubbleTextView) LayoutInflater.from(this)
                .inflate(R.layout.item_app_icon, page, false);
        iconView.setText(appInfo.title);
        iconView.setCompoundDrawablesWithIntrinsicBounds(null, appInfo.icon, null, null);
        iconView.setTag(appInfo);
        iconView.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { launchApp(appInfo.componentName); }
        });
        iconView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                CellLayout.LayoutParams lp = (CellLayout.LayoutParams) v.getLayoutParams();
                DragInfo moveInfo = new DragInfo(appInfo, false);
                moveInfo.sourcePage = page;
                moveInfo.sourceCellX = lp.cellX;
                moveInfo.sourceCellY = lp.cellY;
                startIconDrag(v, moveInfo);
                return true;
            }
        });
        page.addViewToCell(iconView, cellX, cellY);
    }

    private View findIconView(CellLayout page, int cellX, int cellY) {
        for (int i = 0; i < page.getChildCount(); i++) {
            View child = page.getChildAt(i);
            CellLayout.LayoutParams lp = (CellLayout.LayoutParams) child.getLayoutParams();
            if (lp.cellX == cellX && lp.cellY == cellY) return child;
        }
        return null;
    }

    private void removeIconFromWorkspace(DragInfo info) {
        View view = findIconView(info.sourcePage, info.sourceCellX, info.sourceCellY);
        if (view != null) {
            info.sourcePage.removeViewFromCell(view);
            persistLayout();
        }
    }

    private void persistLayout() {
        Set<LauncherModel.Entry> entries = new HashSet<>();
        for (int p = 0; p < workspace.getPageCount(); p++) {
            CellLayout page = workspace.getPage(p);
            for (int i = 0; i < page.getChildCount(); i++) {
                View child = page.getChildAt(i);
                CellLayout.LayoutParams lp = (CellLayout.LayoutParams) child.getLayoutParams();
                AppInfo info = (AppInfo) child.getTag();
                if (info == null) continue;
                LauncherModel.Entry e = new LauncherModel.Entry();
                e.page = p;
                e.cellX = lp.cellX;
                e.cellY = lp.cellY;
                e.flattenedComponent = info.serialize();
                entries.add(e);
            }
        }
        LauncherModel.saveAll(this, entries);
    }

    private void loadSavedLayout() {
        Set<LauncherModel.Entry> entries = LauncherModel.load(this);
        PackageManager pm = getPackageManager();
        for (LauncherModel.Entry e : entries) {
            ComponentName cn = AppInfo.deserialize(e.flattenedComponent);
            if (cn == null) continue;
            try {
                ActivityInfo ai = pm.getActivityInfo(cn, 0);
                CharSequence label = ai.loadLabel(pm);
                Drawable icon = ai.loadIcon(pm);
                AppInfo info = new AppInfo(cn, label, icon);

                CellLayout page = workspace.getPage(e.page);
                if (page == null) continue;
                if (!page.isCellFree(e.cellX, e.cellY)) continue;

                addIconView(page, e.cellX, e.cellY, info);
            } catch (PackageManager.NameNotFoundException ignored) {
                // app was uninstalled since the last save; drop the stale entry
            }
        }
    }

    private void toggleAllApps() {
        if (allAppsContainer.getVisibility() == View.VISIBLE) {
            hideAllApps();
        } else {
            showAllApps();
        }
    }

    private void showAllApps() {
        allAppsContainer.setAlpha(0f);
        allAppsContainer.setTranslationY(60);
        allAppsContainer.setVisibility(View.VISIBLE);
        allAppsContainer.animate().alpha(1f).translationY(0).setDuration(220).start();
    }

    private void hideAllApps() {
        allAppsContainer.animate().alpha(0f).translationY(60).setDuration(180)
                .withEndAction(new Runnable() {
                    @Override public void run() { allAppsContainer.setVisibility(View.GONE); }
                }).start();
    }

    private void updatePageIndicator(int activePage) {
        for (int i = 0; i < pageIndicator.getChildCount(); i++) {
            ImageView dot = (ImageView) pageIndicator.getChildAt(i);
            dot.setImageResource(i == activePage ? R.drawable.dot_active : R.drawable.dot_inactive);
        }
    }

    private void launchApp(ComponentName componentName) {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setComponent(componentName);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Couldn't open that app", Toast.LENGTH_SHORT).show();
        }
    }

    private void launchGlobalSearch() {
        try {
            startActivity(new Intent(Intent.ACTION_WEB_SEARCH));
        } catch (Exception e) {
            Toast.makeText(this, "No search app found", Toast.LENGTH_SHORT).show();
        }
    }

    private void showAddDialog() {
        new AlertDialog.Builder(this, android.R.style.Theme_Holo_Dialog)
                .setTitle(R.string.add_to_home)
                .setItems(new CharSequence[]{getString(R.string.add_apps), getString(R.string.add_wallpaper)},
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == 0) {
                                    showAllApps();
                                } else {
                                    try {
                                        startActivity(Intent.createChooser(
                                                new Intent(Intent.ACTION_SET_WALLPAPER),
                                                getString(R.string.select_wallpaper)));
                                    } catch (Exception e) {
                                        Toast.makeText(LauncherActivity.this,
                                                "No wallpaper picker found", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        }).show();
    }
}
