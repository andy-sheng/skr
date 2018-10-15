package com.wali.live.watchsdk.watchtop.adapter;

import android.graphics.drawable.Drawable;
import android.support.annotation.CheckResult;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import com.base.global.GlobalData;
import com.base.image.fresco.BaseImageView;
import com.base.log.MyLog;
import com.base.utils.CommonUtils;
import com.base.utils.display.DisplayUtils;
import com.base.utils.language.LocaleUtil;
import com.facebook.drawee.drawable.ScalingUtils;
import com.jakewharton.rxbinding.view.RxView;
import com.mi.live.data.config.GetConfigManager;
import com.mi.live.data.query.model.ViewerModel;
import com.mi.live.data.user.User;
import com.wali.live.common.barrage.view.utils.NobleConfigUtils;
import com.wali.live.common.listener.OnItemClickListener;
import com.wali.live.manager.WatchRoomCharactorManager;
import com.wali.live.utils.AvatarUtils;
import com.wali.live.utils.ItemDataFormatUtils;
import com.wali.live.utils.level.VipLevelUtil;
import com.wali.live.watchsdk.R;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.functions.Action1;

/**
 * Created by lan on 15-11-26.
 */
public class UserAvatarRecyclerAdapter extends RecyclerView.Adapter<UserAvatarRecyclerAdapter.UserAvatarHolder> {
    private static final String TAG = "UserAvatarRecyclerAdapter";

    private static int[] mCrownBackgrounds = {R.drawable.avatar_item_crown_gold, R.drawable.avatar_item_crown_sliver, R.drawable.avatar_item_crown_cuprum};
    private LinkedList<ViewerModel> mViewerList = new LinkedList<>();

    public static final int ITEM_TYPE_NORMAL = 100;    //列表的header
    public static final int ITEM_TYPE_FOOTER_BTN = 101;    //列表的footer  展示loadmore正在加载 和 列表为空的状态
    private static final int LENGTH_DP_10 = DisplayUtils.dip2px(10f);
    private static final int LENGTH_DP_12 = DisplayUtils.dip2px(12);
    private static final int LENGTH_DP_16 = DisplayUtils.dip2px(16f);

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
        if (dataList != null) {
            mViewerList.addAll(dataList);
        }
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

        holder.reset();
        //这个adapter观众头像和管理员头像 管理员头像maxCnt大于0
        ViewerModel viewer = mViewerList.get(position);
        AvatarUtils.loadAvatarByUidTs(holder.avatarIv, viewer.getUid(), viewer.getAvatar(), true);
        if (mManagerCount < 0) {
            viewer = mViewerList.get(position);
            AvatarUtils.loadAvatarByUidTs(holder.avatarIv, viewer.getUid(), viewer.getAvatar(), true);
        } else {
            viewer = mViewerList.get(position - 1);
            Boolean isManager = WatchRoomCharactorManager.getInstance().isManager(viewer.getUid());
            AvatarUtils.loadAvatarByUidTsGray(holder.avatarIv, viewer.getUid(), viewer.getAvatar(), true, isManager);
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

        // 设置有星票贡献的
        int ticketCount = viewer.getCurrentLiveTicket();

        if (!viewer.isNoble()) {
            adjustLayoutNotNobel(holder);
        }

        if (!TextUtils.isEmpty(viewer.getUserNobelID())) {
            bindActivityIcon(holder, viewer.getUserNobelID());
        } else {
            holder.medalBadgeIv.setVisibility(View.GONE);
            holder.userNobleMedal.setVisibility(View.GONE);
            if (viewer.isNoble()) {
                bindNobelIcon(holder, viewer.getNobleLevel());
            } else if (ticketCount > 0) {
                holder.setTicket(getNumberDesc(ticketCount), getTicketBackground(position));
            } else if (viewer.getVipLevel() > 0) {
                bindVipLevel(holder, viewer.getVipLevel());
            } else {
                if (viewer.getCertificationType() > 0 && !viewer.isRedName()) {
                    Drawable badgeDrawable = ItemDataFormatUtils.getCertificationImgSource(viewer.getCertificationType());
                    holder.updateBadgeIv(badgeDrawable, LENGTH_DP_16, LENGTH_DP_16);
                } else {
                    holder.updateBadgeIv(viewer.isRedName() ? ItemDataFormatUtils.getSmallRedName() :
                                    ItemDataFormatUtils.getLevelSmallImgSource(viewer.getLevel()),
                            LENGTH_DP_10, LENGTH_DP_10);
                }
            }
        }

    }

