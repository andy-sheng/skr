package com.wali.live.modulechannel.util.video;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import com.common.log.MyLog;
import com.common.mvp.Presenter;
import com.common.utils.NetworkUtils;
import com.common.utils.U;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhaomin on 17-9-18.
 */

public class VideoVisibilityCalculatorControl implements Presenter, ScrollDirectionHelper.OnDetectScrollListener {
    private static final String TAG = "VideoVisibilityCalculatorControl";

    private static final int VIDEO_PLAY_VISIBLE_PERCENT = 70;

    private RecyclerView mRecyclerView;

    private IVideoHolder mCurrentPlayingVideo;

    private ScrollDirectionHelper.ScrollDirection mScrollDirection = null;

    private boolean mSelected;

    private boolean mIsActivityForeGround;

    private List<IVideoHolder> mAllVideoHolder;

    public VideoVisibilityCalculatorControl(RecyclerView mRecyclerView) {
        this.mRecyclerView = mRecyclerView;
        mAllVideoHolder = new ArrayList<>();
        EventBus.getDefault().register(this);
    }

    public void onScroll(int firstVisibleItem, int lastVisbleItem) {
        for (int i = firstVisibleItem; i <= lastVisbleItem; i++) {
            RecyclerView.ViewHolder viewHolder = mRecyclerView.findViewHolderForAdapterPosition(i);
            if (viewHolder instanceof IVideoHolder) {
                if (((IVideoHolder) viewHolder).getVisibleVideoPercent() < VIDEO_PLAY_VISIBLE_PERCENT) {
                    ((IVideoHolder) viewHolder).pause();
                    MyLog.d(TAG, " onscroll firstItem is OneVideoItemHolder pause: " + ((IVideoHolder) viewHolder).getVisibleVideoPercent());
                }
            }
        }
    }

    public int onScrollStateIdle(int firstVisibleItem, int lastVisbleItem) {
        MyLog.d(TAG, " onScrollStateIdle firstItem : " + firstVisibleItem + " last: " + lastVisbleItem + " direction: " + (mScrollDirection != null ? mScrollDirection : " null"));
        if (!U.getNetworkUtils().isWifi()) {
            MyLog.w(TAG, " onScrollStateIdle NOT WIFI STOP");
            return 0;
        }
        //to avoid that the last or the first one never play when two items are both 100% on screen.
        if (!mRecyclerView.canScrollVertically(1)) {
            RecyclerView.ViewHolder holder = mRecyclerView.findViewHolderForAdapterPosition(lastVisbleItem);
            if (holder instanceof IVideoHolder && checkPlayVideo((IVideoHolder) holder)) {
                return lastVisbleItem;
            }
        } else if (!mRecyclerView.canScrollVertically(-1)) {
            RecyclerView.ViewHolder holder = mRecyclerView.findViewHolderForAdapterPosition(firstVisibleItem);
            if (holder instanceof IVideoHolder && checkPlayVideo((IVideoHolder) holder)) {
                return firstVisibleItem;
            }
        }

        if (mCurrentPlayingVideo != null && mCurrentPlayingVideo.isPlaying()) {
            int percent = (mCurrentPlayingVideo).getVisibleVideoPercent();
            MyLog.d(TAG, " onScrollStateIdle current isPlaying " + percent + " pos: " + mCurrentPlayingVideo.getPostion());
            if (mCurrentPlayingVideo.getPostion() <= lastVisbleItem && mCurrentPlayingVideo.getPostion() >= firstVisibleItem && (percent >= VIDEO_PLAY_VISIBLE_PERCENT)) {
                return 0;
            }
        }

        if (mScrollDirection == ScrollDirectionHelper.ScrollDirection.UP) {
            for (int i = lastVisbleItem; i >= firstVisibleItem; i--) {
                RecyclerView.ViewHolder holder = mRecyclerView.findViewHolderForAdapterPosition(i);
                if (holder instanceof IVideoHolder && checkPlayVideo((IVideoHolder) holder)) {
                    MyLog.d(TAG, " onScrollStateIdle holder visible enough  index: " + i);
                    return i;
                }
            }
        } else {
            for (int i = firstVisibleItem; i <= lastVisbleItem; i++) {
                RecyclerView.ViewHolder holder = mRecyclerView.findViewHolderForAdapterPosition(i);
                if (holder instanceof IVideoHolder && checkPlayVideo((IVideoHolder) holder)) {
                    MyLog.d(TAG, " onScrollStateIdle holder visible enough  index: " + i);
                    return i;
                }
            }
        }
        return 0;
    }

