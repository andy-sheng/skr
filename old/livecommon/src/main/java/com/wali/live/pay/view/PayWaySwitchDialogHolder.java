package com.wali.live.pay.view;

import android.content.Context;
import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.base.dialog.MyAlertDialog;
import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.live.module.common.R;
import com.wali.live.pay.constant.PayWay;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 获取切换支付方式对话框的类
 * Created by rongzhisheng on 16-11-12.
 */

public class PayWaySwitchDialogHolder {
    private static final String TAG = PayWaySwitchDialogHolder.class.getSimpleName();

    private Context mContext;
    private List<PayWay> mPayWayList;
    /**
     * 支付方式列表变动时，置为null
     */
    private MyAlertDialog mSelectPayWayDialog;
    private ImageView mSelectedPayWayRadio;
    /**
     * 支付方式列表变动时，clear
     */
    private Map<PayWay, ImageView> mPayWayRadioMap = new HashMap<>();
    private IPayWaySwitchListener mPayWaySwitchListener;

    public PayWaySwitchDialogHolder(@NonNull Context context, @NonNull List<PayWay> payWayList, @NonNull IPayWaySwitchListener payWaySwitchListener) {
        mContext = context;
        mPayWayList = payWayList;
        mPayWaySwitchListener = payWaySwitchListener;
    }

    @NonNull
    @CheckResult
    public MyAlertDialog getSelectPayWayDialog(PayWay selectedPayWay) {
        if (mSelectPayWayDialog == null) {
            MyAlertDialog.Builder builder = new MyAlertDialog.Builder(mContext);
            builder.setTitle(R.string.select_pay_way_dialog_title);
            LayoutInflater layoutInflater = LayoutInflater.from(mContext);
            View payWayList = layoutInflater.inflate(R.layout.recharge_pay_way_switch_dialog, null);
            LinearLayout payWayContainer = (LinearLayout) payWayList.findViewById(R.id.pay_way_container);
            // 根据语言和渠道添加支付方式
            final int size = mPayWayList.size();
            for (int i = 0; i < size; i++) {
                addPayWay(payWayContainer, mPayWayList.get(i), i == size - 1);
            }
            builder.setView(payWayList);
            //builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            //    @Override
            //    public void onClick(DialogInterface dialog, int which) {
            //
            //    }
            //});
            mSelectPayWayDialog = builder.create();
        }
        // 初始化mSelectedPayWay，设置默认选中
        for (Map.Entry<PayWay, ImageView> entry : mPayWayRadioMap.entrySet()) {
            if (selectedPayWay == entry.getKey()) {
                mSelectedPayWayRadio = entry.getValue();
                toggleCurrentSelection(true);
            } else {
                entry.getValue().setVisibility(View.GONE);
            }
        }
        return mSelectPayWayDialog;
    }

    private void onItemClick(View item) {
        if (mSelectedPayWayRadio != null) {
            for (Map.Entry<PayWay, ImageView> entry : mPayWayRadioMap.entrySet()) {
                if (entry.getValue() == mSelectedPayWayRadio) {
                    if (mPayWaySwitchListener != null) {
                        mPayWaySwitchListener.onPayWaySwitched(entry.getKey());
                    }
                    if (mSelectPayWayDialog != null) {
                        mSelectPayWayDialog.dismiss();
                    }
                    return;
                }
            }
            MyLog.e(TAG, "unexpected selected payway");
        } else {
            MyLog.e(TAG, "no pay way was selected, please check radio logic");
        }
    }

    private void addPayWay(@NonNull LinearLayout payWayContainer, @NonNull PayWay payWay, boolean isLast) {
        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        final View payWayItem = layoutInflater.inflate(R.layout.recharge_pay_way_item_switch_dialog, payWayContainer, false);
        if (isLast) {
            payWayItem.setBackground(GlobalData.app().getResources().getDrawable(R.drawable.recharge_pay_way_switch_dialog_last_list_item_bg));
        }

        ImageView payWayIv = (ImageView) payWayItem.findViewById(R.id.pay_way_iv);
        final ImageView radio = (ImageView) payWayItem.findViewById(R.id.radio1);
        final TextView payWayNameTv = (TextView) payWayItem.findViewById(R.id.pay_way_name);

        payWayIv.setImageDrawable(GlobalData.app().getResources().getDrawable(payWay.getIcon()));
        payWayNameTv.setText(payWay.getName());
        payWayItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setCheckedView(radio);
                onItemClick(payWayItem);
            }
        });

        mPayWayRadioMap.put(payWay, radio);
        payWayContainer.addView(payWayItem);
    }

    /**
     * 必须选择且仅能选择一项
     */
    private void setCheckedView(ImageView radio) {
        if (mSelectedPayWayRadio == radio) {
            return;
        }
        toggleCurrentSelection(false);
        mSelectedPayWayRadio = radio;
        toggleCurrentSelection(true);
    }

    private void toggleCurrentSelection(boolean show) {
        if (mSelectedPayWayRadio != null) {
            mSelectedPayWayRadio.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    public interface IPayWaySwitchListener {
        /**
         * 用户主动切换支付方式
         * @param payWay
         */
        void onPayWaySwitched(PayWay payWay);
    }

}
