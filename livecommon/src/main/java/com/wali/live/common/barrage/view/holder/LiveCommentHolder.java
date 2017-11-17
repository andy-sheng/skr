package com.wali.live.common.barrage.view.holder;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.view.View;
import android.widget.TextView;

import com.base.global.GlobalData;
import com.base.image.fresco.BaseImageView;
import com.base.log.MyLog;
import com.base.utils.CommonUtils;
import com.base.utils.display.DisplayUtils;
import com.base.utils.span.SpanUtils;
import com.live.module.common.R;
import com.mi.live.data.config.GetConfigManager;
import com.wali.live.common.barrage.event.BarrageCommonEvent;
import com.wali.live.common.barrage.view.adapter.LiveCommentRecyclerAdapter;
import com.wali.live.common.barrage.view.utils.CommentFansLevelCache;
import com.wali.live.common.barrage.view.utils.FansInfoUtils;
import com.wali.live.common.model.CommentModel;

import org.greenrobot.eventbus.EventBus;

import java.lang.ref.WeakReference;

/**
 * Created by chengsimin on 16/7/18.
 */
public class LiveCommentHolder extends RecyclerView.ViewHolder {
    public TextView levelTv;
    public TextView barrageTv;
    public TextView goodsTitle;
    public TextView goodsPrice;
    public BaseImageView giftIv;
    SpannableStringBuilder commentSpan;

    public LiveCommentHolder(View itemView) {
        super(itemView);
        this.levelTv = (TextView) itemView.findViewById(R.id.level_tv);
        this.barrageTv = (TextView) itemView.findViewById(R.id.barrage_tv);
        this.giftIv = (BaseImageView) itemView.findViewById(R.id.gift_iv);
        this.commentSpan = new SpannableStringBuilder();
        this.barrageTv.setShadowLayer(2f, 2.5f, 2.5f, R.color.color_black);
        this.barrageTv.setMovementMethod(new LinkMovementMethod());
    }

    public void reset() {
        commentSpan.clear();
        commentSpan.clearSpans();
    }

    public void setAll() {
        barrageTv.setText(commentSpan);
    }


    public void setNameSpan(String name, int colorId, int certificationType, boolean needSpace, boolean isSell) {
        if (TextUtils.isEmpty(name)) {
            return;
        }

        commentSpan.append(name);
        if (certificationType > 0) {
            // 微博小飞镖
            int len = commentSpan.length();
            commentSpan.append("a");
            Drawable drawable = GetConfigManager.getInstance().getCertificationTypeDrawable(certificationType).certificationDrawableLiveComment;
            commentSpan.setSpan(new SpanUtils.MyImageSpan(drawable, ImageSpan.ALIGN_BOTTOM),
                    len, commentSpan.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        if (needSpace) {
            commentSpan.append(" ");
        }
        if (isSell) {
            commentSpan.setSpan(new ForegroundColorSpan(GlobalData.app().getResources().getColor(R.color.color_f6b723)),
                    0, commentSpan.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        } else {
            // 颜色
            commentSpan.setSpan(new ForegroundColorSpan(GlobalData.app().getResources().getColor(colorId)),
                    0, commentSpan.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

    }
    public void setFansMedalInfo() {
        if (!TextUtils.isEmpty(liveComment.getFansMedal()) && liveComment.getFansLevel() > 0 && this.mContext != null && this.mContext.get() != null) {
            Bitmap bitmap = CommentFansLevelCache.getLevelOrLike(String.valueOf(liveComment.getFansLevel()));

            if (bitmap == null) {
                bitmap = FansInfoUtils.getBitmapByFansLevel(liveComment.getFansLevel(), liveComment.getFansMedal(), this.mContext.get());
                CommentFansLevelCache.setLevelOrLike(String.valueOf(liveComment.getFansLevel()), bitmap);
            }
            Drawable levelDrawable = new BitmapDrawable(bitmap);
            int len = commentSpan.length();
            levelDrawable.setBounds(0, 0, (int) (levelDrawable.getIntrinsicWidth() * DisplayUtils.getDensity()), (int) (levelDrawable.getIntrinsicHeight() * DisplayUtils.getDensity()));
            int currentSdk = Build.VERSION.SDK_INT;
            int lineSpace = currentSdk >= 22 ? 0 : DisplayUtils.dip2px(5);//行间距需求5.0版本适配
            SpanUtils.CenterImageSpan imageSpan = new SpanUtils.CenterImageSpan(levelDrawable, lineSpace);
            commentSpan.append("a");
            commentSpan.setSpan(imageSpan, len, commentSpan.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            commentSpan.append(" ");
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void setComment(CharSequence comment, int colorId, SpanUtils.MyClickableSpan clickListener) {
        int len = commentSpan.length();
        commentSpan.append(comment);
        commentSpan.setSpan(new ForegroundColorSpan(GlobalData.app().getResources().getColor(colorId)),
                len, commentSpan.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        if (clickListener != null) {
            //点击
            commentSpan.setSpan(clickListener, 0, commentSpan.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        barrageTv.setShadowLayer(2f, 2.5f, 2.5f, R.color.color_black);
        barrageTv.setPadding(0, 0, 16, 3);
        barrageTv.setBackground(null);
    }

    public void setLikeDrawable(Drawable drawable) {
        //实验一下
        int len = commentSpan.length();
        commentSpan.append("a");
        int currentSdk = Build.VERSION.SDK_INT;
        int lineSpace = currentSdk >= 22 ? 0 : DisplayUtils.dip2px(5);//行间距需求5.0版本适配
        SpanUtils.MyImageSpan imageSpan = new SpanUtils.MyImageSpan(drawable, ImageSpan.ALIGN_BASELINE, lineSpace);
        commentSpan.setSpan(imageSpan,
                len, commentSpan.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    // 防止过多 new 对象
    private SpanUtils.MyClickableSpan clickSpan;
    private CommentModel liveComment;
    private LiveCommentRecyclerAdapter.NameClickListener mLiveCommentNameClickListener;

    public SpanUtils.MyClickableSpan getClickSpan() {
        if (clickSpan == null) {
            clickSpan = new SpanUtils.MyClickableSpan(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!liveComment.isCanClickable() || CommonUtils.isFastDoubleClick(1000)) {
                        return;
                    }
                    if (mContext != null && mContext.get() != null) {
                        if (mLiveCommentNameClickListener != null) {
                            mLiveCommentNameClickListener.onClickName(liveComment.getSenderId());
                        }
                        EventBus.getDefault().post(new BarrageCommonEvent.HideKeyBoardEvent());
                    }
                }
            });
        }
        return clickSpan;
    }

    public void setModel(CommentModel liveComment) {
        this.liveComment = liveComment;
    }

    public void setNameClickListener(LiveCommentRecyclerAdapter.NameClickListener mLiveCommentNameClickListener) {
        this.mLiveCommentNameClickListener = mLiveCommentNameClickListener;
    }

    WeakReference<Context> mContext;

    public void setContext(WeakReference<Context> mContext) {
        this.mContext = mContext;
    }
}
