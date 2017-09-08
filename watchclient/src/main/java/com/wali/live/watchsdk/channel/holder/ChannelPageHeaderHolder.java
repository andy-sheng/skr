package com.wali.live.watchsdk.channel.holder;

import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

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
    public static final int VIDEO_VIEW_HEIGHT = (DisplayUtils.getScreenWidth() - DisplayUtils.dip2px(50)) * 9 / 16;

    private int[] mBtnLayoutIds;
    protected HeaderVideoView mVideoView;
    protected BaseImageView mHeaderIv;
    protected LinearLayout mNavigateContainer;
    private BaseImageView mShadowIv;
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
        mShadowIv = $(R.id.shadow_iv);
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
    }

    @Override
    protected void bindPageHeaderModel(final ChannelPageHeaderViewModel viewModel) {
        super.bindPageHeaderModel(viewModel);
        if (TextUtils.isEmpty(viewModel.getVideoCoverUrl())
                || TextUtils.isEmpty(viewModel.getVideoUrl())) {
            mVideoView.setVisibility(View.GONE);
            mShadowIv.setVisibility(View.GONE);
        } else {
            ViewGroup.LayoutParams playViewParams = mVideoView.getLayoutParams();
            playViewParams.height = VIDEO_VIEW_HEIGHT;
            mVideoView.setLayoutParams(playViewParams);
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

        FrescoWorker.loadImage(mHeaderIv, ImageFactory.newHttpImage(viewModel.getCoverUrl())
                .setCallBack(mFrescoCallBack)
                .setWidth(DisplayUtils.getScreenWidth())
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
                final int pos = i;
                IFrescoCallBack iFrescoCallBack = new IFrescoCallBack() {
                    @Override
                    public void processWithInfo(ImageInfo info) {
                        MyLog.d(TAG, "image info width: " + info.getWidth() + " height: " + info.getHeight());
                        ViewGroup.LayoutParams params = mBtnIvs[pos].getLayoutParams();
                        params.width = info.getWidth();//DisplayUtils.dip2px(info.getWidth() / 3);
                        params.height = info.getHeight();//DisplayUtils.dip2px(info.getHeight() / 3);
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
