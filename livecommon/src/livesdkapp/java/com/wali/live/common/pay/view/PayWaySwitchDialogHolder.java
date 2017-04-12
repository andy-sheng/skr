package com.wali.live.common.pay.view;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
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
import com.wali.live.common.pay.constant.PayWay;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.wali.live.common.pay.constant.RechargeConfig.getPayWayInfoMap;

/**
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
            builder.setMessage(GlobalData.app().getString(R.string.select_pay_way_dialog_title));
            LayoutInflater layoutInflater = LayoutInflater.from(mContext);
            View payWayList = layoutInflater.inflate(R.layout.recharge_pay_way_list, null);
            LinearLayout payWayContainer = (LinearLayout) payWayList.findViewById(R.id.pay_way_container);
            // 根据语言和渠道添加支付方式
            for (PayWay payWay : mPayWayList) {
                addPayWay(payWayContainer, payWay);
            }
            builder.setView(payWayList);
            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (mSelectedPayWayRadio != null) {
                        for (Map.Entry<PayWay, ImageView> entry : mPayWayRadioMap.entrySet()) {
                            if (entry.getValue() == mSelectedPayWayRadio) {
                                if (mPayWaySwitchListener != null) {
                                    mPayWaySwitchListener.onPayWaySwitched(entry.getKey());
                                }
                                dialog.dismiss();
                                return;
                            }
                        }
                        MyLog.e(TAG, "unexpected selected payway");
                    } else {
                        MyLog.e(TAG, "no pay way was selected, please check radio logic");
                    }
                }
            });
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

    private void addPayWay(@NonNull LinearLayout payWayContainer, @NonNull PayWay payWay) {
        Drawable icon = mContext.getResources().getDrawable(getPayWayInfoMap().get(payWay).mIconId);
        icon.setBounds(0, 0, icon.getMinimumWidth(), icon.getMinimumHeight());
        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        View payWayItem = layoutInflater.inflate(R.layout.recharge_pay_way_item_with_radio, null);
        TextView payWayTv = (TextView) payWayItem.findViewById(R.id.pay_way);
        final ImageView radio = (ImageView) payWayItem.findViewById(R.id.radio1);
        payWayTv.setCompoundDrawables(icon, null, null, null);
        payWayTv.setText(getPayWayInfoMap().get(payWay).mNameId);
        payWayItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performItemClicked(radio);
            }
        });
        //radio.setOnClickListener(v -> performItemClicked(radio));
        mPayWayRadioMap.put(payWay, radio);
        payWayContainer.addView(payWayItem);
    }

    /**
     * 必须选择且仅能选择一项
     */
    private void performItemClicked(ImageView radio) {
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
