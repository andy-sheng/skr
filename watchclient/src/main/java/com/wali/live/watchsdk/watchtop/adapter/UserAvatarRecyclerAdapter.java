package com.wali.live.watchsdk.watchtop.adapter;

import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.base.image.fresco.BaseImageView;
import com.base.log.MyLog;
import com.base.utils.display.DisplayUtils;
import com.jakewharton.rxbinding.view.RxView;
import com.mi.live.data.query.model.ViewerModel;
import com.wali.live.common.listener.OnItemClickListener;
import com.wali.live.utils.AvatarUtils;
import com.wali.live.utils.ItemDataFormatUtils;
import com.wali.live.watchsdk.R;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.functions.Action1;

/**
 * Created by lan on 15-11-26.
 */
public class UserAvatarRecyclerAdapter extends RecyclerView.Adapter<UserAvatarRecyclerAdapter.UserAvatarHolder> {
    private static final String TAG = UserAvatarRecyclerAdapter.class.getSimpleName();

    private static int[] mCrownBackgrounds = {R.drawable.avatar_item_crown_gold, R.drawable.avatar_item_crown_sliver, R.drawable.avatar_item_crown_cuprum};
    private ArrayList<ViewerModel> mViewerList = new ArrayList<>();

    public static final int ITEM_TYPE_NORMAL = 100;    //列表的header

    private OnItemClickListener mClickListener;

    private Pair<Integer, Float> mLastItemPositionAndAlpha = new Pair<>(-1, 1.0f);
    private Pair<Integer, Float> mLastSecondPositionAndAlpha = new Pair<>(-1, 1.0f);


    public void setOnItemClickListener(OnItemClickListener listener) {
        mClickListener = listener;
    }

    public UserAvatarRecyclerAdapter() {
    }

    public void setViewerList(List<ViewerModel> dataList) {
        setViewerList(dataList, false);
    }

    public void setViewerList(List<ViewerModel> dataList, boolean force) {
        mViewerList.clear();
        mViewerList.addAll(dataList);
        MyLog.d(TAG,"setViewerList size:"+mViewerList.size());
        notifyDataSetChanged();
    }


    @Override
    public int getItemCount() {
        return mViewerList.size();
    }

    @Override
    public UserAvatarHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        MyLog.d(TAG,"onCreateViewHolder");
        View itemView;
        switch (viewType) {
            case ITEM_TYPE_NORMAL:
                itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_avatar_item, parent, false);
                break;
            default:
                itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_avatar_item, parent, false);
                break;

        }
        return new UserAvatarHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final UserAvatarHolder holder, final int position) {
        MyLog.d(TAG,"onBindViewHolder");
        ViewerModel viewer;
        //这个adapter观众头像和管理员头像 管理员头像maxCnt大于0
        viewer = mViewerList.get(position);
        AvatarUtils.loadAvatarByUidTs(holder.avatarIv, viewer.getUid(), viewer.getAvatar(), true);

        if (position < 3 && mViewerList.size() >= 5) {
            //         holder.crownIv.setVisibility(View.VISIBLE);
            holder.crownIv.setBackground(holder.crownIv.getContext().getResources().getDrawable(mCrownBackgrounds[position]));
        } else {
            holder.crownIv.setVisibility(View.INVISIBLE);
        }

        if (viewer.getCertificationType() > 0 && !viewer.isRedName()) {
            holder.badgeIv.getLayoutParams().width = DisplayUtils.dip2px(16f);
            holder.badgeIv.getLayoutParams().height = DisplayUtils.dip2px(16f);
            holder.badgeIv.setImageDrawable(ItemDataFormatUtils.getCertificationImgSource(viewer.getCertificationType()));
        } else {
            holder.badgeIv.getLayoutParams().width = DisplayUtils.dip2px(10f);
            holder.badgeIv.getLayoutParams().height = DisplayUtils.dip2px(10f);
            if (viewer.isRedName()) {
                holder.badgeIv.setImageDrawable(ItemDataFormatUtils.getSmallRedName());
            } else {
                holder.badgeIv.setImageDrawable(ItemDataFormatUtils.getLevelSmallImgSource(viewer.getLevel()));
            }
        }

        if (mClickListener != null) {
            RxView.clicks(holder.itemView)
                    .throttleFirst(500, TimeUnit.MILLISECONDS)
                    .subscribe(new Action1<Void>() {
                        @Override
                        public void call(Void aVoid) {
                            mClickListener.onItemClick(holder.itemView, position);
                        }
                    });
        }

        if (position == mLastItemPositionAndAlpha.first) {   // 最右一个item 设置alpha
            holder.itemView.setAlpha(mLastItemPositionAndAlpha.second);
        } else if (position == mLastSecondPositionAndAlpha.first) {
            holder.itemView.setAlpha(mLastSecondPositionAndAlpha.second); // 最右倒数第二个 设置alpha
        } else {
            holder.itemView.setAlpha(1.0f);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return ITEM_TYPE_NORMAL;
    }

    public void setLastItemPositionAndAlpha(Pair<Integer, Float> positionAndAlpha) {
        mLastItemPositionAndAlpha = positionAndAlpha;
    }

    public void setLastSecondPositionAndAlpha(Pair<Integer, Float> positionAndAlpha) {
        mLastSecondPositionAndAlpha = positionAndAlpha;
    }

    public ViewerModel getViewer(int position) {
        return mViewerList.get(position);
    }

    public class UserAvatarHolder extends RecyclerView.ViewHolder {
        public BaseImageView avatarIv;
        public ImageView badgeIv;

        public ImageView crownIv;

        public UserAvatarHolder(View itemView) {
            super(itemView);
            avatarIv = (BaseImageView) itemView.findViewById(R.id.user_avatar_iv);
            badgeIv = (ImageView) itemView.findViewById(R.id.user_badge_iv);
            crownIv = (ImageView) itemView.findViewById(R.id.user_crown);
        }
    }
}
