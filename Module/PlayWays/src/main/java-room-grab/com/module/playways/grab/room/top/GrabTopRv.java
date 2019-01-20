package com.module.playways.grab.room.top;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.common.core.myinfo.MyUserInfoManager;
import com.common.core.userinfo.model.UserInfoModel;
import com.common.view.ex.ExLinearLayout;
import com.module.playways.RoomData;
import com.module.playways.rank.prepare.model.PlayerInfoModel;
import com.module.playways.rank.prepare.model.RoundInfoModel;
import com.module.rank.R;
import com.zq.live.proto.Common.UserInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GrabTopRv extends ExLinearLayout {
    private HashMap<Integer, GrabTopItemView> mInfoMap = new HashMap<>();
    private RoomData mRoomData;

    public GrabTopRv(Context context) {
        super(context);
    }

    public GrabTopRv(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public GrabTopRv(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setModeGrab() {
        // 切换到抢唱模式,
        RoundInfoModel now = mRoomData.getRealRoundInfo();
        List<PlayerInfoModel> playerInfoModels = mRoomData.getPlayerInfoList();
        for (PlayerInfoModel playerInfoModel : playerInfoModels) {
            UserInfoModel userInfo = playerInfoModel.getUserInfo();
            GrabTopItemView grabTopItemView = mInfoMap.get(userInfo.getUserId());
            if (grabTopItemView == null) {
                grabTopItemView = new GrabTopItemView(getContext());
                mInfoMap.put(userInfo.getUserId(), grabTopItemView);
            }
            grabTopItemView.setVisibility(VISIBLE);
            grabTopItemView.bindData(userInfo);
            grabTopItemView.setGrap(false);
            grabTopItemView.tryAddParent(this);
        }
        if (now != null) {
            for (int uid : now.getHasGrabUserSet()) {
                GrabTopItemView grabTopItemView = mInfoMap.get(uid);
                if (grabTopItemView != null) {
                    grabTopItemView.setGrap(true);
                }
            }
        }
    }

    public void setModeSing(long singUid) {
        // 切换到抢唱模式,
        RoundInfoModel now = mRoomData.getRealRoundInfo();
        List<PlayerInfoModel> playerInfoModels = mRoomData.getPlayerInfoList();
        for (PlayerInfoModel playerInfoModel : playerInfoModels) {
            UserInfoModel userInfo = playerInfoModel.getUserInfo();
            GrabTopItemView grabTopItemView = mInfoMap.get(userInfo.getUserId());
            if (grabTopItemView == null) {
                grabTopItemView = new GrabTopItemView(getContext());
                mInfoMap.put(userInfo.getUserId(), grabTopItemView);
            }
            if (singUid == userInfo.getUserId()) {
                grabTopItemView.setVisibility(GONE);
            } else {
                grabTopItemView.setVisibility(VISIBLE);
            }
            grabTopItemView.bindData(userInfo);
            grabTopItemView.setLight(true);
            grabTopItemView.tryAddParent(this);
        }

        if (now != null) {
            for (int uid : now.getHasLightOffUserSet()) {
                GrabTopItemView grabTopItemView = mInfoMap.get(uid);
                if (grabTopItemView != null) {
                    grabTopItemView.setLight(false);
                }
            }
        }
    }

    public void grap(int uid) {
        GrabTopItemView grabTopItemView = mInfoMap.get(uid);
        if (grabTopItemView != null) {
            grabTopItemView.setGrap(true);
        }
    }

    public void lightOff(int uid) {
        GrabTopItemView grabTopItemView = mInfoMap.get(uid);
        if (grabTopItemView != null) {
            grabTopItemView.setLight(false);
        }
    }


    public void setRoomData(RoomData roomData) {
        mRoomData = roomData;
    }


}
