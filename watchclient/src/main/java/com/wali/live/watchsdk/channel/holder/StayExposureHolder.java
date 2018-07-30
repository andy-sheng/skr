package com.wali.live.watchsdk.channel.holder;

import android.os.Handler;
import android.os.Looper;
import android.view.View;

import com.base.log.MyLog;
import com.wali.live.watchsdk.channel.helper.HolderHelper;
import com.wali.live.watchsdk.channel.viewmodel.BaseJumpItem;
import com.wali.live.watchsdk.channel.viewmodel.ChannelBannerViewModel;
import com.wali.live.watchsdk.channel.viewmodel.ChannelLiveGroupViewModel;
import com.wali.live.watchsdk.channel.viewmodel.ChannelLiveViewModel;
import com.wali.live.watchsdk.channel.viewmodel.ChannelNavigateViewModel;
import com.wali.live.watchsdk.channel.viewmodel.ChannelPageHeaderViewModel;
import com.wali.live.watchsdk.channel.viewmodel.ChannelRankingViewModel;
import com.wali.live.watchsdk.channel.viewmodel.ChannelShowViewModel;
import com.wali.live.watchsdk.channel.viewmodel.ChannelTwoTextViewModel;
import com.wali.live.watchsdk.channel.viewmodel.ChannelUserViewModel;
import com.wali.live.watchsdk.channel.viewmodel.ChannelViewModel;
import com.wali.live.watchsdk.eventbus.EventClass;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;


/**
 * Created by liuting on 18-7-25.
 *
 * 停留超过1s的曝光打点基类
 */

public abstract class StayExposureHolder extends BaseHolder<ChannelViewModel> {
    private final static long STAY_DEFAUT_TIME = 1000;

    private Handler mUiHandler;
    private Runnable mStayExposureRunnable;

    public StayExposureHolder(View itemView) {
        super(itemView);
    }

    @Override
    protected void bindView() {
        MyLog.d(TAG, "bindView");
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        sendPostIfNeed();
    }

    public void onHolderDetached() {
        MyLog.d(TAG, "onHolderDetached");

        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }

        if (mUiHandler != null && mStayExposureRunnable != null) {
            MyLog.d(TAG, "removeCallbacks");
            mUiHandler.removeCallbacks(mStayExposureRunnable);
        }
    }


    private void sendPostIfNeed() {
        MyLog.d(TAG, "sendPostIfNeed");
        EventClass.SelectChannelEvent event = EventBus.getDefault().getStickyEvent(EventClass.SelectChannelEvent.class);
        sendPostIfNeed(event);
    }

    private void sendPostIfNeed(EventClass.SelectChannelEvent event) {
        if (event == null || mViewModel == null) {
            return;
        }

        MyLog.d(TAG, "sendPostIfNeed  holderchannelId=" + mViewModel.getChannelId() +"  eventchannelId=" + event.channelId);

        if (event.channelId == mViewModel.getChannelId()) {
            if (mUiHandler == null) {
                mUiHandler = new Handler(Looper.getMainLooper());
            }
            if (mStayExposureRunnable == null) {
                mStayExposureRunnable = createStayExposureRunnable();
            }

            MyLog.d(TAG, "postDelay stayExposure runnable");
            mUiHandler.postDelayed(mStayExposureRunnable, STAY_DEFAUT_TIME);
        }
    }


    private Runnable createStayExposureRunnable() {
        return new Runnable() {
            @Override
            public void run() {
                if (mViewModel != null) {
                    MyLog.d(TAG, "ready to send exposure tag");
                    // 打点
                    if (mViewModel instanceof ChannelShowViewModel) {
                        MyLog.d(TAG, "instanceof ChannelShowViewModel");
                        ChannelShowViewModel viewModel = (ChannelShowViewModel) mViewModel;
                        sendStayExposureItemData(viewModel.getItemDatas());

                    } else if (mViewModel instanceof ChannelTwoTextViewModel) {
                        MyLog.d(TAG, "instanceof ChannelTwoTextViewModel");
                        ChannelTwoTextViewModel viewModel = (ChannelTwoTextViewModel) mViewModel;
                        sendStayExposureItemData(viewModel.getItemDatas());

                    } else if (mViewModel instanceof ChannelUserViewModel) {
                        MyLog.d(TAG, "instanceof ChannelUserViewModel");
                        ChannelUserViewModel viewModel = (ChannelUserViewModel) mViewModel;
                        sendStayExposureItemData(viewModel.getItemDatas());

                    } else if (mViewModel instanceof ChannelLiveViewModel) {
                        MyLog.d(TAG, "instanceof ChannelLiveViewModel");
                        ChannelLiveViewModel viewModel = (ChannelLiveViewModel) mViewModel;
                        sendStayExposureItemData(viewModel.getItemDatas());

                    } else if (mViewModel instanceof ChannelNavigateViewModel) {
                        MyLog.d(TAG, "instanceof ChannelNavigateViewModel");
                        ChannelNavigateViewModel viewModel = ((ChannelNavigateViewModel) mViewModel);
                        sendStayExposureItemData(viewModel.getItemDatas());

                    } else if (mViewModel instanceof ChannelRankingViewModel) {
                        MyLog.d(TAG, "instanceof ChannelRankingViewModel");
                        ChannelRankingViewModel viewModel = (ChannelRankingViewModel) mViewModel;
                        sendStayExposureItemData(viewModel.getItemDatas());

                    } else if(mViewModel instanceof ChannelLiveGroupViewModel){
                        MyLog.d(TAG, "instanceof ChannelLiveGroupViewModel");
                        ChannelLiveGroupViewModel viewModel = (ChannelLiveGroupViewModel) mViewModel;
                        sendStayExposureItemData(viewModel.getItemDatas());

                    }else if (mViewModel instanceof ChannelBannerViewModel) {
                        MyLog.d(TAG, "instanceof ChannelBannerViewModel");
                        ChannelBannerViewModel viewModel = (ChannelBannerViewModel) mViewModel;
                        sendStayExposureItemData(viewModel.getItemDatas());
                    }
                }
            }
        };
    }

    /**
     * 发送曝光数据
     * @param list
     */
    private void sendStayExposureItemData(List<? extends BaseJumpItem> list) {
        if (list != null && !list.isEmpty()) {
            for (BaseJumpItem item : list) {
                MyLog.d(TAG,"item recommend= " + item.getRecommendTag());
                HolderHelper.sendStayExposureCommand(item);
            }
        } else {
            MyLog.d(TAG,"item list is empty");
        }
    }

    /**
     * 切换频道消息
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSelectChannelEvent(EventClass.SelectChannelEvent event) {
        MyLog.d(TAG, "onSelectChannelEvent");
        if (event != null) {
            if (mUiHandler != null && mStayExposureRunnable != null) {
                MyLog.d(TAG, "removeCallbacks");
                mUiHandler.removeCallbacks(mStayExposureRunnable);
            }
            sendPostIfNeed(event);
        }
    }
}
