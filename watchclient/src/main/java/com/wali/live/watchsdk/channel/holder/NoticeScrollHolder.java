package com.wali.live.watchsdk.channel.holder;

import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.base.utils.date.DateTimeUtils;
import com.base.utils.display.DisplayUtils;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.channel.helper.HolderHelper;
import com.wali.live.watchsdk.channel.view.IScrollListener;
import com.wali.live.watchsdk.channel.view.RepeatScrollView;
import com.wali.live.watchsdk.channel.viewmodel.ChannelNoticeViewModel;
import com.wali.live.watchsdk.channel.viewmodel.ChannelNoticeViewModel.NoticeItem;

import java.util.List;

/**
 * Created by lan on 16/6/28.
 *
 * @module 频道
 */
public class NoticeScrollHolder extends FixedHolder {
    private static final int HEIGHT = DisplayUtils.dip2px(60f);

    private RepeatScrollView mRepeatScrollView;
    private ViewGroup mScrollView1;
    private ViewGroup mScrollView2;

    private TextView mTimeTv1;
    private TextView mTimeTv2;

    private TextView mSingleTv1;
    private TextView mSingleTv2;
    private ImageView mArrowIv;

    private List<ChannelNoticeViewModel.NoticeItem> mItems;

    public NoticeScrollHolder(View itemView) {
        super(itemView);
    }

    @Override
    protected void initContentView() {

        mRepeatScrollView = $(R.id.repeat_scroll_view);
        mRepeatScrollView.init(R.layout.michannel_single_notice_scroll_item, HEIGHT);

        mScrollView1 = mRepeatScrollView.getChildView(0);
        mScrollView2 = mRepeatScrollView.getChildView(1);

        mTimeTv1 = $(mScrollView1, R.id.time_tv);
        mTimeTv2 = $(mScrollView2, R.id.time_tv);

        mSingleTv1 = $(mScrollView1, R.id.title_tv);
        mSingleTv2 = $(mScrollView2, R.id.title_tv);

        mArrowIv = $(R.id.arrow_iv);
    }

    @Override
    protected boolean needTitleView() {
        return false;
    }

    @Override
    protected void bindView() {
        super.bindView();
        if (mViewModel instanceof ChannelNoticeViewModel) {
            ChannelNoticeViewModel viewModel = mViewModel.get();
            bindForecastViewModel(viewModel);
        }
    }

    protected void bindForecastViewModel(final ChannelNoticeViewModel viewModel) {
        mItems = viewModel.getItemDatas();
        if (mItems == null || mItems.size() == 0) {
            itemView.setOnClickListener(null);
            mRepeatScrollView.enterNullMode();
            return;
        }
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(viewModel.getAllViewUri())) {
                    HolderHelper.sendClickCommand(viewModel.getRecommendTag());
                    mJumpListener.jumpScheme(viewModel.getAllViewUri());
                } else {
                    MyLog.e(TAG, "bindForecastViewModel schemeUrl is empty");
                }
            }
        });

        if (TextUtils.isEmpty(viewModel.getAllViewUri())) {
            mArrowIv.setVisibility(View.GONE);
            itemView.setClickable(false);
        } else {
            mArrowIv.setVisibility(View.VISIBLE);
            itemView.setClickable(true);
        }

        mRepeatScrollView.setListener(new IScrollListener() {
            @Override
            public void onFirstIndexed() {
                bindFirstItem(mItems.get(0));
            }

            @Override
            public void onIndexChanged(int index) {
                bindFirstItem(mItems.get(index % mItems.size()));
                bindSecondItem(mItems.get((index + 1) % mItems.size()));
            }
        });
        if (mItems.size() == 1) {
            mRepeatScrollView.enterSingleMode();
            return;
        }
        mRepeatScrollView.enterScrollMode();
    }

    protected void bindFirstItem(NoticeItem item) {
        mTimeTv1.setText(DateTimeUtils.formatTimeStringForNotice(GlobalData.app(), item.getBeginTime()));
        mSingleTv1.setText(item.getTitle());
    }

    protected void bindSecondItem(NoticeItem item) {
        mTimeTv2.setText(DateTimeUtils.formatTimeStringForNotice(GlobalData.app(), item.getBeginTime()));
        mSingleTv2.setText(item.getTitle());
    }
}
