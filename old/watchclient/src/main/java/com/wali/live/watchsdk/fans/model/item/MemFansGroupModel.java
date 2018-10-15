package com.wali.live.watchsdk.fans.model.item;

import com.wali.live.proto.VFansProto;
import com.wali.live.watchsdk.fans.model.FansGroupDetailModel;
import com.wali.live.watchsdk.fans.model.type.BaseTypeModel;

/**
 * Created by lan on 2017/11/8.
 */
public class MemFansGroupModel extends BaseTypeModel {
    private long mZuid;

    private String mGroupName;      // 宠爱团名称
    private int mPetExp;            // 宠爱值
    private int mPetLevel;          // 宠爱等级
    private long mJoinTime;         // 加入时间
    private long mVipExpire;        // 会员过期时间
    private int mVipLevel;          // vip 等级, 只有当vip_expire大于当前时间才有效
    private String mMedalValue;     // 宠爱等级对应的勋章

    private String mZuidNickname;   // 创建者昵称
    private long mZuidAvatar;       // 创建者头像

    private FansGroupDetailModel mDetailModel;

    public MemFansGroupModel(VFansProto.MemGroupInfo protoMemGroup) {
        parse(protoMemGroup);
    }

    public void parse(VFansProto.MemGroupInfo protoMemGroup) {
        mZuid = protoMemGroup.getZuid();

        mGroupName = protoMemGroup.getGroupName();
        mPetExp = protoMemGroup.getPetExp();
        mPetLevel = protoMemGroup.getPetLevel();
        mJoinTime = protoMemGroup.getJoinTime();
        mVipExpire = protoMemGroup.getVipExpire();
        mVipLevel = protoMemGroup.getVipLevel();
        mMedalValue = protoMemGroup.getMedalValue();

        mZuidNickname = protoMemGroup.getZuidNickname();
        mZuidAvatar = protoMemGroup.getZuidAvatar();
    }

    public long getZuid() {
        return mZuid;
    }

    public String getGroupName() {
        return mGroupName;
    }

    public int getPetExp() {
        return mPetExp;
    }

    public int getPetLevel() {
        return mPetLevel;
    }

    public long getJoinTime() {
        return mJoinTime;
    }

    public long getVipExpire() {
        return mVipExpire;
    }

    public int getVipLevel() {
        return mVipLevel;
    }

    public String getMedalValue() {
        return mMedalValue;
    }

    public String getZuidNickname() {
        return mZuidNickname;
    }

    public long getZuidAvatar() {
        return mZuidAvatar;
    }

    public FansGroupDetailModel getDetailModel() {
        if (mDetailModel == null) {
            mDetailModel = new FansGroupDetailModel(this);
        }
        return mDetailModel;
    }

    @Override
    protected int defaultType() {
        return ViewType.TYPE_MEM_GROUP;
    }
}
