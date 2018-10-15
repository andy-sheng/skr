package com.wali.live.livesdk.live.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.base.global.GlobalData;
import com.base.image.fresco.BaseImageView;
import com.base.image.fresco.FrescoWorker;
import com.base.image.fresco.image.ImageFactory;
import com.base.utils.display.DisplayUtils;
import com.base.utils.image.ImageUtils;
import com.base.utils.span.SpanUtils;
import com.mi.live.data.config.GetConfigManager;
import com.mi.live.data.push.model.BarrageMsgType;
import com.mi.live.data.repository.GiftRepository;
import com.wali.live.common.barrage.view.adapter.LiveCommentRecyclerAdapter;
import com.wali.live.common.heartview.HeartItemManager;
import com.wali.live.common.model.CommentModel;
import com.wali.live.dao.Gift;
import com.wali.live.livesdk.R;

import java.util.HashMap;

/**
 * Created by chenyong on 2016/12/16.
 */

public class GameRepeatScrollItemView extends RelativeLayout {
    private static final String TAG = "GameRepeatScrollItemView";

    TextView mBarrageTv;
    BaseImageView mGiftIv;

    private SpannableStringBuilder mCommentSpan = new SpannableStringBuilder();

    private HashMap<String, Bitmap> mBitmapHashMap = new HashMap<>();

    public GameRepeatScrollItemView(Context context) {
        super(context);
        init(context);
    }

    public GameRepeatScrollItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public GameRepeatScrollItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private <T> T $(int id) {
        return (T) findViewById(id);
    }

    private void init(Context context) {
        inflate(context, R.layout.game_chat_room_scroll_item_view, this);
        mBarrageTv = $(R.id.barrage_tv);
        mGiftIv = $(R.id.gift_iv);
    }

    public void setCommentContent(CommentModel commentModel) {
        mCommentSpan.clear();
        mCommentSpan.clearSpans();
        bindCommentView(commentModel);
        mBarrageTv.setText(mCommentSpan);
    }

    public void bindCommentView(CommentModel commentModel) {
        String name = commentModel.getName();
        if (!TextUtils.isEmpty(name)) {
            if (name.length() > LiveCommentRecyclerAdapter.DEFAULT_SUB_NAME_LENGTH) {
                name = name.substring(0, LiveCommentRecyclerAdapter.DEFAULT_SUB_NAME_LENGTH);
                name += "...";
            }
        }
        setNameSpan(name, commentModel.getNameColor(), commentModel.getCertificationType(),
                commentModel.getMsgType() != BarrageMsgType.B_MSG_TYPE_TEXT, commentModel.getMsgType() == BarrageMsgType.B_MSG_TYPE_SELL);
        if ((commentModel.getMsgType() == BarrageMsgType.B_MSG_TYPE_GIFT
                || commentModel.getMsgType() == BarrageMsgType.B_MSG_TYPE_GLABAL_MSG)) {
            Gift gift = GiftRepository.findGiftById((int) commentModel.getGiftId());
            if (gift != null) {
                mGiftIv.setVisibility(View.VISIBLE);
                setComment(GlobalData.app().getString(R.string.game_give_one_gift, gift.getName()), commentModel.getCommentColor());
                FrescoWorker.loadImage(mGiftIv, ImageFactory.newHttpImage(gift.getPicture()).build());
                mBarrageTv.setMaxWidth(DisplayUtils.dip2px(190));
            }
        } else {
            mBarrageTv.setMaxWidth(Integer.MAX_VALUE);
            mGiftIv.setVisibility(View.GONE);
            setComment(commentModel.getBody(), commentModel.getCommentColor());
        }

        if (commentModel.getMsgType() == BarrageMsgType.B_MSG_TYPE_LIKE) {
            Drawable drawable = null;
            // 先找有没有特别的特效图片
            String path = commentModel.getLikePath();
            if (!TextUtils.isEmpty(path)) {
                Bitmap bitmap = null;
                if (!mBitmapHashMap.containsKey(path)) {
                    bitmap = ImageUtils.getLocalBitmap(path);
                    mBitmapHashMap.put(path, bitmap);
                } else {
                    bitmap = mBitmapHashMap.get(path);
                }
                if (bitmap != null) {
                    drawable = new BitmapDrawable(bitmap);
                }
            }
            // 如果没有，用星星
            if (drawable == null) {
                int resId = HeartItemManager.getResId(commentModel.getLikeId() - 1);
                if (resId != 0) {
                    drawable = GlobalData.app().getResources().getDrawable(resId);
                }
            }
            if (drawable != null) {
                drawable.setBounds(0, 0, DisplayUtils.dip2px(16f), DisplayUtils.dip2px(16f));
                setLikeDrawable(drawable);
            }
        }
    }

    private void setNameSpan(String name, int colorId, int certificationType, boolean needSpace, boolean isSell) {
        if (TextUtils.isEmpty(name)) {
            return;
        }

        mCommentSpan.append(name);
        if (certificationType > 0) {
            // 微博小飞镖
            int len = mCommentSpan.length();
            mCommentSpan.append("a");
            Drawable drawable = GetConfigManager.getInstance().getCertificationTypeDrawable(certificationType).certificationDrawableLiveComment;
            mCommentSpan.setSpan(new SpanUtils.MyImageSpan(drawable, ImageSpan.ALIGN_BOTTOM),
                    len, mCommentSpan.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        if (needSpace) {
            mCommentSpan.append(" ");
        }
        if (isSell) {
            mCommentSpan.setSpan(new ForegroundColorSpan(GlobalData.app().getResources().getColor(com.live.module.common.R.color.color_f6b723)),
                    0, mCommentSpan.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        } else {
            // 颜色
            mCommentSpan.setSpan(new ForegroundColorSpan(GlobalData.app().getResources().getColor(colorId)),
                    0, mCommentSpan.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void setComment(CharSequence comment, int colorId) {
        int len = mCommentSpan.length();
        mCommentSpan.append(comment);
        mCommentSpan.setSpan(new ForegroundColorSpan(GlobalData.app().getResources().getColor(colorId)),
                len, mCommentSpan.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        mBarrageTv.setShadowLayer(2f, 2.5f, 2.5f, com.live.module.common.R.color.color_black);
        mBarrageTv.setPadding(0, 0, 16, 3);
        mBarrageTv.setBackground(null);
    }

    private void setLikeDrawable(Drawable drawable) {
        int len = mCommentSpan.length();
        mCommentSpan.append("a");
        int currentSdk = Build.VERSION.SDK_INT;
        int lineSpace = currentSdk >= 22 ? 0 : DisplayUtils.dip2px(5);//行间距需求5.0版本适配
        SpanUtils.MyImageSpan imageSpan = new SpanUtils.MyImageSpan(drawable, ImageSpan.ALIGN_BASELINE, lineSpace);
        mCommentSpan.setSpan(imageSpan,
                len, mCommentSpan.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }
}
