package com.wali.live.common.barrage.view.holder;

import android.annotation.TargetApi;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.ColorRes;
import android.support.v4.util.Pair;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.TextView;

import com.base.global.GlobalData;
import com.base.image.fresco.BaseImageView;
import com.base.log.MyLog;
import com.base.utils.CommonUtils;
import com.base.utils.display.DisplayUtils;
import com.base.utils.span.SpanUtils;
import com.base.utils.toast.ToastUtils;
import com.live.module.common.R;
import com.mi.live.data.config.GetConfigManager;
import com.wali.live.common.barrage.view.adapter.LiveCommentRecyclerAdapter;
import com.wali.live.common.barrage.view.utils.CommentNobelLevelCache;
import com.wali.live.common.barrage.view.utils.CommentVfansLevelCache;
import com.wali.live.common.barrage.view.utils.CommentVipLevelIconCache;
import com.wali.live.common.barrage.view.utils.NobleConfigUtils;
import com.wali.live.common.barrage.view.utils.VfansInfoUtils;
import com.wali.live.common.model.CommentModel;
import com.wali.live.common.view.LevelIconsLayout;
import com.wali.live.event.HideSoftInputEvent;
import com.wali.live.event.JumpSchemeEvent;
import com.wali.live.utils.ItemDataFormatUtils;
import com.wali.live.utils.level.VipLevelUtil;

import org.greenrobot.eventbus.EventBus;

import java.lang.ref.WeakReference;
import java.util.List;

/**
 * Created by chengsimin on 16/7/18.
 */
public class LiveCommentHolder extends RecyclerView.ViewHolder {
    private static final String TAG = "LiveCommentHolder";

    public TextView levelTv;
    public TextView barrageTv;
    public TextView goodsTitle;
    public TextView goodsPrice;
    //    public BaseImageView imgGoods;
//    public LinearLayout llytImgArea;
    public BaseImageView giftIv;
    SpannableStringBuilder commentSpan;

    private final static double SPAN_TEXT_SCALE = 0.8;//由文本高度确定icon高度时需要的比例

    public LiveCommentHolder(View itemView) {
        super(itemView);
        this.levelTv = (TextView) itemView.findViewById(R.id.level_tv);
        this.barrageTv = (TextView) itemView.findViewById(R.id.barrage_tv);
        this.giftIv = (BaseImageView) itemView.findViewById(R.id.gift_iv);
//        this.imgGoods = (BaseImageView) itemView.findViewById(R.id.imgGoods);
//        this.goodsTitle = (TextView) itemView.findViewById(R.id.txtGoodsDetail);
//        this.llytImgArea = (LinearLayout) itemView.findViewById(R.id.imgInfoArea);
//        this.goodsPrice = (TextView) itemView.findViewById(R.id.txtGoodsPrice);
        this.commentSpan = new SpannableStringBuilder();
        this.barrageTv.setShadowLayer(1f, 1f, 1f, R.color.black);
        this.barrageTv.setMovementMethod(new LinkMovementMethod());
    }

    private void saveToClipboard(Context context, String barrageText) {
        ClipboardManager cm = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText(context.getString(R.string.copy_link), barrageText);
        cm.setPrimaryClip(clipData);
        ToastUtils.showToast(R.string.copied_to_clipboard);
    }

    public void reset() {
        commentSpan.clear();
        commentSpan.clearSpans();
        itemView.setAlpha(1.0f);
    }

    public void setAll() {
//        if (levelTv.getVisibility() == View.VISIBLE) {
//            //不能用LeadingMarginSpan加margin，5.0一下系统会异常换行
//            barrageTv.setText("         ");
//        }
        barrageTv.append(commentSpan);
        barrageTv.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
//TODO AtEvent
//                if (itemView.getContext() instanceof Activity
//                        && !AppCommonUtils.goLoginFragmentIfInTouristMode((Activity) itemView.getContext(), 0)) {
//                    String name = liveComment.getName();
//                    if (!TextUtils.isEmpty(name)) {
//                        EventBus.getDefault().post(AtEvent.newInstance(name, liveComment.getSenderId()));
//                    }
//                }
                return true;
            }
        });
    }

