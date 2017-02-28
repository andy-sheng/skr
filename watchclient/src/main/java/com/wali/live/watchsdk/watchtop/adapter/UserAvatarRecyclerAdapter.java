package com.wali.live.watchsdk.watchtop.adapter;

import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;

import com.base.global.GlobalData;
import com.base.image.fresco.BaseImageView;
import com.base.log.MyLog;
import com.base.utils.display.DisplayUtils;
import com.jakewharton.rxbinding.view.RxView;
import com.mi.live.data.manager.LiveRoomCharactorManager;
import com.mi.live.data.query.model.ViewerModel;
import com.wali.live.common.listener.OnItemClickListener;
import com.wali.live.utils.AvatarUtils;
import com.wali.live.utils.ItemDataFormatUtils;
import com.wali.live.watchsdk.R;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.functions.Action1;

/**
 * Created by lan on 15-11-26.
 */
public class UserAvatarRecyclerAdapter extends RecyclerView.Adapter<UserAvatarRecyclerAdapter.UserAvatarHolder> {
    private static final String TAG = UserAvatarRecyclerAdapter.class.getSimpleName();

    private static int[] mCrownBackgrounds = {R.drawable.avatar_item_crown_gold, R.drawable.avatar_item_crown_sliver, R.drawable.avatar_item_crown_cuprum };
    private LinkedList<ViewerModel> mViewerList = new LinkedList<>();

    public static final int ITEM_TYPE_NORMAL = 100;    //列表的header
    public static final int ITEM_TYPE_FOOTER_BTN = 101;    //列表的footer  展示loadmore正在加载 和 列表为空的状态

    private OnItemClickListener mClickListener;

    private View mFooterBtn;
    View.OnClickListener mFooterBtnListener;

    private int mLastLayoutPosition;  // 屏幕右边最后一个item 位置
    private Pair<Integer, Float> mLastItemPositionAndAlpha = new Pair<>(-1, 1.0f);
    private Pair<Integer, Float> mLastSecondPositionAndAlpha = new Pair<>(-1, 1.0f);

    private int mManagerCount = -1;

    public void setMaxManagerNumAndClickListener(int cnt, View.OnClickListener listener) {
        mManagerCount = cnt;
        mFooterBtnListener = listener;
    }

    private AdapterView.OnItemLongClickListener mLongClickListener;

    public void setOnItemClickListener(OnItemClickListener listener) {
        mClickListener = listener;
    }

    public void setOnItemLongClickListener(AdapterView.OnItemLongClickListener listener) {
        mLongClickListener = listener;
    }

    public UserAvatarRecyclerAdapter() {
    }

    public void setViewerList(List<ViewerModel> dataList) {
        setViewerList(dataList, false);
    }

    public void setViewerList(List<ViewerModel> dataList, boolean force) {
        mViewerList.clear();
        mViewerList.addAll(dataList);
        notifyDataSetChanged();
    }

    public void addViewerList(Collection<ViewerModel> dataList) {
        mViewerList.addAll(dataList);
        notifyDataSetChanged();
    }

    public ViewerModel getViewer(int position) {
        if (mManagerCount < 0) {
            if (position < 0 || position >= getItemCount()) {
                return null;
            }
            return mViewerList.get(position);
        } else {
            if (position < 1 || position >= getItemCount()) {
                return null;
            }
            return mViewerList.get(position - 1);
        }
    }


    @Override
    public int getItemCount() {
        int count = (mManagerCount > 0 && (mViewerList == null ? 0 : mViewerList.size()) <= mManagerCount) ? 1 : 0;
        return (mViewerList == null ? 0 : mViewerList.size()) + count;
    }

    @Override
    public UserAvatarHolder onCreateViewHolder(ViewGroup parent, int viewType) {
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
        if (getItemViewType(position) == ITEM_TYPE_FOOTER_BTN) {
            return;
        }

        //这个adapter观众头像和管理员头像 管理员头像maxCnt大于0
        ViewerModel viewer = mViewerList.get(position);
        AvatarUtils.loadAvatarByUidTs(holder.avatarIv, viewer.getUid(), viewer.getAvatar(), true);
        if (position < 3 && mViewerList.size() >= 5 ) {
            //         holder.crownIv.setVisibility(View.VISIBLE);
            holder.crownIv.setBackground(holder.crownIv.getContext().getResources().getDrawable(mCrownBackgrounds[position]));
        } else {
            holder.crownIv.setImageResource(0);
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

       /* if (position == mLastItemPositionAndAlpha.first) {   // 最右一个item 设置alpha
            holder.itemView.setAlpha(mLastItemPositionAndAlpha.second);
        } else if (position == mLastSecondPositionAndAlpha.first) {
            holder.itemView.setAlpha(mLastSecondPositionAndAlpha.second); // 最右倒数第二个 设置alpha
        } else {
            holder.itemView.setAlpha(1.0f);
        }*/
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

    public class UserAvatarHolder extends RecyclerView.ViewHolder {
        public BaseImageView avatarIv;

        public ImageView badgeIv;

        public ImageView crownIv;

        public UserAvatarHolder(View itemView) {
            super(itemView);
            if (mFooterBtn == itemView) {
                return;
            }
            avatarIv = (BaseImageView) itemView.findViewById(R.id.user_avatar_iv);
            badgeIv = (ImageView) itemView.findViewById(R.id.user_badge_iv);
            crownIv = (ImageView)itemView.findViewById(R.id.user_crown);
        }
    }
}
