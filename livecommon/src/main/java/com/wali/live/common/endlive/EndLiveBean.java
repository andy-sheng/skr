package com.wali.live.common.endlive;

import android.os.Bundle;

import com.mi.live.data.account.MyUserInfoManager;
import com.mi.live.data.api.LiveManager;
import com.mi.live.data.user.User;

/**
 * Created by jiyangli on 16-7-6.
 */
public class EndLiveBean implements IEndLiveModel {
    //主播ID
    private long mOwnerId;
    //房间号
    private String mRoomId;
    //时间戳
    private long mAvatarTs;
    //主播信息
    private User mOwner;
    //从哪进入
    private int mFrom;

    private int mViewerCnt;
    private long uuid;
    private int mLiveType;

    public EndLiveBean() {
    }

    @Override
    public void initData(Bundle bundle) {
        mOwnerId = bundle.getLong(UserEndLiveFragment.EXTRA_OWNER_ID, -1);
        mRoomId = bundle.getString(UserEndLiveFragment.EXTRA_ROOM_ID, "");
        mAvatarTs = bundle.getLong(UserEndLiveFragment.EXTRA_AVATAR_TS, 0);
        mFrom = bundle.getInt(UserEndLiveFragment.EXTRA_FROM, 0);
        mOwner = (User) bundle.getSerializable(UserEndLiveFragment.EXTRA_OWNER);
        mViewerCnt = bundle.getInt(UserEndLiveFragment.EXTRA_VIEWER);
        uuid = MyUserInfoManager.getInstance().getUser().getUid();
        mLiveType = bundle.getInt(UserEndLiveFragment.EXTRA_LIVE_TYPE, 0);
    }

    @Override
    public long getOwnerId() {
        return mOwnerId;
    }

    @Override
    public long getAvaTarTs() {
        return mAvatarTs;
    }

    @Override
    public long getUuid() {
        return uuid;
    }

    @Override
    public int getViewerCount() {
        return mViewerCnt;
    }

    @Override
    public int getOwnerCertType() {
        return mOwner.getCertificationType();
    }

    @Override
    public String getRoomId() {
        return mRoomId;
    }

    @Override
    public String getNickName() {
        return mOwner.getNickname();
    }

    @Override
    public boolean isFocused() {
        return mOwner.isFocused();
    }

    @Override
    public int getLiveType() {
        return mLiveType;
    }

    @Override
    public long getZuid() {
        return mOwner.getUid();
    }

}
