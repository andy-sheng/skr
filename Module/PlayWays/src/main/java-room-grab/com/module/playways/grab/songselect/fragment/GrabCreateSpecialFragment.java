package com.module.playways.grab.songselect.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.alibaba.fastjson.JSON;
import com.common.base.BaseFragment;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExImageView;
import com.module.playways.grab.room.GrabRoomServerApi;
import com.module.playways.grab.songselect.model.SpecialModel;
import com.module.playways.grab.songselect.view.SpecialSelectView;
import com.module.rank.R;

import java.util.HashMap;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.RequestBody;

/**
 * 选择房间属性
 */
public class GrabCreateSpecialFragment extends BaseFragment {

    ExImageView mIvBack;
    SpecialSelectView mSpecialView;

    int mRoomType;

    @Override
    public int initView() {
        return R.layout.grab_create_specail_fragment_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mIvBack = (ExImageView) mRootView.findViewById(R.id.iv_back);
        mSpecialView = (SpecialSelectView) mRootView.findViewById(R.id.special_view);

        Bundle bundle = getArguments();
        if (bundle != null) {
            mRoomType = bundle.getInt(GrabCreateRoomFragment.KEY_ROOM_TYPE);
        }

        mIvBack.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                U.getFragmentUtils().popFragment(GrabCreateSpecialFragment.this);
            }
        });

        mSpecialView.setSpecialSelectListner(new SpecialSelectView.SpecialSelectListner() {
            @Override
            public void onClickSpecial(SpecialModel model, List<String> music) {
                createRoom(model.getTagID());
            }
        });
    }

    /**
     * 创建房间
     *
     * @param tagID 专场id
     */
    private void createRoom(int tagID) {
        GrabRoomServerApi grabRoomServerApi = ApiManager.getInstance().createService(GrabRoomServerApi.class);
        HashMap<String, Object> map = new HashMap<>();
        map.put("roomType", mRoomType);
        map.put("tagID", tagID);

        RequestBody body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSOIN), JSON.toJSONString(map));
        ApiMethods.subscribe(grabRoomServerApi.createRoom(body), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if (result.getErrno() == 0) {
                    // TODO: 2019/3/20  房间创建完成
                } else {
                    // 房间创建失败
                    U.getToastUtil().showShort("" + result.getErrmsg());
                }
            }
        }, this);
    }

    @Override
    public boolean useEventBus() {
        return false;
    }
}
