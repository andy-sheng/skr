package com.module.rankingmode.room.view;

import android.content.Context;
import android.graphics.Color;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

import com.common.core.avatar.AvatarUtils;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.utils.SpanUtils;
import com.common.utils.U;
import com.common.view.ex.ExImageView;
import com.common.view.ex.ExTextView;
import com.facebook.drawee.view.SimpleDraweeView;
import com.module.rankingmode.R;
import com.module.rankingmode.prepare.model.PlayerInfo;
import com.module.rankingmode.prepare.model.RoundInfoModel;
import com.module.rankingmode.room.model.RoomData;

public class TurnChangeCardView extends RelativeLayout {

    RoomData mRoomData;

    ExImageView mTurnChangeBgIv;
    SimpleDraweeView mTurnCurrentIv;
    ExTextView mTurnNameTv;
    ExTextView mTurnSongTv;

    SimpleDraweeView mTurnNextIv;
    ExTextView mTurnNextInfoTv;

    public TurnChangeCardView(Context context) {
        super(context);
        init();
    }

    public TurnChangeCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TurnChangeCardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public boolean setData(RoomData data) {
        this.mRoomData = data;

        if (mRoomData.getRealRoundInfo() == null) {
            return false;
        }
        int curUid = mRoomData.getRealRoundInfo().getUserID();
        int seq = mRoomData.getRealRoundInfo().getRoundSeq();

        PlayerInfo nexInfo = null;
        PlayerInfo curInfo = null;
        if (seq == 3) {
            nexInfo = null;
            for (PlayerInfo playerInfo : mRoomData.getPlayerInfoList()) {
                if (playerInfo.getUserInfo().getUserId() == curUid) {
                    curInfo = playerInfo;
                }
            }
        } else {
            int nextUid = 0;
            for (RoundInfoModel roundInfoModel : mRoomData.getRoundInfoModelList()) {
                if (roundInfoModel.getRoundSeq() == seq + 1) {
                    nextUid = roundInfoModel.getUserID();
                    break;
                }
            }

            for (PlayerInfo playerInfo : mRoomData.getPlayerInfoList()) {
                if (playerInfo.getUserInfo().getUserId() == curUid) {
                    curInfo = playerInfo;
                } else if (playerInfo.getUserInfo().getUserId() == nextUid) {
                    nexInfo = playerInfo;
                }
            }

        }

        if (curInfo == null) {
            return false;
        }

        bindData(curInfo, nexInfo);
        return true;
    }

    public void init() {
        inflate(getContext(), R.layout.room_turn_change_view_layout, this);
        mTurnChangeBgIv = (ExImageView) findViewById(R.id.turn_change_bg_iv);
        mTurnCurrentIv = (SimpleDraweeView) findViewById(R.id.turn_current_iv);
        mTurnNameTv = (ExTextView) findViewById(R.id.turn_name_tv);
        mTurnSongTv = (ExTextView) findViewById(R.id.turn_song_tv);
        mTurnNextIv = (SimpleDraweeView) findViewById(R.id.turn_next_iv);
        mTurnNextInfoTv = (ExTextView) findViewById(R.id.turn_next_info_tv);
    }

    public void bindData(PlayerInfo cur, PlayerInfo next) {
        if (cur != null) {
            if (cur.getUserInfo().getUserId() == MyUserInfoManager.getInstance().getUid()) {
                mTurnChangeBgIv.setBackground(getResources().getDrawable(R.drawable.room_turn_card_bg_blue));
            } else {
                mTurnChangeBgIv.setBackground(getResources().getDrawable(R.drawable.room_turn_card_bg_red));
            }
            AvatarUtils.loadAvatarByUrl(mTurnCurrentIv, AvatarUtils.newParamsBuilder(cur.getUserInfo().getAvatar())
                    .setCircle(true)
                    .setBorderWidth(U.getDisplayUtils().dip2px(3))
                    .setBorderColor(Color.WHITE)
                    .build());
            mTurnNameTv.setText(cur.getUserInfo().getUserNickname());
            mTurnSongTv.setText(cur.getSongList().get(0).getItemName());
        }

        if (next != null) {
            AvatarUtils.loadAvatarByUrl(mTurnNextIv, AvatarUtils.newParamsBuilder(next.getUserInfo().getAvatar())
                    .setCircle(true)
                    .setBorderWidth(U.getDisplayUtils().dip2px(2))
                    .setBorderColor(Color.WHITE)
                    .build());
            SpannableStringBuilder ssb = new SpanUtils()
                    .append("下一首由").append(next.getUserInfo().getUserNickname()).setClickSpan(new ClickableSpan() {
                        @Override
                        public void onClick(View widget) {
                            U.getToastUtil().showShort("事件触发了");
                        }

                        @Override
                        public void updateDrawState(TextPaint ds) {
                            ds.setColor(Color.YELLOW);
                            ds.setUnderlineText(false);
                        }
                    }).append("点击事件").create();
            mTurnNextInfoTv.setText(ssb);
        } else {
            mTurnNextIv.setVisibility(GONE);
            mTurnNextInfoTv.setVisibility(GONE);
        }
    }

}
