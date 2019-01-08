package com.module.playways.rank.room.view;

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
import com.module.rank.R;
import com.module.playways.rank.prepare.model.PlayerInfoModel;
import com.module.playways.rank.prepare.model.RoundInfoModel;
import com.module.playways.rank.room.model.RoomData;
import com.zq.live.proto.Common.ESex;

public class TurnChangeCardView extends RelativeLayout {

    RoomData mRoomData;

    ExImageView mTurnChangeBgIv;
    SimpleDraweeView mTurnCurrentIv;
    ExTextView mTurnNameTv;
    ExTextView mTurnSongTv;

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

        PlayerInfoModel nexInfo = null;
        PlayerInfoModel curInfo = null;
        if (seq == 3) {
            nexInfo = null;
            for (PlayerInfoModel playerInfo : mRoomData.getPlayerInfoList()) {
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

            for (PlayerInfoModel playerInfo : mRoomData.getPlayerInfoList()) {
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

    }

    public void bindData(PlayerInfoModel cur, PlayerInfoModel next) {
        if (cur != null) {
            if (cur.getUserInfo().getUserId() == MyUserInfoManager.getInstance().getUid()) {
                mTurnNameTv.setText("轮到你唱啦！");
                mTurnSongTv.setText("《" + cur.getSongList().get(0).getItemName() + "》");
            } else {
                mTurnNameTv.setText("《" + cur.getSongList().get(0).getItemName() + "》");
                mTurnSongTv.setText("演唱：" + cur.getUserInfo().getNickname());
            }

            if (cur.getUserInfo().getSex() == ESex.SX_MALE.getValue()) {
                AvatarUtils.loadAvatarByUrl(mTurnCurrentIv, AvatarUtils.newParamsBuilder(cur.getUserInfo().getAvatar())
                        .setCircle(true)
                        .setBorderWidth(U.getDisplayUtils().dip2px(3))
                        .setBorderColor(Color.parseColor("#33A4E1"))
                        .build());
            } else if (cur.getUserInfo().getSex() == ESex.SX_FEMALE.getValue()) {
                AvatarUtils.loadAvatarByUrl(mTurnCurrentIv, AvatarUtils.newParamsBuilder(cur.getUserInfo().getAvatar())
                        .setCircle(true)
                        .setBorderWidth(U.getDisplayUtils().dip2px(3))
                        .setBorderColor(Color.parseColor("#FF79A9"))
                        .build());
            }

        }

//        if (next != null) {
//            AvatarUtils.loadAvatarByUrl(mTurnNextIv, AvatarUtils.newParamsBuilder(next.getUserInfo().getAvatar())
//                    .setCircle(true)
//                    .setBorderWidth(U.getDisplayUtils().dip2px(2))
//                    .setBorderColor(Color.WHITE)
//                    .build());
//            SpannableStringBuilder ssb = new SpanUtils()
//                    .append("下一首由").append(next.getUserInfo().getNickname()).setClickSpan(new ClickableSpan() {
//                        @Override
//                        public void onClick(View widget) {
//                            U.getToastUtil().showShort("事件触发了");
//                        }
//
//                        @Override
//                        public void updateDrawState(TextPaint ds) {
//                            ds.setColor(Color.YELLOW);
//                            ds.setUnderlineText(false);
//                        }
//                    }).append("点击事件").create();
//            mTurnNextInfoTv.setText(ssb);
//        } else {
//            mTurnNextIv.setVisibility(GONE);
//            mTurnNextInfoTv.setVisibility(GONE);
//        }
    }

}
