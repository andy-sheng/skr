package com.wali.live.watchsdk.contest.rank.model;

import com.wali.live.proto.LiveSummitProto;
import com.wali.live.watchsdk.lit.recycler.viewmodel.BaseViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lan on 2018/1/11.
 */
public class ContestRankModel extends BaseViewModel {
    private List<ContestRankItemModel> mItemList;

    public ContestRankModel(List<LiveSummitProto.RankItem> protoList) {
        parse(protoList);
    }

    public void parse(List<LiveSummitProto.RankItem> protoList) {
        if (mItemList == null) {
            mItemList = new ArrayList();
        }
        for (int i = 0; i < protoList.size(); i++) {
            mItemList.add(new ContestRankItemModel(protoList.get(i), (i + 1)));
        }
    }

    public List<ContestRankItemModel> getItemList() {
        return mItemList;
    }
}
