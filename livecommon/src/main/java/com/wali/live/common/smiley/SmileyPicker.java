package com.wali.live.common.smiley;

import android.app.Activity;
import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.base.log.MyLog;
import com.live.module.common.R;
import com.base.utils.display.DisplayUtils;
import com.mi.live.data.preference.MLPreferenceUtils;
import com.wali.live.common.keyboard.KeyboardUtils;

import java.util.List;

/**
 * @module com.xiaomi.channel.smileypick
 * <p>
 * Created by MK on 15/10/20.
 */
public class SmileyPicker extends LinearLayout implements ViewPager.OnPageChangeListener {
    private static final String TAG = SmileyPicker.class.getSimpleName();

    private ViewPager mViewPager;
    private SmileyAdapter mAdapter;
    private EditText mEditText;
    private SmileyPoint mSmileyPoint;

    private boolean mIsPickerShowed;
    private int mPickerHeight;
    private int mPageCount = 0;


    private boolean shouldHide;
    private boolean mIsInited = false;

    private int mCurrentAnimeId;

    public List<SmileyItem> getSmileyItemCaches() {
        if (null != mAdapter) {
            return mAdapter.getSmileyCaches();
        }
        return null;
    }

    public SmileyPicker(Context context, AttributeSet attrs) {
        super(context, attrs);

        inflate(context, R.layout.smiley_picker, this);

        mViewPager = (ViewPager) findViewById(R.id.smiley_content);
        mSmileyPoint = (SmileyPoint) findViewById(R.id.smiley_point);
    }

    private void clearAllViewsAndArgs() {
        mPageCount = 0;
        mCurrentAnimeId = 0;
        if (null != mAdapter) {
            mAdapter.clearAllPages();
        }
        setShouldHide(true);
    }

    //只显示一种表情
    public void initSmiley() {
        clearAllViewsAndArgs();
        mAdapter = new SmileyAdapter();
        mViewPager.setAdapter(mAdapter);
        mViewPager.addOnPageChangeListener(this);
        int pageNum = addSmiley(SmileyParser.getInstance().mSmileyV6Ids, SmileyParser.getInstance().mSmileyV6Texts, 8, 3);
        mViewPager.setCurrentItem(0);
        mSmileyPoint.setPageNum(pageNum, 0);
        if (mAdapter.getCount() >= 1) {
            final SmileyPage page = mAdapter.getPage(0);
            if (!page.mIsInited) {
                page.load();
            }
            page.postDelayed(new Runnable() {

                @Override
                public void run() {
                    if (mAdapter.getCount() > 1) {
                        SmileyPage nextPage = mAdapter.getPage(1);
                        if (!nextPage.mIsInited) {
                            nextPage.load();
                        }
                    }
                }
            }, 100);
        }
        mIsInited = true;
    }

    private int addSmiley(int[] list, String[] texts, int columnNum, int rowNum) {
        int pageNum = list.length / (columnNum * rowNum - 1)
                + (list.length % (columnNum * rowNum - 1) == 0 ? 0 : 1);
        for (int i = 0; i < pageNum; i++) {
            int start = (columnNum * rowNum - 1) * i;
            int end = start + columnNum * rowNum - 1;
            end = end > list.length ? list.length : end;
            addSmileyPage(list, texts, columnNum, rowNum, start, end, pageNum, i);
        }

        View splitView = new View(getContext());
        splitView.setBackgroundColor(getResources().getColor(R.color.color_black_trans_10));
        LayoutParams params = new LayoutParams(1, LayoutParams.FILL_PARENT);
        splitView.setLayoutParams(params);
        mPageCount += pageNum;
        return pageNum;
    }

