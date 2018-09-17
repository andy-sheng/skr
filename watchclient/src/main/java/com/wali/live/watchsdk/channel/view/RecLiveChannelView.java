package com.wali.live.watchsdk.channel.view;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

import com.base.global.GlobalData;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.channel.data.ChannelDataStore;
import com.wali.live.watchsdk.channel.presenter.IChannelPresenter;
import com.wali.live.watchsdk.channel.presenter.RecChannelPresenter;
import com.wali.live.watchsdk.channel.viewmodel.ChannelPlaceHolderModel;
import com.wali.live.watchsdk.channel.viewmodel.ChannelViewModel;
import com.wali.live.watchsdk.eventbus.EventClass;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import static com.wali.live.watchsdk.channel.data.ChannelDataStore.GAME_WATCH_CHANNEL_FROM_PORTRAIT;

/**
 * Created by liuting on 18-9-12.
 * 游戏直播间更多直播频道列表
 */

public class RecLiveChannelView extends BaseLiveChannelView {
    private @ChannelDataStore.ReqFrom int mReqFrom = GAME_WATCH_CHANNEL_FROM_PORTRAIT;

    // 游戏直播间竖屏更多直播　向上滑动超过该值则可隐藏头顶的tabBar
    private static int SCROLL_DY = GlobalData.app().getResources().getDimensionPixelSize(R.dimen.view_dimen_208);

    public RecLiveChannelView(Context context) {
        super(context);
    }

    public RecLiveChannelView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void init(Context context) {
        super.init(context);
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            private int mTotalDy = 0;

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                mTotalDy += dy;
                if (mChannelId != -1) {
                    if (dy < 0 && (-dy) > 8) {
                        EventBus.getDefault().post(new EventClass.RecLiveChannelShowTabBarEvent(true, mChannelId));
                    } else if (dy > 0 && dy > 8 && mTotalDy >= SCROLL_DY) {
                        EventBus.getDefault().post(new EventClass.RecLiveChannelShowTabBarEvent(false, mChannelId));
                    }
                }
            }
        });
    }

    @Override
    protected IChannelPresenter initChannelPresenter() {
        return new RecChannelPresenter(getActivity(), this);
    }

    @Override
    public void updateView(List<ChannelViewModel> models, long channelId) {
        setChannelId(channelId); // 首页频道的channelId 是外部set进来的　直播间更多直播频道Id是拉取的数据返回
        if (mReqFrom == GAME_WATCH_CHANNEL_FROM_PORTRAIT) {
            ChannelViewModel channelViewModel = new ChannelPlaceHolderModel(getResources().getDimensionPixelSize(R.dimen.view_dimen_208));
            models.add(0, channelViewModel);
        }
        super.updateView(models, channelId);
    }

    public void setRequestParam(long viewerId, long anchorId, String packageName, long gameId,
                                @ChannelDataStore.RecType int recType, @ChannelDataStore.ReqFrom int reqFrom) {
        mReqFrom = reqFrom;
        ((RecChannelPresenter)mPresenter).setRequestParam(viewerId, anchorId, packageName, gameId, recType, reqFrom);
    }
}
