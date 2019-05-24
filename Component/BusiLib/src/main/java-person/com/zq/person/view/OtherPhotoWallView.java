package com.zq.person.view;

import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.RelativeLayout;

import com.alibaba.fastjson.JSON;
import com.common.base.BaseFragment;
import com.common.callback.Callback;
import com.common.core.userinfo.UserInfoServerApi;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.common.view.recyclerview.RecyclerOnItemClickListener;
import com.component.busilib.R;
import com.imagebrowse.ImageBrowseView;
import com.imagebrowse.big.BigImageBrowseFragment;
import com.imagebrowse.big.DefaultImageBrowserLoader;
import com.kingja.loadsir.core.LoadService;
import com.kingja.loadsir.core.LoadSir;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener;
import com.zq.person.adapter.PhotoAdapter;
import com.zq.person.model.PhotoModel;

import java.util.List;

public class OtherPhotoWallView extends RelativeLayout {

    public final static String TAG = "PhotoWallView";
    int mUserId;

    int offset;  // 拉照片偏移量
    int DEFAUAT_CNT = 20;       // 默认拉取一页的数量
    boolean mHasMore = false;

    SmartRefreshLayout mSmartRefresh;
    RecyclerView mPhotoView;
    BaseFragment mFragment;
    PhotoAdapter mPhotoAdapter;
    UserInfoServerApi mUserInfoServerApi;

    AppCanSrollListener mListener;
    LoadService mLoadService;

    public OtherPhotoWallView(BaseFragment fragment, int userID, AppCanSrollListener listener) {
        super(fragment.getContext());
        this.mFragment = fragment;
        this.mUserId = userID;
        this.mUserInfoServerApi = ApiManager.getInstance().createService(UserInfoServerApi.class);
        this.mListener = listener;
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.photo_other_wall_view_layout, this);

        mSmartRefresh = (SmartRefreshLayout) findViewById(R.id.smart_refresh);
        mPhotoView = (RecyclerView) findViewById(R.id.photo_view);


        mSmartRefresh.setEnableRefresh(false);
        mSmartRefresh.setEnableLoadMore(true);
        mSmartRefresh.setEnableLoadMoreWhenContentNotFull(false);
        mSmartRefresh.setEnableOverScrollDrag(true);
        mSmartRefresh.setOnRefreshLoadMoreListener(new OnRefreshLoadMoreListener() {
            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
                getPhotos(mUserId, offset, DEFAUAT_CNT, null);
            }

            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {

            }
        });

        mPhotoView.setFocusableInTouchMode(false);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 3);
        mPhotoView.setLayoutManager(gridLayoutManager);
        mPhotoAdapter = new PhotoAdapter(new RecyclerOnItemClickListener() {
            @Override
            public void onItemClicked(View view, final int position, Object model) {
                BigImageBrowseFragment.open(true, mFragment.getActivity(), new DefaultImageBrowserLoader<PhotoModel>() {
                    @Override
                    public void init() {

                    }

                    @Override
                    public void load(ImageBrowseView imageBrowseView, int position, PhotoModel item) {
                        if (TextUtils.isEmpty(item.getPicPath())) {
                            imageBrowseView.load(item.getLocalPath());
                        } else {
                            imageBrowseView.load(item.getPicPath());
                        }
                    }

                    @Override
                    public int getInitCurrentItemPostion() {
                        return position;
                    }

                    @Override
                    public List<PhotoModel> getInitList() {
                        return mPhotoAdapter.getDataList();
                    }

                    @Override
                    public void loadMore(boolean backward, int position, PhotoModel data, final Callback<List<PhotoModel>> callback) {
                        if (backward) {
                            // 向后加载
                            getPhotos(mUserId, mPhotoAdapter.getSuccessNum(), DEFAUAT_CNT, new Callback<List<PhotoModel>>() {

                                @Override
                                public void onCallback(int r, List<PhotoModel> list) {
                                    if (callback != null && list != null) {
                                        callback.onCallback(0, list);
                                    }
                                }
                            });
                        }
                    }

                    @Override
                    public boolean hasMore(boolean backward, int position, PhotoModel data) {
                        if (backward) {
                            return mHasMore;
                        }
                        return false;
                    }

                    @Override
                    public boolean hasMenu() {
                        return false;
                    }
                });
            }
        }, PhotoAdapter.TYPE_OTHER_PERSON_CENTER);
        mPhotoView.setAdapter(mPhotoAdapter);

        LoadSir mLoadSir = new LoadSir.Builder()
                .addCallback(new PersonEmptyCallback(R.drawable.tongxunlu_fensikongbaiye, "这个人很神秘，都没有照片"))
                .build();
        mLoadService = mLoadSir.register(mSmartRefresh, new com.kingja.loadsir.callback.Callback.OnReloadListener() {
            @Override
            public void onReload(View v) {
                getPhotos();
            }
        });
    }

    public void getPhotos() {
        getPhotos(mUserId, 0, DEFAUAT_CNT, null);
    }

    public void getPhotos(int userId, final int offset, int cnt, final Callback<List<PhotoModel>> callback) {
        ApiMethods.subscribe(mUserInfoServerApi.getPhotos(userId, offset, cnt), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if (result.getErrno() == 0) {
                    if (result != null && result.getErrno() == 0) {
                        List<PhotoModel> list = JSON.parseArray(result.getData().getString("pic"), PhotoModel.class);
                        int newOffset = result.getData().getIntValue("offset");
                        int totalCount = result.getData().getIntValue("totalCount");
                        if (offset == 0) {
                            addPhotos(list, newOffset, totalCount, true);
                        } else {
                            addPhotos(list, newOffset, totalCount, false);
                        }
                        if (callback != null) {
                            callback.onCallback(0, list);
                        }
                    }
                } else {
                    addPhotosFail();
                }
            }

            @Override
            public void onNetworkError(ErrorType errorType) {
                super.onNetworkError(errorType);
                addPhotosFail();
            }
        }, mFragment);
    }


    public void addPhotos(List<PhotoModel> list, int newOffset, int totalNum, boolean clear) {
        offset = newOffset;
        mSmartRefresh.finishLoadMore();

        if (clear) {
            mPhotoAdapter.getDataList().clear();
        }

        if (list != null && list.size() > 0) {
            mHasMore = true;
            mLoadService.showSuccess();
            mSmartRefresh.setEnableLoadMore(true);
            mPhotoAdapter.getDataList().addAll(list);
            mPhotoAdapter.notifyDataSetChanged();
            if (mListener != null) {
                mListener.notifyAppbarSroll(true);
            }
        } else {
            mHasMore = false;
            mSmartRefresh.setEnableLoadMore(false);
            if (mPhotoAdapter.getDataList() != null && mPhotoAdapter.getDataList().size() > 0) {
                // 没有更多了
            } else {
                // 没有数据
                mLoadService.showCallback(PersonEmptyCallback.class);
                if (mListener != null) {
                    mListener.notifyAppbarSroll(false);
                }
            }
        }
    }


    public void addPhotosFail() {
        mSmartRefresh.finishLoadMore();
        if (mPhotoAdapter.getDataList() == null || mPhotoAdapter.getDataList().size() == 0) {
            if (mListener != null) {
                mListener.notifyAppbarSroll(false);
            }
        }
    }

    public interface AppCanSrollListener {
        void notifyAppbarSroll(boolean canScroll);
    }
}
