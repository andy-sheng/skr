package com.module.playways.rank.song.fragment;

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
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.common.base.BaseActivity;
import com.common.base.BaseFragment;
import com.common.base.FragmentDataListener;
import com.common.core.userinfo.UserInfoManager;
import com.common.log.MyLog;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.common.utils.FragmentUtils;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.recyclerview.RecyclerOnItemClickListener;
import com.common.view.titlebar.CommonTitleBar;
import com.component.busilib.callback.EmptyCallback;
import com.component.busilib.callback.ErrorCallback;
import com.component.busilib.callback.LoadingCallback;
import com.jakewharton.rxbinding2.view.RxView;
import com.kingja.loadsir.callback.Callback;
import com.kingja.loadsir.core.LoadService;
import com.kingja.loadsir.core.LoadSir;
import com.module.playways.PlayWaysActivity;
import com.module.playways.audioroom.AudioRoomActivity;
import com.module.playways.rank.prepare.fragment.AuditionFragment;
import com.module.playways.rank.prepare.fragment.AuditionPrepareResFragment;
import com.module.playways.rank.prepare.fragment.PrepareResFragment;
import com.module.playways.rank.prepare.model.PrepareData;
import com.module.playways.rank.song.SongSelectServerApi;
import com.module.playways.rank.song.adapter.SongSelectAdapter;
import com.module.playways.rank.song.holder.SongSearchFooter;
import com.module.playways.rank.song.model.SongModel;
import com.module.playways.rank.song.view.SearchFeedbackView;
import com.module.rank.R;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.OnClickListener;
import com.orhanobut.dialogplus.ViewHolder;
import com.zq.dialog.PersonInfoDialogView;
import com.zq.toast.CommonToastView;

import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;

import static com.module.playways.PlayWaysActivity.KEY_GAME_TYPE;

public class SearchSongFragment extends BaseFragment {

    CommonTitleBar mTitlebar;

    RecyclerView mSearchResult;
    LinearLayoutManager mLinearLayoutManager;
    SongSelectAdapter mSongSelectAdapter;

    int mGameType;
    String mKeyword;
    DialogPlus mSearchFeedbackDialog;

    CompositeDisposable mCompositeDisposable;
    PublishSubject<String> mPublishSubject;
    DisposableObserver<ApiResult> mDisposableObserver;

