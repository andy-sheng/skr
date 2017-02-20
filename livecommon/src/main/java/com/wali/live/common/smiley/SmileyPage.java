package com.wali.live.common.smiley;

import android.content.Context;
import android.text.Editable;
import android.view.KeyEvent;
import android.view.View;
import android.widget.LinearLayout;


import com.live.module.common.R;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscriber;

/**
 * @module smileypick
 * <p/>
 * Created by MK on 15/10/20.
 */
public class SmileyPage extends LinearLayout {

    private int mColumnNum;
    private int mRowNum;

    private int mSmileyCount = 0;

    private LinearLayout mCurrentRow;
    public int mPageNum; // 同类表情的页数
    public int mPageIndex; // 在同类表情中的位置

    public boolean mIsInited = false;
    private List<SmileyPicker.SmileyItemData> mData = new ArrayList<>();

    private SmileyPicker mPicker;

    public SmileyPage(Context context, int columnNum, int rowNum, int pageNum, int pageIndex,
                      SmileyPicker picker) {
        super(context);
        mColumnNum = columnNum;
        mRowNum = rowNum;
        setOrientation(LinearLayout.VERTICAL);
        mPageNum = pageNum;
        mPageIndex = pageIndex;
        mPicker = picker;
    }

    public void addSmiley(SmileyPicker.SmileyItemData data) {
        mData.add(data);
        mSmileyCount++;
    }

    private boolean addSmileyView(View view) {
        if (mSmileyCount >= mColumnNum * mRowNum) {
            return false;
        }
        if (mSmileyCount % mColumnNum == 0) {
            int childCnt = this.getChildCount();
            int currentCnt = mSmileyCount / mColumnNum;
            if (currentCnt >= childCnt) {
                mCurrentRow = new LinearLayout(getContext());
                mCurrentRow.setOrientation(LinearLayout.HORIZONTAL);
                addView(mCurrentRow);
                LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, 0);
                params.weight = 1.0f;
                mCurrentRow.setLayoutParams(params);
            } else {
                mCurrentRow = (LinearLayout) this.getChildAt(currentCnt);
            }
        }
        mCurrentRow.addView(view);
        LayoutParams params = new LayoutParams(0, LayoutParams.MATCH_PARENT);
        params.weight = 1.0f;
        view.setLayoutParams(params);
        mSmileyCount++;
        return true;
    }

    private SmileyItem buildSmileyItem() {
        SmileyItem si = null;
        if (mPicker.getSmileyItemCaches() != null && mPicker.getSmileyItemCaches().size() > 0) {
            si = mPicker.getSmileyItemCaches().remove(0);
        }
        if (null == si) {
            si = new SmileyItem(getContext(), R.layout.smiley_item);

        }
        return si;
    }

    public void load() {
        try {

            mSmileyCount = 0;

            for (int i = 0; i < mColumnNum * mRowNum - 1; i++) {
                SmileyItem item = buildSmileyItem();
                if (i < mData.size()) {
                    item.setImageDrawableResId(mData.get(i).mImageResId);
                    final String emojString = mData.get(i).mText;

                    item.setOnClickListener(new OnClickListener() {

                        @Override
                        public void onClick(View arg0) {
                            int cursorPos = mPicker.getEditText().getSelectionStart();
                            mPicker.getEditText().getText().insert(
                                    cursorPos,
                                    SmileyParser.getInstance().addSmileySpans(getContext(), emojString,
                                            mPicker.getEditText().getTextSize(), true));
                        }
                    });
                }
                addSmileyView(item);
            }
            SmileyItem deleteItem = buildSmileyItem();
            deleteItem.setImageDrawableResId(R.drawable.smilies_bottom_icon_delete);
            deleteItem.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    doDelete();
                }
            });
            addSmileyView(deleteItem);

            mIsInited = true;
        }catch (OutOfMemoryError error){
        }
    }

    private void doDelete() {
        if (mPicker.getEditText() == null) {
            throw new IllegalStateException("EditText must be set.(use setEditText())");
        }

        try {
            Editable editable = mPicker.getEditText().getText();
            int end = mPicker.getEditText().getSelectionStart();
            int start = end - 1;

            if (start < 0) {
                return;
            }

            String text = editable.subSequence(start, end).toString();
            if ("】".equals(text)) {
                String result = "";
                for (start--; start >= 0; start--) {
                    result = editable.subSequence(start, start + 1).toString();
                    if ("【".equals(result)) {
                        break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        mPicker.getEditText().dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL));
    }

    public void removeItems(final List<SmileyItem> itemCaches) {
        mIsInited = false;
        if (null != itemCaches) {
            int lCnt = this.getChildCount();
            if (lCnt > 0) {
                for (int i = 0; i < lCnt; i++) {
                    LinearLayout ll = (LinearLayout) this.getChildAt(i);
                    if (null != ll) {
                        int iCnt = ll.getChildCount();
                        if (iCnt > 0) {
                            for (int j = 0; j < iCnt; j++) {
                                SmileyItem si = (SmileyItem) ll.getChildAt(j);
                                if (null != si) {
                                    si.reset();
                                    itemCaches.add(si);
                                }
                            }
                        }
                        ll.removeAllViews();
                    }
                }
                this.removeAllViews();
            }
        }
    }

}
