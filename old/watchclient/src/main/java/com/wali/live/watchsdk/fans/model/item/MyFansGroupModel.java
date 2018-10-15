package com.wali.live.watchsdk.fans.model.item;

import com.wali.live.proto.VFansProto;
import com.wali.live.watchsdk.fans.model.type.BaseTypeModel;

/**
 * Created by lan on 2017/11/8.
 */
public class MyFansGroupModel extends BaseTypeModel {
    private String mGroupName;
    private long mCreateTime;       // unix时间戳 秒
    private int mCharmExp;          // 主播魅力值榜
    private String mCharmTitle;     // 魅力值称号
    private int mRanking;           // 魅力值排名
    private int mCharmLevel;        // 魅力值等级

    public MyFansGroupModel(VFansProto.MyGroupInfo protoMyGroup) {
        parse(protoMyGroup);
    }

    public void parse(VFansProto.MyGroupInfo protoMyGroup) {
        mGroupName = protoMyGroup.getGroupName();
        mCreateTime = protoMyGroup.getCreateTime();
        mCharmExp = protoMyGroup.getCharmExp();
        mCharmTitle = protoMyGroup.getCharmTitle();
        mRanking = protoMyGroup.getRanking();
        mCharmLevel = protoMyGroup.getCharmLevel();
    }

    public String getGroupName() {
        return mGroupName;
    }

    public long getCreateTime() {
        return mCreateTime;
    }

    public int getCharmExp() {
        return mCharmExp;
    }

    public String getCharmTitle() {
        return mCharmTitle;
    }

    public int getRanking() {
        return mRanking;
    }

    public int getCharmLevel() {
        return mCharmLevel;
    }

    @Override
    protected int defaultType() {
        return ViewType.TYPE_MY_GROUP;
    }
}
