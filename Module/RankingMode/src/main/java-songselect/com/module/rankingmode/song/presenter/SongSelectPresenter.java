package com.module.rankingmode.song.presenter;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.log.MyLog;
import com.common.mvp.RxLifeCyclePresenter;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.module.rankingmode.song.SongSelectServerApi;
import com.module.rankingmode.song.model.TagModel;
import com.module.rankingmode.song.view.ISongTagView;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class SongSelectPresenter extends RxLifeCyclePresenter {

    ISongTagView view;

    public SongSelectPresenter(ISongTagView view) {
        this.view = view;
    }

    /**
     * 获取曲库剧本的标签
     *
     * @param mode
     * @param offset
     * @param cnt
     * @return
     */
    public void getSongsListTags(int mode, int offset, int cnt) {
        SongSelectServerApi songSelectServerApi = ApiManager.getInstance().createService(SongSelectServerApi.class);
//        ApiMethods.subscribe(songSelectServerApi.getSongsListTags(mode, offset, cnt)
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread()), new ApiObserver<ApiResult>() {
//            @Override
//            public void process(ApiResult result) {
//                if (result.getErrno() == 0) {
//                    List<TagModel> list = new ArrayList<>();
//                    JSONArray jsonArray = (JSONArray) result.getData().get("tags");
//                    for (int i = 0; i < jsonArray.size(); i++) {
//                        JSONObject jsonObject = (JSONObject) jsonArray.get(i);
//                        String tagId = jsonObject.getString("tagID");
//                        String name = jsonObject.getString("name");
//                        TagModel tagModel = new TagModel(tagId, name);
//                        list.add(tagModel);
//                    }
//                    if (view != null) {
//                        view.loadSongsTags(list);
//                    }
//                } else {
//                    if (view != null) {
//                        view.loadSongsTagsFail();
//                    }
//                }
//            }
//        }, this);

        songSelectServerApi.getSongsListTags(mode, offset, cnt)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ApiResult>() {

                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(ApiResult result) {
                        if (result.getErrno() == 0) {
                            List<TagModel> list = new ArrayList<>();
                            JSONArray jsonArray = (JSONArray) result.getData().get("tags");
                            for (int i = 0; i < jsonArray.size(); i++) {
                                JSONObject jsonObject = (JSONObject) jsonArray.get(i);
                                String tagId = jsonObject.getString("tagID");
                                String name = jsonObject.getString("name");
                                TagModel tagModel = new TagModel(tagId, name);
                                list.add(tagModel);
                            }
                            if (view != null) {
                                view.loadSongsTags(list);
                            }
                        } else {
                            if (view != null) {
                                view.loadSongsTagsFail();
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (view != null) {
                            view.loadSongsTagsFail();
                        }
                    }

                    @Override
                    public void onComplete() {

                    }
                });

    }

}
