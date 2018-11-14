package com.common.emoji;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.text.Editable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.RelativeLayout;

import com.common.emoji.sticker.StickerAdapter;
import com.common.emoji.sticker.StickerCategory;
import com.common.emoji.sticker.StickerItem;
import com.common.emoji.sticker.StickerManager;
import com.common.utils.U;

import java.util.ArrayList;
import java.util.List;


/**
 * CSDN_LQR
 * 表情控件的ViewPager适配器(emoji + 贴图)
 * <p>
 * 这里涉及到两个属性
 * tabposition 在底部表情分类栏tab索引
 * vppostion 在viewpager的索引
 */
public class EmotionViewPagerAdapter extends PagerAdapter {
    private int mPageCount;
    private int mEmojiPageNum = 0;
    private ArrayList<StickerGroup> mStickerGroupList = new ArrayList<>();

    private int mEmotionLayoutWidth;
    private int mEmotionLayoutHeight;

    private IEmotionSelectedListener listener;
    EditText mMessageEditText;

    public static class StickerGroup {
        public StickerCategory mStickerCategory;
        public ArrayList<ArrayList<StickerItem>> mStickerItemPageList = new ArrayList<>();
    }


    public void attachEditText(EditText messageEditText) {
        mMessageEditText = messageEditText;
    }

    public EmotionViewPagerAdapter(int emotionLayoutWidth, int emotionLayoutHeight, IEmotionSelectedListener listener, boolean loadSticker) {
        mEmotionLayoutWidth = emotionLayoutWidth;
        mEmotionLayoutHeight = emotionLayoutHeight;
        mEmojiPageNum = (int) Math.ceil(EmojiManager.getDisplayCount() / (float) EmotionLayout.EMOJI_PER_PAGE);
        mPageCount += mEmojiPageNum;

        if (loadSticker) {
            List<StickerCategory> stickerCategoryList = StickerManager.getInstance().getStickerCategories();
            for (int i = 0; i < stickerCategoryList.size(); i++) {
                StickerCategory stickerCategory = stickerCategoryList.get(i);
                StickerGroup stickerGroup = new StickerGroup();
                stickerGroup.mStickerCategory = stickerCategory;

                ArrayList<StickerItem> oneGroup = new ArrayList<>();
                for (int j = 0; j < stickerCategory.getCount(); j++) {
                    StickerItem sticker = stickerCategory.getStickers().get(j);
                    if (sticker != null) {
                        oneGroup.add(sticker);
                        if (oneGroup.size() == EmotionLayout.STICKER_PER_PAGE) {
                            stickerGroup.mStickerItemPageList.add(oneGroup);
                            oneGroup = new ArrayList<>();
                        }
                    }
                }
                if (!oneGroup.isEmpty()) {
                    stickerGroup.mStickerItemPageList.add(oneGroup);
                }
                mPageCount += stickerGroup.mStickerItemPageList.size();
                mStickerGroupList.add(stickerGroup);
            }
        }
        this.listener = listener;
    }

    public int getEmojiPageCount() {
        return mEmojiPageNum;
    }

    /**
     * 传入一个大的类别索引
     * 返回改类别在viewpager的第一页的索引
     *
     * @param tabPosi
     * @return
     */
    public int findFirstVpPostionByTabPostion(int tabPosi) {
        if (tabPosi == 0) {
            return 0;
        } else {
            int index = mEmojiPageNum;
            for (int i = 0; i < tabPosi - 1; i++) {
                index += mStickerGroupList.get(i).mStickerItemPageList.size();
            }
            return index;
        }
    }

    /**
     * 聚合返回值用的
     */
    public static class IndexInfo {
        public StickerGroup mStickerGroup;
        public int mTabPostion;
        public int mIndexInGroup;

        public IndexInfo(StickerGroup stickerGroup, int tabPostion, int indexInGroup) {
            mStickerGroup = stickerGroup;
            mTabPostion = tabPostion;
            mIndexInGroup = indexInGroup;
        }
    }

