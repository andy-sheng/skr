package com.wali.live.watchsdk.personalcenter.relation.contact;

import com.mi.live.data.data.UserListData;

import java.util.List;

/**
 * Created by zhujianning on 18-6-21.
 */

public class FollowListContact {
    public interface Iview {
        void loadFollowListSuccess(List<UserListData> datas);
    }

    public interface Ipresenter {
        void loadFollowList(long uid);
    }
}