//    public void setSellGoodsInfo(String title, String imgUrl, double price, long productId) {
//        llytImgArea.setVisibility(View.VISIBLE);
//        goodsTitle.setText(title);
//        goodsPrice.setText(String.valueOf(price));
//        AvatarUtils.loadCoverByUrl(imgGoods, imgUrl, false);
//        llytImgArea.setOnClickListener(
//                v -> EventBus.getDefault().post(new EventClass.UserActionEvent(EventClass.UserActionEvent.EVENT_TYPE_CLICK_PUSH_IMG, productId, null))
//        );
//
//    }
//    public void setBackground() {
//        this.barrageTv.setShadowLayer(0f, 0f, 0f, R.color.color_black);
//        barrageTv.setBackgroundResource(R.drawable.live_bullet_screen_bg);
//        barrageTv.setPadding(16,16,16,16);
//    }

    /**
     * @param beforeNickNameConfigList 徽章
     * @param afterNickNameConfigList
     * @param name
     * @param colorId
     * @param certificationType
     * @param isNotTextBarrage
     * @param isSell
     * @param clickListener
     * @param isEnter
     */
    public void setNameSpan(List<String> beforeNickNameConfigList, List<String> afterNickNameConfigList, String name, @ColorRes int colorId,
                            int certificationType, boolean isNotTextBarrage, boolean isSell, SpanUtils.MyClickableSpan clickListener, boolean isEnter, boolean isGameLive) {

        if (TextUtils.isEmpty(name)) {
            return;
        }

        if (name.length() > 15 && isEnter) {
            name = name.substring(0, 12) + "...";
        }

        //勋章
        setMedalIconPrefix(beforeNickNameConfigList);
        commentSpan.append(name);

        if (certificationType > 0) {
            // 微博小飞镖
            commentSpan.append(" ");
            int len = commentSpan.length();
            commentSpan.append("a");

            Drawable drawable = ItemDataFormatUtils.getCertificationImgSource(certificationType);

            double scale = (double) drawable.getIntrinsicWidth() / (double) drawable.getIntrinsicHeight();//原图的宽高比

            MyLog.v("test " + scale + "  " + drawable.getIntrinsicWidth() + "  " + drawable.getIntrinsicHeight() + "   " + 20 / 16);
//
//            scale  = 20/16;

            int newMeasuredHeight = (int) (SPAN_TEXT_SCALE * barrageTv.getLineHeight());//重新测量后icon的高度
            int newMeasuredWidth = (int) (newMeasuredHeight * scale);//重新测量后icon的宽度
            int top = (barrageTv.getLineHeight() - newMeasuredHeight) / 2;
            drawable.setBounds(0, top, newMeasuredWidth, newMeasuredHeight);

//            drawable.setBounds(0, 0, (int) (drawable.getIntrinsicWidth() * DisplayUtils.getDensity()), (int) (drawable.getIntrinsicHeight() * DisplayUtils.getDensity()));//思敏老师的原代码
            int currentSdk = Build.VERSION.SDK_INT;
            int lineSpace = currentSdk >= Build.VERSION_CODES.LOLLIPOP_MR1 ? 0 : DisplayUtils.dip2px(5);//行间距需求5.0版本适配
            commentSpan.setSpan(new SpanUtils.CenterImageSpan(drawable, lineSpace),
                    len, commentSpan.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        //名字加粗
        if (!isGameLive){
            commentSpan.setSpan(new StyleSpan(Typeface.BOLD), 0, commentSpan.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        setMedalIconPrefix(afterNickNameConfigList);

        //是否是文本消息
        if (!isNotTextBarrage) {
            //是否是文本消息
            int len = commentSpan.length() - 2 > 0 ? commentSpan.length() - 2 : 0;
            commentSpan.setSpan(new ForegroundColorSpan(GlobalData.app().getResources().getColor(colorId)),
                    len, commentSpan.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            commentSpan.append(": ");

        } else {
            if (afterNickNameConfigList == null) {
                commentSpan.append(" ");
            }
        }

        if (isSell) {
            commentSpan.setSpan(new ForegroundColorSpan(GlobalData.app().getResources().getColor(R.color.color_f6b723)),
                    0, commentSpan.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        } else {
            // 颜色
            commentSpan.setSpan(new ForegroundColorSpan(GlobalData.app().getResources().getColor(colorId)),
                    0, commentSpan.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        if (clickListener != null) {
            //点击
            commentSpan.setSpan(clickListener, 0, commentSpan.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    public void setLevelInfo() {
        levelTv.setHorizontallyScrolling(false);
        //vipLevel
        Pair<Boolean, Integer> pair = VipLevelUtil.getLevelBadgeResId(liveComment.getVipLevel(),
                liveComment.isVipFrozen(), false);
        if (pair.first == true) {
            Context context = mContext.get();
            TextView vipLevelView = LevelIconsLayout.getDefaultTextView(context == null ? GlobalData.app() : context);
            vipLevelView.setBackgroundResource(pair.second);
            //vipLevelView.setText(String.valueOf(liveComment.getVipLevel()));
            Bitmap bitmap = CommentVipLevelIconCache.getVipLevelIconBitmap(liveComment.getVipLevel(), vipLevelView);
            Drawable vipLevelDrawable = new BitmapDrawable(bitmap);
            vipLevelDrawable.setBounds(0, 0, (int) (vipLevelDrawable.getIntrinsicWidth() * DisplayUtils.getDensity()),
                    (int) (vipLevelDrawable.getIntrinsicHeight() * DisplayUtils.getDensity()));
            updateCommonSpanInDrawable(vipLevelDrawable);
        }
    }

    public void setVfansMedalInfo() {
        if (!TextUtils.isEmpty(liveComment.getVfansMedal()) && liveComment.getVfansLevel() > 0
                && this.mContext != null && this.mContext.get() != null) {
            Bitmap bitmap = CommentVfansLevelCache.getLevelOrLike(String.valueOf(liveComment.getVfansLevel()));

            if (bitmap == null) {
                bitmap = VfansInfoUtils.getBitmapByVfansLevel(liveComment.getVfansLevel(),
                        liveComment.getVfansMedal(), this.mContext.get());
                CommentVfansLevelCache.setLevelOrLike(String.valueOf(liveComment.getVfansLevel()), bitmap);
            }

            Drawable levelDrawable = new BitmapDrawable(bitmap);
            levelDrawable.setBounds(0, 0, (int) (levelDrawable.getIntrinsicWidth() * DisplayUtils.getDensity()),
                    (int) (levelDrawable.getIntrinsicHeight() * DisplayUtils.getDensity()));
            updateCommonSpanInDrawable(levelDrawable);
        }
    }

    public void setNobleMedalInfo() {
        if (liveComment.isNoble()) {
            int nobelLevel = liveComment.getNobleLevel();
            Bitmap bitmap = CommentNobelLevelCache.getNobelLevelBitmap(String.valueOf(nobelLevel));
            if (bitmap == null) {
                int drawableResId = NobleConfigUtils.getImageResoucesByNobelLevelInBarrage(nobelLevel);
                bitmap = NobleConfigUtils.getBitmapByDrawableResId(drawableResId, mContext.get());
                CommentNobelLevelCache.setNobelLevelBitmap(String.valueOf(nobelLevel), bitmap);
            }

            Drawable levelDrawable = new BitmapDrawable(bitmap);
            levelDrawable.setBounds(0, 0, (int) (levelDrawable.getIntrinsicWidth() * DisplayUtils.getDensity()),
                    (int) (levelDrawable.getIntrinsicHeight() * DisplayUtils.getDensity()));
            updateCommonSpanInDrawable(levelDrawable);
        }
    }

    private void updateCommonSpanInDrawable(Drawable spanDrawable) {
        int currentSdk = Build.VERSION.SDK_INT;
        int lineSpace = currentSdk >= 22 ? 0 : DisplayUtils.dip2px(5);//行间距需求5.0版本适配
        SpanUtils.CenterImageSpan imageSpan = new SpanUtils.CenterImageSpan(spanDrawable, lineSpace);
        int len = commentSpan.length();
        commentSpan.append("a");
        commentSpan.setSpan(imageSpan, len, commentSpan.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        commentSpan.append(" ");
    }

    private void setMedalIconPrefix(List<String> medalList) {
        if (medalList != null && !medalList.isEmpty()) {
            for (int index = 0; index < medalList.size(); index++) {
                Drawable drawable = GetConfigManager.getInstance().getMedalProIconPrefix(medalList.get(index));
                if (drawable != null) {
                    drawable.setBounds(0, 0, (int) (drawable.getIntrinsicWidth() * DisplayUtils.getDensity()),
                            (int) (drawable.getIntrinsicHeight() * DisplayUtils.getDensity()));
                    updateCommonSpanInDrawable(drawable);
                }
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void setComment(List<String> beforeContentConfigList, List<String> afterContentConfigList,
                           CharSequence comment, @ColorRes int colorId, SpanUtils.MyClickableSpan clickListener) {
        setMedalIconPrefix(beforeContentConfigList);

        int len = commentSpan.length();
        commentSpan.append(comment);
        commentSpan.setSpan(new ForegroundColorSpan(GlobalData.app().getResources().getColor(colorId)),
                len, commentSpan.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        setMedalIconPrefix(afterContentConfigList);
        if (clickListener != null) {
            //点击
            commentSpan.setSpan(clickListener, len, commentSpan.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

//        barrageTv.setShadowLayer(2f, 2.5f, 2.5f, R.color.color_black);
//        barrageTv.setPadding(0, 0, 16, 3);
        barrageTv.setBackground(null);
    }

    public void setLikeDrawable(Drawable drawable) {
        //实验一下
        int len = commentSpan.length();
        commentSpan.append("a");
//        int currentSdk = Build.VERSION.SDK_INT;
//        int lineSpace = currentSdk >= 20 ? 0 : DisplayUtils.dip2px(5);//行间距需求5.0版本适配
        int lineSpace = 0;//不需要适配了
        SpanUtils.MyImageSpan imageSpan = new SpanUtils.MyImageSpan(drawable, ImageSpan.ALIGN_BASELINE, lineSpace);
        commentSpan.setSpan(imageSpan,
                len, commentSpan.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    // 防止过多 new 对象
    private SpanUtils.MyClickableSpan commentClickSpan;
    private SpanUtils.MyClickableSpan nameClickSpan;
    private CommentModel liveComment;
    private LiveCommentRecyclerAdapter.LiveCommentNameClickListener mLiveCommentNameClickListener;

    public SpanUtils.MyClickableSpan getClickSpan(boolean isFromNameClick) {
        if (isFromNameClick) {
            if (nameClickSpan == null) {
                nameClickSpan = new SpanUtils.MyClickableSpan(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!liveComment.isCanClickable() || CommonUtils.isFastDoubleClick(1000)) {
                            return;
                        }
                        if (mContext != null && mContext.get() != null) {
                            if (mLiveCommentNameClickListener != null) {
                                mLiveCommentNameClickListener.onClickName(liveComment.getSenderId());
                            }
                            EventBus.getDefault().post(new HideSoftInputEvent());
                        }
                    }
                });
            }
            return nameClickSpan;
        } else {
            if (commentClickSpan == null) {
                commentClickSpan = new SpanUtils.MyClickableSpan(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (TextUtils.isEmpty(liveComment.getInnerGlobalRoomMessageSchemaUrl())) {
                            return;
                        }

                        MyLog.d(TAG, "schemaUrl from onClick" + liveComment.getInnerGlobalRoomMessageSchemaUrl());

                        if (!liveComment.isCanClickable() || CommonUtils.isFastDoubleClick(1000)) {
                            return;
                        }
                        if (mContext != null && mContext.get() != null) {
                            if (mLiveCommentNameClickListener != null) {
                                mLiveCommentNameClickListener.onClickComment(liveComment.getInnerGlobalRoomMessageSchemaUrl());
                            } else {
                                EventBus.getDefault().post(new JumpSchemeEvent(liveComment.getInnerGlobalRoomMessageSchemaUrl()));
                            }
                            EventBus.getDefault().post(new HideSoftInputEvent());
                        }
                    }
                });
            }
            return commentClickSpan;
        }
    }

    public void setModel(CommentModel liveComment) {
        this.liveComment = liveComment;
    }

    public void setNameClickListener(LiveCommentRecyclerAdapter.LiveCommentNameClickListener mLiveCommentNameClickListener) {
        this.mLiveCommentNameClickListener = mLiveCommentNameClickListener;
    }

    private WeakReference<Context> mContext;

    public void setContext(WeakReference<Context> mContext) {
        this.mContext = mContext;
    }
}
