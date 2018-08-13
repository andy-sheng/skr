package com.wali.live.watchsdk.watch.view.watchgameview;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.base.image.fresco.BaseImageView;
import com.thornbirds.component.view.IComponentView;
import com.thornbirds.component.view.IViewProxy;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.component.WatchComponentController;
import com.wali.live.watchsdk.component.presenter.LiveCommentPresenter;
import com.wali.live.watchsdk.watch.presenter.watchgamepresenter.WatchGameChatTabPresenter;
import com.wali.live.watchsdk.watch.presenter.watchgamepresenter.WatchGameLiveCommentPresenter;

public class WatchGameChatTabView extends RelativeLayout implements
        IComponentView<WatchGameChatTabView.IPresenter, WatchGameChatTabView.IView>, View.OnClickListener {

    WatchGameChatTabPresenter mWatchGameChatTabPresenter;

    WatchGameLiveCommentView mWatchGameLiveCommentView;
    WatchGameLiveCommentPresenter mWatchGameLiveCommentPresenter;


    RelativeLayout mAnchorInfoContainer;
    BaseImageView mAnchorAvatarIv;
    TextView mAnchorNameTv;
    TextView mAnchorRoomTv;
    TextView mFocusBtn;
    TextView mViewerNum;
    RelativeLayout mCommentContainer;
    WatchGameLiveCommentView mLiveCommentView;

    public WatchGameChatTabView(Context context, WatchComponentController componentController) {
        super(context);
        init(context, componentController);
    }

    private void init(Context context, WatchComponentController componentController) {
        inflate(context, R.layout.watch_game_tab_chat_layout, this);
        mWatchGameLiveCommentView = (WatchGameLiveCommentView) this.findViewById(R.id.live_comment_view);
        mWatchGameLiveCommentView.setToken(componentController.mRoomChatMsgManager.toString());
        mWatchGameLiveCommentPresenter = new WatchGameLiveCommentPresenter(componentController);
        mWatchGameLiveCommentPresenter.setView(mWatchGameLiveCommentView.getViewProxy());
        mWatchGameLiveCommentView.setPresenter(mWatchGameLiveCommentPresenter);

        mWatchGameChatTabPresenter = new WatchGameChatTabPresenter(componentController);
        mWatchGameChatTabPresenter.setView(this.getViewProxy());
        this.setPresenter(mWatchGameChatTabPresenter);


        mAnchorInfoContainer = (RelativeLayout) this.findViewById(R.id.anchor_info_container);
        mAnchorAvatarIv = (BaseImageView) this.findViewById(R.id.anchor_avatar_iv);
        mAnchorNameTv = (TextView) this.findViewById(R.id.anchor_name_tv);
        mAnchorRoomTv = (TextView) this.findViewById(R.id.anchor_room_tv);
        mFocusBtn = (TextView) this.findViewById(R.id.focus_btn);
        mViewerNum = (TextView) this.findViewById(R.id.viewer_num);
        mCommentContainer = (RelativeLayout) this.findViewById(R.id.comment_container);
        mLiveCommentView = (WatchGameLiveCommentView) this.findViewById(R.id.live_comment_view);

    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (mWatchGameLiveCommentPresenter != null) {
            mWatchGameLiveCommentPresenter.startPresenter();
        }
        if (mWatchGameChatTabPresenter != null) {
            mWatchGameChatTabPresenter.startPresenter();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mWatchGameLiveCommentPresenter != null) {
            mWatchGameLiveCommentPresenter.stopPresenter();
        }
        if (mWatchGameChatTabPresenter != null) {
            mWatchGameChatTabPresenter.stopPresenter();
        }
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public IView getViewProxy() {
        return new IView() {

            @Override
            public void updateViewerNum(int viewerCnt) {
                viewerCnt = 11222;
                String info;
                if (viewerCnt > 9999) {
                    info = String.format("%.1f万人", viewerCnt / 10000.0);
                } else {
                    info = String.format("%d人", viewerCnt);
                }
                mViewerNum.setText(info);
            }

            @Override
            public <T extends View> T getRealView() {
                return (T) WatchGameChatTabView.this;
            }
        };
    }

    @Override
    public void setPresenter(IPresenter iPresenter) {

    }

    public interface IView extends IViewProxy {
        void updateViewerNum(int viewerCnt);
    }

    public interface IPresenter {
    }
}
