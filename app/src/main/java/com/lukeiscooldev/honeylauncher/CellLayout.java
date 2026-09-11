package com.lukeiscooldev.honeylauncher;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

/**
 * One page of the workspace: a fixed grid that positions children by
 * cellX/cellY instead of the usual flow layout rules.
 */
public class CellLayout extends ViewGroup {

    public static final int COLUMNS = 4;
    public static final int ROWS = 4;

    private int cellWidth;
    private int cellHeight;
    private final boolean[][] occupied = new boolean[COLUMNS][ROWS];

    public CellLayout(Context context) {
        super(context);
    }

    public CellLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public static class LayoutParams extends ViewGroup.LayoutParams {
        public int cellX;
        public int cellY;

        public LayoutParams(int cellX, int cellY) {
            super(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            this.cellX = cellX;
            this.cellY = cellY;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        cellWidth = width / COLUMNS;
        cellHeight = height / ROWS;

        int childWidthSpec = MeasureSpec.makeMeasureSpec(cellWidth, MeasureSpec.AT_MOST);
        int childHeightSpec = MeasureSpec.makeMeasureSpec(cellHeight, MeasureSpec.AT_MOST);

        for (int i = 0; i < getChildCount(); i++) {
            getChildAt(i).measure(childWidthSpec, childHeightSpec);
        }
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            LayoutParams lp = (LayoutParams) child.getLayoutParams();
            int cl = lp.cellX * cellWidth;
            int ct = lp.cellY * cellHeight;
            child.layout(cl, ct, cl + cellWidth, ct + cellHeight);
        }
    }

    public boolean isCellFree(int cellX, int cellY) {
        if (cellX < 0 || cellY < 0 || cellX >= COLUMNS || cellY >= ROWS) return false;
        return !occupied[cellX][cellY];
    }

    public void markOccupied(int cellX, int cellY, boolean value) {
        if (cellX >= 0 && cellY >= 0 && cellX < COLUMNS && cellY < ROWS) {
            occupied[cellX][cellY] = value;
        }
    }

    public void addViewToCell(View view, int cellX, int cellY) {
        LayoutParams lp = new LayoutParams(cellX, cellY);
        addView(view, lp);
        markOccupied(cellX, cellY, true);
    }

    public void removeViewFromCell(View view) {
        LayoutParams lp = (LayoutParams) view.getLayoutParams();
        if (lp != null) {
            markOccupied(lp.cellX, lp.cellY, false);
        }
        removeView(view);
    }

    public int[] pointToCell(float x, float y) {
        if (cellWidth == 0 || cellHeight == 0) return new int[]{-1, -1};
        int cx = (int) (x / cellWidth);
        int cy = (int) (y / cellHeight);
        return new int[]{cx, cy};
    }
}
