package com.wali.live.watchsdk.channel.holder;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.base.image.fresco.BaseImageView;
import com.base.log.MyLog;
import com.base.utils.display.DisplayUtils;
import com.facebook.drawee.drawable.ScalingUtils;
import com.wali.live.proto.CommonChannelProto;
import com.wali.live.utils.AvatarUtils;
import com.wali.live.utils.ItemDataFormatUtils;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.channel.viewmodel.ChannelLiveViewModel;
import com.wali.live.watchsdk.channel.viewmodel.ChannelLiveViewModel.BaseItem;
import com.wali.live.watchsdk.channel.viewmodel.ChannelLiveViewModel.BaseLiveItem;
import com.wali.live.watchsdk.channel.viewmodel.ChannelLiveViewModel.SimpleItem;
import com.wali.live.watchsdk.channel.viewmodel.ChannelLiveViewModel.TVItem;
import com.wali.live.watchsdk.channel.viewmodel.ChannelLiveViewModel.UserItem;
import com.wali.live.watchsdk.channel.viewmodel.ChannelLiveViewModel.VideoItem;
import com.wali.live.watchsdk.channel.viewmodel.ChannelNavigateViewModel;
import com.wali.live.watchsdk.channel.viewmodel.ChannelNavigateViewModel.NavigateItem;
import com.wali.live.watchsdk.channel.viewmodel.ChannelShowViewModel;
import com.wali.live.watchsdk.channel.viewmodel.ChannelShowViewModel.OneTextItem;
import com.wali.live.watchsdk.channel.viewmodel.ChannelTwoTextViewModel;
import com.wali.live.watchsdk.channel.viewmodel.ChannelTwoTextViewModel.TwoLineItem;
import com.wali.live.watchsdk.channel.viewmodel.ChannelUserViewModel;
import com.wali.live.watchsdk.channel.viewmodel.ChannelUserViewModel.UserItemData;

import java.util.List;

import static com.wali.live.watchsdk.channel.viewmodel.ChannelLiveViewModel.RichText.LEFT_LABEL_BG;

/**
 * Created by lan on 16/6/28.
 *
 * @module 频道
 */
public abstract class RepeatHolder extends FixedHolder {

    protected static final int[] LEFT_LABEL_TEXT_COLOR = {R.color.white, R.color.white, R.color.color_fed533};

    protected int mLeftLabelImageWidth = DisplayUtils.dip2px(80); //左上角配角标的宽度
    protected int mLeftLabelImageHeight = DisplayUtils.dip2px(40); //左上角配角标的高度

    protected int[] mParentIds;
    protected int[] mIvIds;
    protected int[] mTvIds;
    protected int[] mBadgeIvIds;
    protected int mBottomContainerId;
    protected int mLeftLabelTvId;
    protected int mLeftLabelIvId;
    protected int mMarkTvId;

    protected ViewGroup[] mParentViews;
    protected BaseImageView[] mImageViews;
    protected TextView[] mTextViews;
    protected ImageView[] mBadgeIvs;
    protected BaseImageView[] mLeftLabelIvs;
    protected View[] mBottomContainers;
    protected TextView[] mLeftLabelTvs;
    protected TextView[] mMarkTvs;

    protected int mViewSize;
    protected int mItemWidth; // 一排单个item的宽度

    public RepeatHolder(View itemView) {
        super(itemView);

        if (isChangeImageSize()) {
            changeImageSize();
        }
    }

    protected abstract void initContentViewId();

    @Override
    protected void initContentView() {
        initContentViewId();

        newContentView();

        for (int i = 0; i < mViewSize; i++) {
            mParentViews[i] = $(mParentIds[i]);
            bindSingleCardView(i);
        }
    }

    protected void newContentView() {
        mParentViews = new ViewGroup[mViewSize];
        mImageViews = new BaseImageView[mViewSize];
        mTextViews = new TextView[mViewSize];
        mBadgeIvs = new ImageView[mViewSize];
    }

