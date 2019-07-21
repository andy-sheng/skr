package com.zq.person.view;

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.RelativeLayout;

import com.common.base.BaseFragment;
import com.common.callback.Callback;
import com.common.log.MyLog;
import com.common.view.recyclerview.RecyclerOnItemClickListener;
import com.component.busilib.R;
import com.imagebrowse.ImageBrowseView;
import com.imagebrowse.big.BigImageBrowseFragment;
import com.imagebrowse.big.DefaultImageBrowserLoader;
import com.respicker.ResPicker;
import com.respicker.activity.ResPickerActivity;
import com.respicker.model.ImageItem;
import com.zq.person.adapter.PhotoAdapter;
import com.zq.person.model.AddPhotoModel;
import com.zq.person.model.PhotoModel;
import com.zq.person.presenter.PhotoCorePresenter;

import java.util.ArrayList;
import java.util.List;

/**
 * 照片墙view
 */
public class PhotoWallView extends RelativeLayout implements IPhotoWallView {

    public final String TAG = "PhotoWallView";

    int DEFAUAT_CNT = 20;       // 默认拉取一页的数量
    boolean mHasMore = false;

    RecyclerView mPhotoView;
    BaseFragment mFragment;
    RequestCallBack mCallBack;
    PhotoAdapter mPhotoAdapter;
    PhotoCorePresenter mPhotoCorePresenter;

    long mLastUpdateInfo = 0;    //上次更新成功时间

    public PhotoWallView(BaseFragment fragment, RequestCallBack callBack) {
        super(fragment.getContext());
        this.mFragment = fragment;
        this.mCallBack = callBack;
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.photo_wall_view_layout, this);
        mPhotoView = (RecyclerView) findViewById(R.id.photo_view);

        mPhotoCorePresenter = new PhotoCorePresenter(this, mFragment);

        mPhotoView.setFocusableInTouchMode(false);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 3);
        mPhotoView.setLayoutManager(gridLayoutManager);
        mPhotoAdapter = new PhotoAdapter(new RecyclerOnItemClickListener() {
            @Override
            public void onItemClicked(View view, int position, final Object model) {
                if (model instanceof AddPhotoModel) {
                    goAddPhotoFragment();
                } else if (model instanceof PhotoModel) {
                    // 跳到看大图
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
                            return mPhotoAdapter.getPostionOfItem((PhotoModel) model);
                        }

                        @Override
                        public List<PhotoModel> getInitList() {
                            return mPhotoAdapter.getDataList();
                        }

                        @Override
                        public void loadMore(boolean backward, int position, PhotoModel data, final Callback<List<PhotoModel>> callback) {
                            if (backward) {
                                // 向后加载
                                mPhotoCorePresenter.getPhotos(mPhotoAdapter.getSuccessNum(), DEFAUAT_CNT, new Callback<List<PhotoModel>>() {
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
                            return true;
                        }

                        @Override
                        public boolean hasDeleteMenu() {
                            return true;
                        }

                        @Override
                        public Callback<PhotoModel> getDeleteListener() {
                            return new Callback<PhotoModel>() {
                                @Override
                                public void onCallback(int r, PhotoModel obj) {
                                    mPhotoCorePresenter.deletePhoto(obj);
                                }
                            };
                        }
                    });
                }
            }
        }, PhotoAdapter.TYPE_PERSON_CENTER);

        mPhotoAdapter.setPhotoManageListener(new PhotoAdapter.PhotoManageListener() {
            @Override
            public void delete(PhotoModel model) {
                mPhotoCorePresenter.deletePhoto(model);
            }

            @Override
            public void reupload(PhotoModel model) {
                ArrayList<PhotoModel> photoModelArrayList = new ArrayList<>(1);
                photoModelArrayList.add(model);
                mPhotoCorePresenter.upload(photoModelArrayList, true);
            }
        });
        mPhotoView.setAdapter(mPhotoAdapter);
        mPhotoCorePresenter.loadUnSuccessPhotoFromDB();
    }

    public void uploadPhotoList(List<ImageItem> imageItems) {
        mPhotoCorePresenter.uploadPhotoList(imageItems);
    }

    public void getPhotos(boolean isFlag) {
        long now = System.currentTimeMillis();
        if (!isFlag) {
            // 10分钟更新一次吧
            if ((now - mLastUpdateInfo) < 10 * 60 * 1000) {
                return;
            }
        }
        if (mPhotoAdapter.getSuccessNum() == 0) {
            mPhotoCorePresenter.getPhotos(0, DEFAUAT_CNT);
        }
    }

    public void getMorePhotos() {
        mPhotoCorePresenter.getPhotos(mPhotoAdapter.getSuccessNum(), DEFAUAT_CNT);
    }

    void goAddPhotoFragment() {
        ResPicker.getInstance().setParams(ResPicker.newParamsBuilder()
                .setMultiMode(true)
                .setShowCamera(true)
                .setCrop(false)
                .setSelectLimit(9)
                .setIncludeGif(false)
                .build()
        );
        ResPickerActivity.open(mFragment.getActivity());
    }


    @Override
    public void addPhoto(List<PhotoModel> list, boolean clear, int totalNum) {
        MyLog.d(TAG, "showPhoto" + " list=" + list + " clear=" + clear + " totalNum=" + totalNum);

        mLastUpdateInfo = System.currentTimeMillis();

        if (mCallBack != null) {
            mCallBack.onRequestSucess();
        }
        if (list != null && list.size() > 0) {
            // 有数据
            mHasMore = true;
//            mSmartRefresh.setEnableLoadMore(true);
            if (clear) {
                mPhotoAdapter.setDataList(list);
            } else {
                mPhotoAdapter.insertLast(list);
            }
        } else {
            mHasMore = false;
//            mSmartRefresh.setEnableLoadMore(false);
            if (mPhotoAdapter.getDataList() != null && mPhotoAdapter.getDataList().size() > 0) {
                // 没有更多了
            } else {
                // 没有数据
            }
        }
    }

    @Override
    public void insertPhoto(PhotoModel photoModel) {
        mPhotoAdapter.insertFirst(photoModel);
    }

    @Override
    public void deletePhoto(PhotoModel photoModel, boolean numchange) {
        if (numchange) {
//            mTotalPhotoNum--;
//            setPhotoNum();
        }
        mPhotoAdapter.delete(photoModel);
    }


    @Override
    public void updatePhoto(PhotoModel photoModel) {
        if (photoModel.getStatus() == PhotoModel.STATUS_SUCCESS) {
//            mTotalPhotoNum++;
//            setPhotoNum();
        }
        mPhotoAdapter.update(photoModel);
    }

    @Override
    public void loadDataFailed() {
        if (mCallBack != null) {
            mCallBack.onRequestSucess();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mPhotoCorePresenter != null) {
            mPhotoCorePresenter.destroy();
        }
    }
}
