package com.wali.live.recharge.util;

import android.support.annotation.NonNull;

import com.base.global.GlobalData;
import com.live.module.common.R;

/**
 * Created by rongzhisheng on 16-12-23.
 */

public class RechargeUtil {
    @NonNull
    public static CharSequence getErrorMsgByErrorCode(int errorCode) {
        return GlobalData.app().getString(R.string.pay_error_code_cancel);
    }
}
