package com.wali.live.modulechannel.adapter.holder;

import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import com.common.core.avatar.AvatarUtils;
import com.common.image.fresco.BaseImageView;
import com.common.utils.ImageUtils;
import com.common.utils.U;
import com.facebook.drawee.drawable.ScalingUtils;
import com.wali.live.modulechannel.R;
import com.wali.live.modulechannel.helper.HolderHelper;
import com.wali.live.modulechannel.model.viewmodel.ChannelLiveGroupViewModel;
import com.wali.live.modulechannel.view.LiveGroupListOuterThreeIcomView;

import java.util.Arrays;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

/**
 * Created by lan on 16/6/28.
 *
 * @module 频道
 * @description 并列2个方形图片的item
 */
public class TwoLiveGroupCardHolder extends FixedHolder {
    protected BaseImageView[] mCoverTwoIvs;
    protected BaseImageView[] mFrameTwoIvs;
    protected LiveGroupListOuterThreeIcomView[] mLiveGroupListOuterThreeIcomViews;

    protected View mMarginArea;
    protected View mArrayArea;

    protected ViewGroup[] mParentViews;
    protected int[] mParentIds;

    public TwoLiveGroupCardHolder(View itemView) {
        super(itemView);
    }

    @Override
    protected void initContentView() {
        mParentIds = new int[]{
                R.id.single_card_1,
                R.id.single_card_2,
        };

        int size = mParentIds.length;

        mParentViews = new ViewGroup[size];
        mCoverTwoIvs = new BaseImageView[size];
        mFrameTwoIvs = new BaseImageView[size];
        mLiveGroupListOuterThreeIcomViews = new LiveGroupListOuterThreeIcomView[size];

        for (int i = 0; i < size; i++) {
            mParentViews[i] = $(mParentIds[i]);
            mCoverTwoIvs[i] = $(mParentViews[i], R.id.cover_iv);
            mFrameTwoIvs[i] = $(mParentViews[i], R.id.cover_iv_second);
            mLiveGroupListOuterThreeIcomViews[i] = $(mParentViews[i], R.id.gm_icons);
            int width = ((U.getDisplayUtils().getScreenWidth() - SIDE_MARGIN * 2 - MIDDLE_MARGIN_TWO) / 2);
            mCoverTwoIvs[i].getLayoutParams().height = width;
            mFrameTwoIvs[i].getLayoutParams().height = width;
        }

        mMarginArea = $(R.id.margin_area);
        mArrayArea = $(R.id.array_area);

//        mMarginArea.getLayoutParams().height = DisplayUtils.dip2px(3.33f);
        mImageBorderWidth = 0;
    }

    @Override
    protected void bindLiveGroupViewModel(final ChannelLiveGroupViewModel channelLiveGroupViewModel) {
        if(channelLiveGroupViewModel.getItemDatas().size() == 0){
            return;
        }

        Observable.range(0, channelLiveGroupViewModel.getItemDatas().size())
                .subscribe(new Observer<Integer>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Integer integer) {
                        mParentViews[integer].setVisibility(View.VISIBLE);
                        final ChannelLiveGroupViewModel.GroupDate groupDate = channelLiveGroupViewModel.getItemDatas().get(integer);
                        mLiveGroupListOuterThreeIcomViews[integer]
                                .bindDate(new LiveGroupListOuterThreeIcomView.LiveGroupListOuterThreeIcons(Arrays.asList(groupDate.getLiveCovers()), groupDate.getGroupCnt())
                                        , mFrameTwoIvs[integer].getWidth(), mFrameTwoIvs[integer].getHeight());

                        String url = !TextUtils.isEmpty(groupDate.getFrameUri()) ? groupDate.getFrameUri() + U.getImageUtils().getSizeSuffix(ImageUtils.SIZE.SIZE_480) : groupDate.getFrameUri();

                        bindImageWithCorner(mFrameTwoIvs[integer], url, false, mFrameTwoIvs[integer].getWidth(), mFrameTwoIvs[integer].getHeight(), ScalingUtils.ScaleType.FIT_XY, null);
                        bindImageWithCorner(mCoverTwoIvs[integer], url, false, mCoverTwoIvs[integer].getWidth(), mCoverTwoIvs[integer].getHeight(), getScaleType(), null);

                        mFrameTwoIvs[integer].setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                jumpSchema(groupDate.getSchemeUri(), channelLiveGroupViewModel.getSectionId());
                            }
                        });
                        //TOdo-暂时去了
//                        HolderHelper.sendExposureCommand(SchemeUtils.getRecommendTag(groupDate.getSchemeUri()));
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });

//        Observable.range(0, channelLiveGroupViewModel.getItemDatas().size())
//                .subscribe(new Subscriber<Integer>() {
//                               @Override
//                               public void onCompleted() {
//
//                               }
//
//                               @Override
//                               public void onError(Throwable e) {
//                                   MyLog.e(TAG, e.getMessage());
//                               }
//
//                               @Override
//                               public void onNext(Integer integer) {
//                                   mParentViews[integer].setVisibility(View.VISIBLE);
//                                   final ChannelLiveGroupViewModel.GroupDate groupDate = channelLiveGroupViewModel.getItemDatas().get(integer);
//                                   mLiveGroupListOuterThreeIcomViews[integer]
//                                           .bindDate(new LiveGroupListOuterThreeIcomView.LiveGroupListOuterThreeIcons(Arrays.asList(groupDate.getLiveCovers()), groupDate.getGroupCnt())
//                                                   , mFrameTwoIvs[integer].getWidth(), mFrameTwoIvs[integer].getHeight());
//
//                                   bindImageWithCorner(mFrameTwoIvs[integer], AvatarUtils.getImgUrlByAvatarSize(groupDate.getFrameUri(), SIZE_TYPE_AVATAR_MIDDLE), false, mFrameTwoIvs[integer].getWidth(), mFrameTwoIvs[integer].getHeight(), ScalingUtils.ScaleType.FIT_XY, null);
//                                   bindImageWithCorner(mCoverTwoIvs[integer], AvatarUtils.getImgUrlByAvatarSize(groupDate.getCoverUri(), SIZE_TYPE_AVATAR_MIDDLE), false, mCoverTwoIvs[integer].getWidth(), mCoverTwoIvs[integer].getHeight(), getScaleType(), null);
//
//                                   mFrameTwoIvs[integer].setOnClickListener(new View.OnClickListener() {
//                                       @Override
//                                       public void onClick(View v) {
//                                           jumpSchema(groupDate.getSchemeUri(), channelLiveGroupViewModel.getSectionId());
//                                       }
//                                   });
//                                   HolderHelper.sendExposureCommand(SchemeUtils.getRecommendTag(groupDate.getSchemeUri()));
//                               }});

        if (channelLiveGroupViewModel.getItemDatas().size() == 1) {
            mParentViews[1].setVisibility(View.INVISIBLE);
            mFrameTwoIvs[1].setOnClickListener(null);
        }
    }

    public void jumpSchema(String schemaUri, int sectionId) {
        if (mJumpListener != null) {
            mJumpListener.jumpScheme(schemaUri);
        }
    }

    protected ScalingUtils.ScaleType getScaleType() {
        return ScalingUtils.ScaleType.CENTER_CROP;
    }

    @Override
    protected void initTitleView() {
        super.initTitleView();
        mSplitLine.setVisibility(View.GONE);
    }

}
