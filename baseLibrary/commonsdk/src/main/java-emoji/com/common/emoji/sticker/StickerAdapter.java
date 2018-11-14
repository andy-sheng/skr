package com.common.emoji.sticker;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;

import com.common.emoji.EmotionLayout;
import com.common.image.fresco.BaseImageView;
import com.common.image.fresco.FrescoWorker;
import com.common.image.model.ImageFactory;
import com.common.utils.U;

import java.util.List;

/**
 * CSDN_LQR
 * 贴图适配器
 */

public class StickerAdapter extends BaseAdapter {

    private Context mContext;
    List<StickerItem> mDataList;
    private int mEmotionLayoutWidth;
    private int mEmotionLayoutHeight;
    private float mPerWidth;
    private float mPerHeight;
    private float mIvSize;

    public StickerAdapter(Context context, List<StickerItem> dataList, int emotionLayoutWidth, int emotionLayoutHeight) {
        mContext = context;
        this.mDataList = dataList;

        mEmotionLayoutWidth = emotionLayoutWidth;
        mEmotionLayoutHeight = emotionLayoutHeight - U.getDisplayUtils().dip2px(35 + 26 + 50);//减去底部的tab高度、小圆点的高度才是viewpager的高度，再减少30dp是让表情整体的顶部和底部有个外间距
        mPerWidth = mEmotionLayoutWidth * 1f / EmotionLayout.STICKER_COLUMNS;
        mPerHeight = mEmotionLayoutHeight * 1f / EmotionLayout.STICKER_ROWS;

        float ivWidth = mPerWidth * .8f;
        float ivHeight = mPerHeight * .8f;
        mIvSize = Math.min(ivWidth, ivHeight);
    }


    @Override
    public int getCount() {
        int count = mDataList.size();
        return count;
    }

    @Override
    public Object getItem(int position) {
        return mDataList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        StickerViewHolder viewHolder;
        if (convertView == null) {
            RelativeLayout rl = new RelativeLayout(mContext);
            rl.setLayoutParams(new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, (int) mPerHeight));

            BaseImageView imageView = new BaseImageView(mContext);
//            imageView.setImageResource(R.drawable.ic_tab_emoji);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            params.width = (int) mIvSize;
            params.height = (int) mIvSize;
            params.addRule(RelativeLayout.CENTER_IN_PARENT);
            imageView.setLayoutParams(params);

            rl.addView(imageView);

            viewHolder = new StickerViewHolder();
            viewHolder.mImageView = imageView;

            convertView = rl;
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (StickerViewHolder) convertView.getTag();
        }

        StickerItem sticker = mDataList.get(position);
        String stickerBitmapPath = StickerManager.getInstance().getStickerBitmapPath(sticker.getCategory(), sticker.getName());
        FrescoWorker.loadImage(viewHolder.mImageView,ImageFactory.newLocalImage(stickerBitmapPath).build());
        return convertView;
    }

    static class StickerViewHolder {
        public BaseImageView mImageView;
    }
}
