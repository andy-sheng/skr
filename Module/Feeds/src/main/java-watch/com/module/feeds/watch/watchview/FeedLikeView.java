package com.module.feeds.watch.watchview;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.common.core.userinfo.UserInfoManager;
import com.common.utils.SpanUtils;
import com.common.utils.U;
import com.component.person.event.ShowPersonCenterEvent;
import com.module.feeds.R;
import com.module.feeds.watch.model.FeedUserInfo;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

/**
 * 点赞的神曲，每一项可以点击
 */
public class FeedLikeView extends TextView {

    public FeedLikeView(Context context) {
        super(context);
        initView();
    }

    public FeedLikeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public FeedLikeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        setMovementMethod(LinkMovementMethod.getInstance());
        setHighlightColor(getResources().getColor(R.color.transparent));
    }

    /**
     * 设置点赞的名字
     *
     * @param topics
     * @return
     */
    public void setLikeUsers(List<FeedUserInfo> topics, int likeNum) {
        setText("");
        if (topics != null && topics.size() > 0) {
            setVisibility(VISIBLE);
            int length = topics.size();
            for (int i = 0; i < length; i++) {
                String clickText;
                FeedUserInfo feedUserInfo = topics.get(i);
                String name = UserInfoManager.getInstance().getRemarkName(feedUserInfo.getUserID(), feedUserInfo.getNickname());
                if (i == length - 1) {
                    clickText = name;
                } else {
                    clickText = name + "、";
                }
                SpannableStringBuilder spannableStringBuilder;
                if (i == 0) {
                    spannableStringBuilder = new SpanUtils()
                            .appendImage(U.getDrawable(R.drawable.feed_like_name_icon), SpanUtils.ALIGN_CENTER)
                            .append(" " + clickText).setForegroundColor(Color.parseColor("#4A90E2"))
                            .setClickSpan(new ClickableSpan() {
                                @Override
                                public void onClick(@NonNull View widget) {
                                    EventBus.getDefault().post(new ShowPersonCenterEvent(feedUserInfo.getUserID()));
                                }

                                @Override
                                public void updateDrawState(@NonNull TextPaint ds) {
                                    ds.setColor(Color.parseColor("#4A90E2"));
                                    ds.setUnderlineText(false);
                                }
                            })
                            .create();
                } else {
                    spannableStringBuilder = new SpanUtils()
                            .append(clickText).setForegroundColor(Color.parseColor("#4A90E2"))
                            .setClickSpan(new ClickableSpan() {
                                @Override
                                public void onClick(@NonNull View widget) {
                                    EventBus.getDefault().post(new ShowPersonCenterEvent(feedUserInfo.getUserID()));
                                }

                                @Override
                                public void updateDrawState(@NonNull TextPaint ds) {
                                    ds.setColor(Color.parseColor("#4A90E2"));
                                    ds.setUnderlineText(false);
                                }
                            })
                            .create();
                }
                append(spannableStringBuilder);
            }

            if (topics.size() > 8) {
                String moreStr = "等" + likeNum + "人赞过";
                SpannableStringBuilder mSpannableString = new SpanUtils()
                        .append(moreStr).setForegroundColor(U.getColor(R.color.black_trans_50))
                        .create();
                append(mSpannableString);
            }
        } else {
            setVisibility(GONE);
        }
    }
}
