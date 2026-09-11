package com.lukeiscooldev.honeylauncher;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.List;

public class AppsAdapter extends BaseAdapter {

    private final Context context;
    private final List<AppInfo> apps;

    public AppsAdapter(Context context, List<AppInfo> apps) {
        this.context = context;
        this.apps = apps;
    }

    @Override
    public int getCount() {
        return apps.size();
    }

    @Override
    public AppInfo getItem(int position) {
        return apps.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        BubbleTextView view;
        if (convertView instanceof BubbleTextView) {
            view = (BubbleTextView) convertView;
        } else {
            view = (BubbleTextView) LayoutInflater.from(context)
                    .inflate(R.layout.item_app_icon, parent, false);
        }
        AppInfo info = apps.get(position);
        view.setText(info.title);
        view.setCompoundDrawablesWithIntrinsicBounds(null, info.icon, null, null);
        view.setTag(info);
        return view;
    }
}
