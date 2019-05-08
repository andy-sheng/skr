package com.module.playways.room.room.quickmsg;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

import com.alibaba.fastjson.JSON;
import com.common.core.account.UserAccountManager;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.common.statistics.StatConstants;
import com.common.statistics.StatisticsAdapter;
import com.common.utils.U;
import com.common.view.recyclerview.RecyclerOnItemClickListener;
import com.component.busilib.constans.GameModeType;
import com.module.playways.voice.activity.VoiceRoomActivity;
import com.module.playways.R;
import com.module.playways.room.msg.event.EventHelper;
import com.module.playways.room.room.RankRoomServerApi;
import com.module.playways.BaseRoomData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.RequestBody;

public class QuickMsgView extends RelativeLayout {

    RecyclerView mQuickMsgRv;
    QuickMsgAdapter mQuickMsgAdapter;
    BaseRoomData mRoomData;
    Listener mListener;

    public QuickMsgView(Context context) {
        super(context);
        init();
    }

    public QuickMsgView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public QuickMsgView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.quick_msg_view_layout, this);
        mQuickMsgRv = (RecyclerView) this.findViewById(R.id.quick_msg_rv);
        mQuickMsgRv.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        mQuickMsgAdapter = new QuickMsgAdapter(new RecyclerOnItemClickListener<QuickMsgModel>() {
            @Override
            public void onItemClicked(View view, int position, QuickMsgModel model) {
                if (mRoomData.getGameType() == GameModeType.GAME_MODE_CLASSIC_RANK) {
                    if (getContext() instanceof VoiceRoomActivity) {
                        StatisticsAdapter.recordCountEvent(UserAccountManager.getInstance().getGategory(StatConstants.CATEGORY_RANK), "chatroom_expresschat", null);
                    }else {
                        StatisticsAdapter.recordCountEvent(UserAccountManager.getInstance().getGategory(StatConstants.CATEGORY_RANK), "game_expresschat", null);
                    }
                } else if (mRoomData.getGameType() == GameModeType.GAME_MODE_GRAB) {
                    StatisticsAdapter.recordCountEvent(UserAccountManager.getInstance().getGategory(StatConstants.CATEGORY_GRAB), "game_expresschat", null);
                }

                String content = model.getText();
                RankRoomServerApi roomServerApi = ApiManager.getInstance().createService(RankRoomServerApi.class);

                HashMap<String, Object> map = new HashMap<>();
                map.put("gameID", mRoomData.getGameId());
                map.put("content", content);

                RequestBody body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map));
                ApiMethods.subscribe(roomServerApi.sendMsg(body), new ApiObserver<ApiResult>() {
                    @Override
                    public void process(ApiResult obj) {

                    }
                });

                EventHelper.pretendCommentPush(content, mRoomData.getGameId());
                if (mListener != null) {
                    mListener.onSendMsgOver();
                }
            }
        });
        mQuickMsgRv.setAdapter(mQuickMsgAdapter);
        setPadding(U.getDisplayUtils().dip2px(18), U.getDisplayUtils().dip2px(4), U.getDisplayUtils().dip2px(7), U.getDisplayUtils().dip2px(4));
        setBackgroundResource(R.drawable.kuaijie_bj);
    }

    public void setRoomData(BaseRoomData roomData) {
        mRoomData = roomData;
        String[] arrays = U.app().getResources().getStringArray(R.array.rank_quick_msg_arr);
        if (mRoomData.getGameType() == GameModeType.GAME_MODE_GRAB) {
            arrays = U.app().getResources().getStringArray(R.array.grab_quick_msg_arr);
        }

        List<QuickMsgModel> list = new ArrayList<>();
        for (String s : arrays) {
            QuickMsgModel quickMsgModel = new QuickMsgModel();
            quickMsgModel.setText(s);
            list.add(quickMsgModel);
        }
        mQuickMsgAdapter.setDataList(list);
    }

    public void setListener(Listener l) {
        mListener = l;
    }

    public interface Listener {
        void onSendMsgOver();
    }
}
