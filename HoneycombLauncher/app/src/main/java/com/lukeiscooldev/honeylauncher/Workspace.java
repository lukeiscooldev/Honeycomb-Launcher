package com.lukeiscooldev.honeylauncher;

import android.content.Context;
import android.util.AttributeSet;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Scroller;

/**
 * Horizontally paged container of CellLayout pages, roughly matching the
 * old Launcher2 "Workspace" behaviour: fling/scroll paging with snapping,
 * plus drag routing to whichever page is currently visible.
 */
public class Workspace extends ViewGroup {

    public interface PageChangeListener {
        void onPageChanged(int page);
    }

    public interface WorkspaceDropListener {
        void onIconDropped(CellLayout page, int cellX, int cellY, DragEvent event, boolean isNewFromDrawer);
    }

    private final Scroller scroller;
    private int currentPage = 0;
    private float lastTouchX;
    private VelocityTracker velocityTracker;
    private boolean isDragging;
    private PageChangeListener pageChangeListener;
    private WorkspaceDropListener dropListener;

    public Workspace(Context context, AttributeSet attrs) {
        super(context, attrs);
        scroller = new Scroller(context);
    }

    public void setPageChangeListener(PageChangeListener listener) {
        this.pageChangeListener = listener;
    }

    public void setDropListener(WorkspaceDropListener listener) {
        this.dropListener = listener;
    }

    public CellLayout getPage(int index) {
        if (index < 0 || index >= getChildCount()) return null;
        return (CellLayout) getChildAt(index);
    }

    public int getPageCount() {
        return getChildCount();
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void snapToPage(int page) {
        page = Math.max(0, Math.min(page, getChildCount() - 1));
        currentPage = page;
        int targetX = page * getWidth();
        scroller.startScroll(getScrollX(), 0, targetX - getScrollX(), 0, 400);
        postInvalidate();
        if (pageChangeListener != null) pageChangeListener.onPageChanged(currentPage);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        for (int i = 0; i < getChildCount(); i++) {
            getChildAt(i).measure(
                    MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
        }
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int width = r - l;
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            child.layout(i * width, 0, (i + 1) * width, b - t);
        }
        scrollTo(currentPage * width, 0);
    }

    @Override
    public void computeScroll() {
        if (scroller.computeScrollOffset()) {
            scrollTo(scroller.getCurrX(), scroller.getCurrY());
            postInvalidate();
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lastTouchX = ev.getX();
                isDragging = false;
                break;
            case MotionEvent.ACTION_MOVE:
                float dx = ev.getX() - lastTouchX;
                if (Math.abs(dx) > 24) {
                    isDragging = true;
                }
                break;
        }
        return isDragging;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (velocityTracker == null) velocityTracker = VelocityTracker.obtain();
        velocityTracker.addMovement(ev);

        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lastTouchX = ev.getX();
                if (!scroller.isFinished()) scroller.abortAnimation();
                break;
            case MotionEvent.ACTION_MOVE:
                float dx = lastTouchX - ev.getX();
                lastTouchX = ev.getX();
                scrollBy((int) dx, 0);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                velocityTracker.computeCurrentVelocity(1000);
                float velocity = velocityTracker.getXVelocity();
                int width = getWidth();
                int page;
                if (velocity < -600 && currentPage < getChildCount() - 1) {
                    page = currentPage + 1;
                } else if (velocity > 600 && currentPage > 0) {
                    page = currentPage - 1;
                } else {
                    page = width == 0 ? currentPage : Math.round(getScrollX() / (float) width);
                }
                snapToPage(page);
                velocityTracker.recycle();
                velocityTracker = null;
                isDragging = false;
                break;
        }
        return true;
    }

    /**
     * Routes a DragEvent (already relative to this view) to the currently
     * visible page. Because the visible page always occupies exactly this
     * view's on-screen bounds while scrolled into place, no extra offset
     * math is needed for the coordinates.
     */
    public boolean handleDragEvent(DragEvent event) {
        CellLayout page = getPage(currentPage);
        if (page == null) return false;

        switch (event.getAction()) {
            case DragEvent.ACTION_DROP:
                int[] cell = page.pointToCell(event.getX(), event.getY());
                if (page.isCellFree(cell[0], cell[1])) {
                    Object localState = event.getLocalState();
                    boolean isNew = !(localState instanceof DragInfo) || ((DragInfo) localState).isNew;
                    if (dropListener != null) {
                        dropListener.onIconDropped(page, cell[0], cell[1], event, isNew);
                    }
                    return true;
                }
                return false;
            default:
                return true;
        }
    }
}
