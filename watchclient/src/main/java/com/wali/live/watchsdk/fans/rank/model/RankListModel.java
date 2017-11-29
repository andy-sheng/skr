package com.wali.live.watchsdk.fans.rank.model;

import com.wali.live.proto.VFansProto;
import com.wali.live.watchsdk.fans.rank.data.RankFansData;
import com.wali.live.watchsdk.lit.recycler.viewmodel.BaseViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhaomin on 17-6-14.
 */
public class RankListModel extends BaseViewModel {
    private int nextStart;
    private boolean hasMore;
    private List<RankFansData> rankDataList;
    private RankFansData myRankData;

    public RankListModel(VFansProto.MemberListRsp rsp) {
        nextStart = rsp.getNextStart();
        hasMore = rsp.getHasMore();
        rankDataList = new ArrayList<>();
        for (VFansProto.MemberInfo info : rsp.getMemListList()) {
            rankDataList.add(new RankFansData(info));
        }
        if (rsp.hasRanking() && rsp.hasPetExp()) {
            myRankData = new RankFansData(rsp.getPetExp(), rsp.getPetLevel(), rsp.getMedalValue(), rsp.getRanking(), rsp.getCatchUpExp(), "");
        }
    }

    public RankListModel(VFansProto.GroupRankListRsp rsp) {
        nextStart = rsp.getNextStart();
        hasMore = rsp.getHasMore();
        rankDataList = new ArrayList<>();
        for (VFansProto.GroupRankInfo info : rsp.getGroupListList()) {
            rankDataList.add(new RankFansData(info));
        }
        if (rsp.hasRanking() && rsp.hasCharmExp()) {
            myRankData = new RankFansData(rsp.getCharmExp(), rsp.getCharmLevel(), rsp.getCharmTitle(), rsp.getRanking(), rsp.getCatchUpExp(), rsp.getGroupName());
        }
    }

    public int getNextStart() {
        return nextStart;
    }

    public void setNextStart(int nextStart) {
        this.nextStart = nextStart;
    }

    public boolean isHasMore() {
        return hasMore;
    }

    public void setHasMore(boolean hasMore) {
        this.hasMore = hasMore;
    }

    public List<RankFansData> getRankDataList() {
        return rankDataList;
    }

    public void setRankDataList(List<RankFansData> rankDataList) {
        this.rankDataList = rankDataList;
    }

    public RankFansData getMyRankData() {
        return myRankData;
    }

    public void setMyRankData(RankFansData myRankData) {
        this.myRankData = myRankData;
    }
}