    private boolean checkPlayVideo(IVideoHolder holder) {
        if (((IVideoHolder) holder).getVisibleVideoPercent() >= VIDEO_PLAY_VISIBLE_PERCENT) {
            if (mCurrentPlayingVideo != null && mCurrentPlayingVideo != holder && mCurrentPlayingVideo.isPlaying()) {
                mCurrentPlayingVideo.pause();
            }

            if (mIsActivityForeGround
                    && mSelected) {
                ((IVideoHolder) holder).play();
                mCurrentPlayingVideo = (IVideoHolder) holder;
                if (!mAllVideoHolder.contains(holder)) {
                    mAllVideoHolder.add(holder);
                }
            } else {
                MyLog.d(TAG, " onScrollStateIdle checkPlayVideo isActivityResume" + mIsActivityForeGround + ", selected " + mSelected);
            }
            return true;
        }
        return false;
    }

    public void pauseAllVideo() {
        MyLog.d(TAG, " pauseAllVideo childCount : " + mRecyclerView.getChildCount() + " cache size: " + mAllVideoHolder.size());
        for (int i = 0; i < mRecyclerView.getChildCount(); i++) {
            View view = mRecyclerView.getChildAt(i);
            RecyclerView.ViewHolder viewHolder = mRecyclerView.getChildViewHolder(view);
            if (viewHolder instanceof IVideoHolder) {
                ((IVideoHolder) viewHolder).pause();
            }
        }
        for (IVideoHolder holder : mAllVideoHolder) {
            holder.pause();
        }
    }

    private void wifiTo4g() {
        MyLog.w(TAG, " wifiTo4g ");
        if (mCurrentPlayingVideo != null && mCurrentPlayingVideo.isPlaying()) {
            MyLog.w(TAG, " wifiTo4g pause");
            mCurrentPlayingVideo.pause();
        }
    }

    public void setSelected(boolean selected) {
        this.mSelected = selected;
    }

    private void play() {
        MyLog.w(TAG, " play isActivityResume " + mIsActivityForeGround + " selected: " + mSelected);
        if (mCurrentPlayingVideo != null
                && !mCurrentPlayingVideo.isPlaying()
                && mIsActivityForeGround
                && mSelected) {
            mCurrentPlayingVideo.play();
        }
    }

    @Override
    public void addToLifeCycle() {

    }

    @Override
    public void start() {

    }

    @Override
    public void resume() {
        MyLog.d(TAG, "resume " + mSelected);
        mIsActivityForeGround = true;
        if (mCurrentPlayingVideo != null
                && !mCurrentPlayingVideo.isPlaying()
                && mSelected) {
            mRecyclerView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mCurrentPlayingVideo.onResume();
                }
            }, 200);
        }
    }

    @Override
    public void pause() {
        MyLog.d(TAG, "pause");
        mIsActivityForeGround = false;
        if (mCurrentPlayingVideo != null) {
            mCurrentPlayingVideo.onPause();
        }
    }

    @Override
    public void stop() {

    }

    @Override
    public void destroy() {
        MyLog.w(TAG, "destroy: " + mRecyclerView.getChildCount() + " cache size: " + mAllVideoHolder.size());
        EventBus.getDefault().unregister(this);
        for (int i = 0; i < mRecyclerView.getChildCount(); i++) {
            View view = mRecyclerView.getChildAt(i);
            RecyclerView.ViewHolder viewHolder = mRecyclerView.getChildViewHolder(view);
            if (viewHolder instanceof IVideoHolder) {
                ((IVideoHolder) viewHolder).destroy();
            }
        }
        for (IVideoHolder holder : mAllVideoHolder) {
            holder.pause();
        }
        mAllVideoHolder.clear();
    }

    //网络变化toast提示
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(NetworkUtils.NetworkChangeEvent event) {
        if (null != event) {

            if (U.getNetworkUtils().hasNetwork()) {
                if (U.getNetworkUtils().is2G()
                        || U.getNetworkUtils().is3G()
                        || U.getNetworkUtils().is4G()) {
                    wifiTo4g();
                } else if (U.getNetworkUtils().isWifi()) {
                    play();
                }
            }
        }
    }


    @Override
    public void onScrollDirectionChanged(ScrollDirectionHelper.ScrollDirection scrollDirection) {
        mScrollDirection = scrollDirection;
    }
}
