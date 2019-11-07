package com.module.msg.fragment;

import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.android.arouter.launcher.ARouter;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExImageView;
import com.component.busilib.manager.WeakRedDotManager;
import com.module.RouterConstants;
import com.module.msg.custom.MyConversationListAdapter;
import com.module.msg.follow.LastNewsModel;

import java.util.List;

import io.rong.imkit.R;
import io.rong.imkit.fragment.ConversationListFragment;
import io.rong.imkit.widget.adapter.ConversationListAdapter;

public class MyConversationListFragment extends ConversationListFragment {

    ConstraintLayout mSpecialFollowArea;
    ImageView mSpecialFollowIv;
    ExImageView mSpecialRedIv;
    TextView mSpecialDescTv;
    ConstraintLayout mLastFollowArea;
    ImageView mLastFollowIv;
    ExImageView mLastRedIv;
    TextView mLastDescTv;
    ConstraintLayout mCommentLikeArea;
    ImageView mCommentLikeIv;
    ExImageView mCommentLikeRedIv;
    TextView mCommentLikeDescTv;
    ConstraintLayout mGiftArea;
    ImageView mGiftIv;
    ExImageView mGiftRedIv;
    TextView mGiftDescTv;

    boolean isShowSpRed = false;
    boolean isShowLastRed = false;
    boolean isShowCommentLikeRed = false;
    boolean isShowGiftRed = false;

    public void showLastNews(List<LastNewsModel> news) {
        if (news != null && news.size() > 0) {
            for (int i = 0; i < news.size(); i++) {
                if (news.get(i).getListType() == LastNewsModel.TYPE_SP_FOLLOW) {
                    mSpecialDescTv.setText(news.get(i).getLatestNews());
                } else if (news.get(i).getListType() == LastNewsModel.TYPE_LAST_FOLLOW) {
                    mLastDescTv.setText(news.get(i).getLatestNews());
                } else if (news.get(i).getListType() == LastNewsModel.TYPE_POSTS_COMMENT_LIKE) {
                    mCommentLikeDescTv.setText(news.get(i).getLatestNews());
                } else if (news.get(i).getListType() == LastNewsModel.TYPE_GIFT) {
                    mGiftDescTv.setText(news.get(i).getLatestNews());
                }
            }
        }
    }

    public void showSPRedDot(boolean isShow) {
        isShowSpRed = isShow;
        if (mSpecialRedIv != null) {
            if (isShow) {
                mSpecialRedIv.setVisibility(View.VISIBLE);
            } else {
                mSpecialRedIv.setVisibility(View.GONE);
            }
        }
    }

    public void showLastFollowRedDot(boolean isShow) {
        isShowLastRed = isShow;
        if (mLastRedIv != null) {
            if (isShow) {
                mLastRedIv.setVisibility(View.VISIBLE);
            } else {
                mLastRedIv.setVisibility(View.GONE);
            }
        }
    }

    public void showCommentLikeRedDot(boolean isShow) {
        isShowCommentLikeRed = isShow;
        if (mCommentLikeRedIv != null) {
            if (isShow) {
                mCommentLikeRedIv.setVisibility(View.VISIBLE);
            } else {
                mCommentLikeRedIv.setVisibility(View.GONE);
            }
        }
    }

    public void showGiftRedDot(boolean isShow) {
        isShowGiftRed = isShow;
        if (mGiftRedIv != null) {
            if (isShow) {
                mGiftRedIv.setVisibility(View.VISIBLE);
            } else {
                mGiftRedIv.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public ConversationListAdapter onResolveAdapter(Context context) {
        return new MyConversationListAdapter(context);
    }

    @Override
    protected List<View> onAddHeaderView() {
        List<View> heads = super.onAddHeaderView();
        View head = LayoutInflater.from(this.getContext()).inflate(R.layout.custom_rc_item_conversation_head, null);
        mSpecialFollowArea = head.findViewById(R.id.special_follow_area);
        mSpecialFollowIv = head.findViewById(R.id.special_follow_iv);
        mSpecialRedIv = head.findViewById(R.id.special_red_iv);
        mSpecialDescTv = head.findViewById(R.id.special_desc_tv);
        mLastFollowArea = head.findViewById(R.id.last_follow_area);
        mLastFollowIv = head.findViewById(R.id.last_follow_iv);
        mLastRedIv = head.findViewById(R.id.last_red_iv);
        mLastDescTv = head.findViewById(R.id.last_desc_tv);
        mCommentLikeArea = head.findViewById(R.id.comment_like_area);
        mCommentLikeIv = head.findViewById(R.id.comment_like_iv);
        mCommentLikeRedIv = head.findViewById(R.id.comment_like_red_iv);
        mCommentLikeDescTv = head.findViewById(R.id.comment_like_desc_tv);
        mGiftArea = head.findViewById(R.id.gift_area);
        mGiftIv = head.findViewById(R.id.gift_iv);
        mGiftRedIv = head.findViewById(R.id.gift_red_iv);
        mGiftDescTv = head.findViewById(R.id.gift_desc_tv);

        showSPRedDot(isShowSpRed);
        showLastFollowRedDot(isShowLastRed);
        showCommentLikeRedDot(isShowCommentLikeRed);
        showGiftRedDot(isShowGiftRed);

        mSpecialFollowArea.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                ARouter.getInstance().build(RouterConstants.ACTIVITY_SPECIAL_FOLLOW)
                        .navigation();
                WeakRedDotManager.getInstance().updateWeakRedRot(WeakRedDotManager.MESSAGE_SP_FOLLOW, 0);
            }
        });

        mLastFollowArea.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                ARouter.getInstance().build(RouterConstants.ACTIVITY_LAST_FOLLOW)
                        .navigation();
                WeakRedDotManager.getInstance().updateWeakRedRot(WeakRedDotManager.MESSAGE_FOLLOW_RED_ROD_TYPE, 0);
            }
        });

        mCommentLikeArea.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                ARouter.getInstance().build(RouterConstants.ACTIVITY_COMMENT_LIKE)
                        .navigation();
                WeakRedDotManager.getInstance().updateWeakRedRot(WeakRedDotManager.MESSAGE_POSTS_COMMENT_LIKE_TYPE, 0);
            }
        });

        mGiftArea.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                ARouter.getInstance().build(RouterConstants.ACTIVITY_GIFT_RECORD)
                        .navigation();
                WeakRedDotManager.getInstance().updateWeakRedRot(WeakRedDotManager.MESSAGE_GIFT_TYPE, 0);
            }
        });
        heads.add(head);
        return heads;
    }
}
