//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.imkit.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import io.rong.common.RLog;
import io.rong.imkit.R;

public class AutoRefreshListView extends ListView {
    private static final String TAG = "AutoRefreshListView";
    private io.rong.imkit.widget.AutoRefreshListView.OnRefreshListener refreshListener;
    private List<OnScrollListener> scrollListeners = new ArrayList();
    private io.rong.imkit.widget.AutoRefreshListView.State state;
    private io.rong.imkit.widget.AutoRefreshListView.Mode mode;
    private io.rong.imkit.widget.AutoRefreshListView.Mode currentMode;
    private boolean refreshableStart;
    private boolean refreshableEnd;
    private ViewGroup refreshHeader;
    private ViewGroup refreshFooter;
    private Iterator<OnScrollListener> iterator;
    private int offsetY;
    private boolean isBeingDragged;
    private int startY;

    public AutoRefreshListView(Context context) {
        super(context);
        this.state = io.rong.imkit.widget.AutoRefreshListView.State.RESET;
        this.mode = io.rong.imkit.widget.AutoRefreshListView.Mode.START;
        this.currentMode = io.rong.imkit.widget.AutoRefreshListView.Mode.START;
        this.refreshableStart = true;
        this.refreshableEnd = true;
        this.isBeingDragged = false;
        this.startY = 0;
        this.init(context);
    }

