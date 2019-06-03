package com.zq.person.view;

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
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
import com.zq.person.adapter.PhotoAdapter;
import com.zq.person.model.PhotoModel;

import java.util.List;

public class OtherPhotoWallView extends RelativeLayout {

    public final static String TAG = "PhotoWallView";
    int mUserId;
    RequestCallBack mCallBack;

    int offset;  // 拉照片偏移量
    int DEFAUAT_CNT = 20;       // 默认拉取一页的数量
    long mLastUpdateInfo = 0;
    boolean mHasMore = false;

    RecyclerView mPhotoView;
    BaseFragment mFragment;
    PhotoAdapter mPhotoAdapter;
    UserInfoServerApi mUserInfoServerApi;

    AppCanSrollListener mListener;

    public OtherPhotoWallView(BaseFragment fragment, int userID, RequestCallBack callBack, AppCanSrollListener listener) {
        super(fragment.getContext());
        this.mFragment = fragment;
        this.mUserId = userID;
        this.mCallBack = callBack;
        this.mUserInfoServerApi = ApiManager.getInstance().createService(UserInfoServerApi.class);
        this.mListener = listener;
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.photo_other_wall_view_layout, this);

        mPhotoView = (RecyclerView) findViewById(R.id.photo_view);
        mPhotoView.setFocusableInTouchMode(false);
        mPhotoView.setLayoutManager(new GridLayoutManager(getContext(), 3));
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
    }

    public void getPhotos(boolean isFlag) {
        long now = System.currentTimeMillis();
        if (!isFlag) {
            // 10分钟更新一次吧
            if ((now - mLastUpdateInfo) < 10 * 60 * 1000) {
                return;
            }
        }
        getPhotos(mUserId, 0, DEFAUAT_CNT, null);
    }

    public void getMorePhotos() {
        getPhotos(mUserId, offset, DEFAUAT_CNT, null);
    }

    private void getPhotos(int userId, final int offset, int cnt, final Callback<List<PhotoModel>> callback) {
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
        mLastUpdateInfo = System.currentTimeMillis();

        if (mCallBack != null) {
            mCallBack.onRequestSucess();
        }

        if (clear) {
            mPhotoAdapter.getDataList().clear();
        }

        if (list != null && list.size() > 0) {
            if (!(mPhotoView.getLayoutManager() instanceof GridLayoutManager)) {
                mPhotoView.setLayoutManager(new GridLayoutManager(getContext(), 3));
            }
            mHasMore = true;
            mPhotoAdapter.getDataList().addAll(list);
            mPhotoAdapter.notifyDataSetChanged();
            if (mListener != null) {
                mListener.notifyAppbarSroll(true);
            }
        } else {
            mHasMore = false;
            if (mPhotoAdapter.getDataList() != null && mPhotoAdapter.getDataList().size() > 0) {
                // 没有更多了
            } else {
                // 没有数据
                mPhotoView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
                if (mListener != null) {
                    mListener.notifyAppbarSroll(false);
                }
            }
        }
    }


    public void addPhotosFail() {
        if (mCallBack != null) {
            mCallBack.onRequestSucess();
        }
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
