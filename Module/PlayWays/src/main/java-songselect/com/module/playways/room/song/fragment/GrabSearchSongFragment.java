package com.module.playways.room.song.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;

import com.alibaba.fastjson.JSON;
import com.common.base.BaseFragment;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.recyclerview.RecyclerOnItemClickListener;
import com.common.view.titlebar.CommonTitleBar;
import com.module.playways.room.song.SongSelectServerApi;
import com.module.playways.room.song.adapter.SongSelectAdapter;
import com.module.playways.room.song.model.SongModel;
import com.module.playways.R;
import com.module.playways.room.song.view.SearchFeedbackView;
import com.module.playways.songmanager.SongManagerActivity;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.OnDismissListener;
import com.orhanobut.dialogplus.ViewHolder;
import com.component.toast.CommonToastView;

import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;

public class GrabSearchSongFragment extends BaseFragment {

    CommonTitleBar mTitlebar;

    RecyclerView mSearchResult;
    LinearLayoutManager mLinearLayoutManager;
    SongSelectAdapter mSongSelectAdapter;

    String mKeyword;
    DialogPlus mSearchFeedbackDialog;

    CompositeDisposable mCompositeDisposable;
    PublishSubject<String> mPublishSubject;
    DisposableObserver<ApiResult> mDisposableObserver;

    int mFrom;
    boolean isOwner;