    @Override
    public int initView() {
        return R.layout.search_song_fragment_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {

        mTitlebar = (CommonTitleBar) mRootView.findViewById(R.id.titlebar);
        mSearchResult = (RecyclerView) mRootView.findViewById(R.id.search_result);

        Bundle bundle = getArguments();
        if (bundle != null) {
            mGameType = bundle.getInt(KEY_GAME_TYPE);
        }

        mLinearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        mSearchResult.setLayoutManager(mLinearLayoutManager);
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
                if (getActivity() instanceof AudioRoomActivity) {
                    U.getToastUtil().showShort("试音房");
                    if (songModel.isAllResExist()) {
                        PrepareData prepareData = new PrepareData();
                        prepareData.setSongModel(songModel);
                        prepareData.setBgMusic(songModel.getRankUserVoice());

                        mRootView.post(new Runnable() {
                            @Override
                            public void run() {
                                U.getFragmentUtils().addFragment(FragmentUtils.newAddParamsBuilder(getActivity(), AuditionFragment.class)
                                        .setAddToBackStack(true)
                                        .setHasAnimation(true)
                                        .addDataBeforeAdd(0, prepareData)
                                        .setFragmentDataListener(new FragmentDataListener() {
                                            @Override
                                            public void onFragmentResult(int requestCode, int resultCode, Bundle bundle, Object obj) {

                                            }
                                        })
                                        .build());
                            }
                        });
                    } else {
                        U.getFragmentUtils().addFragment(FragmentUtils.newAddParamsBuilder((BaseActivity) getContext(), AuditionPrepareResFragment.class)
                                .setAddToBackStack(false)
                                .setHasAnimation(true)
                                .addDataBeforeAdd(0, songModel)
                                .setFragmentDataListener(new FragmentDataListener() {
                                    @Override
                                    public void onFragmentResult(int requestCode, int resultCode, Bundle bundle, Object obj) {

                                    }
                                })
                                .build());
                    }
                    return;
                }

                if (getActivity() instanceof PlayWaysActivity) {
                    U.getFragmentUtils().addFragment(FragmentUtils.newAddParamsBuilder((BaseActivity) getContext(), PrepareResFragment.class)
                            .setAddToBackStack(true)
                            .setNotifyHideFragment(SongSelectFragment.class)
                            .setHasAnimation(true)
                            .addDataBeforeAdd(0, songModel)
                            .addDataBeforeAdd(1, mGameType)
                            .addDataBeforeAdd(2, true)
                            .setFragmentDataListener(new FragmentDataListener() {
                                @Override
                                public void onFragmentResult(int requestCode, int resultCode, Bundle bundle, Object obj) {

                                }
                            })
                            .build());
                }
            }
        }, SongSelectAdapter.HAS_FOOTER_SEARCH);
        mSearchResult.setAdapter(mSongSelectAdapter);

        mTitlebar.setListener(new CommonTitleBar.OnTitleBarListener() {
            @Override
            public void onClicked(View v, int action, String extra) {
                switch (action) {
                    case CommonTitleBar.ACTION_SEARCH_SUBMIT:
                        searchMusicItems(extra);
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
                U.getFragmentUtils().popFragment(SearchSongFragment.this);
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
                U.getKeyBoardUtils().showSoftInputKeyBoard(getContext());
            }
        }, 200);
    }

    private void showSearchFeedback() {
        SearchFeedbackView searchFeedbackView = new SearchFeedbackView(getContext());
        mSearchFeedbackDialog = DialogPlus.newDialog(getContext())
                .setContentHolder(new ViewHolder(searchFeedbackView))
                .setContentBackgroundResource(R.color.transparent)
                .setOverlayBackgroundResource(R.color.black_trans_50)
                .setExpanded(false)
                .setGravity(Gravity.CENTER)
                .setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(@NonNull DialogPlus dialog, @NonNull View view) {
                        if (view.getId() == R.id.cancel_tv) {
                            // 取消
                            dialog.dismiss();
                        } else if (view.getId() == R.id.confirm_tv) {
                            // 提交
                            String songName = searchFeedbackView.getSongName();
                            String songSinger = searchFeedbackView.getSongSinger();
                            if (!TextUtils.isEmpty(songName) || !TextUtils.isEmpty(songSinger)) {
                                dialog.dismiss();
                                reportNotExistSong(songName, songSinger);
                            } else {
                                U.getToastUtil().showShort("歌曲名和歌手至少输入一个哟～");
                            }
                        }
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
            public ObservableSource<ApiResult> apply(String s) throws Exception {
                SongSelectServerApi songSelectServerApi = ApiManager.getInstance().createService(SongSelectServerApi.class);
                return songSelectServerApi.searchMusicItems(s).subscribeOn(Schedulers.io());
            }
        }).observeOn(AndroidSchedulers.mainThread()).subscribe(mDisposableObserver);
        mCompositeDisposable = new CompositeDisposable();
        mCompositeDisposable.add(mDisposableObserver);
    }

    private void searchMusicItems(String keyword) {
        mKeyword = keyword;
        if (TextUtils.isEmpty(keyword)) {
            U.getToastUtil().showShort("搜索内容为空");
            return;
        }
        SongSelectServerApi songSelectServerApi = ApiManager.getInstance().createService(SongSelectServerApi.class);
        ApiMethods.subscribe(songSelectServerApi.searchMusicItems(keyword), new ApiObserver<ApiResult>() {
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
