package com.mi.live.data.repository;

import com.mi.live.data.repository.datasource.LiveShowStore;
import com.wali.live.proto.LiveShowProto;

import javax.inject.Inject;

import rx.Observable;

/**
 * Created by chengsimin on 16/6/13.
 */
public class LiveShowRepository {

    private final LiveShowStore liveShowStore;

    @Inject
    public LiveShowRepository(LiveShowStore liveShowStore) {
        this.liveShowStore = liveShowStore;
    }

    public Observable<LiveShowProto.GetTopicLiveRsp> getLiveListOfTopicc(final String topic, final int pageCount) {
        return this.liveShowStore.getLiveListOfTopic(topic, pageCount);
    }
}
