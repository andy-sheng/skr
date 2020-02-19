package com.module.playways.grab.room.invite;

import com.common.core.userinfo.model.UserInfoModel;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiResult;
import com.component.dialog.InviteFriendDialog;

import io.reactivex.Observable;

public interface IInviteCallBack extends InviteFriendDialog.IInviteDialogCallBack {
    //在朋友圈，qq空间，qq好友，微信好友的邀请title
    String getShareTitle();

    //在朋友圈，qq空间，qq好友，微信好友的邀请des
    String getShareDes();

    //邀请接口
    Observable<ApiResult> getInviteObservable(UserInfoModel model);

    int getRoomID();

    //也是gameType
    int getFrom();
}
