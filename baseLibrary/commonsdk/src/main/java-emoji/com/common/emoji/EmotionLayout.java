package com.common.emoji;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.common.base.R;
import com.common.emoji.sticker.StickerCategory;
import com.common.emoji.sticker.StickerManager;
import com.common.utils.U;

import java.util.List;

/**
 * CSDN_LQR
 * 表情布局
 */
public class EmotionLayout extends LinearLayout implements View.OnClickListener {

    public static final int EMOJI_COLUMNS = 7;
    public static final int EMOJI_ROWS = 3;
    public static final int EMOJI_PER_PAGE = EMOJI_COLUMNS * EMOJI_ROWS - 1;//最后一个是删除键

    public static final int STICKER_COLUMNS = 4;
    public static final int STICKER_ROWS = 2;
    public static final int STICKER_PER_PAGE = STICKER_COLUMNS * STICKER_ROWS;

    private int mMeasuredWidth;
    private int mMeasuredHeight;

    //    private int mTabPosi = 0;
    private Context mContext;
    private ViewPager mVpEmotioin;
    private LinearLayout mLlPageNumber;
    private LinearLayout mLlTabContainer;
    private RelativeLayout mRlEmotionAdd;
    private View mBottomSplitLine;
    private LinearLayout mBottomContainer;

    private int mTabCount;
    private SparseArray<View> mTabViewArray = new SparseArray<>();
    private EmotionViewPagerAdapter mEmotionViewPagerAdapter;
    private EmotionTab mSettingTab;

    private boolean mShowSticker = true;
    private EditText mMessageEditText;
    private IEmotionSelectedListener mEmotionSelectedListener;
    private IEmotionExtClickListener mEmotionExtClickListener;
    private boolean mEmotionAddVisiable = false;
    private boolean mEmotionSettingVisiable = false;


    public EmotionLayout(Context context) {
        this(context, null);
    }

    public EmotionLayout(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EmotionLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        init();
        initListener();
    }

    private void init() {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.emotion_layout, this);

