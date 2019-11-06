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

        mSpecialFollowArea.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                ARouter.getInstance().build(RouterConstants.ACTIVITY_SPECIAL_FOLLOW)
                        .navigation();
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

                WeakRedDotManager.getInstance().updateWeakRedRot(WeakRedDotManager.MESSAGE_POSTS_LIKE_TYPE, 0);
                WeakRedDotManager.getInstance().updateWeakRedRot(WeakRedDotManager.MESSAGE_POSTS_COMMENT_LIKE_TYPE, 0);
                WeakRedDotManager.getInstance().updateWeakRedRot(WeakRedDotManager.MESSAGE_POSTS_COMMENT_ADD_TYPE, 0);
            }
        });

        mGiftArea.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                ARouter.getInstance().build(RouterConstants.ACTIVITY_GIFT_RECORD)
                        .navigation();
            }
        });
        heads.add(head);
        return heads;
    }
}
