package com.wali.live.watchsdk.endlive;

import com.wali.live.proto.RoomRecommend;

import java.util.List;

import rx.Observable;

/**
 * Created by jiyangli on 16-7-6.
 */
public interface IUserEndLiveView {
    void initData();
    void hideAvatarZone();
    void setFollowText();
    void popFragment();
    void showFirstAvatar(RoomRecommend.RecommendRoom roomData);
    void showSecondAvatar(RoomRecommend.RecommendRoom roomData);
    void showThirdAvatar(RoomRecommend.RecommendRoom roomData);
    void showFourthAvatar(RoomRecommend.RecommendRoom roomData);
    void followResult(boolean result);
    void getRoomListResult(List<RoomRecommend.RecommendRoom> result);
    <T> Observable.Transformer<T, T> bindUntilEvent();
}
