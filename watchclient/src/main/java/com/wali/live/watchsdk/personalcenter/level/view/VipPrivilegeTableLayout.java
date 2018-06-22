package com.wali.live.watchsdk.personalcenter.level.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.DrawableRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.base.image.fresco.FrescoWorker;
import com.base.image.fresco.image.BaseImage;
import com.base.image.fresco.image.ImageFactory;
import com.base.image.fresco.processor.GrayPostprocessor;
import com.base.log.MyLog;
import com.base.utils.CommonUtils;
import com.base.utils.display.DisplayUtils;
import com.facebook.drawee.view.SimpleDraweeView;
import com.jakewharton.rxbinding.view.RxView;
import com.mi.live.data.config.GetConfigManager;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.personalcenter.level.model.UserVipInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.functions.Action1;

/**
 * 每列宽度等分的表格
 * Created by rongzhisheng on 17-4-27.
 */

public class VipPrivilegeTableLayout extends LinearLayout {
    private static final String TAG = VipPrivilegeTableLayout.class.getSimpleName();
    private static final int LOCAL_MAX_VIP_PRIVILEGE_TYPE = 10;// 本地有的type最高的VIP特权图标

    private int mColumnCount;
    private int mRowCount;
    private float mRowSpacing;
    private List<UserVipInfo.VipPrivilege> mVipPrivilegeList = new ArrayList<>();
    private Context mContext;
    private int mItemLayoutId;
    private LayoutInflater mLayoutInflater;
    private OnVipPrivilegeItemClickListener mOnVipPrivilegeItemClickListener;

    public interface OnVipPrivilegeItemClickListener {
        void onVipPrivilegeItemClick(int type);
    }

    public VipPrivilegeTableLayout(Context context) {
        this(context, null);
    }

