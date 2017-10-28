package com.wali.live.watchsdk.videodetail.adapter;

import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.base.global.GlobalData;
import com.base.image.fresco.BaseImageView;
import com.base.image.fresco.FrescoWorker;
import com.base.image.fresco.image.BaseImage;
import com.base.image.fresco.image.ImageFactory;
import com.base.utils.date.DateTimeUtils;
import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.view.SimpleDraweeView;
import com.mi.live.data.room.model.RoomBaseDataModel;
import com.wali.live.utils.AvatarUtils;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.component.adapter.ClickItemAdapter;

/**
 * Created by zyh on 2017/6/7.
 */

public class DetailReplayAdapter extends ClickItemAdapter<DetailReplayAdapter.ReplayInfoItem,
        DetailReplayAdapter.ReplayHolder, DetailReplayAdapter.IReplayClickListener> {
    private static final String TAG = "DetailReplayAdapter";
    private static final int CORNER_RADIUS = 8;
    private RoomBaseDataModel mMyRoomData;

    public void setMyRoomData(RoomBaseDataModel myRoomData) {
        mMyRoomData = myRoomData;
    }

    @Override
    public ReplayHolder newViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.detail_replay_item, null);
        return new ReplayHolder(view);
    }

    public class ReplayHolder extends ClickItemAdapter.BaseHolder<ReplayInfoItem,
            DetailReplayAdapter.IReplayClickListener> {
        private BaseImageView mCoverIv;
        private BaseImageView mAvatarIv;
        private TextView mNameTv;
        private TextView mDescriptionTv;
        private TextView mTimeTv;
        private TextView mViewerCntTv;

        public ReplayHolder(View view) {
            super(view);
            mCoverIv = $(R.id.cover_iv);
            mAvatarIv = $(R.id.avatar_iv);
            mNameTv = $(R.id.name_tv);
            mDescriptionTv = $(R.id.description_tv);
            mTimeTv = $(R.id.back_tv_left);
            mViewerCntTv = $(R.id.back_tv_right);
        }

        @Override
        public void bindView(final ReplayInfoItem item, final IReplayClickListener iReplayClickListener) {
            if (!TextUtils.isEmpty(item.mLiveCover)) {
                loadAvatarCornerByUrl(mCoverIv, AvatarUtils.getImgUrlByAvatarSize(item.mLiveCover,
                        AvatarUtils.SIZE_TYPE_AVATAR_SMALL), false, R.drawable.avatar_default_b, CORNER_RADIUS);
            } else {
                AvatarUtils.loadAvatarByUidTsCorner(mCoverIv, mMyRoomData.getUid(), mMyRoomData.getAvatarTs(), CORNER_RADIUS, 0, 0);
            }
            AvatarUtils.loadAvatarByUidTs(mAvatarIv, mMyRoomData.getUid(), mMyRoomData.getAvatarTs(), true);
            mNameTv.setText(mMyRoomData.getNickName());
            if (TextUtils.isEmpty(item.mLiveTitle)) {
                mDescriptionTv.setVisibility(View.GONE);
            } else {
                mDescriptionTv.setText(item.mLiveTitle);
                mDescriptionTv.setVisibility(View.VISIBLE);
            }
            String time = DateTimeUtils.formatFeedsHumanableDate(item.mStartTime,
                    System.currentTimeMillis());
            mTimeTv.setText(time);
            mViewerCntTv.setText(String.format(GlobalData.app().getResources()
                    .getQuantityString(R.plurals.live_end_viewer_cnt, item.mViewerCnt,
                            item.mViewerCnt)));
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (iReplayClickListener != null) {
                        iReplayClickListener.onItemClick(item);
                    }
                }
            });
        }

        private void loadAvatarCornerByUrl(final SimpleDraweeView draweeView, final String url,
                                           final boolean isCircle, int loadingAvatarResId,
                                           int corner) {
            BaseImage avatarImg;
            if (TextUtils.isEmpty(url)) {
                avatarImg = ImageFactory.newResImage(loadingAvatarResId).setCornerRadius(corner).build();
            } else {
                avatarImg = ImageFactory.newHttpImage(url).setWidth(draweeView.getWidth()).setHeight(draweeView.getHeight())
                        .setIsCircle(isCircle).setCornerRadius(corner)
                        .setFailureDrawable(loadingAvatarResId > 0 ? GlobalData.app().getResources().getDrawable(
                                loadingAvatarResId) : null)
                        .setFailureScaleType(
                                isCircle ? ScalingUtils.ScaleType.CENTER_INSIDE : ScalingUtils.ScaleType.CENTER_CROP)
                        .build();
            }
            FrescoWorker.loadImage(draweeView, avatarImg);
        }
    }

    public static class ReplayInfoItem extends ClickItemAdapter.BaseItem {
        public String mLiveId;     //房间id
        public int mViewerCnt;     //观众数
        public String mUrl;        //回放地址
        public String mLiveTitle;   //回放标题
        public String mLiveCover;   //回放封面
        public String mShareUrl;    //分享url
        public long mStartTime;    //开始时间

        public ReplayInfoItem(String liveId, int viewerCnt,
                              String url, String liveTitle, String liveCover, String shareUrl,
                              long startTime) {
            mLiveId = liveId;
            mViewerCnt = viewerCnt;
            mUrl = url;
            mLiveTitle = liveTitle;
            mLiveCover = liveCover;
            mShareUrl = shareUrl;
            mStartTime = startTime;
        }
    }

    public interface IReplayClickListener {
        void onItemClick(ReplayInfoItem replayInfoItem);
    }
}