    private void addSmileyPage(int[] list, String[] texts, int columnNum, int rowNum, int start, int end, int pageNum,
                               int pageIndex) {
        if (null == texts) {
            return;
        }
        SmileyPage page = new SmileyPage(getContext(), columnNum, rowNum, pageNum, pageIndex, this); //false, mTabCount,
        page.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));

        for (int i = 0; i < columnNum * rowNum - 1; i++) {
            if (start + i < end) {
                SmileyItemData data = new SmileyItemData();
                data.mImageResId = list[start + i];
                data.mText = texts[start + i];
                page.addSmiley(data);
            }
        }
        ViewPager.LayoutParams params = new ViewPager.LayoutParams();
        params.height = ViewPager.LayoutParams.MATCH_PARENT;
        params.width = ViewPager.LayoutParams.MATCH_PARENT;
        page.setLayoutParams(params);
        page.setPadding(DisplayUtils.dip2px(15), DisplayUtils.dip2px(15), DisplayUtils.dip2px(15),
                DisplayUtils.dip2px(15));
        mAdapter.addView(page);
    }


    public void setEditText(EditText editText) {
        mEditText = editText;
    }

    public EditText getEditText() {
        return mEditText;
    }


    public void show(final Activity activity, final int packageId, int maxHeight,Animation.AnimationListener listener) {
        if (this.getVisibility() == View.VISIBLE) {
            return;
        }
        mIsPickerShowed = true;

        mPickerHeight = MLPreferenceUtils.getKeyboardHeight();
//        MyLog.d(TAG, " mPickerHeight = " + mPickerHeight + " maxHeight = " + maxHeight);
        if (maxHeight <= 0) {
            SmileyPicker.this.getLayoutParams().height = mPickerHeight;
        } else {
            SmileyPicker.this.getLayoutParams().height = Math.min(mPickerHeight, maxHeight);
        }

        KeyboardUtils.hideKeyboard(getContext(), mEditText);
        SmileyPicker.this.setVisibility(View.VISIBLE);
        if (!mIsInited) {
            initSmiley();
        }

        if(listener!=null) {
            Animation animation = onCreateAnimation(true);
            animation.setDuration(300);
            animation.setAnimationListener(listener);
            this.startAnimation(animation);
        }
    }

    public void show(final Activity activity) {
        show(activity, -1, 0,null);
    }

    public void hide(Animation.AnimationListener listener) {
        if (!this.isShown()) {
            return;
        }

        mIsPickerShowed = false;
        this.setVisibility(View.GONE);
        clearAllViewsAndArgs();
        mAdapter.notifyDataSetChanged();
        mIsInited = false;


        if(listener!=null) {
            Animation animation = onCreateAnimation(false);
            animation.setDuration(300);
            animation.setAnimationListener(listener);
            this.startAnimation(animation);
        }
    }

    public void hide() {
        hide(null);
    }

    public Animation onCreateAnimation(boolean enter) {
        if (enter) {
            return AnimationUtils.loadAnimation(getContext(), R.anim.slide_in_from_bottom);
        } else {
            return AnimationUtils.loadAnimation(getContext(), R.anim.slide_out_to_bottom);
        }
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
    }

    public int getPickerHeight() {
        return mPickerHeight;
    }

    @Override
    public void onPageScrollStateChanged(int arg0) {
    }

    @Override
    public void onPageScrolled(int arg0, float arg1, int arg2) {
    }

    @Override
    public void onPageSelected(final int arg0) {
        final SmileyPage page = mAdapter.getPage(arg0);
        mSmileyPoint.setPageNum(page.mPageNum, page.mPageIndex);
        if (!page.mIsInited) {
            page.load();
        }
        if (mAdapter.getCount() > arg0 + 1) {
            SmileyPage nextPage = mAdapter.getPage(arg0 + 1);
            if (!nextPage.mIsInited) {
                nextPage.load();
            }
        }
        if (arg0 > 0) {
            SmileyPage prePage = mAdapter.getPage(arg0 - 1);
            if (!prePage.mIsInited) {
                prePage.load();
            }
        }
    }

    public boolean isPickerShowed() {
        return mIsPickerShowed;
    }

    public void setShouldHide(boolean shouldHide) {
        this.shouldHide = shouldHide;
    }

    //内部类
    public class SmileyItemData {
        public int mImageResId;
        public String mText;
    }


    public boolean isIsInited() {
        return mIsInited;
    }
}
