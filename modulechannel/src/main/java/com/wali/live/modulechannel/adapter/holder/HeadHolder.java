package com.wali.live.modulechannel.adapter.holder;

import android.text.TextPaint;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.common.image.fresco.BaseImageView;
import com.common.image.fresco.FrescoWorker;
import com.common.image.model.BaseImage;
import com.common.image.model.ImageFactory;
import com.common.log.MyLog;
import com.common.utils.U;
import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.view.SimpleDraweeView;
import com.wali.live.modulechannel.R;

import static android.view.View.GONE;
/**
 * Created by lan on 16/6/28.
 *
 * @module 频道
 * @description 显示head的抽象基类
 */
public abstract class HeadHolder extends StayExposureHolder {

    public static int type = 0;
    public final int HEAD_TYPE_ONE_LINE = 2; // title样式 ,左标题 右箭头 一行标题  ！！！默认样式
    public final int HEAD_TYPE_ICON = 3; //第三种类型的title样式，带icon  广场分类标题样式
    //    public final int HEAD_TYPE_HIGH = 4; //第4种类型的title样式，比较高
    public final int HEAD_TYPE_ICON_AT_MORE = 5; // 右边更多标题的左边有一个小icon 可以动   不带副标题   现在我的关注在用
    public final int HEAD_TYPE_NEW_TWO_LINE = 6; // 新的有主次标题的title   小时榜TOP10在用
    protected View mHeadArea;
    protected View mTitleContainer;
    protected TextView mHeadTv;
    protected TextView mSubHeadTv;
    protected TextView mMoreTv;
    protected View mSplitLine;
    protected View mSplitArea;
    protected BaseImageView mHeadIv;
    protected BaseImageView mIcon; // 右测标题左边的 icon 可以动

    public HeadHolder(View itemView) {
        super(itemView);
    }

    protected boolean needTitleView() {
        return true;
    }

    protected void initView() {
        if (needTitleView()) {
            initTitleView();
        }
        initContentView();
    }

    protected void initTitleView() {
        mHeadArea = $(R.id.title_area);
        if (mHeadArea != null) {
            mHeadArea.getLayoutParams().height = U.getDisplayUtils().dip2px(43.33f);
        }

        mHeadTv = $(R.id.head_tv);
        mSubHeadTv = $(R.id.sub_head_tv);
        mMoreTv = $(R.id.more_tv);
        mHeadIv = $(R.id.head_iv);
        mSplitLine = $(R.id.split_line);
        mIcon = $(R.id.icon);
        mTitleContainer = $(R.id.ll_title_container);
        if (mSplitLine != null) {
            mSplitLine.setVisibility(View.GONE);
        }
        mSplitArea = $(R.id.split_area);
        if (mSplitArea != null) {
            mSplitArea.setVisibility(View.GONE);
        }
    }

    protected abstract void initContentView();

    @Override
    protected void bindView() {
        super.bindView();
        if (needTitleView()) {
            bindTitleView();
        }
    }

    private void bindTitleView() {
        // 处理分隔区域，现在由服务器下发分割线，所以不再需要特殊处理
//        bindSplitView();

        // 如果有标题，显示标题；有更多，显示更多
        if (mHeadArea == null) {
            return;
        }
        mIcon.setVisibility(View.GONE);
        if (mViewModel.hasHead()) {
            mHeadArea.setVisibility(View.VISIBLE);
            mHeadTv.setText(mViewModel.getHead());
            mSubHeadTv.setText(mViewModel.getSubHead());

            MyLog.d(TAG, " bindTitleView type: " + mViewModel.getHeadType() + " title " + mViewModel.getHead());
            if (!TextUtils.isEmpty(mViewModel.getHeadUri())) {
                mHeadArea.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        jumpMore();
                    }
                });

