package com.wali.live.pay.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.live.module.common.R;

/**
 * 余额界面<br/>
 *
 * @author caoxiangyu
 * @module 充值
 */
public class BalanceItemView extends RelativeLayout {
    public interface ItemType {
        int ITEM_TYPE_GOLD_GEM = 0;
        int ITEM_TYPE_SILVER_GEM = 1;
        int ITEM_TYPE_GIFT_CARD = 2;
    }
    /**{@link ItemType}里的常量值*/
    private int mItemType;
    private boolean mIsNeverExpired;
    private boolean mIsValid;
    private CharSequence mCount;
    private CharSequence mExpireDate;
    private CharSequence mLeftDay;

    public BalanceItemView(Context context) {
        this(context, null);
    }

    public BalanceItemView(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
        inflate(context, R.layout.balance_item_view, this);
        initView();
    }

    /**
     * 可用余额界面
     */
    public BalanceItemView(Context context, int itemType, boolean isNeverExpired, CharSequence count, CharSequence expireDate, CharSequence leftDay) {
        super(context);
        mItemType = itemType;
        mIsNeverExpired = isNeverExpired;
        mIsValid = true;
        mCount = count;
        mExpireDate = expireDate;
        mLeftDay = leftDay;
        inflate(context, R.layout.balance_item_view, this);
        initView();
    }

    /**
     * 过期余额界面
     */
    public BalanceItemView(Context context, int itemType, boolean isNeverExpired, CharSequence count, CharSequence expireDate) {
        super(context);
        mItemType = itemType;
        mIsNeverExpired = isNeverExpired;
        mIsValid = false;
        mCount = count;
        mExpireDate = expireDate;
        inflate(context, R.layout.balance_item_view, this);
        initView();
    }

    private void initView() {
        findViewById(R.id.never_expired).setVisibility(mIsNeverExpired ? VISIBLE : GONE);
        findViewById(R.id.will_expired).setVisibility(!mIsNeverExpired ? VISIBLE : GONE);

        ImageView itemTypeIcon = (ImageView) findViewById(mIsNeverExpired ? R.id.item_type_icon_1 : R.id.item_type_icon_2);
        switch (mItemType) {
            case ItemType.ITEM_TYPE_GOLD_GEM:
                itemTypeIcon.setImageDrawable(getResources().getDrawable(R.drawable.pay_activity_golden_diamond));
                break;
            case ItemType.ITEM_TYPE_SILVER_GEM:
                itemTypeIcon.setImageDrawable(getResources().getDrawable(R.drawable.pay_activity_silver_diamond));
                break;
            case ItemType.ITEM_TYPE_GIFT_CARD:
                itemTypeIcon.setImageDrawable(getResources().getDrawable(R.drawable.pay_activity_gift));
                break;
        }

        TextView amount = (TextView) findViewById(mIsNeverExpired ? R.id.amount_1 : R.id.amount_2);
        amount.setText(mCount);

        if (!mIsNeverExpired) {
            TextView expireDateText = (TextView) findViewById(R.id.expire_date);
            if (mIsValid) {
                expireDateText.setText(getResources().getString(R.string.recharge_will_expire_date, mExpireDate));
            } else {
                expireDateText.setText(getResources().getString(R.string.recharge_expired_date, mExpireDate));
            }
        }

        TextView status = (TextView) findViewById(R.id.status);
        if (mIsNeverExpired) {
            status.setText(R.string.foever_text);
        } else if (mIsValid) {
            status.setText(mLeftDay);
        } else {
            status.setText(R.string.recharge_already_expired_tip);
        }
    }

    public BalanceItemView setSeparatorVisibility(int visibility) {
        View v = findViewById(R.id.separator);
        if (v != null) {
            v.setVisibility(visibility);
        }
        return this;
    }

}