    /**
     * 传入vppostion 返回对应表情组和表情页在该组的索引
     *
     * @param vpPosition
     * @return
     */
    public IndexInfo getIndexInfoByPostion(int vpPosition) {
        if (vpPosition < mEmojiPageNum) {
            return new IndexInfo(null, 0, vpPosition);
        } else {
            int position = vpPosition - mEmojiPageNum;
            int index = 0;
            // 找到大于postion的最小的一个
            for (int i = 0; i < mStickerGroupList.size(); i++) {
                StickerGroup group = mStickerGroupList.get(i);
                if (group.mStickerItemPageList.size() + index > position) {
                    return new IndexInfo(group, i + 1, position - index);
                } else {
                    index += group.mStickerItemPageList.size();
                }
            }
        }
        return null;
    }

    @Override
    public int getCount() {
        return mPageCount;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {

        Context context = container.getContext();
        RelativeLayout rl = new RelativeLayout(context);
        rl.setGravity(Gravity.CENTER);

        GridView gridView = new GridView(context);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        gridView.setLayoutParams(params);
        gridView.setGravity(Gravity.CENTER);

        gridView.setTag(position);//标记自己是第几页
        if (position < mEmojiPageNum) {
            // 是小表情
            gridView.setOnItemClickListener(emojiListener);
            gridView.setAdapter(new EmojiAdapter(context, mEmotionLayoutWidth, mEmotionLayoutHeight, position * EmotionLayout.EMOJI_PER_PAGE));
            gridView.setNumColumns(EmotionLayout.EMOJI_COLUMNS);
        } else {
            IndexInfo indexInfo = getIndexInfoByPostion(position);
            gridView.setOnItemClickListener(stickerListener);
            List<StickerItem> stickerItems = indexInfo.mStickerGroup.mStickerItemPageList.get(indexInfo.mIndexInGroup);
            gridView.setAdapter(new StickerAdapter(context, stickerItems, mEmotionLayoutWidth, mEmotionLayoutHeight));
            gridView.setNumColumns(EmotionLayout.STICKER_COLUMNS);
        }

        rl.addView(gridView);
        container.addView(rl);
        return rl;

    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    public AdapterView.OnItemClickListener emojiListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Integer vpPostion = (Integer) parent.getTag();
            int index = position + vpPostion * EmotionLayout.EMOJI_PER_PAGE;
            int count = EmojiManager.getDisplayCount();
            if (position == EmotionLayout.EMOJI_PER_PAGE || index >= count) {
                if (listener != null) {
                    listener.onEmojiSelected("/DEL");
                }
                onEmojiSelected("/DEL");
            } else {
                String text = EmojiManager.getDisplayText((int) id);
                if (!TextUtils.isEmpty(text)) {
                    if (listener != null) {
                        listener.onEmojiSelected(text);
                    }
                    onEmojiSelected(text);
                }
            }
        }
    };
    public AdapterView.OnItemClickListener stickerListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Integer vpPostion = (Integer) parent.getTag();
            if (listener != null) {
                IndexInfo indexInfo = getIndexInfoByPostion(vpPostion);
                List<StickerItem> stickerItems = indexInfo.mStickerGroup.mStickerItemPageList.get(indexInfo.mIndexInGroup);
                StickerItem sticker = stickerItems.get(position);
                StickerCategory real = StickerManager.getInstance().getCategory(sticker.getCategory());

                if (real == null) {
                    return;
                }

                listener.onStickerSelected(sticker.getCategory(), sticker.getName(), StickerManager.getInstance().getStickerBitmapPath(sticker.getCategory(), sticker.getName()));
            }
        }
    };

    private void onEmojiSelected(String key) {
        if (mMessageEditText == null) {
            return;
        }
        Editable editable = mMessageEditText.getText();
        if (key.equals("/DEL")) {
            mMessageEditText.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL));
        } else {
            int start = mMessageEditText.getSelectionStart();
            int end = mMessageEditText.getSelectionEnd();
            start = (start < 0 ? 0 : start);
            end = (start < 0 ? 0 : end);
            editable.replace(start, end, key);

            int editEnd = mMessageEditText.getSelectionEnd();
            MoonUtils.replaceEmoticons(U.app(), editable, 0, editable.toString().length());
            mMessageEditText.setSelection(editEnd);
        }
    }

}