                mMoreTv.setVisibility(View.VISIBLE);
                mMoreTv.setText(mViewModel.getHeaderViewAllText());
                mMoreTv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        jumpMore();
                    }
                });

            } else {
                mMoreTv.setVisibility(View.GONE);
                itemView.setOnClickListener(null);
            }

            resetHeaderLayout(mViewModel.getHeadType());
        } else {
            mHeadArea.setVisibility(View.GONE);
        }
    }

    private void resetHeaderLayout(int headerType) {
        LinearLayout.LayoutParams subHeadTvParams = (LinearLayout.LayoutParams) mSubHeadTv.getLayoutParams();
        RelativeLayout.LayoutParams titleContainerParams = (RelativeLayout.LayoutParams) mTitleContainer.getLayoutParams();
        TextPaint tp = mHeadTv.getPaint();
        switch (headerType) {
            case HEAD_TYPE_ICON:
                mHeadArea.getLayoutParams().height = U.getDisplayUtils().dip2px(72.67f);
                mHeadIv.setVisibility(View.VISIBLE);
                mSubHeadTv.setVisibility(View.VISIBLE);
                mHeadTv.setTextSize(15.0f);
                mIcon.setVisibility(View.GONE);
                titleContainerParams.leftMargin = U.getDisplayUtils().dip2px(6.67f);
                subHeadTvParams.topMargin = U.getDisplayUtils().dip2px(4.67f);
                tp.setFakeBoldText(false);
                loadCoverByUrlRoundCorner(mHeadIv, mViewModel.getHeadIconUri(), 1080, mHeadIv.getWidth(), mHeadIv.getHeight());
//                AvatarUtils.loadCoverByUrlRoundCorner(mHeadIv, mViewModel.getHeadIconUri(), 1080, mHeadIv.getWidth(), mHeadIv.getHeight());

                break;
            case HEAD_TYPE_ICON_AT_MORE:
                mHeadArea.getLayoutParams().height = U.getDisplayUtils().dip2px(60.0f);
                mHeadIv.setVisibility(View.GONE);
                mSubHeadTv.setVisibility(View.GONE);
                mHeadTv.setTextSize(18.67f);
                mIcon.setVisibility(View.VISIBLE);
                BaseImage baseImage = ImageFactory.newResImage(R.drawable.channel_head_icon)
                        .setWidth(U.getDisplayUtils().dip2px(13.3f))
                        .setHeight(U.getDisplayUtils().dip2px(13.3f))
                        .setIsCircle(false)
                        .build();
                FrescoWorker.loadImage(mIcon, baseImage);
                titleContainerParams.leftMargin = U.getDisplayUtils().dip2px(13.33f);
                subHeadTvParams.topMargin = U.getDisplayUtils().dip2px(0.0f);
                tp.setFakeBoldText(true);

                break;
            case HEAD_TYPE_NEW_TWO_LINE:
                mHeadArea.getLayoutParams().height = U.getDisplayUtils().dip2px(72.67f);
                mHeadIv.setVisibility(View.GONE);
                mSubHeadTv.setVisibility(View.VISIBLE);
                mHeadTv.setTextSize(18.67f);
                mIcon.setVisibility(View.GONE);
                titleContainerParams.leftMargin = U.getDisplayUtils().dip2px(13.33f);
                subHeadTvParams.topMargin = U.getDisplayUtils().dip2px(2.0f);
                tp.setFakeBoldText(true);
                break;
            default:
                mHeadArea.getLayoutParams().height = U.getDisplayUtils().dip2px(60f);
                mHeadTv.setTextSize(18.67f);
                titleContainerParams.leftMargin = U.getDisplayUtils().dip2px(10f);
                tp.setFakeBoldText(true);
                mHeadIv.setVisibility(View.GONE);
                mSubHeadTv.setVisibility(View.GONE);
                mIcon.setVisibility(View.GONE);
                break;
        }
    }

    private void bindSplitView() {
        // 如果有标题或是组内第一个，同时不是列表第一个的话，就显示分割区域
        if ((mViewModel.hasHead() || mViewModel.isFirst()) && mPosition != 0 && !(mViewModel.isFirst() && mPosition == 1)) {
            mSplitArea.setVisibility(View.VISIBLE);
        } else {
            mSplitArea.setVisibility(View.GONE);
        }
    }

    private void jumpMore() {
        if (mJumpListener != null) {
            mJumpListener.jumpScheme(mViewModel.getHeadUri());
        } else {
         //   HolderHelper.jumpScheme(itemView.getContext(), mViewModel.getHeadUri());
        }

        //Todo-暂时去除打点
//        if (!TextUtils.isEmpty(mViewModel.getStatisticsKey())) {
//            StatisticsWorker.getsInstance().sendCommand(StatisticsWorker.AC_APP, mViewModel.getStatisticsKey(), 1);
//        }
    }

    protected void loadCoverByUrlRoundCorner(final SimpleDraweeView draweeView, final String url, int roundRadius, int width, int heigh) {
        BaseImage avatarImg;
        if (TextUtils.isEmpty(url)) {
            avatarImg = ImageFactory.newResImage(R.drawable.avatar_default).build();
        } else {
            avatarImg = ImageFactory.newHttpImage(url).setWidth(width).setHeight(heigh)
                    .setCornerRadius(roundRadius)
                    .setFailureDrawable(R.drawable.avatar_default > 0 ? U.app().getResources().getDrawable(
                            R.drawable.avatar_default) : null)
                    .setFailureScaleType(ScalingUtils.ScaleType.CENTER_CROP)
                    .build();
        }
        avatarImg.setLoadingDrawable(R.drawable.avatar_default > 0 ? U.app().getResources().getDrawable(
                R.drawable.avatar_default) : null);
        avatarImg.setLoadingScaleType(ScalingUtils.ScaleType.CENTER_CROP);
        FrescoWorker.loadImage(draweeView, avatarImg);
    }
}
