package com.imagebrowse.big;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.common.base.BaseFragment;
import com.common.base.R;
import com.common.log.MyLog;
import com.common.utils.FragmentUtils;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.titlebar.CommonTitleBar;
import com.common.view.viewpager.arraypageradapter.ArrayViewPagerAdapter;
import com.imagebrowse.ImageBrowseView;
import com.trello.rxlifecycle2.android.FragmentEvent;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * 看大图的Fragment
 */
public class BigImageBrowseFragment extends BaseFragment {
    public final static String TAG = "ImageBigPreviewFragment";
    public final static String BIG_IMAGE_PATH = "big_image_path";

    CommonTitleBar mTitlebar;
    ViewPager mImagesVp;
    //ArrayList<IMAGE_DATA> mDataList = new ArrayList<>();
    Loader mLoader;
    int mLastPostion = 0;
    boolean mBackward;
    Disposable mLoadMoreDisposable;

    ArrayViewPagerAdapter mPagerAdapter = new ArrayViewPagerAdapter() {
        @Override
        public View getView(LayoutInflater inflater, ViewGroup container, Object item, int position) {
            ImageBrowseView imageBrowseView = new ImageBrowseView(container.getContext());
            if (mLoader != null) {
                mLoader.load(imageBrowseView, position, item);
            } else {
                //imageBrowseView.load(data);
            }
            imageBrowseView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    //U.getToastUtil().showShort("长按事件");
                    return false;
                }
            });
            imageBrowseView.setOnClickListener(new DebounceViewClickListener() {
                @Override
                public void clickValid(View v) {
                    finish();
                }
            });
            return imageBrowseView;
        }
    };


    @Override
    public int initView() {
        return R.layout.big_image_preview_fragment_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {

        U.getSoundUtils().preLoad(TAG, R.raw.normal_back);

        mTitlebar = (CommonTitleBar) mRootView.findViewById(R.id.titlebar);

        mImagesVp = (ViewPager) mRootView.findViewById(R.id.images_vp);

        mTitlebar.getLeftTextView().setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                finish();
            }
        });

        if (mLoader == null) {
            finish();
            return;
        }
        mPagerAdapter.addAll(mLoader.getInitList());
        mImagesVp.setAdapter(mPagerAdapter);
        setCurrentItem(mLoader.getInitCurrentItemPostion());
        mLastPostion = mImagesVp.getCurrentItem();
        mImagesVp.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                //MyLog.d(TAG, "onPageScrolled" + " position=" + position + " positionOffset=" + positionOffset + " positionOffsetPixels=" + positionOffsetPixels);
            }

            @Override
            public void onPageSelected(int position) {
                MyLog.d(TAG, "onPageSelected" + " position=" + position);
                if (position > mLastPostion) {
                    mBackward = true;
                } else if (position < mLastPostion) {
                    mBackward = false;
                }
                mLastPostion = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                MyLog.d(TAG, "onPageScrollStateChanged" + " state=" + state);
                if (state == 0) {
                    if (mBackward) {
                        // 往后滑动
                        if (mLastPostion >= mPagerAdapter.getCount() - 2) {
                            if (mLoader.hasMore(true,
                                    mPagerAdapter.getCount(),
                                    mPagerAdapter.getItem(mPagerAdapter.getCount() - 1))) {
                                // 需要往后加载更多了
                                loadMore(true, mPagerAdapter.getCount() - 1);
                            }
                        }
                    } else {
                        if (mLastPostion <= 1) {
                            // 需要往后加载更多了
                            if (mLoader.hasMore(false,
                                    0,
                                    mPagerAdapter.getItem(0))) {
                                loadMore(false, 0);
                            }
                        }
                    }
                }
            }
        });
    }

    void loadMore(boolean backward, int postion) {
        if (mLoadMoreDisposable != null && !mLoadMoreDisposable.isDisposed()) {
            return;
        }
        mLoadMoreDisposable = Observable.create(new ObservableOnSubscribe<List>() {
            @Override
            public void subscribe(ObservableEmitter<List> emitter) throws Exception {
                List dataList = mLoader.loadMore(backward, postion, mPagerAdapter.getItem(postion));
                emitter.onNext(dataList);
                emitter.onComplete();
            }
        }).subscribeOn(Schedulers.io())
                .compose(BigImageBrowseFragment.this.bindUntilEvent(FragmentEvent.DESTROY))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List>() {
                    @Override
                    public void accept(List list) throws Exception {
                        if (backward) {
                            mPagerAdapter.addAll(list);
                        } else {
                            mPagerAdapter.addAll(0, list);
                            //setCurrentItem(mImagesVp.getCurrentItem() + list.size());
                        }
                    }
                });
    }

    void setCurrentItem(int postion) {
        MyLog.d(TAG, "setCurrentItem" + " postion=" + postion);
        mImagesVp.setCurrentItem(postion);
    }

    @Override
    public boolean useEventBus() {
        return false;
    }

    @Override
    public void destroy() {
        super.destroy();
        U.getSoundUtils().release(TAG);
        mImagesVp.clearOnPageChangeListeners();
    }

    @Override
    public void finish() {
        super.finish();
        if (getActivity() instanceof BigImageBrowseActivity) {
            getActivity().finish();
        } else {
            U.getFragmentUtils().popFragment(BigImageBrowseFragment.this);
        }
    }

    @Override
    protected boolean onBackPressed() {
        finish();
        return true;
    }

    @Override
    public void setData(int type, @Nullable Object data) {
        if (type == 1) {
            mLoader = (Loader) data;
        }
    }

    public static void open(boolean useActivity, FragmentActivity activity, Loader mLoader) {
        if (mLoader != null) {
            mLoader.init();
        }
        if (useActivity) {
            BigImageBrowseActivity.open(mLoader, activity);
        } else {
            U.getFragmentUtils().addFragment(FragmentUtils.newAddParamsBuilder(activity, BigImageBrowseFragment.class)
                    .addDataBeforeAdd(1, mLoader)
                    .setAddToBackStack(true)
                    .setHasAnimation(true)
                    .setEnterAnim(R.anim.fade_in_center)
                    .setExitAnim(R.anim.fade_out_center)
                    .build()
            );
        }
    }

    public static void open(boolean useActivity, FragmentActivity activity, String path) {
        open(useActivity, activity, new Loader<String>() {
            @Override
            public void init() {

            }

            @Override
            public void load(ImageBrowseView imageBrowseView, int position, Object item) {
                imageBrowseView.load(path);
            }

            @Override
            public int getInitCurrentItemPostion() {
                return 0;
            }

            @Override
            public List<String> getInitList() {
                ArrayList list = new ArrayList<>();
                list.add(path);
                return list;
            }

            @Override
            public List<String> loadMore(boolean backward, int position, String data) {
                return new ArrayList<>();
            }

            @Override
            public boolean hasMore(boolean backward, int position, String data) {
                return false;
            }

        });
    }

    public interface Loader<T> {
        void init();

        void load(ImageBrowseView imageBrowseView, int position, Object item);

        int getInitCurrentItemPostion();

        List<T> getInitList();

        /**
         * 执行在IO线程
         * 一定要判断是向前还是向后
         * backward 为 true，表示要向后加载更多，position 为当前最后一个元素的索引
         * backward 为 false，表示要向前加载更多，position 为当前最前一个元素的索引
         *
         * @param backward
         * @param position 返回新增了多少个元素，主要用在向前load more 时，更正当前元素的索引
         */
        List<T> loadMore(boolean backward, int position, T data);

        /**
         * 一定要判断是向前还是向后
         * backward 为 true，表示要向后加载更多
         * backward 为 false，表示要向前加载更多
         *
         * @param backward
         */
        boolean hasMore(boolean backward, int position, T data);
    }
}
