package com.common.core.myinfo;

import com.common.core.db.GreenDaoManager;
import com.common.core.db.UserInfoDao;
import com.common.milink.MiLinkClientAdapter;
import com.mi.milink.sdk.aidl.PacketData;
import com.wali.live.proto.User.GetOwnInfoReq;
import com.wali.live.proto.User.GetOwnInfoRsp;

import java.util.List;

public class UserInfoServerApi {
    public static GetOwnInfoRsp getOwnInfoRsp(long uid) {
        GetOwnInfoReq request = new GetOwnInfoReq.Builder()
                .setZuid(uid)
                .build();
        PacketData data = new PacketData();
        data.setCommand("zhibo.user.getowninfo");
        data.setData(request.toByteArray());
        PacketData response = MiLinkClientAdapter.getInstance().sendSync(data, 10 * 1000);
        GetOwnInfoRsp rsp = null;
        if (response != null) {
            try {
                rsp = GetOwnInfoRsp.parseFrom(response.getData());
            } catch (Exception e) {

            }
        }
        return rsp;
    }

}
