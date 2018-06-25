package com.wali.live.watchsdk.personalcenter.view;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.base.image.fresco.BaseImageView;
import com.mi.live.data.account.MyUserInfoManager;
import com.wali.live.pay.model.Diamond;
import com.wali.live.pay.view.IRechargeView;
import com.wali.live.recharge.adapter.RechargeRecyclerViewAdapter;
import com.wali.live.recharge.config.RechargeConfig;
import com.wali.live.recharge.presenter.RechargePresenter;
import com.wali.live.utils.AvatarUtils;
import com.wali.live.watchsdk.R;

import java.util.List;

import rx.Observable;

public class MyInfoBalanceView extends RelativeLayout {
    public final static String TAG = "MyInfoBalanceView";

    private TextView mTotalBalanceTv;

    public MyInfoBalanceView(Context context) {
        super(context);
        init(context);
    }


    private void init(Context context) {
        inflate(context, R.layout.my_info_half_balance_layout, this);
        mTotalBalanceTv = (TextView) this.findViewById(R.id.total_balance_tv);
        bindData();
    }

    private void bindData() {
        int t = MyUserInfoManager.getInstance().getDiamondNum() + MyUserInfoManager.getInstance().getVirtualDiamondNum();
        mTotalBalanceTv.setText("" + t);
    }

}