    public VipPrivilegeTableLayout(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VipPrivilegeTableLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setOrientation(VERTICAL);
        mContext = context;
        mLayoutInflater = LayoutInflater.from(context);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.VipPrivilegeTableLayout);
        try {
            mColumnCount = typedArray.getInt(R.styleable.VipPrivilegeTableLayout_column, 0);
            float dimension = typedArray.getDimension(R.styleable.VipPrivilegeTableLayout_rowSpacing, 0);
            MyLog.d(TAG, "dimension:" + dimension);
            mRowSpacing = DisplayUtils.dip2px(dimension);
        } finally {
            typedArray.recycle();
        }
    }

    /**
     * 在{@linkplain #setVipPrivilegeList(List, int)}之前调用
     * @param onVipPrivilegeItemClickListener
     */
    public void setOnVipPrivilegeItemClickListener(OnVipPrivilegeItemClickListener onVipPrivilegeItemClickListener) {
        mOnVipPrivilegeItemClickListener = onVipPrivilegeItemClickListener;
    }

    public void setVipPrivilegeList(@NonNull List<UserVipInfo.VipPrivilege> vipPrivilegeList, @LayoutRes int itemLayoutId) {
        mVipPrivilegeList.clear();
        mVipPrivilegeList.addAll(vipPrivilegeList);
        mItemLayoutId = itemLayoutId;
        mRowCount = getRowCount();
        init();
    }

    private void init() {
        for (int i = 0; i < mVipPrivilegeList.size(); i++) {
            int currentRowIndex = i / mColumnCount;
            int currentColumnIndex = i % mColumnCount;
            if (currentColumnIndex == 0) {
                addView(getRowView(currentRowIndex));
            }
            PrivilegeHolder holder = new PrivilegeHolder(((ViewGroup) getChildAt(currentRowIndex))
                    .getChildAt(currentColumnIndex));
            bindData(holder, i);
        }
    }

    private int getRowCount() {
        int privilegeNumber = mVipPrivilegeList.size();
        int rowCount = privilegeNumber / mColumnCount;
        if (privilegeNumber % mColumnCount != 0) {
            rowCount++;
        }
        return rowCount;
    }

    private View getRowView(int rowIndex) {
        LinearLayout linearLayout = new LinearLayout(mContext);
        linearLayout.setOrientation(HORIZONTAL);
        for (int i = 0; i < mColumnCount; i++) {
            View item = mLayoutInflater.inflate(mItemLayoutId, this, false);
            LayoutParams lp = new LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.weight = 1;
            lp.gravity = Gravity.CENTER_HORIZONTAL;
            linearLayout.addView(item, lp);
        }
        if (rowIndex < mRowCount - 1) {//不是最后一行
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.bottomMargin = (int) mRowSpacing;
            linearLayout.setLayoutParams(layoutParams);
        }
        return linearLayout;
    }

    private void bindData(@NonNull PrivilegeHolder holder, int position) {
        final UserVipInfo.VipPrivilege vipPrivilege = mVipPrivilegeList.get(position);
        holder.mPrivilegeName.setText(vipPrivilege.getName());
        boolean isMore = vipPrivilege.getType() == UserVipInfo.VipPrivilege.TYPE_MORE;
        RxView.clicks(holder.mItemView).throttleFirst(2, TimeUnit.SECONDS).subscribe(new Action1<Void>() {
            @Override
            public void call(Void aVoid) {
                if (mOnVipPrivilegeItemClickListener != null) {
                    mOnVipPrivilegeItemClickListener.onVipPrivilegeItemClick(vipPrivilege.getType());
                }
            }
        });
        if (isMore) {
            holder.mUnlockLevel.setText("");
            holder.mPrivilegeName.setTextColor(CommonUtils.getResources().getColor(R.color.color_c8c8c8));
        } else {
            holder.mUnlockLevel.setText(CommonUtils.getString(R.string.vip_privilege_unlock_level, vipPrivilege.getUnlockLevel()));
            holder.mPrivilegeName.setTextColor(CommonUtils.getResources().getColor(R.color.color_black));
        }
        BaseImage badge = getVipPrivilegeBadgeImage(vipPrivilege.getType(), vipPrivilege.isGained());
        FrescoWorker.loadImage(holder.mPrivilegeBadge, badge);
    }

    private BaseImage getVipPrivilegeBadgeImage(int type, boolean gained) {
        BaseImage baseImage;
        if (type <= LOCAL_MAX_VIP_PRIVILEGE_TYPE) {
            @DrawableRes int privilegeBadgeId;
            if (gained) {
                switch (type) {
                    case 0: privilegeBadgeId = R.drawable.vip_icon_more_disable; break;
                    case 1: privilegeBadgeId = R.drawable.vip_icon_badge_normal; break;
                    case 2: privilegeBadgeId = R.drawable.vip_icon_flying_normal; break;
                    case 3: privilegeBadgeId = R.drawable.vip_icon_enter_normal; break;
                    case 4: privilegeBadgeId = R.drawable.vip_icon_enter_1_normal; break;
                    case 5: privilegeBadgeId = R.drawable.vip_icon_enter_2_normal; break;
                    case 6: privilegeBadgeId = R.drawable.vip_icon_hide_normal; break;
                    case 7: privilegeBadgeId = R.drawable.vip_icon_recharge_normal; break;
                    case 8: privilegeBadgeId = R.drawable.vip_icon_welcome_2_normal; break;
                    case 9: privilegeBadgeId = R.drawable.vip_icon_support_2_normal; break;
                    case 10: privilegeBadgeId = R.drawable.vip_icon_gift_2_normal; break;
                    default: privilegeBadgeId = R.drawable.vip_icon_more_disable; break;
                }
            } else {
                switch (type) {
                    case 1: privilegeBadgeId = R.drawable.vip_icon_badge_disable; break;
                    case 2: privilegeBadgeId = R.drawable.vip_icon_flying_disable; break;
                    case 3: privilegeBadgeId = R.drawable.vip_icon_enter_disable; break;
                    case 4: privilegeBadgeId = R.drawable.vip_icon_enter_1_disable; break;
                    case 5: privilegeBadgeId = R.drawable.vip_icon_enter_2_disable; break;
                    case 6: privilegeBadgeId = R.drawable.vip_icon_hide_disable; break;
                    case 7: privilegeBadgeId = R.drawable.vip_icon_recharge_disable; break;
                    case 8: privilegeBadgeId = R.drawable.vip_icon_welcome_2_disable; break;
                    case 9: privilegeBadgeId = R.drawable.vip_icon_support_2_disable; break;
                    case 10: privilegeBadgeId = R.drawable.vip_icon_gift_2_disable; break;
                    default: privilegeBadgeId = R.drawable.vip_icon_more_disable; break;
                }
            }
            baseImage = ImageFactory.newResImage(privilegeBadgeId).setWidth(213).setHeight(196).build();
        } else {
            baseImage = ImageFactory.newHttpImage(GetConfigManager.getInstance().getVipLevelIconUrlPrefix() + "pr_" + type).
                    setWidth(213).setHeight(196).build();
            baseImage.setFailureDrawable(CommonUtils.getDrawable(R.drawable.vip_icon_more_disable));
            if (!gained) {
                baseImage.setPostprocessor(new GrayPostprocessor());
            }
        }
        return baseImage;
    }

    private static class PrivilegeHolder {
        private View mItemView;
        private SimpleDraweeView mPrivilegeBadge;
        private TextView mPrivilegeName;
        private TextView mUnlockLevel;

        PrivilegeHolder(@NonNull View view) {
            mItemView = view;
            mPrivilegeBadge = (SimpleDraweeView) view.findViewById(R.id.badge);
            mPrivilegeName = (TextView) view.findViewById(R.id.name);
            mUnlockLevel = (TextView) view.findViewById(R.id.unlock_level);
        }
    }

}
