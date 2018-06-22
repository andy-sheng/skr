package com.wali.live.watchsdk.personalcenter.relation.contact;

import com.mi.live.data.data.UserListData;

import java.util.List;

/**
 * Created by zhujianning on 18-6-21.
 */

public class FansListContact {
    public interface Iview {
        void loadFansListSuccess(List<UserListData> dataList);
    }

    public interface Ipresenter {
        void loadFansList(long uuid, int offset);
    }
}
