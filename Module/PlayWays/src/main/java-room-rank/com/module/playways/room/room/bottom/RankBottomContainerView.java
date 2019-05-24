package com.module.playways.room.room.bottom;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import com.common.core.account.UserAccountManager;
import com.common.statistics.StatConstants;
import com.common.statistics.StatisticsAdapter;
import com.common.view.DebounceViewClickListener;
import com.component.busilib.constans.GameModeType;
import com.module.playways.room.room.view.BottomContainerView;
import com.module.playways.R;
import com.zq.live.proto.Room.SpecialEmojiMsgType;

import java.util.HashMap;

public class RankBottomContainerView extends BottomContainerView {
    public RankBottomContainerView(Context context) {
        super(context);
    }

    public RankBottomContainerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected int getLayout() {
        return R.layout.bottom_container_view_layout;
    }

    @Override
    protected void init() {
        super.init();
        mEmoji2Btn.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                // 发送动态表情，爱心
                sendSpecialEmojiMsg(SpecialEmojiMsgType.SP_EMOJI_TYPE_LIKE, "送出爱心");
//                HashMap map = new HashMap();
//                map.put("expressionId2", String.valueOf(SpecialEmojiMsgType.SP_EMOJI_TYPE_LIKE.getValue()));
//                if (mRoomData.getGameType() == GameModeType.GAME_MODE_CLASSIC_RANK) {
//                    StatisticsAdapter.recordCountEvent(UserAccountManager.getInstance().getGategory(StatConstants.CATEGORY_RANK), "game_expression", map);
//                } else if (mRoomData.getGameType() == GameModeType.GAME_MODE_GRAB) {
//                    StatisticsAdapter.recordCountEvent(UserAccountManager.getInstance().getGategory(StatConstants.CATEGORY_GRAB), "game_expression", map);
//                }
            }
        });
    }
}