    @Override
    public int initView() {
        return R.layout.grab_search_song_fragment_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {

        mTitlebar = (CommonTitleBar) getRootView().findViewById(R.id.titlebar);
        mSearchResult = (RecyclerView) getRootView().findViewById(R.id.search_result);

        mLinearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        mSearchResult.setLayoutManager(mLinearLayoutManager);

        int selectMode = SongSelectAdapter.GRAB_MODE;
        if (mFrom == SongManagerActivity.Companion.getTYPE_FROM_DOUBLE()) {
            selectMode = SongSelectAdapter.DOUBLE_MODE;
        }
        mSongSelectAdapter = new SongSelectAdapter(new RecyclerOnItemClickListener() {
            @Override
            public void onItemClicked(View view, int position, Object model) {
                U.getKeyBoardUtils().hideSoftInputKeyBoard(getActivity());
                if (model == null) {
                    // 搜歌反馈
                    showSearchFeedback();
                    return;
                }
                SongModel songModel = (SongModel) model;
                if (getFragmentDataListener() != null) {
                    getFragmentDataListener().onFragmentResult(0, 0, null, songModel);
                }
            }
        }, true, selectMode, isOwner);
        mSearchResult.setAdapter(mSongSelectAdapter);

        mTitlebar.setListener(new CommonTitleBar.OnTitleBarListener() {
            @Override
            public void onClicked(View v, int action, String extra) {
                switch (action) {
                    case CommonTitleBar.ACTION_SEARCH_SUBMIT:
                        searchGrabMusicItems(extra);
                        break;
                    case CommonTitleBar.ACTION_SEARCH_DELETE:
                        mTitlebar.getCenterSearchEditText().setText("");
                        break;
                }
            }
        });

        mTitlebar.getRightTextView().setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                U.getKeyBoardUtils().hideSoftInputKeyBoard(getActivity());
                U.getFragmentUtils().popFragment(GrabSearchSongFragment.this);
            }
        });

        mTitlebar.showSoftInputKeyboard(true);
        initPublishSubject();
        mTitlebar.getCenterSearchEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                mPublishSubject.onNext(editable.toString());
            }
        });

        mTitlebar.postDelayed(new Runnable() {
            @Override
            public void run() {
                U.getKeyBoardUtils().showSoftInputKeyBoard(getActivity());
            }
        }, 200);
    }

    private void searchGrabMusicItems(String keyword) {
        mKeyword = keyword;
        if (TextUtils.isEmpty(keyword)) {
            U.getToastUtil().showShort("搜索内容为空");
            return;
        }
        SongSelectServerApi songSelectServerApi = ApiManager.getInstance().createService(SongSelectServerApi.class);
        ApiMethods.subscribe(mFrom == SongManagerActivity.Companion.getTYPE_FROM_GRAB() ? songSelectServerApi.searchGrabMusicItems(keyword)
                : songSelectServerApi.searchDoubleMusicItems(keyword), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if (result.getErrno() == 0) {
                    List<SongModel> list = JSON.parseArray(result.getData().getString("items"), SongModel.class);
                    loadSongsDetailItems(list, true);
                }
            }

            @Override
            public void onNetworkError(ErrorType errorType) {
                U.getToastUtil().showShort("网络异常，请检查网络后重试");
                super.onNetworkError(errorType);
            }
        }, this);
    }

    public void loadSongsDetailItems(List<SongModel> list, boolean isSubmit) {
        mSearchResult.setVisibility(View.VISIBLE);
        if (list == null || list.size() == 0) {
            return;
        }

        if (isSubmit) {
            U.getKeyBoardUtils().hideSoftInputKeyBoard(getActivity());
        }
        if (mSongSelectAdapter != null) {
            mSongSelectAdapter.setDataList(list);
            mSongSelectAdapter.notifyDataSetChanged();
            mSearchResult.scrollToPosition(0);
        }
    }

    private void showSearchFeedback() {
        SearchFeedbackView searchFeedbackView = new SearchFeedbackView(GrabSearchSongFragment.this);
        searchFeedbackView.setListener(new SearchFeedbackView.Listener() {
            @Override
            public void onClickSubmit(String songName, String songSinger) {
                if (!TextUtils.isEmpty(songName) || !TextUtils.isEmpty(songSinger)) {
                    if (mSearchFeedbackDialog != null) {
                        mSearchFeedbackDialog.dismiss();
                    }
                    reportNotExistSong(songName, songSinger);
                } else {
                    U.getToastUtil().showShort("歌曲名和歌手至少输入一个哟～");
                }
            }

            @Override
            public void onClickCancle() {
                if (mSearchFeedbackDialog != null) {
                    mSearchFeedbackDialog.dismiss();
                }
            }
        });
        mSearchFeedbackDialog = DialogPlus.newDialog(getContext())
                .setContentHolder(new ViewHolder(searchFeedbackView))
                .setContentBackgroundResource(R.color.transparent)
                .setOverlayBackgroundResource(R.color.black_trans_50)
                .setExpanded(false)
                .setGravity(Gravity.BOTTOM)
                .setOnDismissListener(new OnDismissListener() {
                    @Override
                    public void onDismiss(@NonNull DialogPlus dialog) {
                        U.getKeyBoardUtils().hideSoftInputKeyBoard(getActivity());
                    }
                })
                .create();

        mSearchFeedbackDialog.show();
    }

    private void reportNotExistSong(String songName, String songSinger) {
        SongSelectServerApi songSelectServerApi = ApiManager.getInstance().createService(SongSelectServerApi.class);
        ApiMethods.subscribe(songSelectServerApi.reportNotExistSong(songName, songSinger), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if (result.getErrno() == 0) {
                    U.getToastUtil().showSkrCustomShort(new CommonToastView.Builder(U.app())
                            .setImage(R.drawable.touxiangshezhichenggong_icon)
                            .setText("提交成功\n审核通过马上就会上架了")
                            .build());
                } else {
                    U.getToastUtil().showSkrCustomShort(new CommonToastView.Builder(U.app())
                            .setImage(R.drawable.touxiangshezhishibai_icon)
                            .setText("提交缺歌上报失败了")
                            .build());
                }

            }
        }, this);
    }


    private void initPublishSubject() {
        mPublishSubject = PublishSubject.create();
        mDisposableObserver = new DisposableObserver<ApiResult>() {
            @Override
            public void onNext(ApiResult result) {
                if (result.getErrno() == 0) {
                    List<SongModel> list = JSON.parseArray(result.getData().getString("items"), SongModel.class);
                    loadSongsDetailItems(list, false);
                }
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        };
        mPublishSubject.debounce(200, TimeUnit.MILLISECONDS).filter(new Predicate<String>() {
            @Override
            public boolean test(String s) throws Exception {
                return s.length() > 0;
            }
        }).switchMap(new Function<String, ObservableSource<ApiResult>>() {
            @Override
            public ObservableSource<ApiResult> apply(String string) throws Exception {
                SongSelectServerApi songSelectServerApi = ApiManager.getInstance().createService(SongSelectServerApi.class);
                if (mFrom == SongManagerActivity.Companion.getTYPE_FROM_GRAB()) {
                    return songSelectServerApi.searchGrabMusicItems(string).subscribeOn(Schedulers.io());
                } else {
                    return songSelectServerApi.searchDoubleMusicItems(string).subscribeOn(Schedulers.io());
                }
            }
        }).observeOn(AndroidSchedulers.mainThread()).subscribe(mDisposableObserver);
        mCompositeDisposable = new CompositeDisposable();
        mCompositeDisposable.add(mDisposableObserver);
    }

    @Override
    public void setData(int type, @Nullable Object data) {
        super.setData(type, data);
        if (type == 0) {
            mFrom = (Integer) data;
        } else if (type == 1) {
            isOwner = (Boolean) data;
        }
    }

    @Override
    public boolean useEventBus() {
        return false;
    }

    @Override
    public void destroy() {
        super.destroy();
        if (mCompositeDisposable != null) {
            mCompositeDisposable.clear();
        }
        if (mSearchFeedbackDialog != null) {
            mSearchFeedbackDialog.dismiss();
        }
    }
}