    public AutoRefreshListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.state = io.rong.imkit.widget.AutoRefreshListView.State.RESET;
        this.mode = io.rong.imkit.widget.AutoRefreshListView.Mode.START;
        this.currentMode = io.rong.imkit.widget.AutoRefreshListView.Mode.START;
        this.refreshableStart = true;
        this.refreshableEnd = true;
        this.isBeingDragged = false;
        this.startY = 0;
        this.init(context);
    }

    public AutoRefreshListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.state = io.rong.imkit.widget.AutoRefreshListView.State.RESET;
        this.mode = io.rong.imkit.widget.AutoRefreshListView.Mode.START;
        this.currentMode = io.rong.imkit.widget.AutoRefreshListView.Mode.START;
        this.refreshableStart = true;
        this.refreshableEnd = true;
        this.isBeingDragged = false;
        this.startY = 0;
        this.init(context);
    }

    public void setMode(io.rong.imkit.widget.AutoRefreshListView.Mode mode) {
        this.mode = mode;
    }

    public void setOnRefreshListener(io.rong.imkit.widget.AutoRefreshListView.OnRefreshListener refreshListener) {
        this.refreshListener = refreshListener;
    }

    public void setOnScrollListener(OnScrollListener l) {
        throw new UnsupportedOperationException("Use addOnScrollListener instead!");
    }

    public void addOnScrollListener(OnScrollListener l) {
        this.scrollListeners.add(l);
    }

    public void removeOnScrollListener(OnScrollListener l) {
        this.scrollListeners.remove(l);
    }

    public void removeCurrentOnScrollListener() {
        this.iterator.remove();
    }

    private void init(Context context) {
        this.addRefreshView(context);
        super.setOnScrollListener(new OnScrollListener() {
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                io.rong.imkit.widget.AutoRefreshListView.this.iterator = io.rong.imkit.widget.AutoRefreshListView.this.scrollListeners.iterator();

                while (io.rong.imkit.widget.AutoRefreshListView.this.iterator.hasNext()) {
                    OnScrollListener listener = (OnScrollListener) io.rong.imkit.widget.AutoRefreshListView.this.iterator.next();
                    listener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
                }

            }

            public void onScrollStateChanged(AbsListView view, int scrollState) {
                Iterator var3 = io.rong.imkit.widget.AutoRefreshListView.this.scrollListeners.iterator();

                while (var3.hasNext()) {
                    OnScrollListener listener = (OnScrollListener) var3.next();
                    listener.onScrollStateChanged(view, scrollState);
                }

            }
        });
        this.initRefreshListener();
        this.state = io.rong.imkit.widget.AutoRefreshListView.State.RESET;
    }

    private void addRefreshView(Context context) {
        this.refreshHeader = (ViewGroup) View.inflate(context, R.layout.rc_refresh_list_view, (ViewGroup) null);
        this.addHeaderView(this.refreshHeader, (Object) null, false);
        this.refreshFooter = (ViewGroup) View.inflate(context, R.layout.rc_refresh_list_view, (ViewGroup) null);
        this.addFooterView(this.refreshFooter, (Object) null, false);
    }

    private void initRefreshListener() {
        OnScrollListener listener = new OnScrollListener() {
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (scrollState == 0 && io.rong.imkit.widget.AutoRefreshListView.this.state == io.rong.imkit.widget.AutoRefreshListView.State.RESET) {
                    boolean reachTop = io.rong.imkit.widget.AutoRefreshListView.this.getFirstVisiblePosition() < io.rong.imkit.widget.AutoRefreshListView.this.getHeaderViewsCount() && io.rong.imkit.widget.AutoRefreshListView.this.getCount() > io.rong.imkit.widget.AutoRefreshListView.this.getHeaderViewsCount();
                    if (reachTop) {
                        io.rong.imkit.widget.AutoRefreshListView.this.onRefresh(true, false);
                    } else {
                        boolean reachBottom = io.rong.imkit.widget.AutoRefreshListView.this.getLastVisiblePosition() >= io.rong.imkit.widget.AutoRefreshListView.this.getCount() - 1;
                        if (reachBottom) {
                            io.rong.imkit.widget.AutoRefreshListView.this.onRefresh(false, true);
                        }
                    }
                }

            }

            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            }
        };
        this.addOnScrollListener(listener);
    }

    private void onRefresh(boolean start, boolean end) {
        if (this.refreshListener != null) {
            View firstVisibleChild = this.getChildAt(this.getHeaderViewsCount());
            if (firstVisibleChild != null) {
                this.offsetY = firstVisibleChild.getTop();
            }

            if (start && this.refreshableStart && this.mode != io.rong.imkit.widget.AutoRefreshListView.Mode.END) {
                this.currentMode = io.rong.imkit.widget.AutoRefreshListView.Mode.START;
                this.state = io.rong.imkit.widget.AutoRefreshListView.State.REFRESHING;
                this.refreshListener.onRefreshFromStart();
            } else if (end && this.refreshableEnd && this.mode != io.rong.imkit.widget.AutoRefreshListView.Mode.START) {
                this.currentMode = io.rong.imkit.widget.AutoRefreshListView.Mode.END;
                this.state = io.rong.imkit.widget.AutoRefreshListView.State.REFRESHING;
                this.refreshListener.onRefreshFromEnd();
            }

            this.updateRefreshView();
        }

    }

    private void updateRefreshView() {
        switch (this.state) {
            case REFRESHING:
                this.getRefreshView().getChildAt(0).setVisibility(0);
                break;
            case RESET:
                if (this.currentMode == io.rong.imkit.widget.AutoRefreshListView.Mode.START) {
                    this.refreshHeader.getChildAt(0).setVisibility(8);
                } else {
                    this.refreshFooter.getChildAt(0).setVisibility(8);
                }
        }

    }

    private ViewGroup getRefreshView() {
        switch (this.currentMode) {
            case END:
                return this.refreshFooter;
            case START:
            default:
                return this.refreshHeader;
        }
    }

    public void onRefreshStart(io.rong.imkit.widget.AutoRefreshListView.Mode mode) {
        this.state = io.rong.imkit.widget.AutoRefreshListView.State.REFRESHING;
        this.currentMode = mode;
    }

    public io.rong.imkit.widget.AutoRefreshListView.State getRefreshState() {
        return this.state;
    }

    public void onRefreshComplete(int count, int requestCount, boolean needOffset) {
        this.state = io.rong.imkit.widget.AutoRefreshListView.State.RESET;
        this.resetRefreshView(count, requestCount);
        if (needOffset) {
            if (this.currentMode == io.rong.imkit.widget.AutoRefreshListView.Mode.START) {
                this.setSelectionFromTop(count + this.getHeaderViewsCount(), this.refreshableStart ? this.offsetY : 0);
            }

        }
    }

    public void onRefreshComplete() {
        this.state = io.rong.imkit.widget.AutoRefreshListView.State.RESET;
        this.updateRefreshView();
    }

    private void resetRefreshView(int count, int requestCount) {
        if (this.currentMode == io.rong.imkit.widget.AutoRefreshListView.Mode.START) {
            if (this.getCount() == count + this.getHeaderViewsCount() + this.getFooterViewsCount()) {
                this.refreshableStart = count == requestCount;
            } else {
                this.refreshableStart = count > 0;
            }
        } else {
            this.refreshableEnd = count > 0;
        }

        this.updateRefreshView();
    }

    public boolean onTouchEvent(MotionEvent event) {
        try {
            return this.onTouchEventInternal(event);
        } catch (Exception var3) {
            var3.printStackTrace();
            return false;
        }
    }

    private boolean onTouchEventInternal(MotionEvent event) {
        switch (event.getAction()) {
            case 0:
                this.onTouchBegin(event);
                break;
            case 1:
            case 3:
                this.onTouchEnd();
                break;
            case 2:
                this.onTouchMove(event);
        }

        try {
            return super.onTouchEvent(event);
        } catch (Exception var3) {
            RLog.e("AutoRefreshListView", "onTouchEventInternal catch", var3);
            return false;
        }
    }

    private void onTouchBegin(MotionEvent event) {
        int firstItemIndex = this.getFirstVisiblePosition();
        if (!this.refreshableStart && firstItemIndex <= this.getHeaderViewsCount() && !this.isBeingDragged) {
            this.isBeingDragged = true;
            this.startY = (int) event.getY();
        }

    }

    private void onTouchMove(MotionEvent event) {
        this.onTouchBegin(event);
        if (this.isBeingDragged) {
            int offsetY = (int) (event.getY() - (float) this.startY);
            offsetY = Math.max(offsetY, 0) / 2;
            this.refreshHeader.setPadding(0, offsetY, 0, 0);
        }
    }

    private void onTouchEnd() {
        if (this.isBeingDragged) {
            this.refreshHeader.setPadding(0, 0, 0, 0);
        }

        this.isBeingDragged = false;
    }

    public interface OnRefreshListener {
        void onRefreshFromStart();

        void onRefreshFromEnd();
    }

    public static enum Mode {
        START,
        END,
        BOTH;

        private Mode() {
        }
    }

    public static enum State {
        REFRESHING,
        RESET;

        private State() {
        }
    }
}
