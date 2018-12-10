package com.module.rankingmode.song.presenter;

import android.content.Context;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.base.BaseFragment;
import com.common.mvp.RxLifeCyclePresenter;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.module.rankingmode.song.SongSelectServerApi;
import com.module.rankingmode.song.model.SongModel;
import com.module.rankingmode.song.view.ISongTagDetailView;
import com.trello.rxlifecycle2.android.FragmentEvent;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;


public class SongTagDetailsPresenter extends RxLifeCyclePresenter {

    ISongTagDetailView view;

    public SongTagDetailsPresenter(ISongTagDetailView view){
        this.view = view;
    }
    /**
     * 获取曲库剧本的详细条目
     *
     * @param tag 曲库的剧本标签id
     * @param offset 偏移量
     * @param cnt 一页的数量
     * @return
     */
    public void getSongDetailListItems(int tag, int offset, int cnt) {
        SongSelectServerApi songSelectServerApi = ApiManager.getInstance().createService(SongSelectServerApi.class);
        ApiMethods.subscribe(songSelectServerApi.getSongDetailListItems(tag, offset, cnt), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if (result.getErrno() == 0) {
                    List<SongModel> list = new ArrayList<>();
                    JSONArray jsonArray = (JSONArray) result.getData().get("items");
                    for (int i = 0; i < jsonArray.size(); i++) {
                        JSONObject jsonObject = (JSONObject) jsonArray.get(i);
                        String mediaID = jsonObject.getString("mediaID");
                        String coverUrl = jsonObject.getString("cover");
                        String name = jsonObject.getString("name");
                        String owner = jsonObject.getString("owner");
                        String lyricUrl = jsonObject.getString("lyric");
                        String mediaUrl = jsonObject.getString("media");
                        SongModel songModel = new SongModel(mediaID, coverUrl, name, owner, lyricUrl, mediaUrl);
                        list.add(songModel);
                    }

                    if (view!=null){
                        view.loadSongsDetailItems(list);
                    }
                }else {
                    if (view!=null){
                        view.loadSongsDetailItemsFail();
                    }
                }
            }
        }, this);
    }
}