    /**
     * 活动背景图
     */
    private void bindActivityIcon(UserAvatarHolder holder, String picId) {
        holder.getBadgeIv().setVisibility(View.GONE);
        holder.medalBadgeIv.setVisibility(View.VISIBLE);
        ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) holder.medalBadgeIv.getLayoutParams();
        lp.topMargin = DisplayUtils.dip2px(-12f);
        lp.leftMargin = DisplayUtils.dip2px(-14f);
        android.widget.RelativeLayout.LayoutParams params = new android.widget.RelativeLayout.LayoutParams(holder.avatarIv.getLayoutParams());
        params.leftMargin = DisplayUtils.dip2px(1.67f);
        params.topMargin = DisplayUtils.dip2px(1.67f);
        holder.avatarIv.setLayoutParams(params);
        holder.userNobleMedal.setVisibility(View.VISIBLE);
        AvatarUtils.loadPicNoLoad(holder.userNobleMedal, GetConfigManager.getInstance().getMedalUrl(picId, GetConfigManager.MEDAL_TYPE_NOBLE_TWO), false, ScalingUtils.ScaleType.FIT_XY);
        AvatarUtils.loadPicNoLoad(holder.medalBadgeIv, GetConfigManager.getInstance().getMedalUrl(picId, GetConfigManager.MEDAL_TYPE_NOBLE_THREE), false, ScalingUtils.ScaleType.FIT_XY);
    }

    /**
     * 贵族icon
     */
    private void bindNobelIcon(UserAvatarHolder holder, int nobleLevel) {
        holder.updateBadgeIv(NobleConfigUtils.getImageResoucesByNobelLevelInAvatar(nobleLevel),
                DisplayUtils.dip2px(16f), DisplayUtils.dip2px(16f));

        ImageView imageView = holder.getBadgeIv();
        ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) imageView.getLayoutParams();
        lp.topMargin = DisplayUtils.dip2px(-14f);
        lp.leftMargin = DisplayUtils.dip2px(-16f);
        //设置贵族头像金边
        if (nobleLevel > User.NOBLE_LEVEL_FOURTH) {
            android.widget.RelativeLayout.LayoutParams params = new android.widget.RelativeLayout.LayoutParams(holder.avatarIv.getLayoutParams());
            params.leftMargin = DisplayUtils.dip2px(1.67f);
            params.topMargin = DisplayUtils.dip2px(1.67f);
            holder.avatarIv.setLayoutParams(params);
            holder.nobleIv.setVisibility(View.VISIBLE);
            holder.nobleIv.setBackground(holder.nobleIv.getContext().getResources().getDrawable(NobleConfigUtils.getNobleGoldenBackgroundByNobelLevel(nobleLevel)));
        }
    }

    private void adjustLayoutNotNobel(UserAvatarHolder holder) {
        ImageView imageView = holder.getBadgeIv();
        ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) imageView.getLayoutParams();
        lp.topMargin = DisplayUtils.dip2px(-8f);
        lp.leftMargin = DisplayUtils.dip2px(-10f);
    }

    @CheckResult
    private String getNumberDesc(int ticketCount) {
        switch (LocaleUtil.getLanguageCode()) {
            case "zh_CN": {
                if (ticketCount < 1_0000) {
                    return String.valueOf(ticketCount);
                } else if (ticketCount < 99_0000) {
                    String count = String.format("%.1f", ticketCount / 1_0000.0);
                    if (count.endsWith(".0")) {
                        count = count.substring(0, count.length() - ".0".length());
                    }
                    return count + GlobalData.app().getString(R.string.ten_thousand).toString();
                } else {
                    return ticketCount / 1_0000 + GlobalData.app().getString(R.string.ten_thousand).toString();
                }
            }
            default:
                if (ticketCount < 1_000) {// [0, 1k)
                    return String.valueOf(ticketCount);
                } else if (ticketCount < 1_000_000) {// [1k, 1m)
                    return ticketCount / 1_000 + GlobalData.app().getString(R.string.thousand).toString();
                } else {// [1m, +)
                    return ticketCount / 1_000_000 + GlobalData.app().getString(R.string.million).toString();
                }
        }
    }

    @CheckResult
    @DrawableRes
    private int getTicketBackground(int position) {
        switch (position) {
            case 0:
                return R.drawable.live_grade_gifts_first;
            case 1:
                return R.drawable.live_grade_gifts_second;
            case 2:
                return R.drawable.live_grade_gifts_third;
            default:
                return R.drawable.live_grade_gifts_other;
        }
    }

    private void bindVipLevel(UserAvatarHolder holder, int level) {
        int vipLevelIconIndex = level > VipLevelUtil.MAX_LEVEL_IMAGE_NO ? VipLevelUtil.MAX_LEVEL_IMAGE_NO : level;
        String iconName = "vipicon_" + vipLevelIconIndex;
        @DrawableRes int vipLevelIconId;
        try {
            vipLevelIconId = (int) R.drawable.class.getField(iconName).get(null);
            holder.updateBadgeIv(vipLevelIconId, LENGTH_DP_12, LENGTH_DP_12);
        } catch (NoSuchFieldException e) {
            MyLog.e(TAG, "not found drawable:" + iconName, e);
        } catch (IllegalAccessException e) {
            MyLog.e(TAG, "IllegalAccess:" + iconName, e);
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

    public class UserAvatarHolder extends RecyclerView.ViewHolder {
        public BaseImageView avatarIv;
        public ImageView badgeIv;
        public TextView ticketCount;
        public ImageView crownIv;

        public ImageView nobleIv;

        public BaseImageView userNobleMedal;
        public BaseImageView medalBadgeIv;

        public UserAvatarHolder(View itemView) {
            super(itemView);
            if (mFooterBtn == itemView) {
                return;
            }
            avatarIv = (BaseImageView)itemView.findViewById(R.id.user_avatar_iv);
            badgeIv = (ImageView) itemView.findViewById(R.id.user_badge_iv);
            ticketCount =(TextView) itemView.findViewById(R.id.ticket_count);
            crownIv =(ImageView) itemView.findViewById(R.id.user_crown);
            nobleIv =(ImageView) itemView.findViewById(R.id.user_noble_golden);
            userNobleMedal =(BaseImageView) itemView.findViewById(R.id.user_noble_medal);
            medalBadgeIv = (BaseImageView) itemView.findViewById(R.id.medal_badge_iv);
        }

        public void reset() {
            badgeIv.setVisibility(View.GONE);
            ticketCount.setVisibility(View.GONE);
            nobleIv.setVisibility(View.GONE);
        }

        public void setTicket(CharSequence text, @DrawableRes int id) {
            ticketCount.setText(text);
            ticketCount.setBackgroundResource(id);
            ticketCount.setVisibility(View.VISIBLE);
        }

        public void updateBadgeIv(@Nullable Drawable drawable, int width, int height) {
            ViewGroup.LayoutParams lp = badgeIv.getLayoutParams();
            lp.height = width;
            lp.width = height;
            badgeIv.setLayoutParams(lp);
            badgeIv.setImageDrawable(drawable);
            badgeIv.setVisibility(View.VISIBLE);
        }

        public void updateBadgeIv(@DrawableRes int drawableResId, int width, int height) {
            ViewGroup.LayoutParams lp = badgeIv.getLayoutParams();
            lp.height = width;
            lp.width = height;
            badgeIv.setLayoutParams(lp);
            badgeIv.setImageResource(drawableResId);
            badgeIv.setVisibility(View.VISIBLE);
        }

        public ImageView getBadgeIv() {
            return badgeIv;
        }
    }
}