    protected void bindSingleCardView(int i) {
        mImageViews[i] = $(mParentViews[i], mIvIds[i]);
        if (mTvIds != null) {
            mTextViews[i] = $(mParentViews[i], mTvIds[i]);
        }
        if (mBadgeIvIds != null) {
            mBadgeIvs[i] = $(mParentViews[i], mBadgeIvIds[i]);
        }
    }

    protected boolean isChangeImageSize() {
        return false;
    }

    protected void changeImageSize() {
        ViewGroup.MarginLayoutParams mlp;
        for (int i = 0; i < mViewSize; i++) {
            mlp = (ViewGroup.MarginLayoutParams) mImageViews[i].getLayoutParams();
            mlp.width = getImageWidth();
            mlp.height = getImageHeight();
        }
    }

    protected int getImageWidth() {
        return 0;
    }

    protected int getImageHeight() {
        return 0;
    }

    protected ScalingUtils.ScaleType getScaleType() {
        return ScalingUtils.ScaleType.CENTER_CROP;
    }

    protected abstract boolean isCircle();

    protected void setParentVisibility(int startIndex) {
        for (int i = startIndex; i < mViewSize; i++) {
            mParentViews[i].setVisibility(View.INVISIBLE);
        }
    }


    @Override
    protected void bindShowModel(ChannelShowViewModel viewModel) {
        List<ChannelShowViewModel.OneTextItem> itemDatas = viewModel.getItemDatas();
        int minSize = Math.min(mViewSize, itemDatas.size());
        setParentVisibility(minSize);
        for (int i = 0; i < minSize; i++) {
            mParentViews[i].setVisibility(View.VISIBLE);

            final OneTextItem item = itemDatas.get(i);
            if (item != null) {
                bindImageWithBorder(mImageViews[i], item.getImgUrl(), isCircle(), 320, 320, getScaleType());
                mImageViews[i].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        jumpItem(item);
                    }
                });
                mTextViews[i].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        jumpItem(item);
                    }
                });
                mTextViews[i].setText(item.getText());

                bindItemOnShowModel(item, i);
                exposureItem(item);
            }
        }
    }

    protected void bindItemOnShowModel(OneTextItem item, int i) {
    }

    @Override
    protected void bindTwoTextModel(ChannelTwoTextViewModel viewModel) {
        List<TwoLineItem> itemDatas = viewModel.getItemDatas();
        int minSize = Math.min(mViewSize, itemDatas.size());
        setParentVisibility(minSize);
        for (int i = 0; i < minSize; i++) {
            mParentViews[i].setVisibility(View.VISIBLE);

            final TwoLineItem item = itemDatas.get(i);
            if (item != null) {
                if (viewModel.isFullColumn()) {
                    bindImage(mImageViews[i], item.getImgUrl(), isCircle(), 320, 320, getScaleType());
                } else {
                    bindImageWithBorder(mImageViews[i], item.getImgUrl(), isCircle(), 320, 320, getScaleType());
                }
                mImageViews[i].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        jumpItem(item);
                    }
                });
                mTextViews[i].setText(item.getName());

                bindItemOnTwoLineModel(item, i);
                exposureItem(item);
            }
        }
    }

    protected void bindItemOnTwoLineModel(TwoLineItem item, int i) {

    }

    @Override
    protected void bindUserModel(ChannelUserViewModel viewModel) {
        List<UserItemData> itemDatas = viewModel.getItemDatas();
        int minSize = Math.min(mViewSize, itemDatas.size());
        setParentVisibility(minSize);
        for (int i = 0; i < minSize; i++) {
            mParentViews[i].setVisibility(View.VISIBLE);
            final UserItemData item = itemDatas.get(i);
            if (item != null) {
                AvatarUtils.loadAvatarByUidTs(mImageViews[i],
                        item.getUser().getUid(),
                        item.getUser().getAvatar(),
                        AvatarUtils.SIZE_TYPE_AVATAR_MIDDLE,
                        isCircle());
                mImageViews[i].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        jumpItem(item);
                    }
                });
                mTextViews[i].setText(item.getUser().getNickname());

                if (mBadgeIvs[i] != null) {
                    if (item.getUser().getCertificationType() > 0) {
                        mBadgeIvs[i].getLayoutParams().width = DisplayUtils.dip2px(18f);
                        mBadgeIvs[i].getLayoutParams().height = DisplayUtils.dip2px(12f);
                        mBadgeIvs[i].setImageDrawable(ItemDataFormatUtils.getCertificationImgSource(item.getUser().getCertificationType()));
                    } else {
                        mBadgeIvs[i].getLayoutParams().width = DisplayUtils.dip2px(10.0f);
                        mBadgeIvs[i].getLayoutParams().height = DisplayUtils.dip2px(10.0f);
                        mBadgeIvs[i].setImageDrawable(ItemDataFormatUtils.getLevelSmallImgSource(item.getUser().getLevel()));
                    }
                }

                bindItemOnUserModel(item, i);
                exposureItem(item);
            }
        }
    }

    protected void bindItemOnUserModel(UserItemData item, int i) {
    }

    @Override
    protected void bindLiveModel(ChannelLiveViewModel viewModel) {
        List<BaseItem> itemDatas = viewModel.getItemDatas();
        int minSize = Math.min(mViewSize, itemDatas.size());
        setParentVisibility(minSize);
        for (int i = 0; i < minSize; i++) {
            mParentViews[i].setVisibility(View.VISIBLE);
            BaseItem item = itemDatas.get(i);
            if (item != null) {
                MyLog.d(TAG, "bindLiveModel imageUrl : " + item.getImageUrl());
                resetItem(i);
                bindItemOnLiveModel(item, i);

                if (item instanceof BaseLiveItem) {
                    bindBaseLiveItem((BaseLiveItem) item, i);
                } else if (item instanceof UserItem) {
                    bindUserItem((UserItem) item, i);
                } else if (item instanceof VideoItem) {
                    bindVideoItem((VideoItem) item, i);
                } else if (item instanceof TVItem) {
                    bindTVItem((TVItem) item, i);
                } else if (item instanceof SimpleItem) {
                    bindSimpleItem((SimpleItem) item, i);
                } else if (item instanceof ChannelLiveViewModel.LiveGroupItem) {
                    bindLiveGroupItem((ChannelLiveViewModel.LiveGroupItem) item, i);
                } else if (item instanceof ChannelLiveViewModel.RadioGroupItem) {
                    bindRadioGroupItem((ChannelLiveViewModel.RadioGroupItem) item, i);
                } else if (item instanceof ChannelLiveViewModel.ImageItem) {
                    bindImageItem((ChannelLiveViewModel.ImageItem)item, i);
                }
                exposureItem(item);
            }
        }
    }

    protected void resetItem(int i) {

    }

    protected void bindItemOnLiveModel(final BaseItem item, int i) {
        if (mViewModel.isFullColumn()) {
            bindImage(mImageViews[i], item.getImageUrl(), isCircle(), 320, 320, getScaleType());
        } else {
            bindImageWithBorder(mImageViews[i], item.getImageUrl(), isCircle(), 320, 320, getScaleType());
        }
        mImageViews[i].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                jumpItem(item);
            }
        });
        boolean hasLabel = bindLabel(item.getLabel(), item.getNameText(), mTextViews[i]);

        // 底部有label，label是可以点击的，显示在底部文字开始的地方，那么整个区域就不再响应点击
        if (mBottomContainers != null && i < mBottomContainers.length) {
            if (item.getUser() != null && !hasLabel) {
                mBottomContainers[i].setOnClickListener(
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                jumpItem(item);
                            }
                        }
                );
            } else {
                mBottomContainers[i].setOnClickListener(null);
            }
        }
    }

    protected void bindBaseLiveItem(BaseLiveItem item, int i) {

    }

    protected void bindUserItem(UserItem item, int index) {
    }

    protected void bindVideoItem(VideoItem item, int index) {
    }

    protected void bindTVItem(TVItem item, int index) {
    }

    protected void bindSimpleItem(SimpleItem item, int index) {
    }

    protected void bindImageItem(ChannelLiveViewModel.ImageItem item, int index) {

    }

    /**直播间组
     *
     * @param item
     * @param index
     */
    protected void bindLiveGroupItem(ChannelLiveViewModel.LiveGroupItem item, int index) {

    }

    /**
     * 电台组
     * @param item
     * @param index
     */
    protected void bindRadioGroupItem(ChannelLiveViewModel.RadioGroupItem item, int index) {

    }

    @Override
    protected void bindNavigateModel(ChannelNavigateViewModel viewModel) {
        List<NavigateItem> itemDatas = viewModel.getItemDatas();
        int minSize = Math.min(mViewSize, itemDatas.size());
        for (int i = minSize; i < mViewSize; i++) {
            mParentViews[i].setVisibility(View.GONE);
        }
        for (int i = 0; i < minSize; i++) {
            if (i == minSize - 1) {
                mParentViews[i].findViewById(R.id.view_dirver).setVisibility(View.GONE);
            }
            mParentViews[i].setVisibility(View.VISIBLE);
            final NavigateItem item = itemDatas.get(i);
            if (item != null) {
                if (viewModel.isFullColumn()) {
                    bindImage(mImageViews[i], item.getImgUrl(), isCircle(), 320, 320, getScaleType());
                } else {
                    bindImageWithBorder(mImageViews[i], item.getImgUrl(), isCircle(), 320, 320, getScaleType());
                }

                mParentViews[i].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        jumpItem(item);
                    }
                });
                mTextViews[i].setText(item.getText());

                bindItemOnNavigateModel(item, i);
                exposureItem(item);
            }
        }
    }

    protected void bindItemOnNavigateModel(NavigateItem item, int i) {

    }

    /**
     * 左上角标签 配文字 对应topLeft
     *
     * @param item
     * @param i
     */
    protected void bindLeftLabel(final BaseItem item, int i) {
        if (mLeftLabelTvs == null || i >= mLeftLabelTvs.length) {
            return;
        }
        if (item.getTopLeft() == null) {
            mLeftLabelTvs[i].setVisibility(View.GONE);
            return;
        }
        String text = item.getTopLeft().getText();
        if (!TextUtils.isEmpty(text)) {
            if(item.getTopLeft().hasBgColor()){
                GradientDrawable bgDrawable = item.getTopLeft().getBgDrawable();
                //leftTop, rightTop, rightBottom, leftBottom of (X,Y)
                int rightTop = mLeftLabelImageHeight >> 1;
                int rightBottom = mLeftLabelImageHeight >> 1;
                int leftTop = mImageCornerRadius << 1;
                float[] radius = {0, 0, rightTop, rightTop, rightBottom, rightBottom, 0, 0 };
                bgDrawable.setCornerRadii(radius);
                mLeftLabelTvs[i].setBackground(bgDrawable);

                int leftPadding = DisplayUtils.dip2px(7f);
                int rightPadding = DisplayUtils.dip2px(7f);
                mLeftLabelTvs[i].setPadding(leftPadding, 0, rightPadding, 0);

                ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) mLeftLabelTvs[i].getLayoutParams();
                layoutParams.leftMargin = 0;
                mLeftLabelTvs[i].setLayoutParams(layoutParams);
            }else {
                int id = item.getTopLeft().getBgID() - 1;
                if (id < 0 || id > LEFT_LABEL_BG.length - 1) {
                    MyLog.w(TAG, " bindLeftLabel unknown img id : " + item.getTopLeft().getBgID() + "name:" + item.getNameText());
                    id = 0;
                }
                int leftPadding = 0;
                int rightPadding = 0;
                mLeftLabelTvs[i].setGravity(Gravity.CENTER);
                if (id >= 3 ) {
                    mLeftLabelTvs[i].setGravity(Gravity.CENTER | Gravity.RIGHT);
                    rightPadding = DisplayUtils.dip2px(6.67f);
                } else if (id == 2) {
                    leftPadding = DisplayUtils.dip2px(6.67f);
                    rightPadding = DisplayUtils.dip2px(8.33f);
                }
                mLeftLabelTvs[i].setPadding(leftPadding, 0, rightPadding, 0);
                mLeftLabelTvs[i].setBackground(itemView.getContext().getResources().getDrawable(LEFT_LABEL_BG[id]));
                mLeftLabelTvs[i].setTextColor(Color.WHITE);
                ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) mLeftLabelTvs[i].getLayoutParams();
                layoutParams.leftMargin = id != 2 ? DisplayUtils.dip2px(6.67f) : 0;
                mLeftLabelTvs[i].setLayoutParams(layoutParams);
            }
        }
        mLeftLabelTvs[i].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(item.getTopLeft().getJumpUrl())) {
                    mJumpListener.jumpScheme(item.getTopLeft().getJumpUrl());
                }
            }
        });
        bindText(mLeftLabelTvs[i], text);
    }

    /**
     * 左上角标签 配图片 对应ListWidgetInfo
     */
    protected void bindLeftWidgetInfo(BaseItem item, int i) {
        if (mLeftLabelIvs == null || i >= mLeftLabelTvs.length) {
            return;
        }
        CommonChannelProto.ListWidgetInfo widgetInfo = item.getWidgetInfo();
        if (widgetInfo != null) {
            String iconUrl = widgetInfo.getIconUrl();
            final String jumpUrl = widgetInfo.getJumpSchemeUri();
            if (!TextUtils.isEmpty(iconUrl)) {
                ViewGroup.MarginLayoutParams layoutParam = (ViewGroup.MarginLayoutParams) mLeftLabelIvs[i].getLayoutParams();
                if (layoutParam.width != mLeftLabelImageWidth) {
                    layoutParam.width = mLeftLabelImageWidth;
                    layoutParam.height = mLeftLabelImageHeight;
                    mLeftLabelIvs[i].setLayoutParams(layoutParam);
                }
                mLeftLabelIvs[i].setVisibility(View.VISIBLE);
                bindImage(mLeftLabelIvs[i], iconUrl, false, mLeftLabelImageWidth, mLeftLabelImageHeight,
                        ScalingUtils.ScaleType.FIT_START);
                mLeftLabelIvs[i].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!TextUtils.isEmpty(jumpUrl)) {
                            mJumpListener.jumpScheme(jumpUrl);
                        }
                    }
                });
            }
        }
    }

    /**
     * 左上角有三个元素： 门票直播， 展示图片的label， 展示文字的label ,优先级递增
     */
    protected void bindLeftTopCorner(BaseItem item, int i) {
        bindLeftLabel(item, i);
        if (mLeftLabelTvs != null && i < mLeftLabelTvs.length && mLeftLabelTvs[i].getVisibility() != View.VISIBLE) {
            bindLeftWidgetInfo(item, i);
            if (mLeftLabelIvs != null && i < mLeftLabelIvs.length && mLeftLabelIvs[i].getVisibility() != View.VISIBLE) {
                bindMarkTv(item, i);
            }
        }
    }

    /**
     * 展示门票直播
     */
    protected void bindMarkTv(BaseItem item, int i) {
        if (mMarkTvs != null && i < mMarkTvs.length) {
            if (item instanceof ChannelLiveViewModel.LiveItem && ((ChannelLiveViewModel.LiveItem) item).isTicket()) {
                mMarkTvs[i].setVisibility(View.VISIBLE);
            }
        }
    }
}
