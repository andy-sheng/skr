package com.imagebrowse.big;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;

import com.common.base.BaseFragment;
import com.common.base.R;
import com.common.utils.FragmentUtils;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.titlebar.CommonTitleBar;
import com.imagebrowse.ImageBrowseView;
import com.trello.rxlifecycle2.android.FragmentEvent;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
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


    PagerAdapter mPagerAdapter = new PagerAdapter() {

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            ImageBrowseView imageBrowseView = new ImageBrowseView(container.getContext());
            if (mLoader != null) {
                mLoader.load(imageBrowseView, position);
            } else {
                //imageBrowseView.load(data);
            }
            container.addView(imageBrowseView);
            return imageBrowseView;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            container.removeView((View) object);
        }

        @Override
        public int getCount() {
            if (mLoader != null) {
                return mLoader.getCount();
            } else {
                return 0;
            }
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == object;
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
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

        mImagesVp.setAdapter(mPagerAdapter);
        mImagesVp.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position > mLastPostion) {
                    // 往后滑动
                    if (position >= mLoader.getCount() - 2) {
                        if (mLoader.hasMore(true)) {
                            // 需要往后加载更多了
                            loadMore(true, mLoader.getCount() - 1);
                        }
                    }
                } else if (position < mLastPostion) {
                    if (position <= 1) {
                        // 需要往后加载更多了
                        if (mLoader.hasMore(false)) {
                            loadMore(false, 0);
                        }
                    }
                }
                mLastPostion = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        if (mLoader == null) {
            finish();
        }
    }

    void loadMore(boolean backward, int postion) {
        Observable.create(new ObservableOnSubscribe<Object>() {
            @Override
            public void subscribe(ObservableEmitter<Object> emitter) throws Exception {
                mLoader.loadMore(backward, postion);
                emitter.onComplete();
            }
        }).subscribeOn(Schedulers.io())
                .compose(BigImageBrowseFragment.this.bindUntilEvent(FragmentEvent.DESTROY))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Object>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Object o) {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {
                        mPagerAdapter.notifyDataSetChanged();
                    }
                });
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
    public void setData(int type, @Nullable Object data) {
        if (type == 1) {
            mLoader = (Loader) data;
        }
    }

    public static void open(boolean useActivity, FragmentActivity activity, Loader mLoader) {
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
        open(useActivity, activity, new Loader() {
            @Override
            public void load(ImageBrowseView imageBrowseView, int position) {
                imageBrowseView.load(path);
            }

            @Override
            public int getCount() {
                return 1;
            }

            @Override
            public void loadMore(boolean backward, int position) {

            }

            @Override
            public boolean hasMore(boolean backward) {
                return false;
            }
        });
    }

    public interface Loader {
        void load(ImageBrowseView imageBrowseView, int position);

        int getCount();


        /**
         * backward 为 true，表示要向后加载更多，position 为当前最后一个元素的索引
         * backward 为 false，表示要向前加载更多，position 为当前最前一个元素的索引
         *
         * @param backward
         * @param position
         */
        void loadMore(boolean backward, int position);

        boolean hasMore(boolean backward);
    }
}
