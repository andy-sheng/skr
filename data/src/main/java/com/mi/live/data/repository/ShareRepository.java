package com.mi.live.data.repository;

import com.mi.live.data.repository.datasource.ShareStore;
import com.wali.live.proto.ShareProto;

import java.util.List;

import rx.Observable;

/**
 * Created by zhangyuehuan on 16/7/13.
 */
public class ShareRepository {

    private final ShareStore shareStore;

    public ShareRepository() {
        this.shareStore = new ShareStore();
    }

    public Observable<ShareProto.GetShareTagTailRsp> getTagTailForShare(final long uuid,
                                                                        final ShareProto.RoleType roleType,
                                                                        final List<ShareProto.ChannelType> channelType, final ShareProto.PeriodType periodType) {
        return this.shareStore.getTagTailForShare(uuid, roleType, channelType, periodType);
    }
}
