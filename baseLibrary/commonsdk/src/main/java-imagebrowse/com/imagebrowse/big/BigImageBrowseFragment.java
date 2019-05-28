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
import com.common.callback.Callback;
import com.common.log.MyLog;
import com.common.utils.FragmentUtils;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.titlebar.CommonTitleBar;
import com.common.view.viewpager.arraypageradapter.ArrayViewPagerAdapter;
import com.dialog.list.DialogListItem;
import com.dialog.list.ListDialog;
import com.imagebrowse.ImageBrowseView;

import java.util.ArrayList;
import java.util.List;

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
    ListDialog mMenuDialog;

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
        if (mLoader == null) {
            finish();
            return;
        }

        U.getSoundUtils().preLoad(TAG, R.raw.normal_back);

        mTitlebar = (CommonTitleBar) mRootView.findViewById(R.id.titlebar);

        mImagesVp = (ViewPager) mRootView.findViewById(R.id.images_vp);

        mTitlebar.getLeftTextView().setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                finish();
            }
        });

        if (mLoader.hasDeleteMenu()) {
            mTitlebar.getRightImageButton().setOnClickListener(new DebounceViewClickListener() {
                @Override
                public void clickValid(View v) {
                    mMenuDialog = new ListDialog(getContext());
                    List<DialogListItem> listItems = new ArrayList<>();
                    if (mLoader.hasDeleteMenu()) {
                        listItems.add(new DialogListItem("删除", new Runnable() {
                            @Override
                            public void run() {
                                int cp = mImagesVp.getCurrentItem();
                                if (mLoader.getDeleteListener() != null) {
                                    mLoader.getDeleteListener().onCallback(0, mPagerAdapter.getItem(cp));
                                }
                                if (mPagerAdapter.getCount() > 0) {
                                    mPagerAdapter.remove(cp);
                                }
                                mMenuDialog.dissmiss();
                                if (mPagerAdapter.getCount() <= 0) {
                                    finish();
                                }
                            }
                        }));
                    }

                    listItems.add(new DialogListItem("取消", new Runnable() {
                        @Override
                        public void run() {
                            if (mMenuDialog != null) {
                                mMenuDialog.dissmiss();
                            }
                        }
                    }));
                    mMenuDialog.showList(listItems);
                }
            });
        } else {
            mTitlebar.getRightImageButton().setVisibility(View.GONE);
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
        mLoader.loadMore(backward, postion, mPagerAdapter.getItem(postion), new Callback<List>() {
            @Override
            public void onCallback(int r, List list) {
                if (backward) {
                    mPagerAdapter.addAll(list);
                } else {
                    mPagerAdapter.addAll(0, list);
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
        if (mImagesVp != null) {
            mImagesVp.clearOnPageChangeListeners();
        }
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

    /**
     * 浏览组图 数据从Loader里的各个回调里拿
     * @param useActivity
     * @param activity
     * @param mLoader
     */
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

    /**
     * 浏览单个大图
     * @param useActivity
     * @param activity
     * @param path
     */
    public static void open(boolean useActivity, FragmentActivity activity, String path) {
        open(useActivity, activity, new DefaultImageBrowserLoader<String>() {
            @Override
            public void init() {

            }

            @Override
            public void load(ImageBrowseView imageBrowseView, int position, String item) {
                imageBrowseView.load(item);
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
            public void loadMore(boolean backward, int position, String data, Callback<List<String>> callback) {

            }

            @Override
            public boolean hasMore(boolean backward, int position, String data) {
                return false;
            }

        });
    }

}
