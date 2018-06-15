package com.wali.live.watchsdk.channel.holder;

import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.base.image.fresco.BaseImageView;
import com.base.image.fresco.FrescoWorker;
import com.base.image.fresco.IFrescoCallBack;
import com.base.image.fresco.image.ImageFactory;
import com.base.log.MyLog;
import com.base.utils.display.DisplayUtils;
import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.imagepipeline.image.ImageInfo;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.channel.view.HeaderVideoView;
import com.wali.live.watchsdk.channel.viewmodel.ChannelNavigateViewModel.NavigateItem;
import com.wali.live.watchsdk.channel.viewmodel.ChannelPageHeaderViewModel;

import java.util.List;

/**
 * Created by zyh on 2017/8/28.
 *
 * @module 世姐header的holder
 */
public class ChannelPageHeaderHolder extends FixedHolder {
    //视频view高度
    private static final int VIDEO_VIEW_WIDTH = DisplayUtils.getScreenWidth() * 926 / 1080;
    private static final int VIDEO_VIEW_HEIGHT = VIDEO_VIEW_WIDTH * 9 / 16;
    private static final int VIDEO_VIEW_BOTTOM_MARGIN = DisplayUtils.getScreenWidth() * 70 / 1080;
    private static final int BTN_HEIGHT = DisplayUtils.getScreenWidth() * 260 / 1080;
    private static final int BTN_MARGIN = DisplayUtils.getScreenWidth() * 48 / 1080;

    private static float mBgRatio = 1280 / 1080f;
    private int[] mBtnLayoutIds;
    protected HeaderVideoView mVideoView;
    protected BaseImageView mHeaderIv;
    protected LinearLayout mNavigateContainer;
    private BaseImageView[] mBtnIvs;
    private ViewGroup[] mParentLayout;

    IFrescoCallBack mFrescoCallBack = new IFrescoCallBack() {
        @Override
        public void processWithInfo(ImageInfo info) {
            MyLog.d(TAG, "image info width: " + info.getWidth() + " height: " + info.getHeight());
            //保证图片宽高比
            ViewGroup.LayoutParams params = mHeaderIv.getLayoutParams();
            params.width = DisplayUtils.getScreenWidth();
            params.height = info.getHeight() * DisplayUtils.getScreenWidth() / info.getWidth();
            mHeaderIv.setLayoutParams(params);
        }

        @Override
        public void processWithFailure() {
            MyLog.d(TAG, "process fail");
        }

        @Override
        public void process(Object object) {

        }
    };

    public ChannelPageHeaderHolder(View itemView) {
        super(itemView);
    }

    @Override
    protected void initContentView() {
        mBtnLayoutIds = new int[]{
                R.id.button_layout1,
                R.id.button_layout2,
                R.id.button_layout3,
                R.id.button_layout4,
                R.id.button_layout5
        };
        mVideoView = $(R.id.video_view);
        mHeaderIv = $(R.id.header_bg_iv);
        mNavigateContainer = $(R.id.navigation_header_container);

        int size = mBtnLayoutIds.length;
        mParentLayout = new ViewGroup[size];
        for (int i = 0; i < size; i++) {
            mParentLayout[i] = $(mBtnLayoutIds[i]);
        }

        mBtnIvs = new BaseImageView[size];
        for (int i = 0; i < size; i++) {
            mBtnIvs[i] = $(mParentLayout[i], R.id.btn_iv);
        }
        adjustBtnContainer();
        adjustVideoView();
    }

    private void adjustVideoView() {
        ViewGroup.MarginLayoutParams playViewParams = (ViewGroup.MarginLayoutParams) mVideoView.getLayoutParams();
        playViewParams.height = VIDEO_VIEW_HEIGHT;
        playViewParams.width = VIDEO_VIEW_WIDTH;
        playViewParams.bottomMargin = VIDEO_VIEW_BOTTOM_MARGIN;
    }

    private void adjustBtnContainer() {
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mNavigateContainer.getLayoutParams();
        lp.leftMargin = BTN_MARGIN;
        lp.rightMargin = BTN_MARGIN;
        lp.height = BTN_HEIGHT;
    }

    @Override
    protected void bindPageHeaderModel(final ChannelPageHeaderViewModel viewModel) {
        super.bindPageHeaderModel(viewModel);
        if (TextUtils.isEmpty(viewModel.getVideoCoverUrl())) {
            mVideoView.setVisibility(View.GONE);
        } else {
            mVideoView.setVisibility(View.VISIBLE);
            mVideoView.setData(viewModel.getVideoUrl(), viewModel.getVideoCoverUrl());
            mVideoView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!TextUtils.isEmpty(viewModel.getVideoSchemeUri())) {
                        mJumpListener.jumpScheme(viewModel.getVideoSchemeUri());
                    }
                }
            });
        }
        mBgRatio = viewModel.getRatio();
        FrescoWorker.loadImage(mHeaderIv, ImageFactory.newHttpImage(viewModel.getCoverUrl())
                .setCallBack(mFrescoCallBack)
                .setWidth(DisplayUtils.getScreenWidth())
                .setHeight((int) (DisplayUtils.getScreenWidth() * mBgRatio))
                .setScaleType(ScalingUtils.ScaleType.CENTER_CROP)
                .build());
        mHeaderIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mJumpListener.jumpScheme(viewModel.getCoverSchemeUri());
            }
        });

        List<NavigateItem> list = viewModel.getItemDatas();
        if (list == null || list.size() <= 0) {
            mNavigateContainer.setVisibility(View.GONE);
        } else {
            mNavigateContainer.setVisibility(View.VISIBLE);
            for (int i = 0; i < list.size(); i++) {
                final NavigateItem item = list.get(i);
                if (item == null || TextUtils.isEmpty(item.getImgUrl())) {
                    continue;
                }
                exposureItem(item);

                final int pos = i;
                IFrescoCallBack iFrescoCallBack = new IFrescoCallBack() {
                    @Override
                    public void processWithInfo(ImageInfo info) {
                        MyLog.d(TAG, "image info width: " + info.getWidth() + " height: " + info.getHeight());
                        ViewGroup.LayoutParams params = mBtnIvs[pos].getLayoutParams();
                        params.width = info.getWidth();
                        params.height = info.getHeight();
                        mBtnIvs[pos].setLayoutParams(params);
                    }

                    @Override
                    public void processWithFailure() {
                        MyLog.d(TAG, "process fail");
                    }

                    @Override
                    public void process(Object object) {

                    }
                };
                FrescoWorker.loadImage(mBtnIvs[i], ImageFactory.newHttpImage(item.getImgUrl())
                        .setHeight(BTN_HEIGHT)
                        .setCallBack(iFrescoCallBack)
                        .setScaleType(ScalingUtils.ScaleType.CENTER_CROP)
                        .build());
                mBtnIvs[i].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        jumpItem(item);
                    }
                });
                mParentLayout[i].setVisibility(View.VISIBLE);
            }
        }
    }
}
