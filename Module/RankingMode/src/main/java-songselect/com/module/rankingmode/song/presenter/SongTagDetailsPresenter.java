package com.module.rankingmode.song.presenter;

import com.alibaba.fastjson.JSON;
import com.common.mvp.RxLifeCyclePresenter;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.module.rankingmode.song.SongSelectServerApi;
import com.module.rankingmode.song.model.SongModel;
import com.module.rankingmode.song.view.ISongTagDetailView;

import java.util.List;

public class SongTagDetailsPresenter extends RxLifeCyclePresenter {

    ISongTagDetailView view;

    public SongTagDetailsPresenter(ISongTagDetailView view) {
        this.view = view;
        addToLifeCycle();
    }

    /**
     * 获取曲库剧本的详细条目
     *
     * @param tag    曲库的剧本标签id
     * @param offset 偏移量
     * @param cnt    一页的数量
     * @return
     */
    public void getSongDetailListItems(int tag, int offset, int cnt) {
        SongSelectServerApi songSelectServerApi = ApiManager.getInstance().createService(SongSelectServerApi.class);
        ApiMethods.subscribe(songSelectServerApi.getSongDetailListItems(tag, offset, cnt), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if (result.getErrno() == 0) {
                    List<SongModel> list = JSON.parseArray(result.getData().getString("items"), SongModel.class);
                    if (view != null) {
                        view.loadSongsDetailItems(list);
                    }
                } else {
                    if (view != null) {
                        view.loadSongsDetailItemsFail();
                    }
                }
            }
        },this);
    }
}
