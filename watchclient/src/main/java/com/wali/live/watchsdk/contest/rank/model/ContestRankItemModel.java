
package com.wali.live.watchsdk.contest.rank.model;

import com.mi.live.data.account.MyUserInfoManager;
import com.mi.live.data.user.User;
import com.wali.live.proto.LiveSummitProto;
import com.wali.live.watchsdk.lit.recycler.viewmodel.BaseViewModel;

public class ContestRankItemModel extends BaseViewModel {
    private int mIndex;

    private User mUser;
    private float mBonus;       //个人本场奖金

    /**
     * 私有构造函数，用来测试
     */
    private ContestRankItemModel(int index) {
        mIndex = index;
        mUser = MyUserInfoManager.getInstance().getUser();
        mBonus = index + 1;
    }

    public ContestRankItemModel(LiveSummitProto.RankItem protoItem, int index) {
        parse(protoItem);
        mIndex = index;
    }

    public void parse(LiveSummitProto.RankItem protoItem) {
        mUser = new User(protoItem.getUserInfo());
        mBonus = protoItem.getBonus();
    }

    public int getIndex() {
        return mIndex;
    }

    public User getUser() {
        return mUser;
    }

    public float getMyBonus() {
        return mBonus;
    }

    public static ContestRankItemModel newTestInstance(int index) {
        return new ContestRankItemModel(index);
    }
}