        mVpEmotioin = (ViewPager) findViewById(R.id.vpEmotioin);
        mLlPageNumber = (LinearLayout) findViewById(R.id.llPageNumber);
        mLlTabContainer = (LinearLayout) findViewById(R.id.llTabContainer);
        mRlEmotionAdd = (RelativeLayout) findViewById(R.id.rlEmotionAdd);
        mBottomSplitLine = (View) findViewById(R.id.bottom_split_line);
        mBottomContainer = (LinearLayout) findViewById(R.id.bottom_container);
        if (mShowSticker) {
            setEmotionAddVisiable(mEmotionAddVisiable);
        } else {
            mBottomContainer.setVisibility(GONE);
            mBottomSplitLine.setVisibility(GONE);
        }
        if (mShowSticker) {
            initTabs();
        }
        mEmotionViewPagerAdapter = new EmotionViewPagerAdapter(mMeasuredWidth, mMeasuredHeight, mEmotionSelectedListener, mShowSticker);
        mVpEmotioin.setAdapter(mEmotionViewPagerAdapter);
        mEmotionViewPagerAdapter.attachEditText(mMessageEditText);
        setSelectTab(0);
    }

    //表情面板底部类别栏
    private void initTabs() {
        //默认添加一个表情tab
        EmotionTab emojiTab = new EmotionTab(mContext, R.drawable.ic_tab_emoji);
        mLlTabContainer.addView(emojiTab);
        mTabViewArray.put(0, emojiTab);

        //添加所有的贴图tab
        List<StickerCategory> stickerCategories = StickerManager.getInstance().getStickerCategories();
        for (int i = 0; i < stickerCategories.size(); i++) {
            StickerCategory category = stickerCategories.get(i);
            EmotionTab tab = new EmotionTab(mContext, category.getCoverImgPath());
            mLlTabContainer.addView(tab);
            mTabViewArray.put(i + 1, tab);
        }

        //最后添加一个表情设置Tab
        mSettingTab = new EmotionTab(mContext, R.drawable.ic_emotion_setting);
        StateListDrawable drawable = new StateListDrawable();
        Drawable unSelected = mContext.getResources().getDrawable(R.color.white);
        drawable.addState(new int[]{-android.R.attr.state_pressed}, unSelected);
        Drawable selected = mContext.getResources().getDrawable(R.color.gray);
        drawable.addState(new int[]{android.R.attr.state_pressed}, selected);
        mSettingTab.setBackground(drawable);
        mLlTabContainer.addView(mSettingTab);
        mTabViewArray.put(mTabViewArray.size(), mSettingTab);
        setEmotionSettingVisiable(mEmotionSettingVisiable);
    }

    private void initListener() {
        if (mLlTabContainer != null) {
            mTabCount = mLlTabContainer.getChildCount() - 1;//不包含最后的设置按钮
            for (int position = 0; position < mTabCount; position++) {
                View tab = mLlTabContainer.getChildAt(position);
                tab.setTag(position);
                tab.setOnClickListener(this);
            }
        }

        mVpEmotioin.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                setCurPageCommon(position);
            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        if (mRlEmotionAdd != null) {
            mRlEmotionAdd.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mEmotionExtClickListener != null) {
                        mEmotionExtClickListener.onEmotionAddClick(v);
                    }
                }
            });
        }
        if (mSettingTab != null) {
            mSettingTab.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mEmotionExtClickListener != null) {
                        mEmotionExtClickListener.onEmotionSettingClick(v);
                    }
                }
            });
        }

    }

    public void setEmotionSelectedListener(IEmotionSelectedListener emotionSelectedListener) {
        if (emotionSelectedListener != null) {
            this.mEmotionSelectedListener = emotionSelectedListener;
        } else {
            Log.i("CSDN_LQR", "IEmotionSelectedListener is null");
        }
    }


    public void setEmotionExtClickListener(IEmotionExtClickListener emotionExtClickListener) {
        if (emotionExtClickListener != null) {
            this.mEmotionExtClickListener = emotionExtClickListener;
        } else {
            Log.i("CSDN_LQR", "IEmotionSettingTabClickListener is null");
        }
    }


    /**
     * 设置表情添加按钮的显隐
     *
     * @param visiable
     */
    public void setEmotionAddVisiable(boolean visiable) {
        mEmotionAddVisiable = visiable;
        if (mRlEmotionAdd != null) {
            mRlEmotionAdd.setVisibility(mEmotionAddVisiable ? View.VISIBLE : View.GONE);
        }
    }

    /**
     * 设置表情设置按钮的显隐
     *
     * @param visiable
     */
    public void setEmotionSettingVisiable(boolean visiable) {
        mEmotionSettingVisiable = visiable;
        if (mSettingTab != null) {
            mSettingTab.setVisibility(mEmotionSettingVisiable ? View.VISIBLE : View.GONE);
        }
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mMeasuredWidth = measureWidth(widthMeasureSpec);
        mMeasuredHeight = measureHeight(heightMeasureSpec);
        setMeasuredDimension(mMeasuredWidth, mMeasuredHeight);
    }


    private int measureWidth(int measureSpec) {
        int result = 0;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        } else {
            result = U.getDisplayUtils().dip2px(200);
            if (specMode == MeasureSpec.AT_MOST) {
                result = Math.min(result, specSize);
            }
        }
        return result;
    }

    private int measureHeight(int measureSpec) {
        int result = 0;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        } else {
            result = U.getDisplayUtils().dip2px(200);
            if (specMode == MeasureSpec.AT_MOST) {
                result = Math.min(result, specSize);
            }
        }
        return result;
    }

    private void setCurPageCommon(int vpPostion) {
        if (vpPostion < mEmotionViewPagerAdapter.getEmojiPageCount()) {
            setSelectTab(0);
            setCurPage(vpPostion, mEmotionViewPagerAdapter.getEmojiPageCount());
        } else {
            EmotionViewPagerAdapter.IndexInfo indexInfo = mEmotionViewPagerAdapter.getIndexInfoByPostion(vpPostion);
            if (indexInfo != null) {
                setSelectTab(indexInfo.mTabPostion);
                setCurPage(indexInfo.mIndexInGroup, indexInfo.mStickerGroup.mStickerItemPageList.size());
            }
        }
    }

    @Override
    public void onClick(View v) {
        int tabPosi = (int) v.getTag();
        //显示表情内容
        mLlPageNumber.removeAllViews();
        int vpPostion = mEmotionViewPagerAdapter.findFirstVpPostionByTabPostion(tabPosi);
        mVpEmotioin.setCurrentItem(vpPostion, false);

        setCurPageCommon(vpPostion);
    }

    private void setSelectTab(int tabPosi) {
        if (mBottomContainer.getVisibility() == VISIBLE) {
            for (int i = 0; i < mTabCount; i++) {
                View tab = mTabViewArray.get(i);
                if (tab != null) {
                    tab.setBackgroundResource(R.drawable.shape_tab_normal);
                }
            }
            View v = mTabViewArray.get(tabPosi);
            if (v != null) {
                v.setBackgroundResource(R.drawable.shape_tab_press);
            }
        }
    }

    /**
     * 选表情时的页面索引小点点
     *
     * @param page      当前在哪个点
     * @param pageCount 一共几个点
     */
    private void setCurPage(int page, int pageCount) {
        int hasCount = mLlPageNumber.getChildCount();
        int forMax = Math.max(hasCount, pageCount);

        ImageView ivCur = null;
        for (int i = 0; i < forMax; i++) {
            if (pageCount <= hasCount) {
                if (i >= pageCount) {
                    mLlPageNumber.getChildAt(i).setVisibility(View.GONE);
                    continue;
                } else {
                    ivCur = (ImageView) mLlPageNumber.getChildAt(i);
                }
            } else {
                if (i < hasCount) {
                    ivCur = (ImageView) mLlPageNumber.getChildAt(i);
                } else {
                    ivCur = new ImageView(mContext);
                    ivCur.setBackgroundResource(R.drawable.selector_view_pager_indicator);
                    LayoutParams params = new LayoutParams(U.getDisplayUtils().dip2px(8), U.getDisplayUtils().dip2px(8));
                    ivCur.setLayoutParams(params);
                    params.leftMargin = U.getDisplayUtils().dip2px(3);
                    params.rightMargin = U.getDisplayUtils().dip2px(3);
                    mLlPageNumber.addView(ivCur);
                }
            }
            ivCur.setId(i);
            ivCur.setSelected(i == page);
            ivCur.setVisibility(View.VISIBLE);
        }
    }

    public void attachEditText(EditText messageEditText) {
        mMessageEditText = messageEditText;
    }

    public void setShowSticker(boolean showSticker) {
        this.mShowSticker = showSticker;
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        Log.d("OOOOOOO","setVisibility" + " visibility=" + visibility);

    }

}
