package com.mi.liveassistant.data;

import android.text.TextUtils;

import com.mi.liveassistant.common.global.GlobalData;
import com.mi.liveassistant.proto.UserProto;

import java.io.Serializable;

/**
 * Created by yurui on 2/23/16.
 */
public class BusinessInfo implements Serializable {

    public String[] mTelList;
    public String mAddress; //位置
    public String mBusinessHours; //营业时间
    public String mIntro; //简介

    public BusinessInfo(UserProto.BusinessUserInfo userInfo) {
        mAddress = TextUtils.isEmpty(userInfo.getAddress()) ? "" : userInfo.getAddress();
//        mBusinessHours = TextUtils.isEmpty(userInfo.getBusinessHours()) ? "" : (GlobalData.app().getResources().getString(R.string.shop_open_time) + userInfo.getBusinessHours());
//        mIntro = TextUtils.isEmpty(userInfo.getIntro()) ? "" : (GlobalData.app().getResources().getString(R.string.shop_intro) + userInfo.getIntro());
        if (userInfo.getServicePhoneList().size() > 0) {
            mTelList = new String[userInfo.getServicePhoneList().size()];
            for (int i = 0; i < mTelList.length; i++) {
                mTelList[i] = userInfo.getServicePhoneList().get(i);
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[")
                .append("mAddress == " + mAddress)
                .append("]");
        return super.toString();
    }
}
